package advanced.gestureSound.weki;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.HashMap;
import java.util.Map;

import de.sciss.net.OSCClient;
import de.sciss.net.OSCListener;
import de.sciss.net.OSCMessage;
import de.sciss.net.OSCServer;

public class OSCWekinatorInManager implements OSCListener {

	private OSCClient c;
	private WekiInListener callback;
	
	
	public interface WekiInListener {
		public void outParamUpdate(Float[] vals);
		public void startSound();
		public void stopSound();
	}
	
	public OSCWekinatorInManager(WekiInListener callback) {
		try {
			c = OSCClient.newUsing(OSCClient.UDP, 0);
			c.addOSCListener(this);
			c.start();
			//OSCServer.
		} catch (Exception e) {
			e.printStackTrace();
		}
		this.callback = callback;
	}
	
	@Override
	public void messageReceived(OSCMessage arg0, SocketAddress arg1, long arg2) {
		if (arg0.getArgCount() > 0) {
			Object x = arg0.getArg(0);
			System.out.println("New message!");
		}
	}

}

