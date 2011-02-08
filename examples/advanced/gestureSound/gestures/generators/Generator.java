package advanced.gestureSound.gestures.generators;

public abstract class Generator {
	public abstract int getLength();
	
	public abstract Float[] update(float val);
}
