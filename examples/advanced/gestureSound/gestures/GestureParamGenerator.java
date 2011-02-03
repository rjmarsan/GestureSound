package advanced.gestureSound.gestures;

import java.util.ArrayList;

import org.mt4j.input.inputData.InputCursor;

import advanced.gestureSound.gestures.qualities.Quality;

public class GestureParamGenerator {

	public static final int HISTORY_SIZE = 10;
	
	ArrayList<Quality> qualities;
	int numQualities;
	public GestureParamGenerator(ArrayList<Quality> qualities) {
		this.qualities = qualities;
		numQualities = qualities.size();
		System.out.println("Generator started with "+getNumGeneratedParams()+" parameters");
	}
	
	public int getNumGeneratedParams() {
		return numQualities*(HISTORY_SIZE*3);
	}
	
	public Float[] update(InputCursor in) {
		Float[] vals = new Float[getNumGeneratedParams()];
		for (int i=0;i<vals.length;i++) vals[i]=0f;
		int i=0;
		
		//calculate the original qualities, and the history of them
		//now with more delta!
		for (Quality q : qualities) {
			float x = q.update(in);
			//history
			writeHistory(vals,i*HISTORY_SIZE,HISTORY_SIZE,x);
			i++;
			//history of 1st order diff
			writeDeltaHistory(vals,i*HISTORY_SIZE,HISTORY_SIZE, (i-1)*HISTORY_SIZE);
			i++;
			//history of 2nd order diff
			writeDeltaHistory(vals,i*HISTORY_SIZE,HISTORY_SIZE, (i-1)*HISTORY_SIZE);
			i++;
		}
		
		
		return vals;
	}
	
	private void writeHistory(Float[] a, int start, int size, float newval) {
		for (int i=0;i<(size-1);i++) {
			a[start+i] = a[start+i+1];
		}
		a[start+size-1] = newval;
	}
	
	//writes the delta of the history you pass in.... techincally the last value will have nothing written. but whatever.
	private void writeDeltaHistory(Float[] a, int start, int size, int nondeltaStart) {
		for (int i=0;i<(size-1);i++) {
			a[start+i] = a[nondeltaStart+1]-a[nondeltaStart];
		}
	}

	
	

}
