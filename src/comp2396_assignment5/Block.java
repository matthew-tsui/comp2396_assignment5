package comp2396_assignment5;

import java.awt.Color;
import java.awt.Image;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.TransferHandler;

/**
 * @author matthewtsui
 * block class represents each sub-image in the program
 */
public class Block extends JLabel{
	/** Constructor
	 * @param i x-coordinate of piece
	 * @param j y-coordinate of piece
	 * @param o order number for solution check
	 */
	Image image;
	public Block(int i, int j, int o){
		//super(i + "," + j + "(" + o + "");
        setSize(70, 70);
        setBorder(BorderFactory.createLineBorder(Color.gray));
        putClientProperty("order", o);  
    }
	
	/**
	 * @return image of the current block
	 */
	public Image getImage() {
		return image;
	}
	
	/**
	 * @param img for the block
	 */
	public void setImage(Image img) {
		image  = img;
	}
}
