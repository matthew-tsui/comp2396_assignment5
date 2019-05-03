package comp2396_assignment5;

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

import comp2396_assignment5.ImageServer.ClientRunnable;



public class ImagePeer extends JPanel{
	private static String serverAddressInput;
	
	private static Socket clientSocket;
	private static BufferedReader in;
	private static PrintWriter out;
	private static String clientName;
	private static Integer clientID;
	private static ImagePeer client;
	private static PeerList peerList;
	private static ArrayList<Integer> receivedBlockList = new ArrayList();
	
	private static JFrame frame;
	private static JPanel container;
	private static JPanel imagePanel;
	private static BufferedImage displayImage;
	
	
	public static void main(String[] args) throws ClassNotFoundException, IOException, ParseException {
		// TODO Auto-generated method stub
		Database.setHash(new SHA1());
		serverAddressInput = newDialog("Connect to server:");
		String usernameInput = newDialog("Username:");
		String passwordInput = newDialog("Password:");
		
		client = new ImagePeer();
		client.establishConnection();
		client.startLogin(usernameInput, passwordInput);
		client.maintainConnection2();
		
		
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
	
	/**
	 * establish connection to server
	 */
	public void establishConnection() {		
		try {
			clientSocket = new Socket("127.0.0.1", 9000);
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
			System.out.println("client received: " + message);
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
					Thread gui = new Thread(new GUIThread());
					gui.start();
					
					Random r = new Random();
					for (int i = 0; i < 100 ;i++) {
						int nextBlockNumber;
						do {
							nextBlockNumber = r.nextInt(100);
						} while(receivedBlockList.contains(nextBlockNumber));
						
						System.out.println("Requesting block: " + nextBlockNumber);
						
						client.requestBlock("server", nextBlockNumber);
					}
					break;
				case "GET_IMG_BLOCK":
					System.out.println("Received block from" + username);
					int blockNumber = (int) (long) json.get("Data_block_number");
					String content = (String) json.get("Data_content");
					ImagePeerGUI.setBlock(JSONSerialize.base64StringToImg((String) content), blockNumber);

					break;
				
			}
		
		}
	}
	/**
	 * maintain connection to server
	 * @throws ClassNotFoundException 
	 */
	public void maintainConnection() throws ClassNotFoundException{
		// Proceed to transaction loop only when user login successfully
		
		while (true) {
			String message;
			try {
				
				message = in.readLine();
				System.out.println("Client received: " + message);
				
				// get login status from json string
				JSONParser parser = new JSONParser();
				JSONObject json;
				try {
					json = (JSONObject) parser.parse(message);
					String username = (String) json.get("You");
					String command = (String) json.get("Command");
					String peerList = (String) json.get("Peer_list");
					if(command.equals("LOGIN_OK")) {						
						setClientName(username);
						setPeerList(PeerList.deserialize(peerList));
						break;
					} else {
						JOptionPane.showMessageDialog(frame, "Login Fail", "Message", JOptionPane.ERROR_MESSAGE);
						System.exit(1);
					}
					
				} catch (ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			
		}
		
		System.out.println("Client login successfully");
		new ImagePeerGUI(getClientID());
		// there is only n - 1 peers excluding the server
		
		Thread Requester = new Thread(new Requester(in, out));
		Requester.start();
				
		Thread Downloader = new Thread(new Downloader(in ,out));
		Downloader.start();
		System.out.println("Started downloading thread successfully");
		
		
		
		/*while(true) {
			try {
				String message = in.readLine();
				PeerList p = getPeerList();
				
				Random r = new Random();
				// there is only n - 1 peers excluding the server
				for (int i = 0; i < 100 ;i++) {
					int nextBlockNumber;
					do {
						nextBlockNumber = r.nextInt(100);
					} while(!receivedBlockList.contains(nextBlockNumber));
					
					System.out.println("Requesting block" + nextBlockNumber);
					
					client.requestBlock("server", nextBlockNumber);
					
				}
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}*/
		
		
	}
	
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
		
		System.out.println("Client sent block request: " + json.toString());
	}
	
	public static class GUIThread implements Runnable{

		@Override
		public void run() {
			// TODO Auto-generated method stub
			new ImagePeerGUI(client.getClientID());
		}
		
	}
	public static class Downloader implements Runnable{
		BufferedReader in;
		PrintWriter out;
		public Downloader(BufferedReader in, PrintWriter out) {
			this.in = in;
			this.out = out;
		}
		
		@Override
		public void run() {
			// TODO Auto-generated method stub
			try {
				String message;
				while ( (message=in.readLine()) != null ) {
					JSONParser parser = new JSONParser();
					JSONObject json = (JSONObject) parser.parse(message);
					
					String command = (String) json.get("Command");
					
					switch(command) {
						case "GET_IMAGE_BLOCK":
							String username = (String) json.get("username");
							System.out.println("Received block from" + username);
							int blockNumber = (Integer) json.get("Data_block_no");
							String content = (String) json.get("Data_content");
							ImagePeerGUI.setBlock(JSONSerialize.base64StringToImg((String) content), blockNumber);
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
		BufferedReader in;
		PrintWriter out;
		public Requester(BufferedReader in, PrintWriter out) {
			this.in = in;
			this.out = out;
		}
		
		@Override
		public void run() {
			// TODO Auto-generated method stub
			Random r = new Random();
			for (int i = 0; i < 100 ;i++) {
				int nextBlockNumber;
				do {
					nextBlockNumber = r.nextInt(100);
				} while(receivedBlockList.contains(nextBlockNumber));
				
				System.out.println("Requesting block" + nextBlockNumber);
				
				client.requestBlock("server", nextBlockNumber);
			}
			
		}
		
	}
	
	
}
