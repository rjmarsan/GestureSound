package advanced.gestureSound.instruments;

import advanced.gestureSound.SC;

public abstract class Instrument {

	
	public abstract void init(SC sc);
	public abstract int declareNumberOfParameters();
	
	public abstract void start();
	public abstract void stop();
	
	public abstract void update(Float[] parameters);
	
	
}
