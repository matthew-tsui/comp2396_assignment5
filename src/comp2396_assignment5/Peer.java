package comp2396_assignment5;

import java.io.Serializable;

public class Peer implements Serializable{
	private String ip;
	private int port;
	
	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}


	public Peer(String ip, int port) {
		this.ip = ip;
		this.port = port;
	}
	
	public void print() {
		System.out.println("IP:"+ ip + " Port: " + port);
	}
}
