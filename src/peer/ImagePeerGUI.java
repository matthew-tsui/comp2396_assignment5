package peer;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.awt.image.CropImageFilter;
import java.awt.image.FilteredImageSource;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;

import comp2396_assignment5.Component;

/**
 * @author matthewtsui
 * GUI of image peer
 */
public class ImagePeerGUI extends JPanel{
	private static JFrame frame;
	private static JPanel container;
	private static JPanel imagePanel;
	private static ArrayList<Component> Blocks = new ArrayList();
	private static Integer clientID;
	
	/**
	 * Constructor
	 */
	public ImagePeerGUI(Integer clientID){
		this.clientID = clientID;
		loadLayout();
	}
	
	/**
	 * Load empty block
	 */
	public void loadLayout() {	
		imagePanel = new JPanel();
		//imagePanel.setBorder(BorderFactory.createLineBorder(Color.gray));
		imagePanel.setLayout(new GridLayout(10, 10, 0, 0));
		imagePanel.setSize(700,700);

		int width = 700;
		int height = 700;

		// initalize Jlabel
		int blockNumber = 0;
		for(int i = 0; i< 10; i++) {
			for(int j = 0; j <10; j++) {
				Component block = new Component(i,j, blockNumber);
				blockNumber++;
				Blocks.add(block);
			}
		}
		 
		 // add Jlabel to puzzle
		 for(int i = 0; i < 100; i++) {
			 Component p = Blocks.get(i);
			 imagePanel.add(p);
		 }

		// Frame setting
		frame = new JFrame ("ImagePeer");
		frame.setDefaultCloseOperation (JFrame.EXIT_ON_CLOSE);
		frame.setLayout(new BorderLayout());
		frame.addWindowListener(new WindowAdapter()
        {
            @Override
            public void windowClosing(WindowEvent e)
            {
                //ImagePeer.sendCloseRequest(clientID);
                e.getWindow().dispose();
            }
        });
		
		container = new JPanel();
		container.setLayout(new BorderLayout());
		container.add(imagePanel, BorderLayout.NORTH);
		
		frame.add(container);
		frame.setSize(700,760);
		frame.setResizable(true);
		frame.setVisible(true);
	}
	
	/** 
	 * @param image to be set
	 * @param number of block to be inserted to view
	 */
	public static void setBlock(BufferedImage image, int number) {	
		Component p = Blocks.get(number);
		p.setIcon(new ImageIcon(image));
		
	}
	
	/**
	 * update layout after swapping image block
	 */
	public static void updateLayout() {
		imagePanel.removeAll();
		for(Component p : Blocks) {
			imagePanel.add(p);
		}
		imagePanel.revalidate();
		imagePanel.repaint();
	}
}
