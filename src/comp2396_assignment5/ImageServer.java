package comp2396_assignment5;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.ServerSocket;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

public class ImageServer {
	private static BufferedImage displayImage;
	private static ServerSocket serverSocket;
	
	//Views
	private static JFrame frame;
	private static JPanel container;
	private static JPanel imagePanel;
	
	private static JLabel imageContainer;
	private static JButton btnLoadAnother;
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		loadImage();
		loadLayout();
		initConnection();
		
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
	
	public static void initConnection() {
		try {
			serverSocket = new ServerSocket(9000);
			while (true) {
				serverSocket.accept();
			}
		} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
		}
	}
}
