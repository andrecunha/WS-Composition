package bruteforce;

import java.util.Arrays;

import general.DoubleComparator;
import general.QoSAttribute;

/**
 * This class implements the Brute Force algorithm.
 * 
 * @author Andre Luiz Verucci da Cunha
 * 
 */
public class BruteForce {

	/**
	 * The QoS attributes.
	 */
	private QoSAttribute[] mQoSAttributes;

	/**
	 * The best composition.
	 */
	private int[] mBestComposition;

	/**
	 * The aggregated QoS of the best composition.
	 */
	private double mBestQoS;

	/**
	 * The composition currently being evaluated by backtrack().
	 */
	private int[] mCurrComposition;

	/**
	 * The values of any QoS attribute, just for code shortness.
	 */
	private double[][] mAuxValues;

	/**
	 * Default constructor.
	 * 
	 * @param qosAttributes
	 *            The QoS attributes of the services.
	 */
	public BruteForce(QoSAttribute[] qosAttributes) {
		mQoSAttributes = qosAttributes;
		mAuxValues = qosAttributes[0].getValues();
		mBestComposition = new int[mAuxValues.length];
		mCurrComposition = new int[mAuxValues.length];
	}

	/**
	 * Enumerates all possible compositions and chooses the one with the best
	 * aggregated QoS.
	 * 
	 * @param from
	 *            The abstract service from which to start the search.
	 */
	private void backtrack(int from) {
		for (int i = 0; i < mAuxValues[from].length; i++) {
			mCurrComposition[from] = i;

			if (from != mAuxValues.length - 1) {
				backtrack(from + 1);
			} else {
				double newQoS = QoSAttribute.calculateAggregatedQoS(
						mQoSAttributes, mCurrComposition);
				if (DoubleComparator.compare(newQoS, mBestQoS) > 0) {
					mBestQoS = newQoS;
					System.arraycopy(mCurrComposition, 0, mBestComposition, 0,
							mCurrComposition.length);
				}
			}
		}
	}

	/**
	 * Calculates and returns the best composition.
	 * @return The best composition.
	 */
	public int[] getBestComposition() {
		mBestQoS = Double.NEGATIVE_INFINITY;

		backtrack(0);

		return mBestComposition;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		double[][] values = { { 1, 0.5, 1 }, { 1, 0.5 }, { 0.5, 0.5, 1 } };

		QoSAttribute attrSum = new QoSAttribute(values,
				QoSAttribute.AGGREGATE_BY_SUM, 0.2f);
		QoSAttribute attrProd = new QoSAttribute(values,
				QoSAttribute.AGGREGATE_BY_PRODUCT, 0.3f);
		QoSAttribute attrAvg = new QoSAttribute(values,
				QoSAttribute.AGGREGATE_BY_AVERAGE, 0.5f);

		QoSAttribute[] attrs = { attrSum, attrProd, attrAvg };

		BruteForce bf = new BruteForce(attrs);

		System.out.println(Arrays.toString(bf.getBestComposition()));
	}

}
