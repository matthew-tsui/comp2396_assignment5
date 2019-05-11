package server;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.awt.image.CropImageFilter;
import java.awt.image.FilteredImageSource;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.event.MouseInputAdapter;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import auth.Database;
import auth.SHA1;
import comp2396_assignment5.Block;
import comp2396_assignment5.ImageReader;
import comp2396_assignment5.JSONUtils;
import comp2396_assignment5.Peer;
import comp2396_assignment5.PeerList;
import peer.ImagePeerGUI;


/**
 * @author Matthew
 * ImageServer class for peer connection
 */
public class ImageServer extends Peer {
	private static BufferedImage displayImage;
	private static ServerSocket serverSocket;
	private static PeerList peerList = new PeerList();
	private static ArrayList<PrintWriter> peerWriters = new ArrayList();
	private static ArrayList<Block> Blocks = new ArrayList();
	
	//Views
	private static JFrame frame;
	private static JPanel container;
	private static JPanel imagePanel;
	private static JButton btnLoadAnother;
	
	// Drag and drop
	private Block source = null;
	private Block dest = null;
	
	/**
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		ImageServer server = new ImageServer();
		server.openConnection();
	}
	
	/** Constructor loading user.txt and db configure
	 * @throws IOException
	 */
	public ImageServer() throws IOException {
		super("127.0.0.1", -1);
		// initalize database
		Database.loadJSON("src/User.txt");
		Database.setHash(new SHA1());
		
		Thread GUI = new Thread(new GUIThread());
		GUI.start();
	}
	
	/**
	 * @return list of peers
	 */
	public static PeerList getPeerList() {
		return peerList;
	}
	
	
	/**
	 * open connection for incoming peers
	 */
	public void openConnection() {
		try {
			// Define a new socket
			serverSocket = new ServerSocket(9000);
			Peer server = new Peer(serverSocket.getInetAddress().getHostAddress().toString(), serverSocket.getLocalPort());
			server.print();
			peerList.addPeer(server);
			
			while (true) {
				
				// Start accepting connection
				Socket s = serverSocket.accept();
				
				System.out.println("Got a connection" + s);
				
				// Get input
				BufferedReader reader = new BufferedReader(new InputStreamReader(s.getInputStream()));
				
				// Create a new peer
				PrintWriter writer = new PrintWriter(s.getOutputStream());
				
				// Add writer to list of peers
				System.out.println("Allowing new thread to client" + s);
				
				Peer newPeer = new Peer(s.getInetAddress().getHostAddress().toString(), s.getPort());
				newPeer.print();
				peerList.addPeer(newPeer);
				peerWriters.add(writer);
				
				Thread newClient = new Thread(new ClientRunnable(s, reader , writer, s.getLocalPort()));
				newClient.start();
				
			}
		} catch (IOException e) {
					// TODO Auto-generated catch block
				e.printStackTrace();
		}
	}
	
	
	
	/**
	 * @author Matthew
	 * This class handle the action of each incoming peer
	 */
	public static class ClientRunnable implements Runnable{
		Socket client;
		BufferedReader reader;
		PrintWriter writer;
		int clientID;
		
		/**
		 * @param client socket
		 * @param in - reader for client
		 * @param out - printwriter for client
		 * @param clientID - client ID for peer list control
		 * @throws IOException
		 */
		public ClientRunnable(Socket client, BufferedReader in, PrintWriter out, int clientID) throws IOException {
			this.client = client;
			this.reader = in;
			this.writer = out;
			this.clientID = clientID;
		}
		
