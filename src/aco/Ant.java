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

	private QoSAttribute[] mQoSValues;
	private float[][] mAggregatedQoSValues;
	private int mCurrentPosition; // Posição do último serviço que foi
									// escolhido.
	private int mDirection;
	private int[] mPartialSolution;
	private boolean mAlreadyFoundCompleteSolution;
	private float[][] mPheromone;
	private float mAlpha;
	private float mBeta;

	private int nestPosition;
	private int sourcePosition;

	public Ant(QoSAttribute[] qosValues, float[][] aggregatedQoSValues,
			float[][] pheromone, float alpha, float beta) {
		int numberOfAbstractServices = qosValues[0].getValues().length;

		nestPosition = -1;
		sourcePosition = numberOfAbstractServices;

		mQoSValues = qosValues;
		mCurrentPosition = -1;
		mDirection = FORWARD;
		mPartialSolution = new int[numberOfAbstractServices];
		mAlreadyFoundCompleteSolution = false;
		mAggregatedQoSValues = aggregatedQoSValues;
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
			mAlreadyFoundCompleteSolution = true;
			return;
		}

		float[] probabilities = new float[mAggregatedQoSValues[mCurrentPosition].length];
		float sum = 0f;
		for (int j = 0; j < probabilities.length; j++) {
			probabilities[j] = ((float) Math.pow(
					mPheromone[mCurrentPosition][j], mAlpha))
					* ((float) Math.pow(
							mAggregatedQoSValues[mCurrentPosition][j], mBeta));
			sum += probabilities[j];
		}

		for (int j = 0; j < probabilities.length; j++) {
			probabilities[j] = (probabilities[j] / sum);
		}

		mPartialSolution[mCurrentPosition] = selectWithProbabilities(probabilities);
	}

	// TODO: Verificar se está certo.
	public float getNewPheromone() {
		if (mCurrentPosition == sourcePosition
				|| (mCurrentPosition == nestPosition && mAlreadyFoundCompleteSolution)) {
			return QoSAttribute.calculateAggregatedQoS(mQoSValues,
					mPartialSolution);
		}
		return 0f;
	}

	public int[] getSolution() {
		if (mCurrentPosition == sourcePosition
				|| (mCurrentPosition == nestPosition && mAlreadyFoundCompleteSolution)) {
			return mPartialSolution;
		} else {
			return null;
		}
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
				QoSAttribute.AGGREGATE_BY_SUM, 1);
		QoSAttribute attrProd = new QoSAttribute(values,
				QoSAttribute.AGGREGATE_BY_PRODUCT, 1);
		QoSAttribute attrAvg = new QoSAttribute(values,
				QoSAttribute.AGGREGATE_BY_AVERAGE, 1);

		QoSAttribute[] attrs = { attrSum, attrProd, attrAvg };

		float[][] pheromone = { { 1, 1, 1 }, { 1, 1 }, { 1, 1, 1 } };
		float[][] aggregatedQoS = QoSAttribute.calculateResultantQoS(attrs);
		float alpha = 1;
		float beta = 1;
		Ant a = new Ant(attrs, aggregatedQoS, pheromone, alpha, beta);

		System.out.println(Arrays.toString(aggregatedQoS[0])
				+ aggregatedQoS[0].length);
		System.out.println(Arrays.toString(aggregatedQoS[1])
				+ aggregatedQoS[1].length);
		System.out.println(Arrays.toString(aggregatedQoS[2])
				+ aggregatedQoS[2].length);

		for (int i = 0; i < 5; i++) {
			a.walk();
			System.out.println(a.getNewPheromone());
		}
	}
}
