package comp2396_assignment5;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.io.File;
import java.util.Scanner;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

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
		String data = "";
        Path fpath = Paths.get(fname);
        
        if (Files.exists(fpath)) {
            data = new String(Files.readAllBytes(fpath));
        } else {
            Files.createFile(fpath);
        }
        
        JSONParser parser = new JSONParser();
        try {
            JSONObject json = (JSONObject) parser.parse(data);
            JSONArray userList = (JSONArray) json.get("user_array");
            for (Object obj : userList) {
                JSONObject eachEntry = (JSONObject) obj;	
                Database.insert(new User((String) eachEntry.get("username"), (String) eachEntry.get("hash_password")));
            }
        } catch (ParseException e) {
            return;
        }
	}

	/**
	 * @param username input
	 * @param password input (hashed)
	 * @return whether the given username and password is valid
	 
	public static Boolean auth(String username, String password) {
        User currentUser = userList.get(username);
        return currentUser.getHashedPassword().equals(password);
	}*/
	
	public static Boolean auth(String username, String password) {
        return true;
	}
}
