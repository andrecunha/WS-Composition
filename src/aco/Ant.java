package aco;

import java.util.Arrays;

/**
 * Represents an ant, the basic computational entity in ACO.
 * 
 * @author Andre Luiz Verucci da Cunha
 * 
 */
public class Ant {

	/* The possible directions the ant can be moving to. */
	public static final int FORWARD = 0x00;
	public static final int BACKWARD = 0x01;

	/**
	 * A vector containing all the QoS attributes to be considered.
	 */
	private QoSAttribute[] mQoSValues;

	/**
	 * The total QoS of each service, used by the ant as a heuristic.
	 */
	private float[][] mTotalQoSValues;

	/**
	 * The index of the last virtual service to which a concrete service has
	 * already been assigned.
	 */
	private int mCurrentPosition;

	/**
	 * The direction the ant is moving to. It is either FORWARD or BACKWARD.
	 */
	private int mDirection;

	/**
	 * The partial solution the ant has already built so far.
	 */
	private int[] mPartialSolution;

	/**
	 * Stores whether a complete solution has already been found. Used to
	 * determine if we should return a solution when the ant is in the nest.
	 */
	private boolean mAlreadyFoundACompleteSolution;

	/**
	 * The pheromone value associated with each concrete service.
	 */
	private float[][] mPheromone;

	/**
	 * The relative importance of the amount of pheromone.
	 */
	private float mAlpha;

	/**
	 * The relative importance of the heuristic information (the total QoS).
	 */
	private float mBeta;

	/**
	 * Represents that the ant is in the nest. It's value is -1.
	 */
	private int nestPosition;

	/**
	 * Represents that the ant is in the food source. It's value is the number
	 * of abstract services.
	 */
	private int sourcePosition;

	/**
	 * Creates an ant.
	 * 
	 * @param qosValues
	 *            A vector containing the QoS attributes.
	 * @param totalQoSValues
	 *            The total QoS value associated with each concrete service.
	 * @param pheromone
	 *            The pheromone associated with each concrete service.
	 * @param alpha
	 *            The relative importance of the amount of pheromone.
	 * @param beta
	 *            The relative importance of the heuristic information (the
	 *            total QoS).
	 */
	public Ant(QoSAttribute[] qosValues, float[][] totalQoSValues,
			float[][] pheromone, float alpha, float beta) {
		int numberOfAbstractServices = qosValues[0].getValues().length;

		nestPosition = -1;
		sourcePosition = numberOfAbstractServices;

		mQoSValues = qosValues;
		mCurrentPosition = -1;
		mDirection = FORWARD;
		mPartialSolution = new int[numberOfAbstractServices];
		mAlreadyFoundACompleteSolution = false;
		mTotalQoSValues = totalQoSValues;
		mPheromone = pheromone;
		mAlpha = alpha;
		mBeta = beta;
	}

	/**
	 * Moves this ant to the next step in it's walk through the search space.
	 */
	public void walk() {
		if (mCurrentPosition == nestPosition) {
			mDirection = FORWARD;
		} else if (mCurrentPosition == sourcePosition) {
			mDirection = BACKWARD;
		}

		if (mDirection == FORWARD) {
			mCurrentPosition++;
		} else {
			mCurrentPosition--;
		}

		if (mCurrentPosition == nestPosition
				|| mCurrentPosition == sourcePosition) {
			mAlreadyFoundACompleteSolution = true;
			return;
		}

		float[] probabilities = new float[mTotalQoSValues[mCurrentPosition].length];
		float sum = 0f;
		for (int j = 0; j < probabilities.length; j++) {
			probabilities[j] = ((float) Math.pow(
					mPheromone[mCurrentPosition][j], mAlpha))
					* ((float) Math.pow(mTotalQoSValues[mCurrentPosition][j],
							mBeta));
			sum += probabilities[j];
		}

		for (int j = 0; j < probabilities.length; j++) {
			probabilities[j] = (probabilities[j] / sum);
		}

		mPartialSolution[mCurrentPosition] = selectWithProbabilities(probabilities);
	}

	/**
	 * 
	 * @return If this ant has just found a solution, returns the pheromone to
	 *         be laid up all over this solution; otherwise, returns 0.
	 */
	public float getNewPheromone() {
		if (mCurrentPosition == sourcePosition
				|| (mCurrentPosition == nestPosition && mAlreadyFoundACompleteSolution)) {
			return QoSAttribute.calculateAggregatedQoS(mQoSValues,
					mPartialSolution);
		}
		return 0f;
	}

	/**
	 * 
	 * @return If this ant has just found a solution, returns the solution;
	 *         otherwise, returns null.
	 */
	public int[] getSolution() {
		if (mCurrentPosition == sourcePosition
				|| (mCurrentPosition == nestPosition && mAlreadyFoundACompleteSolution)) {
			return mPartialSolution;
		}
		return null;
	}

	/**
	 * Selects an element from an array, given the respective probabilities of
	 * each element.
	 * 
	 * @param probabilities
	 *            The probabilities of each element
	 * @return The index of the selected element.
	 */
	private static final int selectWithProbabilities(float[] probabilities) {
		int elem = 0;

		float number = (float) Math.random();
		float sum = probabilities[elem];
		while (number > sum) {
			sum += probabilities[++elem];
		}

		return elem;
	}

	public static void main(String[] args) {
		float[][] values = { { 1, 0.5f, 1 }, { 1, 0.5f }, { 0.5f, 0.5f, 1 } };

		QoSAttribute attrSum = new QoSAttribute(values,
				QoSAttribute.AGGREGATE_BY_SUM, 0.2f);
		QoSAttribute attrProd = new QoSAttribute(values,
				QoSAttribute.AGGREGATE_BY_PRODUCT, 0.3f);
		QoSAttribute attrAvg = new QoSAttribute(values,
				QoSAttribute.AGGREGATE_BY_AVERAGE, 0.5f);

		QoSAttribute[] attrs = { attrSum, attrProd, attrAvg };

		float[][] pheromone = { { 1, 1, 1 }, { 1, 1 }, { 1, 1, 1 } };
		float[][] aggregatedQoS = QoSAttribute.calculateTotalQoS(attrs);
		float alpha = 1;
		float beta = 1;
		Ant a = new Ant(attrs, aggregatedQoS, pheromone, alpha, beta);

		System.out.println(Arrays.toString(aggregatedQoS[0])
				+ aggregatedQoS[0].length);
		System.out.println(Arrays.toString(aggregatedQoS[1])
				+ aggregatedQoS[1].length);
		System.out.println(Arrays.toString(aggregatedQoS[2])
				+ aggregatedQoS[2].length);

		for (int i = 0; i < 12; i++) {
			a.walk();
			System.out.println(a.getNewPheromone());
			System.out.println(Arrays.toString(a.getSolution()));
		}
	}
}
