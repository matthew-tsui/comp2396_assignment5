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

public class PeerList implements Serializable{
	
	ArrayList<Peer> peerList=new ArrayList<Peer>();
	
	synchronized public void addPeer(Peer peer){
		peerList.add(peer);
	}
	
	synchronized public void printPeerList(){
        for (Peer p:peerList){
     	   p.print();
        }
	}
	
	synchronized public void removePeer(int peer){
		peerList.remove(peer);
	} 
	
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
	
	public static PeerList deserialize(String s) throws IOException ,
    ClassNotFoundException {
		byte [] data = Base64.getDecoder().decode( s );
		ObjectInputStream ois = new ObjectInputStream( 
		new ByteArrayInputStream(  data ) );
		Object o  = ois.readObject();
		ois.close();
		return (PeerList) o;
	}
	
	/** Write the object to a Base64 string. */
	public String serialize() throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ObjectOutputStream oos = new ObjectOutputStream( baos );
		oos.writeObject(this);
		oos.close();
		return Base64.getEncoder().encodeToString(baos.toByteArray()); 
	}
}
