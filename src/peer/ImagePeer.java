package peer;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.awt.image.CropImageFilter;
import java.awt.image.FilteredImageSource;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UncheckedIOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import auth.Database;
import auth.SHA1;
import comp2396_assignment5.JSONUtils;
import comp2396_assignment5.Peer;
import comp2396_assignment5.PeerList;
import server.ImageServer.ClientRunnable;



/**
 * @author Matthew
 * Image peer for downloading image block from server
 */
public class ImagePeer extends JPanel{
	private static BufferedReader in;
	private static PrintWriter out;
	private static String clientName;
	private static Integer clientID;
	private static ImagePeer client;
	private static PeerList peerList;
	private static ArrayList<Integer> receivedBlockList = new ArrayList<Integer>();
	
	private static JFrame frame;
	private static JPanel container;
	private static JPanel imagePanel;
	private static BufferedImage displayImage;
	private static ImagePeerGUI GUI;
	
	public static void main(String[] args) throws ClassNotFoundException, IOException, ParseException {
		// TODO Auto-generated method stub
		Database.setHash(new SHA1());
		String serverAddressInput = newDialog("Connect to server:");
		
		if (serverAddressInput.length() == 0) {
			JOptionPane.showMessageDialog(new JFrame(), "Empty server address", "Message", JOptionPane.INFORMATION_MESSAGE);
            System.exit(0);
		}
			
		String usernameInput = newDialog("Username:");
		if (usernameInput.length() == 0) {
			JOptionPane.showMessageDialog(new JFrame(), "Empty username", "Message", JOptionPane.INFORMATION_MESSAGE);
			System.exit(0);
		}
			
		String passwordInput = newDialog("Password:");
		if (passwordInput.length() == 0) {
			JOptionPane.showMessageDialog(new JFrame(), "Empty password", "Message", JOptionPane.INFORMATION_MESSAGE);
            System.exit(0);
		}
			
		
		// for debug only
		String usernameInput1 = "cjli";
		String passwordInput1 = "2396BComp";
		
		client = new ImagePeer();
		client.establishConnection(serverAddressInput);
		client.startLogin(usernameInput, passwordInput);
		client.maintainConnection();
		
	}
	
	/**
	 * establish connection to server
	 */
	public void establishConnection(String address) {		
		try {
			Socket clientSocket = new Socket(address, 9000);
			InputStreamReader sr = new InputStreamReader(clientSocket.getInputStream());
			in = new BufferedReader(sr);
			out = new PrintWriter(clientSocket.getOutputStream());
			setClientID(clientSocket.getLocalPort());
			
			Thread UploadServer = new Thread(new UploadServer());
			UploadServer.start();
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			JOptionPane.showMessageDialog(new JFrame(), "Invalid server address", "Message", JOptionPane.INFORMATION_MESSAGE);
			System.exit(0);
			e.printStackTrace();
		} 

	}
	
	/** This method keep listen from server for source image update and swap
	 * @throws IOException
	 * @throws ParseException
	 * @throws ClassNotFoundException
	 */
	public void maintainConnection() throws IOException, ParseException, ClassNotFoundException {
		String message;
		while ( (message=in.readLine()) != null ) {
			//System.out.println("client received: " + message);
			JSONParser parser = new JSONParser();
			JSONObject json = (JSONObject) parser.parse(message);
			
			String command = (String) json.get("Command");
			String source = (String) json.get("Me");
			String target = (String) json.get("You");
			String peerList = (String) json.get("Peer_list");
			
			if(!source.equals("peer") && peerList != null) {
				setPeerList(PeerList.deserialize(peerList));
			}
			switch (command) {
				case "LOGIN_OK":
					
					setClientName(target);
					
					System.out.println("Client login successfully");
					
					// start gui
					Thread gui = new Thread(new GUIThread());
					gui.start();
					
					// start request block from peers/server
					Thread Requester = new Thread(new Requester());
					Requester.start();
					
					break;
				case "LOGIN_FAIL":
					JOptionPane.showMessageDialog(new JFrame(), "Login Fail", "Message", JOptionPane.INFORMATION_MESSAGE);
	                System.exit(0);
	                break;
	                
				case "GET_IMG_BLOCK":
					int blockNumber = (int) (long) json.get("Data_block_number");
					String content = (String) json.get("Data_content");
					ImagePeerGUI.setBlock(JSONUtils.base64StringToImg((String) content), blockNumber);
					System.out.println("Received block " + blockNumber);
					
					// Mark block as received
					//synchronized(receivedBlockList) {
						receivedBlockList.add(blockNumber);
					//}
					break;
				case "UPDATE_IMG_BLOCK":
					// get first block
					int blockNumber1 = (int) (long) json.get("Data_block_number1");
					String content1 = (String) json.get("Data_content1");
					ImagePeerGUI.setBlock(JSONUtils.base64StringToImg((String) content1), blockNumber1);
					System.out.println("Client updated block number" + blockNumber1);
					
					// get second block
					int blockNumber2 = (int) (long) json.get("Data_block_number2");
					String content2 = (String) json.get("Data_content2");
					ImagePeerGUI.setBlock(JSONUtils.base64StringToImg((String) content2), blockNumber2);
					System.out.println("Client updated block number" + blockNumber2);
					
					//ImagePeerGUI.updateLayout();
					break;
					
				case "UPDATE_SOURCE_IMG":
					String content3 = (String) json.get("Data_content");
					GUI.setImage(JSONUtils.base64StringToImg((String) content3));
					break;
				case "UPDATE_PEERLIST":
					setPeerList(PeerList.deserialize(peerList));
					break;
				
			}
		
		}
		
		
		in.close();
	    out.close();
	}
	

	
	/**
	 * send logout request to server
	 */
	@SuppressWarnings("unchecked")
	public static void logout() {
		JSONObject json = new JSONObject();
		json.put("Me", null);
		json.put("You", "Teacher");
		json.put("clientID", getClientID());
		json.put("Command", "LOGOUT");
		json.put("Data_image_name", null);
		json.put("Data_block_number", null);
		json.put("Data_content",null);
		json.put("Peer_list:", null);
		
		out.println(json.toString());
		out.flush();
	}
	
