package advanced.gestureSound.gestures.qualities;

import java.util.List;

import org.mt4j.input.inputData.AbstractCursorInputEvt;
import org.mt4j.input.inputData.InputCursor;

import Jama.Matrix;
import advanced.gestureSound.gestures.GestureEngine;
import advanced.gestureSound.gestures.filters.KalmanFilter;

public class Velocity extends Quality {

	KalmanFilter filter;
	
	public Velocity(GestureEngine engine) {
		super(engine);
		filter = KalmanFilter.buildKF(0.2, 5, 10);
		filter.setX(new Matrix(new double[][]{{0.01}, {0.01}, {0.01}}));
		filter.predict();
	}

	@Override
	public void update(InputCursor in) {
		float val=0.0f;
		
		val =findVelocity(in);
//		//System.out.println("Curvature: "+val);
//		filter.correct(new Matrix(new double[][]{{val}}));
//		filter.predict();
//		//System.out.println("0:"+filter.getX().get(0,0));
//		//System.out.println("1:"+filter.getX().get(1,0));
//		//System.out.println("2:"+filter.getX().get(2,0));
//		val = (float) filter.getX().get(0,0);
		System.out.println("Velocity: "+val);
		engine. gestureQualityChange("velocity", val, in);
	}
	private float findVelocity(InputCursor in) {
		return in.getVelocityVector().length();

	}
	
}