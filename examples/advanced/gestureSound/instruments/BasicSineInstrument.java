package advanced.gestureSound.instruments;

import java.io.File;
import java.io.IOException;

import de.sciss.jcollider.Buffer;
import de.sciss.jcollider.Synth;
import advanced.gestureSound.SC;
import advanced.gestureSound.gestures.GestureEngine.SynthCommand;

public class BasicSineInstrument extends Instrument {
	String[] params = new String[] {"freq", "cutoff", "amp"};
	
	public void init(SC sc) {
		try {
		s = new Synth("stereosaw", new String[] {"out", "freq", "cutoff", "amp"}, new float[] { 0, 440, 800, 0 }, sc.grpAll);
		s.set("amp", 0);
		} catch (Exception e) {}

	}
	public int declareNumberOfParameters() {
		return 3;
	}
	public String[] declareParameters() {
		return params;
	}
	
	public void start() {
	}
	
	public void stop() {
		try {
			s.set("amp", 0);
		} catch (IOException e) {}
	}
	

	
	public float[] map(float[] values) {
		values[0] = mapFreq(values[0]);
		values[1] = mapCutoff(values[1]);
		values[2] = mapAmp(values[2]);
		return values;
	}
	
	private float mapFreq(float freq) {
		return midiToFreq(Math.min(freq, 127f));
	}
	
	private float mapCutoff(float cutoff) {
		cutoff = Math.min(cutoff, 127f)*100;
		return cutoff;
	}
	
	private float mapAmp(float amp) {
		amp = amp/127f;
		return amp;
	}
}
