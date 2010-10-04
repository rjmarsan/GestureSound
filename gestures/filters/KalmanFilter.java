package advanced.gestureSound.gestures.filters;
import Jama.Matrix;

/**
 * This work is licensed under a Creative Commons Attribution 3.0 License.
 * 
 * @author Ahmed Abdelkader
 */

public class KalmanFilter {
 public Matrix getX() {
		return X;
	}

	public void setX(Matrix x) {
		X = x;
	}

	public Matrix getX0() {
		return X0;
	}

	public void setX0(Matrix x0) {
		X0 = x0;
	}

	public Matrix getF() {
		return F;
	}

	public void setF(Matrix f) {
		F = f;
	}

	public Matrix getB() {
		return B;
	}

	public void setB(Matrix b) {
		B = b;
	}

	public Matrix getU() {
		return U;
	}

	public void setU(Matrix u) {
		U = u;
	}

	public Matrix getQ() {
		return Q;
	}

	public void setQ(Matrix q) {
		Q = q;
	}

	public Matrix getH() {
		return H;
	}

	public void setH(Matrix h) {
		H = h;
	}

	public Matrix getR() {
		return R;
	}

	public void setR(Matrix r) {
		R = r;
	}

	public Matrix getP() {
		return P;
	}

	public void setP(Matrix p) {
		P = p;
	}

	public Matrix getP0() {
		return P0;
	}

	public void setP0(Matrix p0) {
		P0 = p0;
	}


protected Matrix X, X0;
 protected Matrix F, B, U, Q;
 protected Matrix H, R;
 protected Matrix P, P0;
 
 public void predict() {
  X0 = F.times(X).plus(B.times(U));
  
  P0 = F.times(P).times(F.transpose()).plus(Q);
 }
 
 public void correct(Matrix Z) {
  Matrix S = H.times(P0).times(H.transpose()).plus(R); 
  
  Matrix K = P0.times(H.transpose()).times(S.inverse());

  X = X0.plus(K.times(Z.minus(H.times(X0))));
  
  Matrix I = Matrix.identity(P0.getRowDimension(), P0.getColumnDimension());
  P = (I.minus(K.times(H))).times(P0);
  
  
 }
 

 public static KalmanFilter buildKF(double dt, double processNoisePSD, double measurementNoiseVariance) {
	 KalmanFilter KF = new KalmanFilter();
	 
	 //state vector
	 KF.setX(new Matrix(new double[][]{{0, 0, 0}}).transpose());
	 
	 //error covariance matrix
	 KF.setP(Matrix.identity(3, 3));
	 
	 //transition matrix
	 KF.setF(new Matrix(new double[][]{
	   {1, dt, Math.pow(dt, 2)/2},
	   {0,  1,           dt},
	   {0,  0,            1}}));

	 //input gain matrix
	 KF.setB(new Matrix(new double[][]{{0, 0, 0}}).transpose());
	 
	 //input vector
	 KF.setU(new Matrix(new double[][]{{0}}));

	 //process noise covariance matrix
	 KF.setQ(new Matrix(new double[][]{
	   { Math.pow(dt, 5) / 4, Math.pow(dt, 4) / 2, Math.pow(dt, 3) / 2},
	   { Math.pow(dt, 4) / 2, Math.pow(dt, 3) / 1, Math.pow(dt, 2) / 1},
	   { Math.pow(dt, 3) / 1, Math.pow(dt, 2) / 1, Math.pow(dt, 1) / 1}}
	 ).times(processNoisePSD));
	 
	 //measurement matrix
	 KF.setH(new Matrix(new double[][]{{1, 0, 0}}));
	 
	 //measurement noise covariance matrix
	 KF.setR(Matrix.identity(1, 1).times(measurementNoiseVariance));
	 
	 return KF;
 }
}
