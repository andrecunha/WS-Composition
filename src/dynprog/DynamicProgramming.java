package dynprog;

import general.DoubleComparator;
import general.QoSAttribute;

import java.util.Arrays;

/**
 * This class implements the Dynamic Programming algorithm.
 * 
 * @author Andre Luiz Verucci da Cunha
 * 
 */
public class DynamicProgramming extends Thread {

	/**
	 * The solution found by the algorithm.
	 */
	private int[] mSolution;

	/**
	 * The QoS of each node.
	 */
	private double[][] mQoSValues;

	/**
	 * The accumulated QoS of each partial solution.
	 */
	private double[] mAccumQoS;

	/**
	 * The weight of each edge.
	 */
	private double[][][] mEdgesWeights;

	/**
	 * Default constructor.
	 * 
	 * @param qosValues
	 *            The QoS value associated with each node.
	 */
	public DynamicProgramming(double[][] qosValues) {
		mSolution = new int[qosValues.length];
		Arrays.fill(mSolution, -1);

		mAccumQoS = new double[qosValues.length];

		mQoSValues = qosValues;

		mEdgesWeights = new double[qosValues.length - 1][][];
		for (int i = 0; i < mEdgesWeights.length; i++) {
			mEdgesWeights[i] = new double[qosValues[i].length][];
			for (int j = 0; j < mEdgesWeights[i].length; j++) {
				mEdgesWeights[i][j] = new double[mQoSValues[i + 1].length];
			}
		}
	}

	/**
	 * Sets the weight of all edges.
	 * 
	 * @param weights
	 *            The edges weights.
	 */
	public void setEdgesWeights(double[][][] weights) {
		mEdgesWeights = weights;
	}

	/**
	 * Sets the weight of a single edge.
	 * 
	 * @param originAbstract
	 *            The level of the edge's origin.
	 * @param originConcrete
	 *            The concrete service of the edge's origin.
	 * @param destinationConcrete
	 *            The concrete service of the edge's destination.
	 * @param weight
	 *            The new weight.
	 */
	public void setEdgeWeight(int originAbstract, int originConcrete,
			int destinationConcrete, double weight) {
		mEdgesWeights[originAbstract][originConcrete][destinationConcrete] = weight;
	}

	/**
	 * Returns the solution found.
	 * 
	 * @return The solution found.
	 */
	public int[] getSolution() {
		return mSolution;
	}

	/**
	 * Adds the given level to the partial solution, considering that all of the
	 * posterior levels have already been added.
	 * 
	 * @param level
	 *            The level to be added to the solution.
	 * @return Whether it was possible to add the level to the solution.
	 */
	private boolean addLevelToSolution(int level) {
		int noConcreteServices = mQoSValues[level].length;

		double maxAggregatedQoS = Double.MIN_VALUE;
		int indexOfOptimalService = -1;
		double aggregatedQoS;

		for (int j = 0; j < noConcreteServices; j++) {
			if (Double.isNaN(mEdgesWeights[level][j][mSolution[level + 1]])) {
				continue;
			}

			aggregatedQoS = mQoSValues[level][j]
					+ mEdgesWeights[level][j][mSolution[level + 1]]
					+ mAccumQoS[level + 1];

			if (DoubleComparator.compare(aggregatedQoS, maxAggregatedQoS) > 0) {
				maxAggregatedQoS = aggregatedQoS;
				indexOfOptimalService = j;
			}
		}

		if (indexOfOptimalService == -1) {
			return false;
		}

		mSolution[level] = indexOfOptimalService;
		mAccumQoS[level] = mAccumQoS[level + 1] + maxAggregatedQoS;

		return true;
	}

	@Override
	public void run() {
		int noAbstractServices = mQoSValues.length;

		/* First, we add the last level to the partial solution. */
		int indexOfOptimalService = -1;
		double maxTotalQoS = Double.MIN_VALUE;
		for (int i = 0; i < mQoSValues[noAbstractServices - 1].length; i++) {
			if (DoubleComparator.compare(mQoSValues[noAbstractServices - 1][i],
					maxTotalQoS) > 0) {
				indexOfOptimalService = i;
				maxTotalQoS = mQoSValues[noAbstractServices - 1][i];
			}
		}
		mAccumQoS[noAbstractServices - 1] = maxTotalQoS;
		mSolution[noAbstractServices - 1] = indexOfOptimalService;

		/* Then, we add the other levels, in a backward fashion. */
		for (int i = noAbstractServices - 2; i >= 0; i--) {
			if (!addLevelToSolution(i)) {
				mSolution = null;
				break;
			}
		}
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		double[][] values = { { 1, 0.5f, 1 }, { 1, 0.5f }, { 0.5f, 0.5f, 1 } };

		double[][][] edgesWeights = {
				{ { 1, 2 }, { 1, 3 }, { Double.NaN, 2 } },
				{ { 1, 2, Double.NaN }, { 3, 1, 2 } } };

		/*
		 * double[][][] edgesWeights = { { { Double.NaN, Double.NaN }, {
		 * Double.NaN, Double.NaN }, { Double.NaN, Double.NaN } }, { { 1, 2,
		 * Double.NaN }, { 3, 1, 2 } } };
		 */

		QoSAttribute attrSum = new QoSAttribute(values,
				QoSAttribute.AGGREGATE_BY_SUM, 0.2f);
		QoSAttribute attrProd = new QoSAttribute(values,
				QoSAttribute.AGGREGATE_BY_PRODUCT, 0.3f);
		QoSAttribute attrAvg = new QoSAttribute(values,
				QoSAttribute.AGGREGATE_BY_AVERAGE, 0.5f);

		QoSAttribute[] attrs = { attrSum, attrProd, attrAvg };
		double[][] qosValues = QoSAttribute.calculateTotalQoS(attrs);

		DynamicProgramming dynProg = new DynamicProgramming(qosValues);
		dynProg.setEdgesWeights(edgesWeights);

		dynProg.run();

		System.out.println(Arrays.toString(dynProg.mSolution));
	}
}
