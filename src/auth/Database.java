package auth;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
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
	public static void loadJSON(String fname) throws IOException {
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
                JSONObject info = (JSONObject) obj;  
                User newUser = new User((String) info.get("username"), (String) info.get("hash_password"));
        		newUser.setName((String) info.get("Full Name"));
        		newUser.setEmail((String) info.get("Email"));
        		newUser.setPhoneNumber(info.get("Phone number").toString());
        		
        		int loginCount = (int) ((long) info.get("Fail count"));
        		newUser.setFailLoginCnt(loginCount);
        		
        		newUser.setLastLogin((String) info.get("Last Login Date"));
        		newUser.setAccountLocked((boolean) info.get("Account locked"));
        		
                Database.insert(newUser);
            }
        } catch (ParseException e) {
            return;
        }
	}
	
	/**
	 * @param fname desired filename of the output json file
	 * @throws IOException if there is any error
	 */
	@SuppressWarnings("unchecked")
	public static void output(String fname) throws IOException {
        JSONArray user_array = new JSONArray();
        for (User user : userList.values()) {
            JSONObject obj = new JSONObject();
            obj.put("username", user.getUsername());
            obj.put("hash_password", user.getHashedPassword());
            obj.put("Full Name", user.getName());
            obj.put("Email", user.getEmail());
            obj.put("Phone number", user.getPhoneNumber());
            obj.put("Fail count", user.getFailLoginCnt());
            obj.put("Last Login Date", user.getLastLogin());
            obj.put("Account locked", user.getAccountLocked());
            user_array.add(obj);
        }
        
        JSONObject obj = new JSONObject();
        obj.put("user_array", user_array);
        PrintStream out = new PrintStream(fname);
        out.write(obj.toJSONString().getBytes());
        out.close();
       // Files.write(path, obj.toJSONString().getBytes());
	}
	
	/**
	 * @param s user input to be checked
	 * @return whether the password is valid
	 */
	public static Boolean isValidPassword(String s) {

		Boolean valid_flag = false;
		char ch;
	    boolean capitalFlag = false;
	    boolean lowerCaseFlag = false;
	    boolean numberFlag = false;
	    boolean length_flag = false;
		if(s.length() >= 6) {
			length_flag = true;
		} 
		
		
	    for(int i=0;i < s.length();i++) {
	        ch = s.charAt(i);
	        if( Character.isDigit(ch)) {
	            numberFlag = true;
	        } else if (Character.isUpperCase(ch)) {
	            capitalFlag = true;
	        } else if (Character.isLowerCase(ch)) {
	            lowerCaseFlag = true;
	        }
	        if(numberFlag && capitalFlag && lowerCaseFlag && length_flag)
	            valid_flag = true;
	    }
	    if(!valid_flag) {
	    	System.out.println("Your password has to fulfil: at least 1 small letter, 1 capital letter, 1 digit!");
	    }
	    return valid_flag;
	}
	
	/**
	 * @param username username of the user
	 * @return whether the username exist is the system
	 */
	public static Boolean validateUsername(String username) {
		if(!userList.containsKey(username)) {
			System.out.println("Invalid username! Login Failed");
			return false;
		} else {
			return true;
		}
	}
	
	/**
	 * @param username of the user
	 * @return whether the accound is locked
	 */
	public static Boolean checkLockStatus(String username) {
		if(!validateUsername(username)) return false;
		
		User user = userList.get(username);
		if(user.getAccountLocked()) {
			return true;
		}
		
		if(user.getFailLoginCnt() >=3) {
			user.setAccountLocked(true);
			Database.insert(user);
			return true;
		} 
		
		return false;
	}
	
	/**
	 * @param p1 String to compare
	 * @param p2 String to compare
	 * @return if the string are the same
	 */
	public static Boolean checkEqualEntry(String p1, String p2) {
		if(!p1.equals(p2)) {
			System.out.print("Password not match! ");
			return false;
		}
		return true;
	}
	/**
	 * @param username input
	 * @param password input
	 * @return whether the given username and password is valid
	 */
	public static Boolean auth(String username, String password) {
		if(!userList.containsKey(username)) {
			return false;
		} 
		
		//return true;
		//String hash = getHash().hash(password);
        User currentUser = userList.get(username);
        
        boolean success_flag = currentUser.getHashedPassword().equals(password);
        if (success_flag) {
            currentUser.setFailLoginCnt(0);
			currentUser.setLastLogin(LocalDate.now().toString());
			
        } else {
            currentUser.setFailLoginCnt(currentUser.getFailLoginCnt() + 1);
            if (!checkLockStatus(username))
                System.out.println("Login failed!");
        }
        
        return success_flag;
	}
}