	/** login to server
	 * @param username - username of the peer
	 * @param password - password of the peer
	 */
	@SuppressWarnings("unchecked")
	public void startLogin(String username, String password) {
		JSONObject json = new JSONObject();
		json.put("Me", username);
		json.put("You", "Teacher");
		json.put("Command", "LOGIN");
		json.put("clientID", getClientID());
		json.put("Data_image_name", null);
		json.put("Data_block_number", null);
		json.put("Data_content", Database.getHash().hash(password));
		json.put("Peer_list:", null);
		
		out.println(json.toString());
		out.flush();
	}
	
	/**
	 * @param target to be requested
	 * @param blockNum to be requested
	 * @param ip - target ip
	 * @param port - target port
	 */
	@SuppressWarnings("unchecked")
	public void requestBlock(String target, int blockNum, String ip, int port) {

		JSONObject json = new JSONObject();
		json.put("Me", getClientName());
		json.put("You", target);
		json.put("clientID", getClientID());
		json.put("Command", "REQUEST_IMG_BLOCK");
		json.put("Data_image_name", null);
		json.put("Data_block_number", blockNum);
		json.put("Data_content", blockNum);
		json.put("Peer_list:", null);
		
		if(target.equals("server")) {
			out.println(json.toString());
			out.flush();
			System.out.println("Server - Client sent block request to "+ target + ":  " + json.toString());
		} else {
			try {
				Socket clientSocket = new Socket(ip, 9001);
				InputStreamReader sr;
				sr = new InputStreamReader(clientSocket.getInputStream());
				BufferedReader peerIn = new BufferedReader(sr);
				PrintWriter peerOut = new PrintWriter(clientSocket.getOutputStream());
				peerOut.println(json.toString());
				peerOut.flush();
				System.out.println("P2P - Client sent block request to "+ target + ":  " + json.toString() + " port: " + port);
				
				//peerOut.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
		

	}
	
	/**
	 * @author Matthew
	 * GUI thread to start GUI for image peer
	 */
	public static class GUIThread implements Runnable{

		@Override
		public void run() {
			// TODO Auto-generated method stub
			GUI = new ImagePeerGUI(client.getClientID());
		}
		
	}
	
	
	/**
	 * @author Matthew
	 * This method handle the request to server or peers
	 */
	public static class Requester implements Runnable{
		
		/**
		 * this runnable request blocks from either server or peers
		 */
		@Override
		public void run() {
			// TODO Auto-generated method stub
			
			// Generate a list with 0 - 99
			List queue = new ArrayList();
			for (int i = 0; i < 100 ;i++) {
				queue.add(i);
			}
			
			// Shuffle it make the retrieval of blocks random
			Collections.shuffle(queue);
		
			int nextBlockNumber;
			
			int serverCnt = 0;
			int p2pCnt = 0;
			
			PeerList p = getPeerList();
			Random r = new Random();
			for (int i = 0; i < 100 ;i++) {
				client.requestBlock("server", (int) queue.get(i), "0" , 0);
				
				// if there are more then two nodes in the peerlist, which means the third node can get data from server and first peer
				if(p.size() > 2) {
					int nextPeer = r.nextInt(p.size());
					int t = r.nextInt(100);
					
					// t is the reliance to server
					if(t > 80) {
						client.requestBlock("server", (int) queue.get(i), "0" , 0);
						serverCnt++;
					} else {
						client.requestBlock("peer", (int) queue.get(i), (String) peerList.get(nextPeer).getIp(), (int) peerList.get(nextPeer).getPort());
						p2pCnt++;
					}
					
				}else {
					client.requestBlock("server", (int) queue.get(i), "0" , 0);
					serverCnt++;
				}
				
			}
			
			System.out.println("server: " + serverCnt + "/ p2p: " + p2pCnt);
		}
		
	}
	
	/**
	 * @author Matthew
	 * This class turn peer to upload server to handle the request from peers
	 */
	public static class UploadServer implements Runnable{
		ServerSocket ss=null;
		
		// find a new port to start the upload server
		public UploadServer() {
			int port=9000;
			while(ss==null&&port<65536){
				try{
					ss=new ServerSocket(port);
				}catch(IOException e) {
					port++;
				}
			}
			
			System.out.println("Peer Server port: " + port);
		}
		
		@Override
		public void run() {
			// TODO Auto-generated method stub
			try {
				while (true) {
					
					// Start accepting connection from peers
					Socket s = ss.accept();
					
					System.out.println("Got a connection from peer" + s);
					
					// Get input
					BufferedReader reader = new BufferedReader(new InputStreamReader(s.getInputStream()));
					
					// Create a new peer
					PrintWriter writer = new PrintWriter(s.getOutputStream());
					
					// Add writer to list of peers
					System.out.println("Allowing new thread to peer" + s);
					
					Peer newPeer = new Peer(s.getInetAddress().getHostAddress().toString(), s.getLocalPort());
					
					Thread newClient = new Thread(new Uploader(s, reader , writer));
					newClient.start();
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			
				
		}
		
		/**
		 * @author Matthew
		 * This class construct response to peer connection(actually send the response to peers)
		 */
		public static class Uploader implements Runnable {
			Socket client;
			BufferedReader reader;
			PrintWriter writer;
			
			/**
			 * @param client socket
			 * @param in reader from client
			 * @param out printwriter to client
			 */
			public Uploader(Socket client, BufferedReader in, PrintWriter out) {
				this.client = client;
				this.reader = in;
				this.writer = out;
			}
			
			/**
			 * the runnable keep listening from peer and respond
			 */
			@Override
			public void run() {
				// TODO Auto-generated method stub
				String message;
				try {
					while ( (message= reader.readLine()) != null ) {
						System.out.println("PeerServer received: " + message);
						JSONParser parser = new JSONParser();
						JSONObject json = (JSONObject) parser.parse(message);
						
						String command = (String) json.get("Command");
						
						// Return result to client
						JSONObject respond = new JSONObject();
						
						// Get username and clientID
						String username = (String) json.get("Me");			
						int clientID = (int) (long) json.get("clientID");
						switch (command) {
							case "REQUEST_IMG_BLOCK":
								// Get blocknumber
								int blockNumber = (int) (long) json.get("Data_block_number");
								
								//if(!receivedBlockList.contains(blockNumber)) {
									//break;
								//}
								
								// Construct response
								respond.put("Me", "peer");
								respond.put("You", "peer");
								respond.put("Command", "GET_IMG_BLOCK");
								respond.put("Data_image_name", null);
								respond.put("Data_block_number", blockNumber);
								
								Image i = ImagePeerGUI.getBlocks().get(blockNumber).getImage();
								BufferedImage bi = JSONUtils.toBufferedImage(i);
								respond.put("Data_content", JSONUtils.imgToBase64String(bi, "png"));	
								
								try {
									respond.put("Peer_list:", PeerList.serialize(peerList));
								} catch (IOException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
								break;
							
						}
						
						
						writer.println(respond.toString());
						writer.flush();
						System.out.println("Peer server sent: " + respond.toString());
					}
				} catch (IOException | ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			
			}
		}
		
	}
	
	/**
	 * @return current client object
	 */
	public static ImagePeer getClient() {
		return client;
	}
	
	/**
	 * @param id to be set as current client ID
	 */
	public void setClientID(int id) {
		clientID = id;
	}
	
	/**
	 * @return current client ID
	 */
	public static int getClientID() {
		return clientID;
	}
	
	/** 
	 * @param name to be set as current client name
	 */
	public void setClientName(String name) {
		clientName = name;
	}
	
	/**
	 * @return current client name
	 */
	public static String getClientName() {
		return clientName;
	}
	
	/**
	 * @return list of peers
	 */
	public static PeerList getPeerList() {
		return peerList;
	}
	
	/**
	 * @param p list of peers to be set
	 */
	public static void setPeerList(PeerList p) {
		peerList = p;
	}

	/**
	 * @param question to be asked 
	 * @return the answer from user
	 */
	private static String newDialog(String question) {
		JFrame frame = new JFrame("Input");
	    String returnInput = JOptionPane.showInputDialog(
	        frame, 
	        question, 
	        "", 
	        JOptionPane.QUESTION_MESSAGE
	    );
	   
	    return returnInput;
	}
	
}
