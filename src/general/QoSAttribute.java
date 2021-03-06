package general;

import java.util.Arrays;

/**
 * 
 * This class represents a QoS attribute, which is composed by:
 * 
 * - A value for each concrete service, normalized between 0 and 1
 * ("e crescente");
 * 
 * - An aggregation function;
 * 
 * - A corresponding weight, provided by the user.
 * 
 * @author Andre Luiz Verucci da Cunha
 */
public class QoSAttribute {

	/* The possible aggregation functions. */
	public static final int AGGREGATE_BY_SUM = 0x00;
	public static final int AGGREGATE_BY_PRODUCT = 0x01;
	public static final int AGGREGATE_BY_AVERAGE = 0x02;

	/**
	 * The QoS values.
	 */
	private double[][] mValues;

	/**
	 * The function to be used to compute the aggregated QoS. It must be one of
	 * the following values: - AGGREGATE_BY_SUM; - AGGREGATE_BY_PRODUCT; -
	 * AGGREGATE_BY_AVERAGE.
	 */
	private int mAggregationMethod;

	/**
	 * The weight of this attribute.
	 */
	private double mWeight;

	/**
	 * The maximum possible QoS for the current the aggregation function and the
	 * current number of virtual services.
	 */
	private double mMaximumQoS;

	/**
	 * Creates a QoSAttribute instance.
	 * 
	 * @param values
	 *            The QoS values.
	 * @param aggregationMethod
	 *            The aggregation method to be used.
	 * @param weight
	 *            The weight of this attribute.
	 */
	public QoSAttribute(double[][] values, int aggregationMethod, double weight) {

		/* Check for the validity of the given values. */
		for (int i = 0; i < values.length; i++) {
			for (int j = 0; j < values[i].length; j++) {
				if (DoubleComparator.compare(values[i][j], 1d) > 0
						|| DoubleComparator.compare(values[i][j], 0d) < 0) {
					throw new IllegalArgumentException(
							String.format("values[%d][%d] is invalid: %g", i,
									j, values[i][j]));
				}
			}
		}

		mValues = values;
		mAggregationMethod = aggregationMethod;
		mWeight = weight;

		switch (aggregationMethod) {
		case AGGREGATE_BY_SUM:
			mMaximumQoS = values.length;
			break;
		case AGGREGATE_BY_PRODUCT:
			mMaximumQoS = 1f;
			break;
		case AGGREGATE_BY_AVERAGE:
			mMaximumQoS = 1f;
			break;
		default:
			mMaximumQoS = -1;
			break;
		}
	}

	/**
	 * Evaluates the aggregated QoS of a composition.
	 * 
	 * @param composition
	 *            A vector containing the index of the concrete service
	 *            corresponding to each abstract service.
	 * @return The aggregated QoS value.
	 */
	public double getAggregatedQoS(int[] composition) {
		double aggregatedQoS;

		if (composition.length != mValues.length) {
			throw new IllegalArgumentException(String.format(
					"Dimensions mismatch. Expected %d, got %d.",
					mValues.length, composition.length));
		}
		for (int i = 0; i < composition.length; i++) {
			if (composition[i] >= mValues[i].length) {
				throw new IllegalArgumentException(String.format(
						"Composition[%d] is invalid: ", i, composition[i]));
			}
		}

		int count = 0;
		switch (mAggregationMethod) {
		case AGGREGATE_BY_SUM:
			aggregatedQoS = 0f;
			for (int i = 0; i < composition.length; i++) {
				if (composition[i] >= 0) {
					aggregatedQoS += mValues[i][composition[i]];
				}
			}
			break;
		case AGGREGATE_BY_PRODUCT:
			aggregatedQoS = 1f;
			for (int i = 0; i < composition.length; i++) {
				if (composition[i] >= 0) {
					aggregatedQoS *= mValues[i][composition[i]];
					count++;
				}
			}
			if (count == 0) {
				aggregatedQoS = 0;
			}
			break;
		case AGGREGATE_BY_AVERAGE:
			aggregatedQoS = 0f;
			for (int i = 0; i < composition.length; i++) {
				if (composition[i] >= 0) {
					aggregatedQoS += mValues[i][composition[i]];
					count++;
				}
			}
			aggregatedQoS = (count != 0) ? (aggregatedQoS / count) : 0;
			break;
		default:
			aggregatedQoS = -1;
			break;
		}

		return aggregatedQoS;
	}

	/**
	 * 
	 * @return The maximum QoS value.
	 */
	public double getMaximumQoS() {
		return mMaximumQoS;
	}

	/**
	 * 
	 * @return The weight of this attribute.
	 */
	public double getWeight() {
		return mWeight;
	}

	/**
	 * 
	 * @return The QoS values.
	 */
	public double[][] getValues() {
		return mValues;
	}

	/**
	 * Returns the aggregation method of this attribute.
	 * 
	 * @return The aggregation method of this attribute.
	 */
	public int getAggregationMethod() {
		return mAggregationMethod;
	}

	/**
	 * Calculates the total QoS of each concrete service.
	 * 
	 * @param qosValues
	 *            A vector containing all the attributes.
	 * @return A matrix containing the total QoS of each service.
	 */
	public static double[][] calculateTotalQoS(QoSAttribute[] qosValues) {
		double[][] totalQoSValues;
		totalQoSValues = new double[qosValues[0].getValues().length][];

		for (int i = 0; i < totalQoSValues.length; i++) {
			totalQoSValues[i] = new double[qosValues[0].getValues()[i].length];
			Arrays.fill(totalQoSValues[i], 0f);
		}

		for (int attr = 0; attr < qosValues.length; attr++) {
			QoSAttribute currentAttribute = qosValues[attr];
			double[][] currentValues = currentAttribute.getValues();

			for (int i = 0; i < currentValues.length; i++) {
				for (int j = 0; j < currentValues[i].length; j++) {
					totalQoSValues[i][j] += currentValues[i][j]
							* currentAttribute.getWeight();
				}
			}
		}

		return totalQoSValues;
	}

	/**
	 * Calculates the aggregated QoS of a composition where there are more then
	 * one QoS attributes.
	 * 
	 * @param attributes
	 *            The QoS attributes.
	 * @param composition
	 *            A vector containing which concrete service should be used for
	 *            each abstract service.
	 * @return The aggregated QoS corresponding to the given composition.
	 */
	public static double calculateAggregatedQoS(QoSAttribute[] attributes,
			int[] composition) {
		double currentQoSValue = 0;
		double maximumQoSValue = 0;

		for (int i = 0; i < attributes.length; i++) {
			currentQoSValue += attributes[i].getAggregatedQoS(composition)
					* attributes[i].getWeight();
			maximumQoSValue += attributes[i].getMaximumQoS()
					* attributes[i].getWeight();
		}

		return currentQoSValue / maximumQoSValue;
	}
}
