package bb;

import java.util.Comparator;
import java.util.PriorityQueue;

public class BB {

	private PriorityQueue<Simplex> mNodesQueue;
	private Simplex mBestSolution;
	private Simplex mRelaxedBaseProblem;
	private boolean[] mIntegerVariables;
	
	public BB(Simplex relaxedBaseProblem) {
		mNodesQueue = new PriorityQueue<Simplex>(10, new SimplexComparator());
		mRelaxedBaseProblem = relaxedBaseProblem;
	}
	
	public void setIntegerVariables(boolean[] integerVariables) {
		mIntegerVariables = integerVariables;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
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

	/**
	 * The precision of the comparison operation.
	 */
	private static final double EPSILON = 1E-6;

	@Override
	public int compare(Simplex o1, Simplex o2) {
		double obj1 = o1.getObjectiveValueOfOptimalSolution();
		double obj2 = o2.getObjectiveValueOfOptimalSolution();

		if (Math.abs(obj1 - obj2) <= EPSILON) {
			return 0;
		} else if (obj1 - obj2 > EPSILON) {
			return 1;
		} else {
			return -1;
		}
	}
}