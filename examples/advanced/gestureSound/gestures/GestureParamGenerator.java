package advanced.gestureSound.gestures;

import java.util.ArrayList;

import org.mt4j.input.inputData.InputCursor;

import advanced.gestureSound.gestures.generators.Generator;
import advanced.gestureSound.gestures.generators.HistoryGenerator;
import advanced.gestureSound.gestures.qualities.Quality;

public class GestureParamGenerator {

	public class QualityGenerators {
		public Quality q;
		public ArrayList<Generator> g;
		public QualityGenerators(Quality _q, ArrayList<Generator> _g) { q=_q; g=_g; }
	}
	
	ArrayList<QualityGenerators> qualities;
	int numQualities;
	
	public GestureParamGenerator(ArrayList<Quality> qualities) {
		this.qualities = new ArrayList<QualityGenerators>();
		for (Quality q : qualities) {
			ArrayList<Generator> g = new ArrayList<Generator>();
			g.add(new HistoryGenerator());
			this.qualities.add(new QualityGenerators(q,g));
		}
		numQualities = qualities.size();
		System.out.println("Generator started with "+getNumGeneratedParams()+" parameters");
	}
	
	public int getNumGeneratedParams() {
		int num = 0;
		for (QualityGenerators qg : qualities) {
			for (Generator g : qg.g) {
				num += g.getLength();
			}
		}
		return num;
	}
	
	public Float[] update(InputCursor in) {
		Float[] vals = new Float[getNumGeneratedParams()];
		for (int i=0;i<vals.length;i++) vals[i]=0f;
		int i=0;
		
		//calculate the original qualities, and the history of them
		//now with more delta!
		for (QualityGenerators qg : qualities) {
			Quality q = qg.q;
			float newval = q.update(in);
			for (Generator g : qg.g) {
				Float[] newvals = g.update(newval);
				i=writeAll(vals, newvals, i);
			}
		}
		
		printAll(vals);
		return vals;
	}
	
	private int writeAll(Float[] out, Float[] in, int start) {
		for (int i=0;i<in.length;i++) {
			out[start+i] = in[i];
		}
		return in.length+start;
	}
	
	private void printAll(Float[] array) {
		System.out.print("[");
		for (int i=0;i<array.length;i++) {
			System.out.print(array[i]+",");
		}
		System.out.print("]\n");
	}


	
	

}
