package advanced.gestureSound.gestures;

import java.util.ArrayList;

import org.mt4j.input.inputData.InputCursor;

import advanced.gestureSound.gestures.qualities.Quality;

public class GestureParamGenerator {

	ArrayList<Quality> qualities;
	int numQualities;
	public GestureParamGenerator(ArrayList<Quality> qualities) {
		this.qualities = qualities;
		numQualities = qualities.size();
		System.out.println("Generator started with "+numQualities+" parameters");
	}
	
	
	public Float[] update(InputCursor in) {
		Float[] vals = new Float[numQualities];
		int i=0;
		for (Quality q : qualities) {
			vals[i] = q.update(in);
			i++;
		}
		return vals;
	}
	
	

}
