package bb;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

/**
 * 
 * @author Andre Luiz Verucci da Cunha
 * 
 */
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
	private double[] mOriginalObjectiveFunction;

	private boolean mIsInSlackForm;
	private boolean mIsFeasible;
	private boolean mIsBounded;
	private boolean mIsSolved;

	public Simplex() {
		mConstraints = new ArrayList<Constraint>();
	}

	/**
	 * Copy constructor.
	 * 
	 * @param s
	 */
	public Simplex(Simplex s) {
		if (s.mc != null) {
			mc = Arrays.copyOf(s.mc, s.mc.length);
		}

		mv = s.mv;

		mConstraints = new ArrayList<Constraint>(s.mConstraints.size());
		for (Constraint c : s.mConstraints) {
			Constraint c2 = new Constraint(c);
			mConstraints.add(c2);
		}

		mObjective = s.mObjective;
		mOriginalObjectiveFunction = Arrays.copyOf(
				s.mOriginalObjectiveFunction,
				s.mOriginalObjectiveFunction.length);
		
		mIsInSlackForm = false;
		mIsSolved = false;
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
		mIsInSlackForm = false;
		mIsSolved = false;
	}

	/**
	 * 
	 * @param var
	 */
	public void addBinaryVariableConstraint(int var) {
		double[] a = new double[getOriginalNoVariables()];
		a[var - 1] = 1;

		addConstraint(a, LTE, 1);
		
		mIsSolved = false;
	}

	/**
	 * Sets the objective function to:
	 * 
	 * f(x_1, x_2, ..., x_n) = objectiveFunction[0] + objectiveFunction[1]*x[1]
	 * + objectiveFunction[2]*x[2] + ... + objectiveFunction[n]*x[n]
	 * 
	 * @param objective
	 *            Function The objective function coefficients.
	 * @param objective
	 *            Either MINIMIZE or MAXIMIZE.
	 */
	public void setObjectiveFuntion(double[] objectiveFunction, int objective) {
		mOriginalObjectiveFunction = Arrays.copyOf(objectiveFunction,
				objectiveFunction.length);
		mv = objectiveFunction[0];
		mc = Arrays.copyOfRange(objectiveFunction, 1, objectiveFunction.length);
		mObjective = objective;
		mIsInSlackForm &= (objective == MAXIMIZE);
		mIsSolved = false;
	}

	/**
	 * 
	 * @return The original objective function.
	 */
	public double[] getOriginalObjectiveFunction() {
		return mOriginalObjectiveFunction;
	}

	/**
	 * 
	 * @return The original number of variables.
	 */
	public int getOriginalNoVariables() {
		return mOriginalObjectiveFunction.length - 1;
	}

	/**
	 * Puts the current linear problem in the standard form.
	 */
	private void toStandardForm() {
		/* This must be a maximization problem. */
		if (mObjective == MINIMIZE) {
			for (int i = 0; i < mc.length; i++) {
				mc[i] = -mc[i];
			}
			mv = -mv;
			mObjective = MAXIMIZE;
		}

		/* Removing equality constraints. */
		ArrayList<Constraint> newConstraints = new ArrayList<Constraint>();
		Iterator<Constraint> it = mConstraints.iterator();
		while (it.hasNext()) {
			Constraint c = it.next();
			if (c.rel == EQUALS) {
				newConstraints.add(new Constraint(c.a, LTE, c.b));
				newConstraints.add(new Constraint(c.a, GTE, c.b));
				it.remove();
			}
		}
		mConstraints.addAll(newConstraints);

		/* Removing greater-than-or-equal-to constraints. */
		newConstraints = new ArrayList<Constraint>();
		it = mConstraints.iterator();
		while (it.hasNext()) {
			Constraint c = it.next();
			if (c.rel == GTE) {
				double[] newA = Arrays.copyOf(c.a, c.a.length);
				for (int i = 0; i < newA.length; i++) {
					newA[i] = -newA[i];
				}
				newConstraints.add(new Constraint(newA, LTE, -c.b));
				it.remove();
			}
		}
		mConstraints.addAll(newConstraints);
	}

	/**
	 * Puts the current linear program, given in standard form, in the "obvious"
	 * slack form.
	 */
	private void toSlackForm() {
		/* We must set the basic and non-basic index sets. */
		mN = new int[mConstraints.get(0).a.length];
		for (int i = 1; i <= mN.length; i++) {
			mN[i - 1] = i;
		}

		mB = new int[mConstraints.size()];
		for (int i = 1; i <= mB.length; i++) {
			mB[i - 1] = mN.length + i;
		}

		/*
		 * We must allocate space for the new coefficients in the objective
		 * function.
		 */
		double[] newC = new double[mc.length + mB.length];
		System.arraycopy(mc, 0, newC, 0, mc.length);
		mc = newC;

		/* All the constraints must be equalities. */
		int n = mN.length + mB.length;
		mA = new double[n][n];
		mb = new double[n];

		for (int i = 0; i < mConstraints.size(); i++) {
			mb[mB[i] - 1] = mConstraints.get(i).b;
			for (int j = 0; j < mConstraints.get(i).a.length; j++) {
				mA[mB[i] - 1][j] = mConstraints.get(i).a[j];
			}
		}

		mIsInSlackForm = true;
	}

	/**
	 * 
	 * @param leaving
	 * @param entering
	 */
	private void pivot(int leaving, int entering) {
		/* Compute the coefficients of the equation for new variable xe. */
		mb[entering - 1] = mb[leaving - 1] / mA[leaving - 1][entering - 1];
		for (int j : mN) {
			if (j != entering) {
				mA[entering - 1][j - 1] = mA[leaving - 1][j - 1]
						/ mA[leaving - 1][entering - 1];
			}
		}
		mA[entering - 1][leaving - 1] = 1.0 / mA[leaving - 1][entering - 1];

		/* Compute the coefficients of the remaining constraints. */
		for (int i : mB) {
			if (i != leaving) {
				mb[i - 1] -= mA[i - 1][entering - 1] * mb[entering - 1];
				for (int j : mN) {
					if (j != entering) {
						mA[i - 1][j - 1] -= mA[i - 1][entering - 1]
								* mA[entering - 1][j - 1];
					}
				}
				mA[i - 1][leaving - 1] = -mA[i - 1][entering - 1]
						* mA[entering - 1][leaving - 1];
			}
		}

		/* Compute the objective function. */
		mv += mc[entering - 1] * mb[entering - 1];
		for (int j : mN) {
			if (j != entering) {
				mc[j - 1] -= (mc[entering - 1] * mA[entering - 1][j - 1]);
			}
		}
		mc[leaving - 1] = -mc[entering - 1] * mA[entering - 1][leaving - 1];

		/* Compute the new set of basic and non-basic variables. */
		for (int i = 0; i < mN.length; i++) {
			if (mN[i] == entering) {
				mN[i] = leaving;
				break;
			}
		}

		for (int i = 0; i < mB.length; i++) {
			if (mB[i] == leaving) {
				mB[i] = entering;
				break;
			}
		}
	}

	/**
	 * 
	 * @return
	 */
	private int findIndexOfMinimumB() {
		int k = -1;
		double minB = Double.MAX_VALUE;

		// k will be the index of the minimum b_i.
		for (int i = 0; i < mConstraints.size(); i++) {
			if (mConstraints.get(i).b < minB) {
				k = i + 1;
				minB = mConstraints.get(i).b;
			}
		}

		return k;
	}

	/**
	 * 
	 */
	private void initializeSimplex() {
		toStandardForm();

		int k = findIndexOfMinimumB();

		if (mConstraints.get(k - 1).b >= 0) {
			// The initial basic solution is feasible.
			toSlackForm();
			mIsFeasible = true;
			mIsBounded = true;
			return;
		}

		/*
		 * If the initial basic solution is infeasible, we must create and solve
		 * the auxiliary linear program.
		 */
		Simplex lAux = new Simplex(this);

		for (Constraint c : lAux.mConstraints) {
			// Adding -x0 to the end of each constraint.
			double[] newConstraint = new double[c.a.length + 1];
			System.arraycopy(c.a, 0, newConstraint, 0, c.a.length);
			newConstraint[newConstraint.length - 1] = -1;
			c.a = newConstraint;
		}

		// New objective function is -x0.
		double[] newObjectiveFunction = new double[lAux.mOriginalObjectiveFunction.length + 1];
		newObjectiveFunction[newObjectiveFunction.length - 1] = -1;
		lAux.setObjectiveFuntion(newObjectiveFunction, MAXIMIZE);

		int n = lAux.getOriginalNoVariables(); // Includes "x0".
		lAux.toSlackForm();

		int l = n + lAux.findIndexOfMinimumB();
		lAux.pivot(l, n);

		// The basic solution of lAux is now feasible.
		lAux.doMainSimplexLoop();
		
		double[] solution = lAux.getSolution();
		if (solution[solution.length - 1] == 0) {
			/*
			 * Original problem is feasible. We must remove x0 and adjust the
			 * objective function and the constraints.
			 */

			double[] nc = new double[lAux.mc.length];
			System.arraycopy(mc, 0, nc, 0, mc.length);
			mc = nc;

			/*
			 * First, we find i such that c_i != 0 in the original objective
			 * function.
			 */
			int i = -1;
			for (int j : lAux.mB) {
				if (mc[j - 1] != 0) {
					i = j;
					break;
				}
			}

			/* Then, we compute the coefficients of the new objective function. */
			mv += lAux.mb[i - 1] * mc[i - 1];

			for (int j : lAux.mN) {
				mc[j - 1] -= mc[i - 1] * lAux.mA[i - 1][j - 1];
			}
			mc[i - 1] = 0;

			// Removing "x0".
			double[] aux = mc;
			mc = new double[aux.length - 1];
			for (int q = 1; q <= aux.length; q++) {
				if (q == lAux.getOriginalNoVariables()) {
					continue;
				}

				if (q < lAux.getOriginalNoVariables()) {
					mc[q - 1] = aux[q - 1];
				} else {
					mc[q - 2] = aux[q - 1];
				}
			}

			/* Now, we adjust the constraints. */

			mb = new double[lAux.mb.length - 1];
			for (int q = 1; q <= lAux.mb.length; q++) {
				if (q == lAux.getOriginalNoVariables()) {
					continue;
				}

				if (q < lAux.getOriginalNoVariables()) {
					mb[q - 1] = lAux.mb[q - 1];
				} else {
					mb[q - 2] = lAux.mb[q - 1];
				}
			}

			mA = new double[lAux.mA.length - 1][lAux.mA[0].length - 1];
			for (int p = 1; p <= lAux.mA.length; p++) {
				int index;
				if (p == lAux.getOriginalNoVariables()) {
					continue;
				}
				if (p < lAux.getOriginalNoVariables()) {
					index = p - 1;
				} else {
					index = p - 2;
				}

				for (int q = 1; q <= lAux.mA[index].length; q++) {
					if (q == lAux.getOriginalNoVariables()) {
						continue;
					}

					if (q < lAux.getOriginalNoVariables()) {
						mA[index][q - 1] = lAux.mA[p - 1][q - 1];
					} else {
						mA[index][q - 2] = lAux.mA[p - 1][q - 1];
					}
				}
			}

			/* We now adjust the basic and non-basic index sets. */

			mB = new int[lAux.mB.length];
			int r = 0;
			for (int s : lAux.mB) {
				if (s < lAux.getOriginalNoVariables()) {
					mB[r++] = s;
				} else {
					mB[r++] = s - 1;
				}
			}

			mN = new int[lAux.mN.length - 1];

			r = 0;
			for (int s : lAux.mN) {
				if (s == lAux.getOriginalNoVariables()) {
					continue;
				}
				if (s < lAux.getOriginalNoVariables()) {
					mN[r++] = s;
				} else {
					mN[r++] = s - 1;
				}
			}

			mIsFeasible = true;
			mIsBounded = true;
			mIsInSlackForm = true;
		}
	}

	/**
	 * 
	 * @return
	 */
	private int findEnteringVariable() {
		for (int i : mN) {
			if (mc[i - 1] > 0) {
				return i;
			}
		}
		return -1;
	}

	/**
	 * 
	 * @return
	 */
	private void doMainSimplexLoop() {
		int e;
		int l = -1;

		while ((e = findEnteringVariable()) > 0) {
			double[] delta = new double[mB.length + mN.length];
			double minDelta = Double.MAX_VALUE;

			for (int i : mB) {
				if (mA[i - 1][e - 1] > 0) {
					delta[i - 1] = mb[i - 1] / mA[i - 1][e - 1];
				} else {
					delta[i - 1] = Double.NaN;
				}

				if (delta[i - 1] < minDelta) {
					minDelta = delta[i - 1];
					l = i;
				}
			}

			if (delta[l - 1] == Double.NaN) {
				// Problem is unbounded.
				mIsBounded = false;
				return;
			} else {
				pivot(l, e);
			}
		}
		
		mIsSolved = true;
		mIsFeasible = true;
		mIsBounded = true;
	}

	/**
	 * 
	 * @return
	 */
	public boolean solve() {
		initializeSimplex();
		if (!mIsFeasible || !mIsBounded) {
			mIsSolved = true;
			return false;
		}

		doMainSimplexLoop();
		
		return mIsBounded;
	}

	/**
	 * 
	 * @return
	 */
	public double[] getSolution() {
		if (!mIsSolved) {
			throw new IllegalStateException("Problem not solved.");
		}
		if (!mIsFeasible || !mIsBounded) {
			return null;
		}
		
		int n = getOriginalNoVariables();
		double[] solution = new double[n];
		for (int i : mB) {
			if (i - 1 < n) {
				solution[i - 1] = mb[i - 1];
			}
		}
		return solution;
	}

	/**
	 * 
	 * @return
	 */
	public double getObjectiveValueOfOptimalSolution() {
		if (!mIsSolved) {
			throw new IllegalStateException("Problem not solved.");
		}
		if (!mIsFeasible) {
			throw new IllegalStateException("Problem is infeasible.");
		}
		if (!mIsBounded) {
			throw new IllegalStateException("Problem is unbounded.");
		}
		
		double result = mOriginalObjectiveFunction[0];
		double[] solution = getSolution();

		for (int i = 0; i < getOriginalNoVariables(); i++) {
			result += solution[i] * mOriginalObjectiveFunction[i + 1];
		}

		return result;
	}

	/**
	 * 
	 * @return
	 */
	public boolean isFeasible() {
		if (!mIsSolved) {
			throw new IllegalStateException("Problem not solved.");
		}
		return mIsFeasible;
	}

	/**
	 * 
	 * @return
	 */
	public boolean isBounded() {
		if (!mIsSolved) {
			throw new IllegalStateException("Problem not solved.");
		}
		return mIsBounded;
	}

	@Override
	public String toString() {
		StringBuilder b = new StringBuilder();
		
		if (mIsInSlackForm) {
			Arrays.sort(mB);
			Arrays.sort(mN);

			b.append("z\t=\t" + String.format("%+6.3g", mv));
			for (int j : mN) {
				b.append("\t" + String.format("%+6.3g", mc[j - 1]) + " x" + j);
			}
			b.append("\n");

			for (int i : mB) {
				b.append("x" + i + "\t=\t" + String.format("%+6.3g", mb[i - 1]));
				for (int j : mN) {
					b.append("\t" + String.format("%+6.3g", -mA[i - 1][j - 1])
							+ " x" + j);
				}
				b.append("\n");
			}

		} else {
			b.append(mObjective == MINIMIZE ? "Minimize:\n\t" : "Maximize:\n\n");
			b.append("z = " + String.format("%+6.3g", mOriginalObjectiveFunction[0]));
			for (int j = 1; j < mOriginalObjectiveFunction.length; j++) {
				b.append("\t" + String.format("%+6.3g", mOriginalObjectiveFunction[j]) + " x" + j);
			}

			b.append("\n\nSubject to:\n");
			for (Constraint constraint : mConstraints) {
				b.append("\t");
				for (int j = 0; j < constraint.a.length; j++) {
					b.append("\t" + String.format("%+6.3g", constraint.a[j])
							+ " x" + (j + 1));
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
				b.append(String.format("%+6.3g", constraint.b) + "\n");
			}
		}

		if (mIsSolved && mIsFeasible && mIsBounded) {
			b.append("Solution: " + Arrays.toString(getSolution()) + " @ "
					+ getObjectiveValueOfOptimalSolution());
		}
		
		return b.toString().trim();
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		/* ---------------------------------------------------------- */
		Simplex s = new Simplex();

		s.setObjectiveFuntion(new double[] { 0, 2, -1 }, MAXIMIZE);

		s.addConstraint(new double[] { 2, -1 }, LTE, 2);
		s.addConstraint(new double[] { 1, -5 }, LTE, -4);

		// System.out.println(s);

		// s.solve();

		// System.out.println(s);
		// System.out.println(Arrays.toString(s.getSolution()));

		/* ---------------------------------------------------------- */
		s = new Simplex();
		s.setObjectiveFuntion(new double[] { 0, 3, 1, 2 }, MAXIMIZE);

		s.addConstraint(new double[] { 1, 1, 3 }, LTE, 30);
		s.addConstraint(new double[] { 2, 2, 5 }, LTE, 24);
		s.addConstraint(new double[] { 4, 1, 2 }, LTE, 36);

		// System.out.println(s);

		// s.solve();

		// System.out.println(Arrays.toString(s.getSolution()));
		// System.out.println(s.getObjectiveValueOfOptimalSolution());

		/* ---------------------------------------------------------- */
		s = new Simplex();
		s.setObjectiveFuntion(new double[] { 0, 1, 2 }, MAXIMIZE);

		s.addConstraint(new double[] { 1, 1 }, LTE, 4);
		s.addConstraint(new double[] { 1, 0 }, LTE, 2);
		s.addConstraint(new double[] { 0, 1 }, LTE, 3);

		System.out.println(s);

		s.solve();

		System.out.println(Arrays.toString(s.getSolution()));
		System.out.println(s.getObjectiveValueOfOptimalSolution());

		/* ---------------------------------------------------------- */
		s = new Simplex();
		s.setObjectiveFuntion(new double[] { 0, 1, 3 }, MAXIMIZE);

		s.addConstraint(new double[] { 0, 1 }, LTE, 4);
		s.addConstraint(new double[] { 1, 1 }, LTE, 6);
		s.addConstraint(new double[] { 1, 0 }, LTE, 3);
		s.addConstraint(new double[] { 5, 1 }, LTE, 18);

		// System.out.println(s);

		// s.solve();

		// System.out.println(Arrays.toString(s.getSolution()));

		/* ---------------------------------------------------------- */
		s = new Simplex();
		s.setObjectiveFuntion(new double[] { 0, -2, -2 }, MINIMIZE);

		s.addConstraint(new double[] { 1, 1 }, LTE, 4);
		s.addConstraint(new double[] { 1, 0 }, LTE, 3);
		s.addConstraint(new double[] { 0, 1 }, LTE, 7.0 / 2.0);

		// System.out.println(s);

		// s.solve();

		// System.out.println(Arrays.toString(s.getSolution()));
	}

}
