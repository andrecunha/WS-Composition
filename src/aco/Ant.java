package aco;

import java.util.ArrayList;

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

	private ArrayList<QoSAttribute> mQoSValues;
	private float[][] mAggregatedQoSValues;
	private int mCurrentPosition; // Posição do último serviço que foi
									// escolhido.
	private int mDirection;
	private int[] mPartialSolution;
	private float[][] mPheromone;
	private float mAlpha;
	private float mBeta;

	private int nestPosition;
	private int sourcePosition;

	public Ant(ArrayList<QoSAttribute> qosValues,
			float[][] aggregatedQoSValues, float[][] pheromone, float alpha,
			float beta) {
		int numberOfAbstractServices = qosValues.get(0).getValues().length;

		nestPosition = -1;
		sourcePosition = numberOfAbstractServices;

		mQoSValues = qosValues;
		mCurrentPosition = -1;
		mDirection = FORWARD;
		mPartialSolution = new int[numberOfAbstractServices];
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
			probabilities[j] /= (probabilities[j] / sum);
		}

		mPartialSolution[mCurrentPosition] = selectWithProbabilities(probabilities);
	}

	// TODO: Verificar se está certo.
	public float getNewPheromone() {
		if (mCurrentPosition == sourcePosition) {
			float currentQoSValue = 0f;
			float maximumQoSValue = 0f;

			for (int i = 0; i < mQoSValues.size(); i++) {
				currentQoSValue += mQoSValues.get(i).getAggregatedQoS(
						mPartialSolution)
						* mQoSValues.get(i).getWeight();
				maximumQoSValue += mQoSValues.get(i).getMaximumQoS()
						* mQoSValues.get(i).getWeight();
			}

			return currentQoSValue / maximumQoSValue;
		}
		return 0f;
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
}
