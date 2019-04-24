package comp2396_assignment5;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * @author matthewtsui
 * SHA1 hashing algorithm implementation
 */
public class SHA1 extends HashOperation implements Hash {
	
	/* (non-Javadoc)
	 * @see comp2396_assignment3_hash.Hash#hash(java.lang.String)
	 */
	@Override
	public String hash(String str) {
		try {
			MessageDigest md = MessageDigest.getInstance("SHA-1");
			md.update(str.getBytes());
			byte[] hash = md.digest();
			
			String result = "";
			for (byte hs : hash) {
				result += byteToHex(hs);
			}
			return result;
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	
}
