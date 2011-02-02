package advanced.gestureSound.weki;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;

import de.sciss.net.OSCClient;
import de.sciss.net.OSCMessage;
import de.sciss.net.OSCServer;

public class OSCWekinatorOutManager {

	private OSCServer s;
	
	private Map<String, Integer> paramMap;
	private Float[] params;
	
	public OSCWekinatorOutManager() {
		try {
			s = OSCServer.newUsing(OSCClient.UDP, 0);
			s.start();
			//OSCServer.
		} catch (Exception e) {
			e.printStackTrace();
		}
		paramMap = new HashMap<String,Integer>();
		params = new Float[0];
	}
	
	public void send() {
		//System.out.println("Sending: "+params[0]);
		if (params == null) return;
		OSCMessage msg = new OSCMessage("/oscCustomFeatures", params);
		try {
			s.send(msg, new InetSocketAddress( "localhost", 6448 ));
		} catch (IOException e) { e.printStackTrace(); }
	}
	
	public void addParam(String name) {
		int position = params.length;
		params = new Float[position+1]; //make it one bigger
		for (int i=0;i<params.length;i++) params[i]=0.0f;
		paramMap.put(name, position);
	}
	

	public void updateParam(String name, float value) {
		if (paramMap.containsKey(name) && paramMap.get(name) < params.length)
			params[paramMap.get(name)] = value;
	}
	
	public void updateParam(Float[] vals) {
		params = vals;
	}

}

