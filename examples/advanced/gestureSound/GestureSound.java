package advanced.gestureSound;

import java.awt.geom.Point2D;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.mt4j.components.MTComponent;
import org.mt4j.input.IMTInputEventListener;
import org.mt4j.input.inputData.AbstractCursorInputEvt;
import org.mt4j.input.inputData.InputCursor;
import org.mt4j.input.inputData.MTInputEvent;
import org.mt4j.sceneManagement.AbstractScene;

import processing.core.PApplet;
import processing.core.PGraphics;
import advanced.gestureSound.gestures.GestureEngine;
import advanced.gestureSound.gestures.GestureEngine.ParamMap;
import advanced.gestureSound.gestures.GestureEngine.SynthCommand;
import advanced.gestureSound.input.InputDelegate;
import de.sciss.jcollider.Buffer;
import de.sciss.jcollider.Synth;

public class GestureSound extends MTComponent {

	PApplet applet;
	GestureEngine engine;
	InputDelegate inDelegate;
	
	SC sc;
	List<InputCursor> ins;
	
	float lastRight1 = 0f;
	float lastRight2 = 0f;
	float lastLeft1 = 0f;
	float lastLeft2 = 0f;
	public static Buffer b;

	public GestureSound(PApplet applet, final AbstractScene scene) {
		super(applet);
		// TODO Auto-generated constructor stub
		this.applet = applet;
		this.inDelegate = new InputDelegate(applet, scene);
		this.engine = new GestureEngine(applet, inDelegate);
		this.ins = new ArrayList<InputCursor>();
        inDelegate.addInputListener(new IMTInputEventListener() {
        	
        	//@Override
        	public boolean processInputEvent(MTInputEvent inEvt){
        		if(inEvt instanceof AbstractCursorInputEvt){
        			AbstractCursorInputEvt posEvt = (AbstractCursorInputEvt)inEvt;
    				final InputCursor m = posEvt.getCursor();
    				if (posEvt.getId() == AbstractCursorInputEvt.INPUT_ENDED) {
    					ins.remove(m);
    				}
    				else if (posEvt.getId() == AbstractCursorInputEvt.INPUT_DETECTED) {
    					ins.add(m);
    				}
        		}
        		else {
        			System.out.println("Input event! Class:"+inEvt.getClass().toString());
        		}
        		return false;
        	}
		});
		
		
		sc = new SC();
		sc.setupSupercollider();
		
		setupGestures();
	}
	
	
//	public static class DummyMap implements ParamMap {
//		public static OSCManager man = new OSCManager();
//		
//		public final String name;
//		public OSCMap(String name) {
//			this.name = name;
//			man.addParam(name);
//		}
//		
//		public float map(float in) {
//			man.updateParam(name, in);
//			man.send();
//			return in;
//		}
//	}
	public static class DummyMap implements ParamMap {		
		public float map(float in) {
			return in;
		}
	}

	
	public static class CenterPosMap implements ParamMap { 
		public float pos = 0f;
		public float dur = Float.NaN;
		public float sensitivity = 0.05f;
		public float map(float in) {
			//if (dur == Float.NaN)
				dur = (float) b.getDuration();
			pos += in*sensitivity;
			//pos = Math.min(Math.max(0, pos), dur);
			pos = pos % dur;
			//System.out.println("Pos:"+pos+" In:"+in+" Dur:"+dur);
			return pos;
	}}
	public static class TrigRateMap implements ParamMap {
		public static float sensitivity=1.1f;
		public float map(float in) {
			float val = (1/DurMap.durVal*(in*1.1f+0.5f));
			//System.out.println("durVal:"+DurMap.durVal+"velocity:" + in+" TrigRate:"+val);
			if (b.getDuration() != Float.NaN) {
				//val = Math.min(Math.max(0f, val), (float)b.getDuration());
			}
			return val;
		    //return in*4;	
		}
	}
	public static class DurMap implements ParamMap {
		public static float durVal = 0.2f;
		public static float sensitivity=2.7f;
		public static float middle = 0.2f;
		
		public float map(float in) {
				float pos = in*sensitivity;
				//durVal = ((float)b.getDuration());
				durVal = (float) Math.pow(sensitivity, pos)*middle;
				//durVal = (float)((in*sensitivity*b.getDuration()/2f)+/2f);
				if (b.getDuration() != Float.NaN) {
					durVal = Math.min(Math.max(0.01f, durVal), (float)b.getDuration());
				}
				System.out.println("durVal:"+durVal);
				return durVal; 
		}
	}
	public static class RateMap implements  ParamMap { 
		public float sensitivity = 1.0f;
		public static float rateVal = 1.0f;
		public float map(float in) {
			rateVal = 1+in*sensitivity;
			System.out.println("rate:"+rateVal);
			return rateVal; }
	}

