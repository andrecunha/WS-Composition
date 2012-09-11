package aco;

import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;

public class ACO extends Thread {

	private Ant[] mAnts;
	private QoSAttribute[] mQoSAttributes;
	private float[][] mResultantQoS;
	private float[][] mPheromone;
	private float mRho;

	private int mMaxIterations;
	private float mMinQoS;

	private volatile int mIterations;
	private volatile float mBestQoS;
	private volatile int[] mBestSolution;

	public ACO(int noAnts, QoSAttribute[] qosAttributes, float alpha,
			float beta, float rho, float initialPheromone, int maxIterations,
			float minQoS) {
		mQoSAttributes = qosAttributes;
		mAnts = new Ant[noAnts];

		mPheromone = new float[qosAttributes[0].getValues().length][];
		for (int i = 0; i < mPheromone.length; i++) {
			mPheromone[i] = new float[qosAttributes[0].getValues()[i].length];
			Arrays.fill(mPheromone[i], initialPheromone);
		}

		mResultantQoS = calculateResultantQoS(qosAttributes);
		for (int i = 0; i < noAnts; i++) {
			mAnts[i] = new Ant(mQoSAttributes, mResultantQoS, mPheromone,
					alpha, beta);
		}

		mMaxIterations = maxIterations;
		mMinQoS = minQoS;
		mRho = rho;
	}

	@Override
	public void run() {
		mIterations = 0;
		mBestQoS = 0;
		mBestSolution = null;

		while (!shouldStop()) {
			for (int i = 0; i < mAnts.length; i++) {
				mAnts[i].walk();
			}
			updatePheromone();
			updateBestSolution();
			mIterations++;
		}
	}

	public void startWithTimeOut(long milisTimeOut) {
		Timer timer = new Timer();
		TimerTask task = new TimerTask() {

			@Override
			public void run() {
				ACO.this.interrupt();
			}
		};

		timer.schedule(task, milisTimeOut);
		start();
	}

	private boolean shouldStop() {
		if (isInterrupted()) {
			return true;
		} else if (mMaxIterations < 0 && mMinQoS < 0) {
			/* Run forever. */
			return false;
		} else if (mMaxIterations > 0 && mMinQoS < 0) {
			/* Run for a fixed amount of iterations. */
			return mIterations >= mMaxIterations;
		} else if (mMaxIterations < 0 && mMinQoS > 0) {
			/* Run until a solution with an acceptable QoS is found. */
			return mBestQoS >= mMinQoS;
		} else { // mMaxIterations > 0 && mMinQoS > 0
			/*
			 * Run until an acceptable solution is found OR until a maximum
			 * number of iterations is reached.
			 */
			return mBestQoS >= mMinQoS || mIterations >= mMaxIterations;
		}
	}

	private void updatePheromone() {
		for (int i = 0; i < mPheromone.length; i++) {
			for (int j = 0; j < mPheromone[i].length; j++) {
				mPheromone[i][j] = (1 - mRho) * mPheromone[i][j];
			}
		}

		for (Ant a : mAnts) {
			float newPheromone = a.getNewPheromone();
			int[] solution = a.getSolution();

			if (solution == null) {
				continue;
			}

			for (int i = 0; i < solution.length; i++) {
				mPheromone[i][solution[i]] += newPheromone;
			}
		}
	}

	private void updateBestSolution() {
		for (int i = 0; i < mPheromone.length; i++) {
			int indexOfMaxPheromone = 0;
			float maxPheromone = 0;
			for (int j = 0; j < mPheromone[i].length; j++) {
				if (mPheromone[i][j] > maxPheromone) {
					maxPheromone = mPheromone[i][j];
					indexOfMaxPheromone = j;
				}
			}

			mBestSolution[i] = indexOfMaxPheromone;
		}
	}

	public static void main(String[] args) {

	}

	// TODO: Testar essa função.
	public static float[][] calculateResultantQoS(QoSAttribute[] qosValues) {
		float[][] aggregatedQoSValues;
		aggregatedQoSValues = new float[qosValues[0].getValues().length][];

		for (int i = 0; i < aggregatedQoSValues.length; i++) {
			aggregatedQoSValues[i] = new float[qosValues[0].getValues()[i].length];
			Arrays.fill(aggregatedQoSValues[i], 0f);
		}

		for (int attr = 0; attr < qosValues.length; attr++) {
			QoSAttribute currentAttribute = qosValues[attr];
			float[][] currentValues = currentAttribute.getValues();

			for (int i = 0; i < currentValues.length; i++) {
				for (int j = 0; j < currentValues[i].length; j++) {
					aggregatedQoSValues[i][j] += currentValues[i][j]
							* currentAttribute.getWeight();
				}
			}
		}

		return aggregatedQoSValues;
	}

	public static float calculateAggregatedQoS(QoSAttribute[] attributes,
			int[] solution) {
		float currentQoSValue = 0f;
		float maximumQoSValue = 0f;

		for (int i = 0; i < attributes.length; i++) {
			currentQoSValue += attributes[i].getAggregatedQoS(solution)
					* attributes[i].getWeight();
			maximumQoSValue += attributes[i].getMaximumQoS()
					* attributes[i].getWeight();
		}

		return currentQoSValue / maximumQoSValue;
	}
}
