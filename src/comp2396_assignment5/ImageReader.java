package comp2396_assignment5;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JFileChooser;

/**
 * @author matthewtsui
 * ImageReader class to read and resize image
 */
public class ImageReader {
	/**
	 * @return the resized image from file chooser
	 */
	public static BufferedImage load() {
		JFileChooser fc = new JFileChooser();
		fc.setDialogTitle("Choose an image");
		int openReturnVal = fc.showOpenDialog(null);
		
		if (openReturnVal == JFileChooser.APPROVE_OPTION) {
			File file = fc.getSelectedFile();
			BufferedImage bimg = null;
			try {
				bimg = ImageIO.read(new File(file.getAbsolutePath()));
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			if (bimg == null) {
				return null;
			}
			
			BufferedImage resizedImage = resizeImage(bimg);
			
			return resizedImage;
		} else {
			return null;
		}
	}
	
	/**
	 * @return the default resized image (for debug purpose)
	 */
	public static BufferedImage loadDefault() {
		BufferedImage bimg = null;
		try {
			bimg = ImageIO.read(new File("src/image.png"));
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		if (bimg == null) {
			return null;
		}
		
		BufferedImage resizedImage = resizeImage(bimg);
		
		return resizedImage;
	}
	
	/**
	 * @param image to be resize
	 * @return resized image according to specification asss
	 */
	private static BufferedImage resizeImage(BufferedImage image) {
		Image tmp = image.getScaledInstance(700, 700, Image.SCALE_DEFAULT);
		BufferedImage resizedImage = new BufferedImage(700, 700, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g2d = resizedImage.createGraphics();
	    g2d.drawImage(tmp, 0, 0, null);
	    g2d.dispose();
		return resizedImage;
	}
		
	
}
