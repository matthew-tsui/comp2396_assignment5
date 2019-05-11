package comp2396_assignment5;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map.Entry;

import org.json.simple.JSONObject;

/**
 * @author Matthew
 * Peer list store a list of peers 
 */
public class PeerList implements Serializable{
	
	private ArrayList<Peer> peerList=new ArrayList<Peer>();
	
	/**
	 * @param peer object to be added
	 */
	synchronized public void addPeer(Peer peer){
		peerList.add(peer);
	}
	
	/**
	 * print peer list information
	 */
	synchronized public void printPeerList(){
        for (Peer p:peerList){
     	   p.print();
        }
	}
	
	/**
	 * @param peer index to be removed
	 */
	synchronized public void removePeer(int peer){
		peerList.remove(peer);
	} 
	
	/**
	 * @return peer list size
	 */
	synchronized public int size(){
		return peerList.size();
	} 
	
	/**
	 * @param i index of peer list
	 * @return i-th peer in the list
	 */
	synchronized public Peer get(int i){
		return peerList.get(i);
	} 
	
	/**
	 * @param port of peer
	 * @return peer using the port
	 */
	public int getPeerByPort(int port) {
		int index = 0;
		for(Peer p: peerList) {
			if(port == p.getPort()) {
				return index;
			}
			index++;
		}
		return -1;
	}
	
	/**
	 * @param s string to be deserialized
	 * @return PeerList from string
	 * @throws IOException if it fails
	 * @throws ClassNotFoundException if it fails
	 */
	public static PeerList deserialize(String s) throws IOException ,
    ClassNotFoundException {
		byte [] data = Base64.getDecoder().decode(s);
		ObjectInputStream ois = new ObjectInputStream( 
		new ByteArrayInputStream(  data ) );
		Object o  = ois.readObject();
		ois.close();
		return (PeerList) o;
	}
	
	/** Write the object to a Base64 string. 
	 * @param o object to written
	 * @return string from object
	 * @throws IOException if fails
	 */
	public static String serialize(Serializable o) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ObjectOutputStream oos = new ObjectOutputStream( baos );
		oos.writeObject(o);
		oos.close();
		return Base64.getEncoder().encodeToString(baos.toByteArray()); 
	}
}
