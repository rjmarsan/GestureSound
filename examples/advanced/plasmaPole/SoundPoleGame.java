package advanced.plasmaPole;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;

import msafluid.MSAFluidSolver2D;

import org.mt4j.components.MTComponent;

import processing.core.PApplet;
import processing.core.PGraphics;
import de.sciss.jcollider.Group;
import de.sciss.jcollider.JCollider;
import de.sciss.jcollider.NodeWatcher;
import de.sciss.jcollider.Server;
import de.sciss.jcollider.ServerOptions;
import de.sciss.jcollider.Synth;
import de.sciss.jcollider.SynthDef;
import de.sciss.jcollider.UGenInfo;

public class SoundPoleGame extends MTComponent {


	String statusMessage;
	MSAFluidSolver2D fluidSolver;
	PApplet applet;
	
	Pole a;
	Pole b;
	
	final String fs = File.separator;
	private java.util.List defTables;
	public Server server = null;
	public ServerOptions serveropts = null;
	public NodeWatcher nw = null;
	public Group grpAll;
	Synth synth1;


	public SoundPoleGame(PApplet applet, MSAFluidSolver2D fluidSolver) {
		super(applet);
		// TODO Auto-generated constructor stub
		this.fluidSolver = fluidSolver;
		this.applet = applet;
		b = new Pole(applet, fluidSolver);
		b.placeBall(applet.width/4, applet.height/2);
		a = new Pole(applet, fluidSolver);
		a.placeBall((applet.width*3)/4, applet.height/2);

		setupSupercollider();
		//testSupercollider();
		createPole();
	}
	
	private void setupSupercollider() {

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
	
	private static File findFile(String fileName, String[] folders) {
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
	private void sendDefs() {
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
	private void setServerOptions(ServerOptions s)
	{

	}
	
	
	/**
	 * this is a useful function if you're using jcollider's synthdefs.
	 * however I have not.
	 */
	private void createDefs() {
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

	private void initServer() throws IOException {
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
	
	
	
	void testSupercollider() {
		try {
			Synth s = new Synth("stereosine", new String[] {"out", "freq"}, new float[] { 0, 1213f }, grpAll);
			Synth a = new Synth("stereosine", new String[] {"out", "freq"}, new float[] { 1, 1213f }, grpAll);
			System.out.println("Made synth!");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	void createPole() {
		try {
			synth1 = new Synth("stereosine", new String[] {"out", "freq"}, new float[] { 0, 400f }, grpAll);
			System.out.println("Made synth!");
			b.setSynthAndParams(synth1, "freq", "amp", (new Pole.SynthParamMapper() { 
				public float mapX(float x) {return (float) Math.log(x*x)*300; }
				public float mapY(float y) {return y/30; } })
				);
			Synth reverb = new Synth("rjFreeverb1x1", new String[] {"in", "out"}, new float[] { 0, 0 }, synth1, Synth.kAddAfter);
			a.setSynthAndParams(synth1, "room", "mix", (new Pole.SynthParamMapper() { 
				public float mapX(float x) {return (float) Math.log(x*x)*10; }
				public float mapY(float y) {return y/2; } })
				);
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	

	void resetFluid() {
		setupFluid();
		fluidSolver.reset();
	}

	@Override
	public void drawComponent(PGraphics g) {
		b.draw(g, fluidSolver);
		a.draw(g, fluidSolver);
	}
	
	void setupFluid() {
		  fluidSolver.enableRGB(true).setFadeSpeed(0.001f).setDeltaT(0.5f).setVisc(0.0001f);
	}
}
