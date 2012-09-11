package aco;

public class QoSAttribute {

	public static final int AGGREGATE_BY_SUM = 0x00;
	public static final int AGGREGATE_BY_PRODUCT = 0x01;
	public static final int AGGREGATE_BY_AVERAGE = 0x02;

	private float[][] values;
	private int aggregationMethod;
	private float weight;

	public QoSAttribute(float[][] values, int aggregationMethod, int weight) {
		this.values = values;
		this.aggregationMethod = aggregationMethod;
		this.weight = weight;
	}

	public float getAggregatedQoS(int[] composition) {
		float aggregatedQoS;

		if (composition.length != values.length) {
			throw new IllegalArgumentException(String.format(
					"Dimensions mismatch. Expected %d, got %d.", values.length,
					composition.length));
		}
		for (int i = 0; i < composition.length; i++) {
			if ((composition[i] < 0) || (composition[i] >= values[i].length)) {
				throw new IllegalArgumentException(String.format(
						"Composition[%d] is invalid: ", i, composition[i]));
			}
		}

		switch (aggregationMethod) {
		case AGGREGATE_BY_SUM:
			aggregatedQoS = 0f;
			for (int i = 0; i < composition.length; i++) {
				aggregatedQoS += values[i][composition[i]];
			}
			break;
		case AGGREGATE_BY_PRODUCT:
			aggregatedQoS = 1f;
			for (int i = 0; i < composition.length; i++) {
				aggregatedQoS *= values[i][composition[i]];
			}
			break;
		case AGGREGATE_BY_AVERAGE:
			aggregatedQoS = 0f;
			for (int i = 0; i < composition.length; i++) {
				aggregatedQoS += values[i][composition[i]];
			}
			aggregatedQoS /= composition.length;
			break;
		default:
			aggregatedQoS = -1;
			break;
		}

		return aggregatedQoS;
	}

	public float getWeight() {
		return weight;
	}
}
