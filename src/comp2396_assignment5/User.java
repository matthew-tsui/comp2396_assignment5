package comp2396_assignment5;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * @author matthewtsui
 * User class
 */
public class User{
	private	String username;
	private String hashedPassword;

	public User(String username, String hashedPassword) {
		this.username = username;
		this.hashedPassword = hashedPassword;
	}
	
	/**
	 * @return username
	 */
	public String getUsername() {
		return username;
	}
	
	/**
	 * @return hashed password
	 */
	public String getHashedPassword() {
		return hashedPassword;
	}
}
