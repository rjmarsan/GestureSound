package advanced.gestureSound;

import java.io.File;
import java.io.IOException;

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
import advanced.gestureSound.gestures.GestureEngine.Zone;
import de.sciss.jcollider.Buffer;
import de.sciss.jcollider.Synth;

public class GestureSound extends MTComponent {

	PApplet applet;
	GestureEngine engine;
	
	SC sc;
	InputCursor in;

	public GestureSound(PApplet applet, final AbstractScene scene) {
		super(applet);
		// TODO Auto-generated constructor stub
		this.applet = applet;
		this.engine = new GestureEngine(applet);
		
        scene.getCanvas().addInputListener(new IMTInputEventListener() {
        	//@Override
        	public boolean processInputEvent(MTInputEvent inEvt){
        		if(inEvt instanceof AbstractCursorInputEvt){
        			AbstractCursorInputEvt posEvt = (AbstractCursorInputEvt)inEvt;
        			if (posEvt.hasTarget() && posEvt.getTargetComponent().equals(scene.getCanvas())){
        				InputCursor m = posEvt.getCursor();
        				engine.updateEngine(m);
        				in = m;
        			}
        		}
        		return false;
        	}
		});
		
		
		sc = new SC();
		sc.setupSupercollider();
		
		setupGestures();
	}
	
	
	void setupGestures() {
		try {
			File f = new File("data/sounds/amiu.aif");
			System.out.println("Loading sample at: "+f.getAbsolutePath());
			final Buffer b = Buffer.read(sc.server,f.getAbsolutePath());
			Synth synth1 = new Synth("grannyyy", new String[] {"trigRate", "buffer", "dur"}, new float[] { 10, b.getBufNum(), 0.1f }, sc.grpAll);
			engine.addToMap("curvature", synth1, "centerPos", 
					new ParamMap() { public float map(float in) {return (float)((in*5+0.5)*b.getDuration()); }},
					new Zone() { @Override public boolean in(InputCursor in) {return inQuadrant(in,1)||inQuadrant(in,4);}});
			engine.addToMap("velocity", synth1, "trigRate", 
					new ParamMap() { public float map(float in) {return in*4; }},
					new Zone() { @Override public boolean in(InputCursor in) {return inQuadrant(in,1)||inQuadrant(in,4);}});
			engine.addToMap("curvature", synth1, "dur", 
					new ParamMap() { public float map(float in) {return (float)((0.3+in)*b.getDuration()); }},
					new Zone() { @Override public boolean in(InputCursor in) {return inQuadrant(in,2)||inQuadrant(in,3);}});
			engine.addToMap("velocity", synth1, "amp", 
					new ParamMap() { public float map(float in) {return in/10; }},
					new Zone() { @Override public boolean in(InputCursor in) {return inQuadrant(in,2)||inQuadrant(in,3);}});

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	
	@Override
	public void drawComponent(PGraphics g) {
		if (in != null) {
			for (AbstractCursorInputEvt evt : in.getEvents()) {
				g.rect(evt.getPosX(), evt.getPosY(), 10, 10);
			}
		}
	}

}
