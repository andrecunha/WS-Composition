package bb;

import java.util.ArrayList;
import java.util.Arrays;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;

public class Simplex {

	public static final int EQUALS = 0x00;
	public static final int LTE = 0x01;
	public static final int GTE = 0x02;

	public static final int MINIMIZE = 0x00;
	public static final int MAXIMIZE = 0x01;

	private double[][] mA;
	private double[] mb;
	private double[] mc;
	private double mv;
	private int[] mB;
	private int[] mN;

	private ArrayList<Constraint> mConstraints;
	private int mObjective;

	private boolean mIsNormalized;

	public Simplex() {
		mConstraints = new ArrayList<Constraint>();
		mIsNormalized = false;
	}

	/**
	 * Adds the constraint
	 * 
	 * a[0]x[1] + a[1]x[2] + ... + a[n-1]x[n] rel b
	 * 
	 * Where "rel" is either EQUALS, LTE or GTE.
	 * 
	 * @param a
	 * @param rel
	 * @param b
	 */
	public void addConstraint(double[] a, int rel, double b) {
		mConstraints.add(new Constraint(a, rel, b));
		mIsNormalized = false;
	}

	/**
	 * Sets the objective function to:
	 * 
	 * f(x_0, x_1, ..., x_{n-1}) = objectiveFunction[0] +
	 * objectiveFunction[1]*x[0] + objectiveFunction[2]*x[1] + ... +
	 * objectiveFunction[n]*x[n-1]
	 * 
	 * @param objective
	 *            Function The objective function coefficients.
	 * @param objective
	 *            Either MINIMIZE or MAXIMIZE.
	 */
	public void setObjectiveFuntion(double[] objectiveFunction, int objective) {
		mv = objectiveFunction[0];
		mc = Arrays.copyOfRange(objectiveFunction, 1, objectiveFunction.length);
		mObjective = objective;
		mIsNormalized &= (objective == MAXIMIZE);
	}

	/**
	 * Puts the current linear problem in the slack form.
	 */
	private void normalize() {
		/* This must be a maximization problem. */
		if (mObjective == MINIMIZE) {
			for (int i = 0; i < mc.length; i++) {
				mc[i] = -mc[i];
			}
			mv = -mv;
			mObjective = MAXIMIZE;
		}

		/* We must set the basic and non-basic index sets. */
		mN = new int[mConstraints.get(0).a.length];
		for (int i = 1; i <= mN.length; i++) {
			mN[i - 1] = i;
		}

		mB = new int[mConstraints.size()];
		for (int i = 1; i <= mB.length; i++) {
			mB[i - 1] = mN.length + i;
		}

		/* All the constraints must be equalities. */
		mA = new double[mConstraints.size()][mConstraints.get(0).a.length];
		mb = new double[mConstraints.size()];

		for (int i = 0; i < mConstraints.size(); i++) {
			switch (mConstraints.get(i).rel) {
			case EQUALS:
				throw new NotImplementedException();
			case GTE:
				mb[i] = -mConstraints.get(i).b;
				for (int j = 0; j < mConstraints.get(i).a.length; j++) {
					mA[i][j] = -mConstraints.get(i).a[j];
				}
				break;
			case LTE:
				mb[i] = mConstraints.get(i).b;
				for (int j = 0; j < mConstraints.get(i).a.length; j++) {
					mA[i][j] = mConstraints.get(i).a[j];
				}
				break;
			default:
				break;
			}
		}

		mIsNormalized = true;
	}

	private void pivot(int leaving, int entering) {

	}

	public void solve() {

	}

	public int[] getSolution() {
		// TODO: Unimplemented.
		return null;
	}

	@Override
	public String toString() {
		StringBuilder b = new StringBuilder();
		
		if (mIsNormalized) {
			b.append("z\t=\t" + String.format("%+g", mv));
			for (int j = 0; j < mc.length; j++) {
				b.append("\t" + String.format("%+g", mc[j]) + " x" + (j + 1));
			}
			b.append("\n");

			for (int i = 0; i < mB.length; i++) {
				b.append("x" + mB[i] + "\t=\t" + String.format("%+g", mb[i]));
				for (int j = 0; j < mA[i].length; j++) {
					b.append("\t" + String.format("%+g", -mA[i][j]) + " x"
							+ mN[j]);
				}
				b.append("\n");
			}

		} else {
			b.append(mObjective == MINIMIZE ? "Minimize:\n\t" : "Maximize:\n\n");
			b.append("z = " + String.format("%+g", mv));
			for (int j = 0; j < mc.length; j++) {
				b.append("\t" + String.format("%+g", mc[j]) + " x" + (j + 1));
			}

			b.append("\n\nSubject to:\n");
			for (Constraint constraint : mConstraints) {
				b.append("\t");
				for (int j = 0; j < constraint.a.length; j++) {
					b.append("\t"
							+ String.format("%+g", constraint.a[j]) + " x"
							+ (j + 1));
				}
				b.append("\t");
				switch (constraint.rel) {
				case EQUALS:
					b.append("= \t");
					break;
				case GTE:
					b.append(">=\t");
					break;
				case LTE:
					b.append("<=\t");
					break;
				default:
					break;
				}
				b.append(constraint.b + "\n");
			}
		}

		return b.toString();
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Simplex s = new Simplex();

		s.setObjectiveFuntion(new double[] { 0, 3, 1, 2 }, MAXIMIZE);

		s.addConstraint(new double[] { 1, 1, 3 }, LTE, 30);
		s.addConstraint(new double[] { 2, 2, 5 }, LTE, 24);
		s.addConstraint(new double[] { 4, 1, 2 }, LTE, 36);

		System.out.println(s);

		s.normalize();

		System.out.println(s);
	}

}
