package dynprog;

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
	private int[] mPartialSolution;

	/**
	 * The total QoS of each node.
	 */
	private double[][] mTotalQoS;

	/**
	 * The total QoS of each node in the partial solution.
	 */
	private double[] mPartialAccumQoS;

	/**
	 * The weight of each edge.
	 */
	private double[][][] mEdgesWeights;

	/**
	 * Default constructor.
	 * 
	 * @param qosAttributes
	 *            A vector containing the QoS attributes that will be used.
	 */
	public DynamicProgramming(QoSAttribute[] qosAttributes) {
		mPartialSolution = new int[qosAttributes[0].getValues().length];
		Arrays.fill(mPartialSolution, -1);

		mPartialAccumQoS = new double[qosAttributes[0].getValues().length];

		mTotalQoS = QoSAttribute.calculateTotalQoS(qosAttributes);

		mEdgesWeights = new double[qosAttributes[0].getValues().length - 1][][];
		for (int i = 0; i < mEdgesWeights.length; i++) {
			mEdgesWeights[i] = new double[qosAttributes[0].getValues()[i].length][];
			for (int j = 0; j < mEdgesWeights[i].length; j++) {
				mEdgesWeights[i][j] = new double[mTotalQoS[i + 1].length];
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
	 * Adds the given level to the partial solution, considering that all the
	 * posterior levels are already in it.
	 * 
	 * @param level
	 *            The level to be added to the solution.
	 */
	private boolean addLevelToPartialSolution(int level) {
		int noConcreteServices = mTotalQoS[level].length;

		double maxAggregatedQoS = Double.MIN_VALUE;
		int indexOfOptimalService = -1;
		double aggregatedQoS;

		for (int j = 0; j < noConcreteServices; j++) {
			if (mEdgesWeights[level][j][mPartialSolution[level + 1]] == Double.NaN) {
				continue;
			}

			aggregatedQoS = mTotalQoS[level][j]
					+ mEdgesWeights[level][j][mPartialSolution[level + 1]]
					+ mPartialAccumQoS[level + 1];

			if (aggregatedQoS > maxAggregatedQoS) {
				maxAggregatedQoS = aggregatedQoS;
				indexOfOptimalService = j;
			}
		}

		if (indexOfOptimalService == -1) {
			return false;
		}

		mPartialSolution[level] = indexOfOptimalService;
		mPartialAccumQoS[level] = mPartialAccumQoS[level + 1]
				+ maxAggregatedQoS;

		return true;
	}

	@Override
	public void run() {
		int noAbstractServices = mTotalQoS.length;

		/* First, we add the last level to the partial solution. */
		int indexOfOptimalService = -1;
		double maxTotalQoS = Double.MIN_VALUE;
		for (int i = 0; i < mTotalQoS[noAbstractServices - 1].length; i++) {
			if (mTotalQoS[noAbstractServices - 1][i] > maxTotalQoS) {
				indexOfOptimalService = i;
				maxTotalQoS = mTotalQoS[noAbstractServices - 1][i];
			}
		}
		mPartialAccumQoS[noAbstractServices - 1] = maxTotalQoS;
		mPartialSolution[noAbstractServices - 1] = indexOfOptimalService;

		/* Then, we add the other levels, in a backward fashion. */
		for (int i = noAbstractServices - 2; i >= 0; i--) {
			if (!addLevelToPartialSolution(i)) {
				mPartialSolution = null;
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

		DynamicProgramming dynProg = new DynamicProgramming(attrs);
		dynProg.setEdgesWeights(edgesWeights);

		dynProg.run();

		System.out.println(Arrays.toString(dynProg.mPartialSolution));
	}
}
