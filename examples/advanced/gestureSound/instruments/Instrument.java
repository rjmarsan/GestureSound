package advanced.gestureSound.instruments;

import java.io.IOException;

import advanced.gestureSound.SC;
import advanced.gestureSound.weki.OSCWekinatorInManager.WekiInListener;
import de.sciss.jcollider.Synth;

public abstract class Instrument implements WekiInListener {
	public final static boolean DEBUG = true;
	public Synth s;
	
	public abstract void init(SC sc);
	public abstract int declareNumberOfParameters();
	public abstract String[] declareParameters();
	
	public abstract void start();
	public abstract void stop();
	
	public abstract float[] map(float[] values);
	
	/**
	 * a basic method to update one synth.  override for more functionality.
	 */
	public void update(float[] values) {
		values = map(values);
		String[] names = declareParameters();
		try {
			if (s != null && names != null && values != null)  {
				if (DEBUG) {
					printParams(values);
				}
				s.set(declareParameters(), values);
			}
			else {
				System.out.println("Something went wrong while sending synth values! names:"+names+" values:"+values);
			}
		} catch (IOException e) {}
	}	
	
	public void printParams(float[] values) {
		String[] names = declareParameters();
		
		System.out.print("Parameters: \n");
		int max = values.length;
		if (values.length != names.length) {
			System.out.println("\tMISMATCH!!!! parameters:"+names.length+" values:"+values.length);	
			max = Math.min(values.length, names.length);
		}
		for (int i=0;i<max; i++) {
			System.out.println("\t["+names[i]+" \t: "+values[i]+"]");
		}
	}
	
	public float midiToFreq(float midi) {
		return (float)(Math.pow(2, (midi-48f)/12f)*440);
	}
	
	
}
