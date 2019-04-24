package comp2396_assignment5;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class ImageServer {
	private static BufferedImage displayImage;
	private static ServerSocket serverSocket;
	private static ArrayList<PrintWriter> clientOutputStreams = new ArrayList();
	private static BufferedReader reader;
	private static PrintWriter writer;
	
	//Views
	private static JFrame frame;
	private static JPanel container;
	private static JPanel imagePanel;
	
	private static JLabel imageContainer;
	private static JButton btnLoadAnother;
	
	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		ImageServer.loadImage();
		ImageServer.loadLayout();
		ImageServer.openConnection();
		
		// initalize database
		Database.load("src/User.txt");
		Database.setHash(new SHA1());
	}
	
	public static void loadImage() {
		displayImage = ImageReader.load();
		if(displayImage == null) {
			System.exit(0);
		}
	}

	public static void loadLayout() {
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
		
		// imagePanel
		imageContainer = new JLabel();
		imageContainer.setIcon(new ImageIcon(displayImage));
		
		imagePanel = new JPanel();
		imagePanel.setSize(700,700);
		imagePanel.add(imageContainer);
		
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
		
			while (true) {
				
				// Start accepting connection
				Socket s = serverSocket.accept();
				
				// Define print writer
				writer = new PrintWriter(s.getOutputStream());
				
				// Create a new peer
				PrintWriter w = new PrintWriter(s.getOutputStream());
				
				// Add writer to list of peers
				clientOutputStreams.add(w);
				
				Thread dataTransferThread = new Thread(new DataTransferRunnable(s));
				dataTransferThread.start();
				
				System.out.println("Got a connection");
			}
		} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
		}
	}
	
	public static class DataTransferRunnable implements Runnable{
		BufferedReader reader;
		Socket sock;

		public DataTransferRunnable(Socket client) {
			try {
				// get input from client
				sock = client;
				InputStreamReader is = new InputStreamReader(sock.getInputStream());
				reader = new BufferedReader(is);
			} catch (Exception e) {e.printStackTrace();}

		}
		
		@Override
		public void run() {
			// TODO Auto-generated method stub
			try {
				// validate the user
				String message = reader.readLine();
				JSONParser parser = new JSONParser();
				JSONObject json = (JSONObject) parser.parse(message);
				
				String username = (String) json.get("Me");
				String password = (String) json.get("Data_content");
				
				// Return result to client
				JSONObject respond = new JSONObject();
				respond.put("Me", "Teacher");
				respond.put("You", username);
				respond.put("Command", Database.auth(username, password) ? "LOGIN_OK" : "LOGIN _FAIL");
				respond.put("Data_image_name", null);
				respond.put("Data_block_number", null);
				respond.put("Data_content", Database.getHash().hash(password));
				respond.put("Peer_list:", null);
				
				writer.println(json.toString());
				writer.flush();
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
	}
	
}



