package advanced.gestureSound;

import java.io.IOException;

import org.mt4j.components.MTComponent;
import org.mt4j.input.IMTInputEventListener;
import org.mt4j.input.inputData.AbstractCursorInputEvt;
import org.mt4j.input.inputData.InputCursor;
import org.mt4j.input.inputData.MTInputEvent;
import org.mt4j.sceneManagement.AbstractScene;

import advanced.gestureSound.gestures.GestureEngine;
import advanced.gestureSound.gestures.GestureEngine.ParamMap;

import processing.core.PApplet;
import processing.core.PGraphics;
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
		this.engine = new GestureEngine();
		
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
			Synth synth1 = new Synth("stereosine", new String[] {"out", "freq"}, new float[] { 0, 400f }, sc.grpAll);
			engine.addToMap("curvature", synth1, "freq", new ParamMap() { public float map(float in) {return (float) (Math.log((1+in*10)/2)*-4000); }});
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
