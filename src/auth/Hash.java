package auth;

/**
 * @author matthewtsui
 * Hashing interface
 */
public interface Hash {
	/**
	 * @param str
	 * @return hashed string with hashing alogrithm
	 */
	public String hash(String str);
}
