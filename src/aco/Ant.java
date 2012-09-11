package aco;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * 
 * @author Andre Luiz Verucci da Cunha
 *
 */
public class Ant {

	public static final int FORWARD = 0x00;
	public static final int BACKWARD = 0x01;

	private ArrayList<QoSAttribute> qosValues;
	private float[][] aggregatedQoSValues;
	private int currentPosition; // Posição do último serviço que foi escolhido.
	private int direction;
	private int[] partialSolution;

	private int nestPosition;
	private int sourcePosition;

	public Ant(ArrayList<QoSAttribute> qosValues) {
		int numberOfAbstractServices = qosValues.get(0).getValues().length;

		this.qosValues = qosValues;
		this.currentPosition = -1;
		this.direction = FORWARD;
		this.partialSolution = new int[numberOfAbstractServices];
		this.nestPosition = -1;
		this.sourcePosition = numberOfAbstractServices;

		//TODO: Mover isso para outro lugar. Passar o resultado como parâmetro
		// para o construtor.
		aggregatedQoSValues = new float[qosValues.get(0).getValues().length][qosValues
				.get(0).getValues()[0].length];

		for (int i = 0; i < aggregatedQoSValues.length; i++) {
			Arrays.fill(aggregatedQoSValues[i], 0f);
		}

		for (int attr = 0; attr < qosValues.size(); attr++) {
			QoSAttribute currentAttribute = qosValues.get(attr);
			float[][] currentValues = currentAttribute.getValues();

			for (int i = 0; i < currentValues.length; i++) {
				for (int j = 0; j < currentValues[i].length; j++) {
					aggregatedQoSValues[i][j] += currentValues[i][j]
							* currentAttribute.getWeight();
				}
			}
		}
	}

	public void walk() {
		if (currentPosition == nestPosition) {
			direction = FORWARD;
		} else if (currentPosition == sourcePosition) {
			direction = BACKWARD;
		}

		if (direction == FORWARD) {
			currentPosition++;
		} else {
			currentPosition--;
		}

		if (currentPosition == nestPosition
				|| currentPosition == sourcePosition) {
			return;
		}

	}

	// TODO: Verificar se está certo.
	public float getNewPheromone() {
		if (currentPosition == sourcePosition) {
			float currentQoSValue = 0f;
			float maximumQoSValue = 0f;

			for (int i = 0; i < qosValues.size(); i++) {
				currentQoSValue += qosValues.get(i).getAggregatedQoS(
						partialSolution)
						* qosValues.get(i).getWeight();
				maximumQoSValue += qosValues.get(i).getMaximumQoS()
						* qosValues.get(i).getWeight();
			}

			return currentQoSValue / maximumQoSValue;
		}
		return 0f;
	}
}
