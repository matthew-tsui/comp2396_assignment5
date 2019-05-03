package comp2396_assignment5;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;

import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

import javax.imageio.ImageIO;
import javax.swing.Icon;
import javax.swing.ImageIcon;

import org.json.simple.JSONObject;

public class JSONUtils {
	
	public static BufferedImage toBufferedImage(Image img)
	{
	    if (img instanceof BufferedImage)
	    {
	        return (BufferedImage) img;
	    }

	    // Create a buffered image with transparency
	    BufferedImage bimage = new BufferedImage(img.getWidth(null), img.getHeight(null), BufferedImage.TYPE_INT_ARGB);

	    // Draw the image on to the buffered image
	    Graphics2D bGr = bimage.createGraphics();
	    bGr.drawImage(img, 0, 0, null);
	    bGr.dispose();

	    // Return the buffered image
	    return bimage;
	}
	
	public static String imgToBase64String(final RenderedImage img, final String formatName) {
		 final ByteArrayOutputStream os = new ByteArrayOutputStream();
		 try {
		 ImageIO.write(img, formatName, Base64.getEncoder().wrap(os));
		 return os.toString(StandardCharsets.ISO_8859_1.name());
		 } catch (final IOException ioe) {
		 throw new UncheckedIOException(ioe);
		 }
		}
	
	public static BufferedImage base64StringToImg(final String base64String) {
	 try {
	 return ImageIO.read(new ByteArrayInputStream(Base64.getDecoder().decode(base64String)));
	 } catch (final IOException ioe) {
	 throw new UncheckedIOException(ioe);
	 }
	}
}