	public static class VelocityMap implements  ParamMap { 
		public float sensitivity = 0.08f;
		public float map(float in) {return in*sensitivity; }
	}

	
	void setupGestures() {
		try {
			File f = new File("data/sounds/amiu.aif");
			System.out.println("Loading sample at: "+f.getAbsolutePath());
			b = Buffer.read(sc.server,f.getAbsolutePath());
			Synth synth1 = new Synth("grannyyy", new String[] {"trigRate", "buffer", "dur"}, new float[] { 10, b.getBufNum(), 0.1f }, sc.grpAll);
			engine.addToOutMap(synth1, "centerPos", new CenterPosMap());
			engine.addToOutMap(synth1, "trigRate", new TrigRateMap());
			engine.addToOutMap(synth1, "rate", new RateMap());
			engine.addToOutMap(synth1, "amp", new VelocityMap());
			engine.addStopCommand(new SynthCommand(synth1, "amp", 0));
			engine.addStartCommand(new SynthCommand(synth1, "amp", 1));
			synth1.set("amp", 0);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	
	@Override
	public void drawComponent(PGraphics g) {
		g.stroke(100);
		g.fill(150);
		
		for (InputCursor in : ins) {
			if (in.getFirstEvent().getPosX() < this.applet.width/2) { //its on the left half of the screen
//				lastLeft1 = engine.getCurrentValue("curvature", in)*30;
//				lastLeft2 = engine.getCurrentValue("velocity", in)*2;
				g.fill(255,255,0);
			}
			else {
//				lastRight1 = engine.getCurrentValue("curvature", in)*30;
//				lastRight2 = engine.getCurrentValue("velocity", in)*2;
				g.fill(255,0,255);
			}
			
			
			int sizeofpast = in.getEventCount();
			double[] x = new double[sizeofpast];
			double[] y = new double[sizeofpast];
			int c = 0;
			for (AbstractCursorInputEvt evt : in.getEvents()) {
				//g.rect(evt.getPosX(), evt.getPosY(), 4, 4);
				x[c] = evt.getPosX();
				y[c] = evt.getPosY();
				c += 1;
				
				
			}
			//try 3: code from http://www.faculty.idc.ac.il/arik/Java/ex2/index.html
			int n=7;

			if (sizeofpast > n) {
				g.fill(255,255,255);
				for (int count = sizeofpast-1; count > n; count-=n) {
					Point2D[] s = new Point2D[n+1];
					for (int i=0;i<n+1;i++) 
						s[i] = new Point2D.Double(x[count-i],y[count-i]);
					Point2D p1,p0;
					p0 = s[0];
					for (int i = 1 ; i <= 50 ; i++) {
						double t = (double)i/50.0;
						//p1 = Geometry.evalBezierRec(ptArray,t,np);
						p1 = Geometry.evalBezier(s,t);
						g.line((float)p0.getX(), (float)p0.getY(), (float)p1.getX(), (float)p1.getY());
						p0 = p1;
					}
				}
				
				List<AbstractCursorInputEvt> past = in.getEvents();
				n=15;
				sizeofpast = in.getEvents().size();
				if (sizeofpast > n) {
					Point2D[] s = new Point2D[n+1];
					for (int i=0;i<n+1;i++) {
						AbstractCursorInputEvt p = past.get(sizeofpast-i-1);
						s[i] = new Point2D.Double(p.getPosX(),p.getPosY());
					}
					Point2D p2,p1,p0;
					p0 = s[0];
					p1 = Geometry.evalBezier(s,0.2);
					p2 = Geometry.evalBezier(s,0.4);
					//System.out.println("P2: "+p2.getX()+","+p2.getY());
					g.fill(0,0,255);
					g.rect((float)p1.getX(), (float)p1.getY(), 5, 5);
					g.fill(0,255,0);
					g.rect((float)p2.getX(), (float)p2.getY(), 10, 10);
					g.fill(255,0,0);
					g.rect((float)p0.getX(), (float)p0.getY(), 5, 5);
					g.fill(255,255,255);
					g.rect((float)s[s.length-1].getX(), (float)s[s.length-1].getY(), 5, 5);


				}
			}

		}
		g.stroke(100);
		g.fill(150);

		//top left bars
		g.rect(30,50,30,lastLeft1);
		g.rect(70,50,30,lastLeft2);

		//top right bars
		g.rect(this.applet.width-60,50,30,lastRight1);
		g.rect(this.applet.width-100,50,30,lastRight2);
		
		//middle line
		g.line(this.applet.width/2, 0, this.applet.width/2, this.applet.height);
		inDelegate.tick();
	}
	
	

}
