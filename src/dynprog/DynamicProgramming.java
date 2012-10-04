package dynprog;

import general.QoSAttribute;

import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;

public class DynamicProgramming extends Thread {

	private QoSAttribute[] mQoSAttributes;

	private int[] mSolution;

	private float[] mPartialAccumQoS;

	public DynamicProgramming(QoSAttribute[] qosAttributes) {
		mQoSAttributes = qosAttributes;

		mSolution = new int[qosAttributes[0].getValues().length];
		Arrays.fill(mSolution, -1);

		mPartialAccumQoS = new float[qosAttributes[0].getValues().length];
		Arrays.fill(mPartialAccumQoS, 0f);
	}

	private void addLevelToPartialSolution(int level) {
		int noConcreteServices = mQoSAttributes[0].getValues()[level].length;

		float maxAggregatedQoS = Float.MIN_VALUE;
		int indexOfOptimalService = 0;

		for (int j = 0; j < noConcreteServices; j++) {
			mSolution[level] = j;
			float aggregatedQoS = QoSAttribute.calculateAggregatedQoS(
					mQoSAttributes, mSolution);
			if (aggregatedQoS > maxAggregatedQoS) {
				maxAggregatedQoS = aggregatedQoS;
				indexOfOptimalService = j;
			}
		}
		
		mSolution[level] = indexOfOptimalService;
	}

	@Override
	public void run() {
		int noAbstractServices = mQoSAttributes[0].getValues().length;
		
		for (int i = noAbstractServices - 1; i >= 0; i--) {
			addLevelToPartialSolution(i);
		}
	}

	public void startWithTimeOut(long millisTimeOut) {
		Timer timer = new Timer();
		TimerTask task = new TimerTask() {

			@Override
			public void run() {
				DynamicProgramming.this.interrupt();
			}
		};

		timer.schedule(task, millisTimeOut);
		start();
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		float[][] values = { { 1, 0.5f, 1, 0.2f, 0.3f },
				{ 1, 0.5f, 0.2f, 0.3f }, { 0.5f, 0.5f, 1, 0.2f, 0.3f } };

		QoSAttribute attrSum = new QoSAttribute(values,
				QoSAttribute.AGGREGATE_BY_SUM, 0.2f);
		QoSAttribute attrProd = new QoSAttribute(values,
				QoSAttribute.AGGREGATE_BY_PRODUCT, 0.3f);
		QoSAttribute attrAvg = new QoSAttribute(values,
				QoSAttribute.AGGREGATE_BY_AVERAGE, 0.5f);

		QoSAttribute[] attrs = { attrSum, attrProd, attrAvg };
		
		DynamicProgramming dynProg = new DynamicProgramming(attrs);
		
		dynProg.run();
		
		System.out.println(Arrays.toString(dynProg.mSolution));
	}

}
