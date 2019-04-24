package comp2396_assignment5;

import java.io.IOException;
import java.io.PrintStream;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.io.File;
import java.util.Scanner;

/**
 * @author matthewtsui
 * Database module to store all users and perform add/update and delete operations
 */
public class Database {
	/**
	 * Hashmap to store all users
	 */
	private static HashMap<String, User> userList = new HashMap<String, User>();
	private static Hash hashMethod;
	/**
	 * @param user user object to be inserted
	 */
	public static void insert(User user) {
		userList.put(user.getUsername(), user);
	}
	
	/**
	 * @return the userlist that store all user
	 */
	public static HashMap<String, User> getUserList() {
		return userList;
	}
	
	/**
	 * @param h hashing method to be set
	 */
	public static void setHash(Hash h) {
		hashMethod = h;
	}
	
	/**
	 * @return current hashing method
	 */
	public static Hash getHash() {
		return hashMethod;
	}
	
	/**
	 * @param fname - the name of the JSON file
	 * @throws IOException if the file doesnt exist
	 */
	public static void load(String fname) throws IOException {
		
		 File file  = new File("C:\\Users\\pankaj\\Desktop\\test.txt"); 
		 Scanner sc = new Scanner(file); 
			  
	     while (sc.hasNextLine()) {
	    	 String[] line = sc.nextLine().split(",");
	    	 Database.insert(new User(line[0], ));
	      System.out.println(sc.nextLine()); 
	     } 
        //User newUser = new User((String) info.get("username"),(String)info.get("password"));
        
	}

	/**
	 * @param username input
	 * @param password input
	 * @return whether the given username and password is valid
	 */
	public static Boolean auth(String username, String password) {
		String hash = getHash().hash(password);
        User currentUser = userList.get(username);
        return currentUser.getHashedPassword().equals(hash);
	}
}
