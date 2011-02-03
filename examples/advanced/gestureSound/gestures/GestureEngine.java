package advanced.gestureSound.gestures;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import org.mt4j.input.IMTInputEventListener;
import org.mt4j.input.inputData.AbstractCursorInputEvt;
import org.mt4j.input.inputData.InputCursor;
import org.mt4j.input.inputData.MTInputEvent;

import processing.core.PApplet;
import Jama.Matrix;
import advanced.gestureSound.gestures.filters.KalmanFilter;
import advanced.gestureSound.gestures.qualities.Curvature;
import advanced.gestureSound.gestures.qualities.Quality;
import advanced.gestureSound.gestures.qualities.Velocity;
import advanced.gestureSound.input.InputDelegate;
import advanced.gestureSound.weki.OSCWekinatorInManager;
import advanced.gestureSound.weki.OSCWekinatorOutManager;
import advanced.gestureSound.weki.OSCWekinatorInManager.WekiInListener;
import de.sciss.jcollider.Synth;

public class GestureEngine  {
	public static class SynthInfo {
		public Synth synth;
		public String parameter;
		public ParamMap pMap;
		public Zone zone;
		public SynthInfo(Synth synth, String param, ParamMap pMap, Zone z) {
			this.synth = synth; this.parameter = param; this.pMap = pMap; this.zone = z;
		}
	}
	public static class SynthCommand {
		public Synth synth;
		public String parameter;
		public float value;
		public SynthCommand(Synth synth, String param, float value) {
			this.synth = synth; this.parameter = param; this.value = value;
		}
		public void fire() {
			try {
				synth.set(parameter, value);
			} catch (IOException e) {
			}
		}
	}

	public static interface ParamMap {
		/**
		 * Map from the range [-1..1] to whatever the synth param takes
		 * @param in
		 * @return
		 */
		public float map(float in);
	}
	
	public static class Zone {
		public boolean in(InputCursor c) {return true;}
		protected boolean inQuadrant(InputCursor c, int quad) {
			float x = c.getFirstEvent().getPosX()/applet.width;
			float y = c.getFirstEvent().getPosY()/applet.height;
			if (quad == 1 && y<0.5 && x < 0.5) return true;
			if (quad == 2 && y<0.5 && x >= 0.5) return true;
			if (quad == 3 && y>=0.5 && x >= 0.5) return true;
			if (quad == 4 && y>=0.5 && x < 0.5) return true;
			return false;
		}
	}
	

	public ArrayList<SynthInfo> outSynthParams;
	//public HashMap<String, HashMap<InputCursor, Quality>> qualities;
	public HashMap<InputCursor, GestureParamGenerator> inCursorToGeneratorMap;
	public HashMap<InputCursor, KalmanFilter> filters;
	public static PApplet applet;
	
	public OSCWekinatorOutManager wekinatorOut = new OSCWekinatorOutManager();
	public OSCWekinatorInManager wekinatorIn;
	
	public GestureEngine(PApplet app, InputDelegate in) {
		applet = app;
		outSynthParams = new ArrayList<SynthInfo>();
		filters = new HashMap<InputCursor, KalmanFilter>();
		inCursorToGeneratorMap = new HashMap<InputCursor, GestureParamGenerator>();
		setupCursorListener(in);
	}
	
	
	public void setInstrument(WekiInListener in) {
		wekinatorIn = new OSCWekinatorInManager(in);
	}
		
	/**
	 * These guys setup the output parameters, 
	 * so we send info about the gestures to the wekinator, and it sends us back the parameters for these.
	 * @param synth
	 * @param param
	 * @param pMap
	 */
	public void addToOutMap(Synth synth, String param, ParamMap pMap) {
		addToOutMap(synth,param,pMap, new Zone());
	}
	
	public void addToOutMap(Synth synth, String param, ParamMap pMap, Zone z) {
		outSynthParams.add(new SynthInfo(synth,param,pMap,z));
	}

	
	public void setupCursorListener(final InputDelegate in) {
        in.addInputListener(new IMTInputEventListener() {
        	@Override
        	public boolean processInputEvent(MTInputEvent inEvt){
        		if(inEvt instanceof AbstractCursorInputEvt){
        			AbstractCursorInputEvt posEvt = (AbstractCursorInputEvt)inEvt;
        			if (posEvt.hasTarget()){
        				if (posEvt.getId() == AbstractCursorInputEvt.INPUT_ENDED) {
        					System.out.println("Input Ended!");
        					removeCursor(posEvt.getCursor());
        				}
        				else if (posEvt.getId() == AbstractCursorInputEvt.INPUT_DETECTED) {
        					System.out.println("Input Detected!");
        					addCursor(posEvt.getCursor());
        				}
        				else {
        					InputCursor m = posEvt.getCursor();
        					updateEngine(filter(m));
        				}
        			}
        		}
        		return false;
        	}
		});
	}
	
	public void removeCursor(InputCursor in) {
		filters.remove(in);
	}
	public void addCursor(InputCursor in) {
		addQualitiesForCursor(in);
		KalmanFilter f = KalmanFilter.buildKF2D(9, 1, 20); //magicparams, still don't know what they mean.
		f.setX(new Matrix(new double[][]{{in.getCurrentEvtPosX()}, {in.getCurrentEvtPosY()}, {0.01}, {0.01} }));
		f.predict();
		filters.put(in, f);
		
	}
	
	public void addQualitiesForCursor(InputCursor in) {
		ArrayList<Quality> q = new ArrayList<Quality>();
		q.add(new Curvature(this));
		q.add(new Velocity(this));
		inCursorToGeneratorMap.put(in, new GestureParamGenerator(q));
	}
	
	public InputCursor filter(InputCursor in) {
		AbstractCursorInputEvt evt = in.getCurrentEvent();
		if (evt == null) return in;
		KalmanFilter f = filters.get(in);
		if (f == null) return in;
		f.correct(new Matrix(new double[][]{{evt.getPosX(), evt.getPosY()}}).transpose());
		f.predict();
		evt.setPositionX((float) f.getX().get(0,0));  //I get it!
		evt.setPositionY((float) f.getX().get(1,0));
		return in;
	}
	
	public void updateEngine(InputCursor in) {
		if (inCursorToGeneratorMap.containsKey(in)) {
			Float[] inParams = inCursorToGeneratorMap.get(in).update(in);
			System.out.println("inParams:"+inParams);
			wekinatorOut.updateParam(inParams);
			wekinatorOut.send();
		}
	}
	
	

	
	


	public void gestureQualityChange(String quality, float val, InputCursor in) {
		for (SynthInfo info : outSynthParams ) {
			if (info.zone.in(in)) {
				try {
					info.synth.set(info.parameter, info.pMap.map(val));
				} catch (IOException e) {
					/**
					 * oops. not our problem.
					 */
					e.printStackTrace();
				}
			}
		}
	}


}
