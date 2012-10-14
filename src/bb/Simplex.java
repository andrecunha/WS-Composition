package bb;

import java.util.ArrayList;

public class Simplex {

	public static final int EQUALS = 0x00;
	public static final int LTE = 0x01;
	public static final int GTE = 0x02;
	
	public static final int MINIMIZE = 0x00;
	public static final int MAXIMIZE = 0x01;

	private double[][] mA;
	private double[] mb;
	private double[] mc;
	private double[] mObjectiveFunction;
	private int[] mB;
	private int[] mN;

	private ArrayList<Constraint> mConstraints;
	private int mObjective;
	
	public Simplex() {
		mConstraints = new ArrayList<Constraint>();
	}

	/**
	 * Adds the constraint
	 * 
	 * a[0]x[0] + a[1]x[1] + ... + a[n-1]x[n-1] rel b
	 * 
	 * Where "rel" is either EQUALS, LTE or GTE.
	 * 
	 * @param a
	 * @param rel
	 * @param b
	 */
	public void addConstraint(double[] a, int rel, double b) {
		mConstraints.add(new Constraint(a, rel, b));
	}

	/**
	 * Sets the objective function to:
	 * 
	 * f(x_0, x_1, ..., x_{n-1}) = objectiveFunction[0] +
	 * objectiveFunction[1]*x[0] + objectiveFunction[2]*x[1] + ... +
	 * objectiveFunction[n]*x[n-1]
	 * 
	 * @param objective Function The objective function coefficients.
	 * @param objective Either MINIMIZE or MAXIMIZE.
	 */
	public void setObjectiveFuntion(double[] objectiveFunction, int objective) {
		mObjectiveFunction = objectiveFunction;
		mObjective = objective;
	}
	
	/**
	 * Puts the current linear problem in the slack form.
	 */
	private void normalize() {
		
	}

	private void pivot(int leaving, int entering) {

	}

	public void solve() {

	}
	
	public int[] getSolution() {
		// TODO: Unimplemented.
		return null;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {

	}

}
