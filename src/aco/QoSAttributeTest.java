package aco;

import static org.junit.Assert.*;

import org.junit.BeforeClass;
import org.junit.Test;

public class QoSAttributeTest {

	public static final float[][] values = { { 1, 0.5f, 1 }, { 1, 0.5f },
			{ 0.5f, 0.5f, 1 } };
	public static final float[][] invalidValues1 = { { 1.1f, 0.5f, 1 }, { 1, 0.5f },
		{ 0.5f, 0.5f, 1 } };
	public static final float[][] invalidValues2 = { { -1, 0.5f, 1 }, { 1, 0.5f },
		{ 0.5f, 0.5f, 1 } };

	public static QoSAttribute attrSum;
	public static QoSAttribute attrProd;
	public static QoSAttribute attrAvg;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
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
	
	@Test(expected = IllegalArgumentException.class)
	public void test3() {
		int[] composition = { 0, 0 };
		attrSum.getAggregatedQoS(composition);
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void test4() {
		new QoSAttribute(invalidValues1, 0, 0);
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void test5() {
		new QoSAttribute(invalidValues2, 0, 0);
	}
	
	@Test
	public void test6() {
		int[] composition = { 0, 0, 0 };
		assertEquals(attrSum.getAggregatedQoS(composition), 2.5f, 0f);
		assertEquals(attrProd.getAggregatedQoS(composition), 0.5, 0f);
		assertEquals(attrAvg.getAggregatedQoS(composition), 0.833, 0.001f);
	}
	
	@Test
	public void test7() {
		int[] composition = { 1, 0, 1 };
		assertEquals(attrSum.getAggregatedQoS(composition), 2f, 0f);
		assertEquals(attrProd.getAggregatedQoS(composition), 0.25f, 0f);
		assertEquals(attrAvg.getAggregatedQoS(composition), 0.667f, 0.001f);
	}
	
	@Test
	public void test8() {
		int[] composition = { 2, 1, 2 };
		assertEquals(attrSum.getAggregatedQoS(composition), 2.5f, 0f);
		assertEquals(attrProd.getAggregatedQoS(composition), 0.5f, 0f);
		assertEquals(attrAvg.getAggregatedQoS(composition), 0.833f, 0.001f);
	}
	
	@Test
	public void test9() {
		assertEquals(attrSum.getMaximumQoS(), 3f, 0f);
		assertEquals(attrProd.getMaximumQoS(), 1f, 0f);
		assertEquals(attrAvg.getMaximumQoS(), 1f, 0f);
	}
}
