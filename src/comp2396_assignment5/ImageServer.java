package comp2396_assignment5;

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


public class ImageServer extends JPanel{
	private static BufferedImage displayImage;
	private static ServerSocket serverSocket;
	private static ImageServer server;
	
	private static PeerList peerList = new PeerList();
	private static ArrayList<Component> Blocks = new ArrayList();
	
	private static BufferedReader in;
	private static PrintWriter out;
	
	//Views
	private static JFrame frame;
	private static JPanel container;
	private static JPanel imagePanel;
	private static JButton btnLoadAnother;
	
	// Drag and drop
	private Component source = null;
	private Component dest = null;
	
	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		 server = new ImageServer();
	}
	
	public static ImageServer getServer() {
		return server;
	}
	
	public ImageServer() throws IOException {
		// initalize database
		Database.loadJSON("src/User.txt");
		Database.setHash(new SHA1());
		loadImage();
		loadLayout();
		ImageServer.openConnection();
	}
	
	/**
	 * load another image
	 */
	public void loadImage() {
		displayImage = ImageReader.load();
		if(displayImage == null) {
			System.exit(0);
		}
	}
	
	public static PeerList getPeerList() {
		return peerList;
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
				if(displayImage != null) {
					displayImage = newImage;
				}
			}
		});
			
		
		imagePanel = new JPanel();
		imagePanel .setBorder(BorderFactory.createLineBorder(Color.gray));
		imagePanel .setLayout(new GridLayout(10, 10, 0, 0));
		imagePanel .setSize(700,700);
		imagePanel .setBackground(Color.white);

		int width = 700;
		int height = 700;

		//initalize Jlabel
		int blockNumber = 0;
		for(int i = 0; i< 10; i++) {
			for(int j = 0; j <10; j++) {
				Image image = createImage(new FilteredImageSource(displayImage.getSource(),
						new CropImageFilter(j * width / 10, i * height / 10,
								(width / 10), height / 10)));	
				// create new block
				Component block = new Component(i,j, blockNumber);
				
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
			 Component p = Blocks.get(i);
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
		frame.setResizable(true);
		frame.setVisible(true);
	}
	
	public static void openConnection() {
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
				in = new BufferedReader(new InputStreamReader(s.getInputStream()));
				
				// Create a new peer
				out = new PrintWriter(s.getOutputStream());
				
				// Add writer to list of peers
				System.out.println("Allowing new thread to client" + s);
				
				Peer newPeer = new Peer(s.getInetAddress().getHostAddress().toString(), s.getPort());
				newPeer.print();
				peerList.addPeer(newPeer);
		
				Thread newClient = new Thread(new ClientRunnable(s, in , out, s.getPort()));
				newClient.start();
				
			}
		} catch (IOException e) {
					// TODO Auto-generated catch block
				e.printStackTrace();
		}
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
				source = (Component)e.getSource();
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
						Collections.swap(Blocks, Blocks.indexOf(source), Blocks.indexOf(dest));
						updateLayout();
						//checkSolution();
					}
					
				}
			}
			
			/* (non-Javadoc)
			 * @see java.awt.event.MouseAdapter#mouseEntered(java.awt.event.MouseEvent)
			 */
			public void mouseEntered(MouseEvent e) {
				dest = (Component)e.getSource();
				System.out.println("des: " + dest.getClientProperty("order"));
			}
			
	}
	
	/**
	 * update layout after swapping image block
	 */
	public void updateLayout() {
		imagePanel.removeAll();
		for(Component p : Blocks) {
			imagePanel.add(p);
		}
		imagePanel.revalidate();
		imagePanel.repaint();
	}
	
	
	/*public static class PeerMonitorRunnable implements Runnable{

		@Override
		public void run() {
			// TODO Auto-generated method stub
			try {
				updatePeerList();
				Thread.sleep(10000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		public synchronized void updatePeerList() {
			for (Entry<Integer, PrintWriter> entry : listOfPeers.entrySet()) {
			    Integer clientID = entry.getKey();
			    PrintWriter w = entry.getValue();
			    
			    JSONObject respond = new JSONObject();
				respond.put("Me", "Teacher");
				respond.put("You", username);
				respond.put("Command", "ACTIVE_CHECK");
				respond.put("Data_image_name", null);
				respond.put("Data_block_number", null);
				respond.put("Data_content", null);
				respond.put("Peer_list:", null);
				
				w.println(message);
				w.flush();
			    
			   
			}
			Iterator<PrintWriter> it = listOfClient.iterator();
			while (it.hasNext()) {
				try {
					PrintWriter w = it.next();
					w.println(message);
					w.flush();
				} catch (Exception e) { e.printStackTrace(); }
			}
			
			listOfClient.remove(index);
			JSONObject respond = new JSONObject();
			respond.put("Me", "Teacher");
			respond.put("You", username);
			respond.put("Command", "ACTIVE_CHECK");
			respond.put("Data_image_name", null);
			respond.put("Data_block_number", null);
			respond.put("Data_content", null);
			respond.put("Peer_list:", null);
			
			for(int i = 0; i < listOfClient.size(); i++) {
				
			}
		}
	}*/
	
	public static class ClientRunnable implements Runnable{
		Socket client;
		BufferedReader in;
		PrintWriter out;
		int clientID;
		public ClientRunnable(Socket client, BufferedReader in, PrintWriter out, int clientID) throws IOException {
			this.client = client;
			this.in = in;
			this.out = out;
			this.clientID = clientID;
		}
		
		@Override
		public void run() {
			// TODO Auto-generated method stub
			String message;
			try {
				// validate the user
				while ( (message=in.readLine()) != null ) {
					System.out.println("Server received: " + message);
					JSONParser parser = new JSONParser();
					JSONObject json = (JSONObject) parser.parse(message);
					
					String command = (String) json.get("Command");
					
					// Return result to client
					JSONObject respond = new JSONObject();
					String username = (String) json.get("Me");
					
					int clientID = (int) (long) json.get("clientID");
					switch (command) {
						case "LOGIN":
							//clientID = (int) (long) json.get("clientID");
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
							respond.put("Peer_list", peerList.serialize());
							
							break;
						case "REQUEST_IMG_BLOCK":
							int blockNumber = (int) (long) json.get("Data_block_number");
							respond.put("Me", "Teacher");
							respond.put("You", username);
							respond.put("Command", "GET_IMG_BLOCK");
							respond.put("Data_image_name", null);
							respond.put("Data_block_number", blockNumber);
							
							Image i = Blocks.get(blockNumber).getImage();
							BufferedImage bi = JSONSerialize.toBufferedImage(i);
							respond.put("Data_content", JSONSerialize.imgToBase64String(bi, "png"));	
							
							try {
								respond.put("Peer_list:", peerList.serialize());
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							break;
						case "LOGOUT":
							System.out.println("Server received logout request: " + message);
							getPeerList().removePeer(peerList.getPeerByPort(clientID));
							break;	
					}
					out.println(respond.toString());
					out.flush();
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
	
	public static void sendBlock(PrintWriter client, String target, int blockNum) {
		JSONObject json = new JSONObject();
		json.put("Me", "Teacher");
		json.put("You", target);
		json.put("Command", "GET_IMG_BLOCK");
		json.put("Data_image_name", null);
		json.put("Data_block_number", blockNum);
		
		Image i = Blocks.get(blockNum).getImage();
		BufferedImage bi = JSONSerialize.toBufferedImage(i);
		json.put("Data_content", JSONSerialize.imgToBase64String(bi, "png"));	
		
		try {
			json.put("Peer_list:", peerList.serialize());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		client.println(json.toString());
		client.flush();
		System.out.println("Server sent block: " + json.toString());
	}
	

	
}



