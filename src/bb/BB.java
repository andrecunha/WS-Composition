package bb;

import java.util.Arrays;
import java.util.Comparator;
import java.util.PriorityQueue;

public class BB {

	private PriorityQueue<Simplex> mNodesQueue;
	private Simplex mBestSolution;
	private Simplex mRelaxedBaseProblem;
	private boolean[] mIntegerVariables;
	private SimplexComparator mSimplexComparator;

	public BB(Simplex baseRelaxedProblem, boolean[] integerVariables) {
		mSimplexComparator = new SimplexComparator();
		mNodesQueue = new PriorityQueue<Simplex>(10, mSimplexComparator);
		mRelaxedBaseProblem = baseRelaxedProblem;
		mIntegerVariables = integerVariables;
	}

	/**
	 * Chooses which non-integer variable should be branched.
	 * 
	 * @return The variable to be branched, or -1 if all variables are integer.
	 */
	private int chooseVariableToBranch(Simplex problem) {
		int bestVariable = -1;
		double[] solution = problem.getSolution();
		double[] objectiveFunction = problem.getOriginalObjectiveFunction();

		for (int i = 1; i <= solution.length; i++) {
			if (DoubleComparator.compare(solution[i - 1],
					Math.floor(solution[i - 1])) != 0
					&& mIntegerVariables[i - 1]) {
				if (bestVariable == -1) {
					bestVariable = i;
				} else if (objectiveFunction[i] > objectiveFunction[bestVariable]) {
					bestVariable = i;
				}
			}
		}
		return bestVariable;
	}

	/**
	 * 
	 * @return
	 */
	public boolean solve() {
		mNodesQueue.add(mRelaxedBaseProblem);

		while (!mNodesQueue.isEmpty()) {
			Simplex currentProblem = mNodesQueue.remove();
			if (!currentProblem.solve()) {
				// Problem is infeasible or unbounded. Prune by infeasibility.
				continue;
			}

			int var = chooseVariableToBranch(currentProblem);

			if (var == -1) {
				/* Solution is integer. */
				if (mBestSolution == null) {
					mBestSolution = currentProblem;
				} else if (mSimplexComparator.compare(currentProblem,
						mBestSolution) > 0) {
					/* Current solution is better than the best found. */
					mBestSolution = currentProblem;
				}
			} else {
				/* Solution has non-integer variables. We need to branch. */
				Simplex leftChild = new Simplex(currentProblem);
				Simplex rightChild = new Simplex(currentProblem);

				double value = currentProblem.getSolution()[var - 1];
				double[] a = new double[currentProblem.getOriginalNoVariables()];
				a[var - 1] = 1;

				leftChild.addConstraint(a, Simplex.LTE, Math.floor(value));
				rightChild.addConstraint(a, Simplex.GTE, Math.ceil(value));

				mNodesQueue.add(leftChild);
				mNodesQueue.add(rightChild);
			}
		}
		return false;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Simplex baseRelaxedProblem = new Simplex();
		
		baseRelaxedProblem.setObjectiveFuntion(new double[] { 0, 8, 11, 6, 4 },
				Simplex.MAXIMIZE);
		baseRelaxedProblem.addConstraint(new double[] {5, 7, 4, 3}, Simplex.LTE, 14);
		baseRelaxedProblem.addBinaryVariableConstraint(1);
		baseRelaxedProblem.addBinaryVariableConstraint(2);
		baseRelaxedProblem.addBinaryVariableConstraint(3);
		baseRelaxedProblem.addBinaryVariableConstraint(4);
		
		System.out.println(baseRelaxedProblem);
		
		boolean[] integerVariables = new boolean[4];
		Arrays.fill(integerVariables, true);
		
		BB bb = new BB(baseRelaxedProblem, integerVariables);
		bb.solve();
		
		System.out.println(bb.mBestSolution);
	}
}

/**
 * A class for making double-precision comparisons using an epsilon interval.
 * 
 * @author Andre Luiz Verucci da Cunha.
 * 
 */
class DoubleComparator {
	/**
	 * The precision of the comparison operation.
	 */
	private static final double EPSILON = 1E-6;

	/**
	 * Compares its two arguments for order. Returns a negative integer, zero,
	 * or a positive integer as the first argument is less than, equal to, or
	 * greater than the second.
	 * 
	 * @param n1
	 *            The first number to be compared.
	 * @param n2
	 *            The second number to be compared.
	 * @return A negative integer, zero, or a positive integer as the first
	 *         argument is less than, equal to, or greater than the second.
	 */
	public static int compare(Double n1, Double n2) {

		if (Math.abs(n1 - n2) <= EPSILON) {
			return 0;
		} else if (n1 - n2 > EPSILON) {
			return 1;
		} else {
			return -1;
		}
	}
}

/**
 * Class used to compare two linear programs, so that we can create a priority
 * queue of them.
 * 
 * @author Andre Luiz Verucci da Cunha
 * 
 */
class SimplexComparator implements Comparator<Simplex> {

	@Override
	public int compare(Simplex o1, Simplex o2) {
		double obj1 = o1.getObjectiveValueOfOptimalSolution();
		double obj2 = o2.getObjectiveValueOfOptimalSolution();

		return DoubleComparator.compare(obj1, obj2);
	}
}