package auth;

/**
 * @author matthewtsui
 * Hash operation store general method used by hashing alogorithms
 */
public class HashUtils {
	/**
	 * @param num byte to convert to Hex
	 * @return Hex representation of the byte
	 */
	public String byteToHex(byte num) {
	    char[] hexDigits = new char[2];
	    hexDigits[0] = Character.forDigit((num >> 4) & 0xF, 16);
	    hexDigits[1] = Character.forDigit((num & 0xF), 16);
	    return new String(hexDigits);
	}
}
