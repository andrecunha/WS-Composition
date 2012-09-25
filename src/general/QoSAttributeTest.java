package general;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import org.junit.BeforeClass;
import org.junit.Test;

public class QoSAttributeTest {

	public static final float[][] values = { { 1, 0.5f, 1 }, { 1, 0.5f },
			{ 0.5f, 0.5f, 1 } };
	public static final float[][] invalidValues1 = { { 1.1f, 0.5f, 1 },
			{ 1, 0.5f }, { 0.5f, 0.5f, 1 } };
	public static final float[][] invalidValues2 = { { -1, 0.5f, 1 },
			{ 1, 0.5f }, { 0.5f, 0.5f, 1 } };

	public static QoSAttribute attrSum;
	public static QoSAttribute attrProd;
	public static QoSAttribute attrAvg;
	public static QoSAttribute[] attrs;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		attrSum = new QoSAttribute(values, QoSAttribute.AGGREGATE_BY_SUM, 0.2f);
		attrProd = new QoSAttribute(values, QoSAttribute.AGGREGATE_BY_PRODUCT,
				0.3f);
		attrAvg = new QoSAttribute(values, QoSAttribute.AGGREGATE_BY_AVERAGE,
				0.5f);
		attrs = new QoSAttribute[] { attrSum, attrProd, attrAvg };
	}

	/* Ilegal compositions in getAggregatedQoS. */

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

	/* Legal compositions in getAggregatedQoS. */

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

	/* Tests for getMaximumQoS. */

	@Test
	public void test9() {
		assertEquals(attrSum.getMaximumQoS(), 3f, 0f);
		assertEquals(attrProd.getMaximumQoS(), 1f, 0f);
		assertEquals(attrAvg.getMaximumQoS(), 1f, 0f);
	}

	/* Tests for calculateAggregatedQoS. */

	/* Illegal composition. */

	@Test(expected = IllegalArgumentException.class)
	public void test10() {
		int[] composition = { -1, 0, 0 };
		QoSAttribute.calculateAggregatedQoS(attrs, composition);
	}

	/* Legal composition. */

	@Test
	public void test11() {
		int[] composition = { 0, 0, 0 };
		assertEquals(QoSAttribute.calculateAggregatedQoS(attrs, composition),
				0.7619, 0.0001);
	}

	/* Tests for calculateTotalQoS. */

	@Test()
	public void test12() {
		float[][] _values1 = { { 1, 0.5f, 1 }, { 1, 0.5f }, { 0.5f, 0.5f, 1 } };
		float[][] _values2 = { { 0.5f, 0.75f, 1 }, { 1, 0.5f },
				{ 0.5f, 0.5f, 1 } };
		float[][] _values3 = { { 0.5f, 0.5f, 0.8f }, { 1, 0.5f },
				{ 0.5f, 0.5f, 1 } };

		QoSAttribute _attrSum = new QoSAttribute(_values1,
				QoSAttribute.AGGREGATE_BY_SUM, 0.2f);
		QoSAttribute _attrProd = new QoSAttribute(_values2,
				QoSAttribute.AGGREGATE_BY_PRODUCT, 0.3f);
		QoSAttribute _attrAvg = new QoSAttribute(_values3,
				QoSAttribute.AGGREGATE_BY_AVERAGE, 0.5f);
		QoSAttribute[] _attrs = new QoSAttribute[] { _attrSum, _attrProd,
				_attrAvg };

		float[][] totalQoS = QoSAttribute.calculateTotalQoS(_attrs);
		assertArrayEquals(totalQoS[0], new float[] { 0.6f, 0.575f, 0.9f },
				0.001f);
		assertArrayEquals(totalQoS[1], new float[] { 1.0f, 0.5f }, 0.001f);
		assertArrayEquals(totalQoS[2], new float[] { 0.5f, 0.5f, 1.0f }, 0.001f);
	}
}