		/**
		 * This class keep listen peer connection
		 */
		@SuppressWarnings("unchecked")
		@Override
		public void run() {
			// TODO Auto-generated method stub
			String message;
			try {
				// validate the user
				while ( (message=reader.readLine()) != null ) {
					//System.out.println("Server received: " + message);
					JSONParser parser = new JSONParser();
					JSONObject json = (JSONObject) parser.parse(message);
					
					String command = (String) json.get("Command");
					
					// Return result to client
					JSONObject respond = new JSONObject();
					
					// Get username and clientID
					String username = (String) json.get("Me");			
					int clientID = (int) (long) json.get("clientID");
					switch (command) {
						case "LOGIN":
							
							// Get password
							String password = (String) json.get("Data_content");
							
							String loginFeedback;
							if(!Database.auth(username, password)) {
								getPeerList().removePeer(peerList.getPeerByPort(clientID));
								loginFeedback = "LOGIN_FAIL";
							} else {
								loginFeedback = "LOGIN_OK";
							}
							
							respond.put("Me", "Teacher");
							respond.put("You", username);
							respond.put("clientID", clientID);
							respond.put("Command", loginFeedback);
							respond.put("Data_image_name", null);
							respond.put("Data_block_number", null);
							respond.put("Data_content", null);
							respond.put("Peer_list", PeerList.serialize(peerList));
							
							break;
						case "REQUEST_IMG_BLOCK":
							// Get blocknumber
							int blockNumber = (int) (long) json.get("Data_block_number");
							
							// Construct response
							respond.put("Me", "Teacher");
							respond.put("You", username);
							respond.put("Command", "GET_IMG_BLOCK");
							respond.put("Data_image_name", null);
							respond.put("Data_block_number", blockNumber);
							
							Image i = Blocks.get(blockNumber).getImage();
							BufferedImage bi = JSONUtils.toBufferedImage(i);
							respond.put("Data_content", JSONUtils.imgToBase64String(bi, "png"));	
							
							try {
								respond.put("Peer_list:", PeerList.serialize(peerList));
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							break;
						
						case "LOGOUT":
							System.out.println("Server received logout request: " + message);
							getPeerList().removePeer(peerList.getPeerByPort(clientID));
							for(PrintWriter w: peerWriters) {
								JSONObject respond1 = new JSONObject();
								respond.put("Me", "Teacher");
								respond.put("You", "");
								respond.put("clientID", null);
								respond.put("Command", "UPDATE_PEERLIST");
								
								try {
									respond.put("Peer_list:", PeerList.serialize(peerList));
								} catch (IOException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
								
							}
							break;
						
					}
					
					
					writer.println(respond.toString());
					writer.flush();
					System.out.println("Server sent: " + respond.toString());
				
				}
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
	}
	
	
	
	/**
	 * @author Matthew
	 * GUI thread to display GUI
	 */
	public class GUIThread extends JPanel implements Runnable{
		int WIDTH = 700;
		int HEIGHT = 700;
		
		@Override
		public void run() {
			// TODO Auto-generated method stub
			
			// load image to image panel
			displayImage = ImageReader.load();
			if(displayImage == null) {
				System.exit(0);
			}
			
			loadLayout();
		}
		
		
		/** 
		 * When the source image changed, this method update all clients(peers)
		 */
		public void broadcastSourceImageUpdate() {
			for(PrintWriter w: peerWriters) {
				JSONObject respond1 = new JSONObject();
				respond1.put("Me", "Teacher");
				respond1.put("You", "");
				respond1.put("clientID", null);
				respond1.put("Command", "UPDATE_SOURCE_IMG");
				
				Image i = displayImage;
				BufferedImage bi = JSONUtils.toBufferedImage(i);
				respond1.put("Data_content",  JSONUtils.imgToBase64String(bi, "png"));
				
				try {
					respond1.put("Peer_list:", PeerList.serialize(peerList));
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				w.println(respond1.toString());
				w.flush();
				
				System.out.println("Server sent update img source: " + respond1.toString());
			}
		}
		
		/** This method broadcast the block update after user swap to block
		 * @param source of image block
		 * @param dest of image block
		 * @throws IOException
		 */
		public void broadcastImageBlockUpdateAfterSwap(int source, int dest) throws IOException {
			for(PrintWriter w: peerWriters) {
				JSONObject respond1 = new JSONObject();
				respond1.put("Me", "Teacher");
				respond1.put("You", "");
				respond1.put("clientID", null);
				respond1.put("Command", "UPDATE_IMG_BLOCK");
				respond1.put("Data_image_name", null);
				respond1.put("Data_block_number1", source);
				respond1.put("Data_block_number2", dest);
				
				Image i = Blocks.get(source).getImage();
				BufferedImage bi = JSONUtils.toBufferedImage(i);
				respond1.put("Data_content1", JSONUtils.imgToBase64String(bi, "png"));	
				
				Image i2 = Blocks.get(dest).getImage();
				BufferedImage bi2 = JSONUtils.toBufferedImage(i2);	
				respond1.put("Data_content2", JSONUtils.imgToBase64String(bi2, "png"));	
				respond1.put("Peer_list",PeerList.serialize(peerList));
				
				w.println(respond1.toString());
				w.flush();
	
				System.out.println("Server sent update img block: " + respond1.toString());
			}
		}
		
		
		/**
		 * reload source image to block after use choose a new image
		 */
		public void reloadDisplayImage() {
			int blockNumber = 0;
			for(int i = 0; i< 10; i++) {
				for(int j = 0; j <10; j++) {
					Image image = createImage(new FilteredImageSource(displayImage.getSource(),
							new CropImageFilter(j * WIDTH / 10, i * HEIGHT / 10,
									(WIDTH / 10), HEIGHT / 10)));	
					Block block = Blocks.get(blockNumber);
					
					// set block bg
					block.setIcon(new ImageIcon(image));
					
					block.setImage(image);
					updateLayout();
					// add the counter
					blockNumber++;
					
				}
			}
			
			updateLayout();
		}
		
		/**
		 * Load JButton and image block to layout
		 */
		public void loadLayout() {
			// btnLoadAnother
			btnLoadAnother = new JButton("Load Another Image");
			btnLoadAnother.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					// TODO Auto-generated method stub
					BufferedImage newImage = ImageReader.load();
					if(newImage != null) {
						displayImage = newImage;
						reloadDisplayImage();
						broadcastSourceImageUpdate();
					}
				}
			});
				
			
			imagePanel = new JPanel();
			//imagePanel.setBorder(BorderFactory.createLineBorder(Color.gray));
			imagePanel.setLayout(new GridLayout(10, 10, 0, 0));
			imagePanel.setSize(700,700);
			imagePanel.setBackground(null);


			//initalize Jlabel
			int blockNumber = 0;
			for(int i = 0; i< 10; i++) {
				for(int j = 0; j <10; j++) {
					Image image = createImage(new FilteredImageSource(displayImage.getSource(),
							new CropImageFilter(j * WIDTH / 10, i * HEIGHT / 10,
									(WIDTH / 10), HEIGHT / 10)));	
					// create new block
					Block block = new Block(i,j, blockNumber);
					
					// set block bg
					block.setIcon(new ImageIcon(image));
					
					// add handler for swap
					block.addMouseListener(new MouseHandler());
					
					block.setImage(image);
					
					// add it to block array
					Blocks.add(block);
					
					// add the counter
					blockNumber++;
					
				}
			}
			 
			 //add Jlabel to puzzle
			 for(int i = 0; i < 100; i++) {
				 Block p = Blocks.get(i);
				 imagePanel.add(p);
			 }

			
			// Frame setting
			frame = new JFrame ("Assignment 5");
			frame.setDefaultCloseOperation (JFrame.EXIT_ON_CLOSE);
			frame.setLayout(new BorderLayout());
			
			container = new JPanel();
			container.setLayout(new BorderLayout());
			container.add(imagePanel, BorderLayout.NORTH);
			container.add(btnLoadAnother, BorderLayout.SOUTH);
			
			frame.add(container);
			frame.pack();
			frame.setResizable(false);
			frame.setVisible(true);
		}
		
		/**
		 * @author matthewtsui
		 * Custom mouse handler to handle drag and drop event
		 */
		private class MouseHandler extends MouseInputAdapter {
				/* (non-Javadoc)
				 * @see java.awt.event.MouseAdapter#mousePressed(java.awt.event.MouseEvent)
				 */
				public void mousePressed(MouseEvent e) {
					source = (Block)e.getSource();
					//if(isCorrectPosition(source)) {
					//	textArea.append("Image block in correct position!\n");
					//	source = null;
					//}
					//System.out.println("source: " + source.getClientProperty("order"));
				}
				
				/* (non-Javadoc)
				 * @see java.awt.event.MouseAdapter#mouseReleased(java.awt.event.MouseEvent)
				 */
				public void mouseReleased(MouseEvent e) {
					if(source != null && dest != null) {
						if(source == dest){
							// do nothing
						}  else{
							System.out.println("source: " + source.getClientProperty("order") + "dest: " + dest.getClientProperty("order"));
							
							// Record source and dest
							int s1 = Blocks.indexOf(source);
							int s2 = Blocks.indexOf(dest);
							Collections.swap(Blocks, Blocks.indexOf(source), Blocks.indexOf(dest));
							
							// Update layout
							updateLayout();
							
							try {
								// Send the current block of s1 and s2 to client
								broadcastImageBlockUpdateAfterSwap(s1, s2);
							} catch (IOException e1) {
								// TODO Auto-generated catch block
								e1.printStackTrace();
							}
						}
						
					}
				}
				
				/* (non-Javadoc)
				 * @see java.awt.event.MouseAdapter#mouseEntered(java.awt.event.MouseEvent)
				 */
				public void mouseEntered(MouseEvent e) {
					dest = (Block)e.getSource();
					//System.out.println("des: " + dest.getClientProperty("order"));
				}
				
		}
		
		/**
		 * update layout after swapping image block
		 */
		public void updateLayout() {
			imagePanel.removeAll();
			for(Block p : Blocks) {
				imagePanel.add(p);
			}
			imagePanel.revalidate();
			imagePanel.repaint();
		}
	}
}



