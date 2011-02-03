package advanced.gestureSound;

import java.awt.geom.Point2D;
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
import advanced.gestureSound.input.InputDelegate;
import advanced.gestureSound.instruments.BasicSineInstrument;
import advanced.gestureSound.instruments.Instrument;

public class GestureSound extends MTComponent {

	PApplet applet;
	GestureEngine engine;
	InputDelegate inDelegate;
	
	Instrument instrument;
	
	SC sc;
	List<InputCursor> ins;
	
	float lastRight1 = 0f;
	float lastRight2 = 0f;
	float lastLeft1 = 0f;
	float lastLeft2 = 0f;

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
		
		setupInstrument();
	}
	
	
	private void setupInstrument() {
		instrument = new BasicSineInstrument();
		instrument.init(sc);
		engine.setInstrument(instrument);
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
