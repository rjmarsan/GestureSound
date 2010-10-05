package advanced.gestureSound.gestures;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import org.mt4j.input.inputData.InputCursor;

import advanced.gestureSound.gestures.qualities.Curvature;
import advanced.gestureSound.gestures.qualities.Quality;
import de.sciss.jcollider.Synth;

public class GestureEngine {
	public static class SynthInfo {
		public Synth synth;
		public String parameter;
		public ParamMap pMap;
		public SynthInfo(Synth synth, String param, ParamMap pMap) {
			this.synth = synth; this.parameter = param; this.pMap = pMap;
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
	

	public HashMap<String, ArrayList<SynthInfo>> map;
	public Quality[] qualities;
	
	public GestureEngine() {
		map = new HashMap<String, ArrayList<SynthInfo>>();
		qualities =  new Quality[1];
		qualities[0] = new Curvature(this) ;
	}
	
	
	public void addToMap(String quality, Synth synth, String param, ParamMap pMap) {
		if (!map.containsKey(quality))
			map.put(quality, new ArrayList<SynthInfo>());
		map.get(quality).add(new SynthInfo(synth,param,pMap));
	}
	
	
	public void updateEngine(InputCursor in) {
		for (Quality q : qualities) {
			q.update(in);
		}
	}
	
	


	public void gestureQualityChange(String quality, float val) {
		for (SynthInfo info : map.get(quality) ) {
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
