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
	private String name;
	private String email;
	private String phoneNumber;
	private int failLoginCnt = 0;
	private String lastLogin;
	private Boolean accountLocked = false;
	
	public User(String username, String password) {
		this.username = username;
		this.hashedPassword = password;
	}
	/**
	 * @return username
	 */
	public String getUsername() {
		return username;
	}
	/**
	 * @param username to be set
	 */
	public void setUsername(String username) {
		this.username = username;
	}
	
	/**
	 * @return hashed password
	 */
	public String getHashedPassword() {
		return hashedPassword;
	}
	
	/**
	 * @param hashedPassword to be set
	 */
	public void setHashedPassword(String hashedPassword) {
		this.hashedPassword = hashedPassword;
	}
	
	/**
	 * @return full name of user
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * @param full name of user to be set
	 */
	public void setName(String name) {
		this.name = name;
	}
	
	/**
	 * @return user email
	 */
	public String getEmail() {
		return email;
	}
	
	/**
	 * @param email be to set
	 */
	public void setEmail(String email) {
		this.email = email;
	}
	
	/**
	 * @return user phone number
	 */
	public String getPhoneNumber() {
		return phoneNumber;
	}
	
	/**
	 * @param phoneNumber to be set
	 */
	public void setPhoneNumber(String phoneNumber) {
		this.phoneNumber = phoneNumber;
	}
	/**
	 * @return fail login count
	 */
	public int getFailLoginCnt() {
		return failLoginCnt;
	}
	/**
	 * @param failLoginCnt to be set
	 */
	public void setFailLoginCnt(int failLoginCnt) {
		this.failLoginCnt = failLoginCnt;
	}
	/**
	 * @return last login date
	 */
	public String getLastLogin() {
		return lastLogin;
	}
	/**
	 * @param lastLogin date to be set
	 */
	public void setLastLogin(String lastLogin) {
		this.lastLogin = lastLogin;
	}
	
	/**
	 * @return whether account is locked
	 */
	public Boolean getAccountLocked() {
		return accountLocked;
	}
	
	/**
	 * @param accountLocked - to lock the account
	 */
	public void setAccountLocked(Boolean accountLocked) {
		this.accountLocked = accountLocked;
	}
	
}

