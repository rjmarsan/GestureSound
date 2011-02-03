package advanced.gestureSound.instruments;

import java.io.File;
import java.io.IOException;

import de.sciss.jcollider.Buffer;
import de.sciss.jcollider.Synth;
import advanced.gestureSound.SC;
import advanced.gestureSound.gestures.GestureEngine.SynthCommand;

public class BufferInstrument extends Instrument {
	Buffer b;
	String[] params = new String[] {"centerPos", "trigRate", "rate", "amp"};
	
	public void init(SC sc) {
		try {
		File f = new File("data/sounds/amiu.aif");
		System.out.println("Loading sample at: "+f.getAbsolutePath());
		b = Buffer.read(sc.server,f.getAbsolutePath());
		s = new Synth("grannyyy", new String[] {"trigRate", "buffer", "dur"}, new float[] { 10, b.getBufNum(), 0.1f }, sc.grpAll);
		s.set("amp", 0);
		} catch (Exception e) {}

	}
	public int declareNumberOfParameters() {
		return 4;
	}
	
	public void start() {
		try {
			//s.set("amp", 1);
		} catch (Exception e) {}
	}
	public void stop() {
		try {
			s.set("amp", 0);
		} catch (IOException e) {}
	}

	@Override
	public String[] declareParameters() {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public float[] map(float[] values) {
		// TODO Auto-generated method stub
		return null;
	}

}
