package advanced.gestureSound;

import java.awt.geom.Point2D;

//++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
public class Geometry {
// A class for some geometric global functions
//++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++

	// returns the euclidian distance between two Point2Ds
	public static double dist(Point2D p0, Point2D p1) {
		double dx = p1.getX() - p0.getX();
		double dy = p1.getY() - p0.getY();
		double sum = dx*dx + dy*dy;
		return Math.sqrt((double)sum);
	}

	// returns the linear interpolation of two Point2Ds
	public static Point2D interpolate(Point2D p0, Point2D p1,double t) {
		double x = t * p1.getX() + (1-t) * p0.getX();
		double y = t * p1.getY() + (1-t) * p0.getY();
		return new Point2D.Double(x,y);
	}

	// evaluates a bezier defined by the control polygon
	// which Point2Ds are given in the array at the value t
	public static Point2D evalBezier(Point2D arr[],double t) {
		for (int iter = arr.length ; iter > 0 ; iter--) {
			for (int i = 1 ; i < iter ; i++) {
				arr[i-1] = interpolate(arr[i-1],arr[i],t);
			}
		}
		return arr[0];
	}

	// evaluates a bezier defined by the control polygon
	// which Point2Ds are given in the array at the value t
	// Note: this function is recursive
	public static Point2D evalBezierRec(Point2D arr[],double t,int iter) {
		if (iter == 1)
			return arr[0];
		for (int i = 1 ; i < iter ; i++) {
			arr[i-1] = interpolate(arr[i-1],arr[i],t);
		}
		return evalBezierRec(arr,t,iter-1);
	}

}
