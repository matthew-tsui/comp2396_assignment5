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
import comp2396_assignment5.PeerList;
import server.ImageServer.ClientRunnable;



public class ImagePeer extends JPanel{
	private static String serverAddressInput;
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
	
	
	public static void main(String[] args) throws ClassNotFoundException, IOException, ParseException {
		// TODO Auto-generated method stub
		Database.setHash(new SHA1());
		serverAddressInput = newDialog("Connect to server:");
		//String usernameInput = newDialog("Username:");
		//String passwordInput = newDialog("Password:");
		String usernameInput = "cbchan";
		String passwordInput = "HelloWorld0";
		
		client = new ImagePeer();
		client.establishConnection();
		client.startLogin(usernameInput, passwordInput);
		client.maintainConnection2();
		
		
	}
	
	/**
	 * establish connection to server
	 */
	public void establishConnection() {		
		try {
			Socket clientSocket = new Socket("127.0.0.1", 9000);
			InputStreamReader sr = new InputStreamReader(clientSocket.getInputStream());
			in = new BufferedReader(sr);
			out = new PrintWriter(clientSocket.getOutputStream());
			setClientID(clientSocket.getLocalPort());
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 

	}
	public void maintainConnection2() throws IOException, ParseException, ClassNotFoundException {
		String message;
		while ( (message=in.readLine()) != null ) {
			//System.out.println("client received: " + message);
			JSONParser parser = new JSONParser();
			JSONObject json = (JSONObject) parser.parse(message);
			
			String command = (String) json.get("Command");
			String username = (String) json.get("Me");
			switch (command) {
				case "LOGIN_OK":
					String peerList = (String) json.get("Peer_list");
					setClientName(username);
					setPeerList(PeerList.deserialize(peerList));
					
					System.out.println("Client login successfully");
					
					// start gui
					Thread gui = new Thread(new GUIThread());
					gui.start();
					
					
					//Thread Downloader = new Thread(new Downloader());
					//Downloader.start();
					
					Thread Requester = new Thread(new Requester());
					Requester.start();
					
					System.out.println("list: " + receivedBlockList);
					// request blocks from client
					
					/*
					Random r = new Random();
					int nextBlockNumber;
					for (int i = 0; i < 100 ;i++) {
						do {
							nextBlockNumber = r.nextInt(100);
						} while(receivedBlockList.contains(nextBlockNumber));
						
						System.out.println("Requesting block: " + nextBlockNumber);
						
						client.requestBlock("server", nextBlockNumber);
						try {
							Thread.sleep(50);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}*/
					
					
					break;
				case "GET_IMG_BLOCK":
					int blockNumber = (int) (long) json.get("Data_block_number");
					String content = (String) json.get("Data_content");
					ImagePeerGUI.setBlock(JSONUtils.base64StringToImg((String) content), blockNumber);
					System.out.println("Received block " + blockNumber + " from " + username);
					
					// Mark block as received
					//synchronized(receivedBlockList) {
						receivedBlockList.add(blockNumber);
					//}
					//ImagePeerGUI.updateLayout();
					System.out.println("list: " + receivedBlockList.size());
					System.out.println("list content: " + receivedBlockList);
					break;
				case "UPDATE_IMG_BLOCK":
					int blockNumber1 = (int) (long) json.get("Data_block_number1");
					String content1 = (String) json.get("Data_content1");
					ImagePeerGUI.setBlock(JSONUtils.base64StringToImg((String) content1), blockNumber1);
					System.out.println("Client updated block number" + blockNumber1);
					
					int blockNumber2 = (int) (long) json.get("Data_block_number2");
					String content2 = (String) json.get("Data_content2");
					ImagePeerGUI.setBlock(JSONUtils.base64StringToImg((String) content2), blockNumber2);
					System.out.println("Client updated block number" + blockNumber2);
					
					//ImagePeerGUI.updateLayout();
			}
		
		}
		
		
		in.close();
	    out.close();
	}
	
	public static class BlockList extends ArrayList<Integer>{
		public synchronized void add(int blockNumber) {
			this.add(blockNumber);
		}
		
		public synchronized boolean contains(int blockNumber) {
			return (this.contains(blockNumber)) ? true : false;
		}
		
	}
	
	@SuppressWarnings("unchecked")
	public void sendLogoutRequest() {
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
	
	/**
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
	
	@SuppressWarnings("unchecked")
	public void requestBlock(String target, int blockNum) {
		JSONObject json = new JSONObject();
		json.put("Me", getClientName());
		json.put("You", target);
		json.put("clientID", getClientID());
		json.put("Command", "REQUEST_IMG_BLOCK");
		json.put("Data_image_name", null);
		json.put("Data_block_number", blockNum);
		json.put("Data_content", blockNum);
		json.put("Peer_list:", null);
		
		out.println(json.toString());
		out.flush();
		
		System.out.println("Client sent block request to "+ target + ":  " + json.toString());
	}
	
	public static class GUIThread implements Runnable{

		@Override
		public void run() {
			// TODO Auto-generated method stub
			new ImagePeerGUI(client.getClientID());
		}
		
	}
	public static class Downloader implements Runnable{
		@Override
		public void run() {
			// TODO Auto-generated method stub
			try {
				String message;
				while ((message=in.readLine()) != null ) {
					message = in.readLine();
					JSONParser parser = new JSONParser();
					JSONObject json = (JSONObject) parser.parse(message);
					
					String command = (String) json.get("Command");
					String username = (String) json.get("Me");
					
					switch(command) {
						case "GET_IMG_BLOCK":
							int blockNumber = (int) (long) json.get("Data_block_number");
							String content = (String) json.get("Data_content");
							ImagePeerGUI.setBlock(JSONUtils.base64StringToImg((String) content), blockNumber);
							System.out.println("Received block " + blockNumber + " from " + username);
							
							// Mark block as received
							//synchronized(receivedBlockList) {
								receivedBlockList.add(blockNumber);
							//}
							//ImagePeerGUI.updateLayout();
							try {
								Thread.sleep(50);
							} catch (InterruptedException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							System.out.println("list: " + receivedBlockList.size());
							System.out.println("list content: " + receivedBlockList);
							break;
					}

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
	
	public static class Requester implements Runnable{
		
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
			for (int i = 0; i < 100 ;i++) {
				client.requestBlock("server", (int) queue.get(i));
			}
		}
		
	}
	
	public static ImagePeer getClient() {
		return client;
	}
	
	public void setClientID(int id) {
		clientID = id;
	}
	
	public int getClientID() {
		return clientID;
	}
	
	public void setClientName(String name) {
		clientName = name;
	}
	
	public String getClientName() {
		return clientName;
	}
	
	public static PeerList getPeerList() {
		return peerList;
	}
	
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
