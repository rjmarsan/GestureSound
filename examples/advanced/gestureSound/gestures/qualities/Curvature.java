package advanced.gestureSound.gestures.qualities;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

import advanced.gestureSound.Geometry;



import org.mt4j.input.inputData.AbstractCursorInputEvt;
import org.mt4j.input.inputData.InputCursor;

import Jama.Matrix;
import advanced.gestureSound.gestures.GestureEngine;
import advanced.gestureSound.gestures.filters.KalmanFilter;

public class Curvature extends Quality {
	public static String name="curvature";
	
	KalmanFilter filter;
	
	double currentValue=0f;
	ArrayList<double[]> pastValues;

	
	public static Quality cursorDetected(GestureEngine engine) {
		return new Curvature(engine);
	}
	
	
	public Curvature(GestureEngine engine) {
		super(engine);
		pastValues = new ArrayList<double[]>();
		filter = KalmanFilter.buildKF(0.2, 5, 10);
		filter.setX(new Matrix(new double[][]{{0.01}, {0.01}, {0.01}}));
		filter.predict();
	}

	@Override
	public void update(InputCursor in) {
		double val=0.0f;
		
		val = (float) (findCurvature(in)/(Math.PI));
		
		pastValues.add(new double[]{val});
		
		//filter, no. average? Yes.
//		float pastEvtCount = in.getEvents(10).size();
//		val = (currentValue*pastEvtCount + val)/(pastEvtCount+1);
		
//		int size = in.getEvents(200).size();
//		//if (size > 2) { //this is the.... other method. using least squares.
//		if (size < 0) { //nonsense, for now.
//			//size=3;
//			double[][] b = new double[size][2];
//			double[][] a = new double[size][2];
//			int pastValuesSize = pastValues.size();
//			System.out.println("Size of pastValuesL: "+pastValuesSize+" Size of b:"+size);
//			for (int x = 0; x < size; x++) {
//				b[x][0] = pastValues.get(pastValuesSize-x-1)[0];
//				b[x][1] = size-x;
//				a[x][0] = 1;
//				a[x][1] = x+1;
//			}
//	
//			
//			Matrix A = new Matrix(a);
//			Matrix B = new Matrix(b);
//			System.out.println("A: "+A+" B:"+B);
//			Matrix sol = A.solve(B);
//			System.out.println(sol);
//			for (double[] row : sol.getArray())
//				System.out.println("Row: ["+row[0]+","+row[1]+"]");
//			val = (float) sol.getArray()[0][0];
//		}
//		if (size > 2) {
//			
//		}
		//this is the spline method
		int n=15;
		List<AbstractCursorInputEvt> past = in.getEvents();
		int sizeofpast = in.getEvents().size();
		if (sizeofpast > n) {
			Point2D[] s = new Point2D[n+1];
			for (int i=0;i<n+1;i++) {
				AbstractCursorInputEvt p = past.get(sizeofpast-i-1);
				s[i] = new Point2D.Double(p.getPosX(),p.getPosY());
			}
			Point2D p2,p1,p0;
			p0 = s[0];
			p1 = Geometry.evalBezier(s,0.1);
			p2 = Geometry.evalBezier(s,0.2);
			val = findCurvature(p0.getX(), p0.getY(), p1.getX(), p1.getY(), p2.getX(), p2.getY());

		}
		
		//System.out.println("Curvature: "+val);
		//filter.correct(new Matrix(new double[][]{{val}}));
		//filter.predict();
		//System.out.println("0:"+filter.getX().get(0,0));
		//System.out.println("1:"+filter.getX().get(1,0));
		//System.out.println("2:"+filter.getX().get(2,0));
		//val = (float) filter.getX().get(0,0);
		System.out.println("Curvature: "+val);
		currentValue = val;
		engine.gestureQualityChange(name, (float)val, in);
	}
		
	private double findCurvature(InputCursor in) {
		if (in.getEventCount() < 3)
			return 0.0f;
		List<AbstractCursorInputEvt> events = in.getEvents();
		AbstractCursorInputEvt posEvt 	= events.get(events.size()-1);
		AbstractCursorInputEvt prev 	= events.get(events.size()-2);
		AbstractCursorInputEvt prev2 	= events.get(events.size()-3);
		if (prev == null)
			return 0;
		if (prev2 == null)
			return 0;
		return findCurvature(posEvt.getPosX(), posEvt.getPosY(), prev.getPosX(), prev.getPosY(), prev2.getPosX(), prev2.getPosY());
	}
	private double findCurvature(double x1, double y1, double x2, double y2, double x3, double y3) {

		double angle1 = getAngle(x1,y1,x2,y2);
		double angle2 = getAngle(x2,y2,x3,y3);
		if (angle1 == 0.0f || angle2 == 0.0f) {
			return 0.0f;
		}		
		double result = angle1-angle2;
		if (result > Math.PI) {
			//System.out.println("Result too big! taking other atan2: "+result+" New: "+(2*Math.PI-result));
			
			result =  (2*Math.PI-result);
		}
		else if (result < -1*Math.PI) {
			//System.out.println("Result too small! taking other atan2: "+result+" New: "+(2*Math.PI+result));

			result =  (2*Math.PI+result);
		}
		
		//System.out.println("Curvature: "+result+" First Angle:"+angle1+" Second Angle: "+angle2);

		return result;

	}
	private double getAngle(AbstractCursorInputEvt ev1, AbstractCursorInputEvt ev2) {
		return getAngle(ev1.getPosX(),ev1.getPosY(),ev2.getPosX(), ev2.getPosY());
	}
	private double getAngle(double x1, double y1, double x2, double y2) {
		return Math.atan2(x1-x2, y1-y2);
	}

	@Override
	public float getCurrentValue() {
		return (float)currentValue;
	}

	
}