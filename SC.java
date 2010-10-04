package advanced.gestureSound;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;

import de.sciss.jcollider.Group;
import de.sciss.jcollider.JCollider;
import de.sciss.jcollider.NodeWatcher;
import de.sciss.jcollider.Server;
import de.sciss.jcollider.ServerOptions;
import de.sciss.jcollider.Synth;
import de.sciss.jcollider.SynthDef;
import de.sciss.jcollider.UGenInfo;

public class SC {
	
	
	public final String fs = File.separator;
	public java.util.List defTables;
	public Server server = null;
	public ServerOptions serveropts = null;
	public NodeWatcher nw = null;
	public Group grpAll;

	
	public void setupSupercollider() {
		
		try {
			System.out.println("Testing server....");
			//server = new Server("localhost");
			serveropts= new ServerOptions();
			setServerOptions(serveropts);
			server = new Server("localhost",new InetSocketAddress("127.0.0.1",57110),serveropts);

			File f = findFile(JCollider.isWindows ? "scsynth.exe" : "scsynth",
					new String[] {
							fs + "Applications" + fs + "SuperCollider_f",
							fs + "Applications" + fs + "SC3",
							fs + "usr" + fs + "local" + fs + "bin",
							fs + "usr" + fs + "bin", "C:\\Program Files\\SC3",
							"C:\\Program Files (x86)\\SuperCollider" });

			if (f != null) {
				System.out.println("Trying to start program at: " + f);
				Server.setProgram(f.getAbsolutePath());
			} else {
				System.out.println("CANNOT FIND PROGRAM");
			}

			try {
				server.start();
				server.startAliveThread();
				initServer();
				//while(!server.isRunning()) {}
				try {
					Thread.sleep(2);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} catch (IOException e1) {
				System.out.println("Ughhh server won't start");
			}
			// if( server.isRunning() ) initServer();

		} catch (IOException e1) {
			System.out.println("OOPS SOMETHIGN went wrong, server wont start!");
		}
		sendDefs();
	
	}
	
	public static File findFile(String fileName, String[] folders) {
		File f;

		for (int i = 0; i < folders.length; i++) {
			f = new File(folders[i], fileName);
			if (f.exists())
				return f;
		}
		return null;
	}

	/**
	 * this is a useful function if you're using jcollider's synthdefs.
	 * however I have not.
	 */
	public void sendDefs() {
		File dir = new File(System.getProperty("user.dir")+"/data/synthdefs/");
		for (File syn : dir.listFiles()) {
			try {
				//System.out.println("trying to load "+syn+"...");
				if (syn.getName().startsWith("."))
				{
						System.out.println("Ignoring this file ("+syn.getName()+")");
				}
				else
				{
					SynthDef.readDefFile(syn)[0].send(server);
				}
			} catch (IOException e) {
				System.out.println(syn+" is an invalid synthdef! trying to continue without it! if you notice funny behavior, chances are its because of this.");
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * passes a whole bunch of options found in the config file to supercollider
	 * @param s
	 */
	public void setServerOptions(ServerOptions s)
	{

	}
	
	
	/**
	 * this is a useful function if you're using jcollider's synthdefs.
	 * however I have not.
	 */
	public void createDefs() {
		try {
			// UGenInfo.readDefinitions();
			UGenInfo.readBinaryDefinitions();

			// defTables = DemoDefs.create();
			// defTables[ 1 ].addDefs( collDefs );
			// defTables[ 0 ].addDefs( collDefs );
		} catch (IOException e1) {
			e1.printStackTrace();
			// reportError( e1 );
		}
	}

	public void initServer() throws IOException {
		// sendDefs();
		if (!server.didWeBootTheServer()) {
			server.initTree();
			server.notify(true);
		}
		// if( nw != null ) nw.dispose();
		nw = NodeWatcher.newFrom(server);
		grpAll = Group.basicNew(server);
		nw.register(server.getDefaultGroup());
		nw.register(grpAll);
		server.sendMsg(grpAll.newMsg());
	}
	
	
	
	public void testSupercollider() {
		try {
			Synth s = new Synth("stereosine", new String[] {"out", "freq"}, new float[] { 0, 1213f }, grpAll);
			Synth a = new Synth("stereosine", new String[] {"out", "freq"}, new float[] { 1, 1213f }, grpAll);
			System.out.println("Made synth!");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
