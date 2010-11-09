package advanced.gestureSound.input;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.mt4j.components.MTComponent;
import org.mt4j.input.IMTInputEventListener;
import org.mt4j.input.inputData.AbstractCursorInputEvt;
import org.mt4j.input.inputData.InputCursor;
import org.mt4j.input.inputData.MTFingerInputEvt;
import org.mt4j.input.inputData.MTInputEvent;
import org.mt4j.input.inputSources.AbstractInputSource;
import org.mt4j.sceneManagement.AbstractScene;
import org.mt4j.util.math.Vector3D;

import advanced.gestureSound.Geometry;
import advanced.gestureSound.gestures.qualities.Curvature;

import processing.core.PApplet;

public class InputDelegate extends MTComponent {
	List<FadeOut> tickings;

	public InputDelegate(PApplet pApplet, final AbstractScene scene) {
		super(pApplet);
		tickings = new ArrayList<FadeOut>();
		// TODO Auto-generated constructor stub
		
		scene.getCanvas().addInputListener(new IMTInputEventListener() {

        	//@Override
        	public boolean processInputEvent(final MTInputEvent inEvt){
        		if(inEvt instanceof AbstractCursorInputEvt){
        			AbstractCursorInputEvt posEvt = (AbstractCursorInputEvt)inEvt;
        			if (posEvt.hasTarget() && posEvt.getTargetComponent().equals(scene.getCanvas())){
        				final InputCursor m = posEvt.getCursor();
        				if (posEvt.getId() == AbstractCursorInputEvt.INPUT_ENDED) {
        					trailOff(inEvt, m);
        				}
        				else {
        					fireInputEvent(inEvt);
        				}
        				
        			}
        		}
        		return false;
        	}
		});
	}

	public void trailOff(final MTInputEvent inEvt, final InputCursor m) {
		final AbstractCursorInputEvt posEvt = (AbstractCursorInputEvt)inEvt;
		m.getEvents().remove(posEvt);

		/** bezel curve stuff **/
		List<AbstractCursorInputEvt> past = m.getEvents();
		int n= m.getEvents(1000).size();
		int sizeofpast = m.getEvents().size();
		n = Math.min(n, sizeofpast-1);
		final Point2D[] s = new Point2D[n+1];
		final double cX = past.get(sizeofpast-1).getPosX();
		final double cY = past.get(sizeofpast-1).getPosY();
		for (int i=0;i<n+1;i++) {
			AbstractCursorInputEvt p = past.get(sizeofpast-i-1);
			double x = cX-p.getPosX();//(cX-p.getPosX())+cX;//onward into the future!
			double y = cY-p.getPosY();//(cY-p.getPosY())+cY;//onward into the future!
			s[i] = new Point2D.Double(x,y);
			System.out.println("Adding point: "+x+","+y);
		}
		Point2D p0,p1,p2;
		p0 = s[0];
		p1 = Geometry.evalBezier(s,0.2);
		p2 = Geometry.evalBezier(s,0.4);	
		final float curvature = (float) Curvature.findCurvature(p0.getX(),p0.getY(),p1.getX(),p1.getY(),p2.getX(),p2.getY());
		/** end yeah **/
		System.out.println("Curavture: "+curvature);
		
		
		
		tickings.add(new FadeOut() {
			int i=0;
			float currentX = posEvt.getPosX();
			float currentY = posEvt.getPosY();
			float originalVecLength = m.getVelocityVector().length();
			Vector3D currentVec = m.getVelocityVector();

			public void tick() {
				synchronized(m.getEvents()) {

					if (currentVec.length() < 1) { 
						m.getEvents().add(posEvt);
						fireInputEvent(posEvt);
						done();
						return;
					}
					AbstractCursorInputEvt evt = m.getPreviousEvent();
					evt = predictNext(m);
					m.getEvents().add(evt);
//					System.out.println("Sending evt: " + evt + " Stats: "
//							+ evt.getId() + " " + evt.getPosX() + " "
//							+ evt.getPosY() + " " + evt.getWhen());
					fireInputEvent(evt);
					
					i++;
				}
			}
			public AbstractCursorInputEvt predictNext(InputCursor m) {
				AbstractCursorInputEvt evt = m.getPreviousEvent();
				Vector3D out = currentVec.getAdded(new Vector3D(currentX, currentY));
				currentVec = currentVec.getScaled(0.9f);
				currentX = out.x;
				currentY = out.y;
				Point2D pt = Geometry.evalBezier(s,     0.3f/(0.3f+currentVec.length())    );
//				currentX = (float) ((float) (cX-pt.getX())+cX);
//				currentY = (float) ((float) (cY-pt.getY())+cY);
//				if (curvature > 0) 
					float pX = (float) pt.getX();
					float pY = (float) pt.getY();
					currentX = pX;
					//currentX = (float) (pX*Math.cos(curvature)+pY*Math.sin(curvature));
					//currentX = (float) (cX-currentX);
//				else
					currentY = pY;
					//currentY = (float) (pX*Math.sin(curvature)+pY*Math.cos(curvature));
					currentX += cX;
					currentY += cY;

					//currentY = (float) (cY-currentY);
				return new MTFingerInputEvt((AbstractInputSource) evt
						.getSource(), evt.getTargetComponent(), currentX
						, currentY , evt.getId() + 2, m);
			}
			
		});

	}
	
	

	public abstract class FadeOut {
		public boolean done = false;
		public abstract void tick();
		public void done() {
			done = true;
		}
	}

	public void tick() {
			Iterator i = tickings.iterator();
			FadeOut f;
			while (i.hasNext()) {
				f=(FadeOut) i.next();
				f.tick();
				if (f.done){
					i.remove();
				}
			}
			

	}
}
