package comp2396_assignment5;

import java.io.PrintWriter;
import java.io.Serializable;

/**
 * @author Matthew
 * Peer object record the port and ip address of client
 */
public class Peer implements Serializable{
	private String ip;
	private int port;
	
	/**
	 * @return peer ip address
	 */
	public String getIp() {
		return ip;
	}

	/**
	 * @param ip to be set for peer
	 */
	public void setIp(String ip) {
		this.ip = ip;
	}

	/**
	 * @return peer port number
	 */
	public int getPort() {
		return port;
	}

	/**
	 * @param port to be set for peer
	 */
	public void setPort(int port) {
		this.port = port;
	}
	
	/** Constructor
	 * @param ip of peer
	 * @param port of peer
	 */
	public Peer(String ip, int port) {
		this.ip = ip;
		this.port = port;
	}
	
	/**
	 * print peer information
	 */
	public void print() {
		System.out.println("IP:"+ ip + " Port: " + port);
	}
	
	
}
