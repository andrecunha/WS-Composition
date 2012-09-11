package aco;

import static org.junit.Assert.*;

import org.junit.BeforeClass;
import org.junit.Test;

public class QoSAttributeTest {

	public static final float[][] values = { { 1, 2, 3 }, { 4, 5 },
			{ 7, 8, 9 } };

	public static QoSAttribute attrSum;
	public static QoSAttribute attrProd;
	public static QoSAttribute attrAvg;

	@BeforeClass
	public static void init() {
		attrSum = new QoSAttribute(values, QoSAttribute.AGGREGATE_BY_SUM, 1);
		attrProd = new QoSAttribute(values, QoSAttribute.AGGREGATE_BY_PRODUCT,
				1);
		attrAvg = new QoSAttribute(values, QoSAttribute.AGGREGATE_BY_AVERAGE, 1);
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void test0() {
		int[] composition = { -1, 0, 0 };
		attrSum.getAggregatedQoS(composition);
	}

	@Test(expected = IllegalArgumentException.class)
	public void test1() {
		int[] composition = { 3, 0, 0 };
		attrSum.getAggregatedQoS(composition);
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void test2() {
		int[] composition = { 0, 2, 0 };
		attrSum.getAggregatedQoS(composition);
	}
	
	@Test
	public void test3() {
		int[] composition = { 0, 0, 0 };
		assertEquals(attrSum.getAggregatedQoS(composition), 12f, 0f);
		assertEquals(attrProd.getAggregatedQoS(composition), 28f, 0f);
		assertEquals(attrAvg.getAggregatedQoS(composition), 4f, 0f);
	}
	
	@Test
	public void test4() {
		int[] composition = { 1, 0, 1 };
		assertEquals(attrSum.getAggregatedQoS(composition), 14f, 0f);
		assertEquals(attrProd.getAggregatedQoS(composition), 64f, 0f);
		assertEquals(attrAvg.getAggregatedQoS(composition), 4.667f, 0.001f);
	}
}
