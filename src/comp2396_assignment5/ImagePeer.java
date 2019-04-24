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
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;



public class ImagePeer extends JPanel{
	private static String serverAddressInput;
	
	private static Socket clientSocket;
	private static BufferedReader reader;
	private static PrintWriter writer;
	
	private static JFrame frame;
	private static JPanel container;
	private static JPanel imagePanel;
	private static JLabel imageContainer;
	private static BufferedImage displayImage;
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		serverAddressInput = newDialog("Connect to server:");
		String usernameInput = newDialog("Username:");
		String passwordInput = newDialog("Password:");
		
		establishConnection();
		startLogin(usernameInput, passwordInput);
	}
	
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
	
	public void loadLayout() {
		loadImageBlock();
		// Frame setting
		frame = new JFrame ("Assignment 5");
		frame.setDefaultCloseOperation (JFrame.EXIT_ON_CLOSE);
		frame.setLayout(new BorderLayout());
		
		container = new JPanel();
		container.setLayout(new BorderLayout());
		container.add(imagePanel, BorderLayout.NORTH);
		
		frame.add(container);
		frame.pack();
		frame.setResizable(true);
		frame.setVisible(true);
	}
	
	public void loadImageBlock() {
		imagePanel = new JPanel();
		imagePanel.setSize(700,700);
		imagePanel.setLayout(new GridLayout(10, 10, 0, 0));
		
		int width = 700;
		int height = 700;
		
		for(int i = 0; i< 10; i++) {
			for(int j = 0; j <10; j++) {
				Image image = createImage(new FilteredImageSource(displayImage.getSource(),
						new CropImageFilter(j * width / 10, i * height / 10,
								(width / 10), height / 10)));
				JLabel block = new JLabel();
				block.setIcon(new ImageIcon(image));
				imagePanel.add(block);
			}
		}
	}
	
	public static void establishConnection() {
		try {
			clientSocket = new Socket("127.0.0.1", 9000);
			InputStreamReader sr = new InputStreamReader(clientSocket.getInputStream());
			reader = new BufferedReader(sr);
			writer = new PrintWriter(clientSocket.getOutputStream());
			
			Thread dataTransferThread = new Thread(new DataTransferRunnable());
			dataTransferThread.start();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	
	public static void startLogin(String username, String password) {
		JSONObject json = new JSONObject();
		json.put("Me", username);
		json.put("You", "Teacher");
		json.put("Command", "LOGIN");
		json.put("Data_image_name", null);
		json.put("Data_block_number", null);
		json.put("Data_content", Database.getHash().hash(password));
		json.put("Peer_list:", null);
		
		writer.println(json.toString());
		writer.flush();
	}
	
	public static class DataTransferRunnable implements Runnable {
		public void run() {
			String message;
			try {
				while ( (message = reader.readLine()) != null ) {
					System.out.println("read" + message);
				}
			} catch (Exception e) { e.printStackTrace(); }
		}
	}
}
