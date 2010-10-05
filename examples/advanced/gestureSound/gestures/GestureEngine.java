package advanced.gestureSound.gestures;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import org.mt4j.input.inputData.InputCursor;

import processing.core.PApplet;
import advanced.gestureSound.gestures.qualities.Curvature;
import advanced.gestureSound.gestures.qualities.Quality;
import advanced.gestureSound.gestures.qualities.Velocity;
import de.sciss.jcollider.Synth;

public class GestureEngine {
	public static class SynthInfo {
		public Synth synth;
		public String parameter;
		public ParamMap pMap;
		public Zone zone;
		public SynthInfo(Synth synth, String param, ParamMap pMap, Zone z) {
			this.synth = synth; this.parameter = param; this.pMap = pMap; this.zone = z;
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
			if (quad == 4 && y>=0.5 && x > 0.5) return true;
			return false;
		}
	}
	

	public HashMap<String, ArrayList<SynthInfo>> map;
	public Quality[] qualities;
	public static PApplet applet;
	
	public GestureEngine(PApplet app) {
		applet = app;
		map = new HashMap<String, ArrayList<SynthInfo>>();
		qualities =  new Quality[2];
		qualities[0] = new Curvature(this) ;
		qualities[1] = new Velocity(this) ;
	}
	
	
	public void addToMap(String quality, Synth synth, String param, ParamMap pMap) {
		addToMap(quality,synth,param,pMap, new Zone());
	}
	
	public void addToMap(String quality, Synth synth, String param, ParamMap pMap, Zone z) {
		if (!map.containsKey(quality))
			map.put(quality, new ArrayList<SynthInfo>());
		map.get(quality).add(new SynthInfo(synth,param,pMap,z));
	}

	
	
	public void updateEngine(InputCursor in) {
		for (Quality q : qualities) {
			q.update(in);
		}
	}
	
	


	public void gestureQualityChange(String quality, float val, InputCursor in) {
		for (SynthInfo info : map.get(quality) ) {
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
