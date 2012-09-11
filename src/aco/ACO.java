package aco;

import java.util.Arrays;

public class ACO {

	private Ant[] ants;
	
	//TODO: Testar essa função.
	public static float[][] calculateAggregatedQoS(
			QoSAttribute[] qosValues) {
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
	
	public void run() {
		
	}
	
	public static void main(String[] args) {
		
	}

}
