package bb;

import java.util.ArrayList;
import java.util.Arrays;

import general.QoSAttribute;

/**
 * This class adapts a given Web Service composition problem, possibly with
 * constraints over some attributes, into a MIP problem to be solved by Branch
 * and Bound.
 * 
 * @author Andre Luiz Verucci da Cunha
 * 
 */
public class WscHelper {

	/**
	 * The QoS attributes to be considered.
	 */
	private QoSAttribute[] mQoSAttributes;

	/**
	 * The constraints over the aggregated QoS values.
	 */
	private ArrayList<Constraint> mConstraints;

	/**
	 * The number of abstract services.
	 */
	private int mNoAbstractServices;

	/**
	 * The number of concrete services.
	 */
	private int mNoConcreteServices;

	public WscHelper(QoSAttribute[] attributes) {
		mQoSAttributes = attributes;
		mConstraints = new ArrayList<Constraint>();

		mNoAbstractServices = attributes[0].getValues().length;
		for (int i = 0; i < mNoAbstractServices; i++) {
			mNoConcreteServices += attributes[0].getValues()[i].length;
		}
	}

	/**
	 * Adds a constraint over the aggregated value of an attribute.
	 * 
	 * @param attributeIndex
	 *            The index of the attribute.
	 * @param rel
	 *            One of Simplex.GTE, Simplex.EQUALS and Simplex.LTE.
	 * @param b
	 *            The right-hand term of the constraint.
	 */
	public void addConstraintOnAttribute(int attributeIndex, int rel, double b) {
		double[] a = new double[mNoConcreteServices];

		int accum = 0;
		switch (mQoSAttributes[attributeIndex].getAggregationMethod()) {
		case QoSAttribute.AGGREGATE_BY_PRODUCT:
			throw new IllegalArgumentException(
					"Non-linear aggregation function.");
		case QoSAttribute.AGGREGATE_BY_AVERAGE:
			for (int i = 0; i < mNoAbstractServices; i++) {
				for (int j = 0; j < mQoSAttributes[attributeIndex].getValues()[i].length; j++) {
					a[accum] = mQoSAttributes[attributeIndex].getValues()[i][j]
							/ mNoAbstractServices;
					accum++;
				}
			}
			mConstraints.add(new Constraint(a, rel, b));
			break;
		case QoSAttribute.AGGREGATE_BY_SUM:
			for (int i = 0; i < mNoAbstractServices; i++) {
				for (int j = 0; j < mQoSAttributes[attributeIndex].getValues()[i].length; j++) {
					a[accum] = mQoSAttributes[attributeIndex].getValues()[i][j];
					accum++;
				}
			}
			mConstraints.add(new Constraint(a, rel, b));
			break;
		default:
			break;
		}
	}

	/**
	 * Manually adds a constraint.
	 * 
	 * @param c
	 *            The constraint to be added.
	 */
	public void addConstraint(Constraint c) {
		mConstraints.add(c);
	}

	/**
	 * Manually adds a constraint.
	 * 
	 * @param a
	 *            The constraint's coefficients.
	 * @param rel
	 *            One of Simplex.GTE, Simplex.EQUALS and Simplex.LTE.
	 * @param b
	 *            The right-hand term of the constraint.
	 */
	public void addConstraint(double[] a, int rel, double b) {
		mConstraints.add(new Constraint(a, rel, b));
	}

	/**
	 * Returns the MIP problem corresponding to the given QoS attributes and
	 * constraints.
	 * 
	 * @return the MIP problem corresponding to the given QoS attributes and
	 *         constraints.
	 */
	public BranchAndBound getProblem() {
		Simplex s = new Simplex();

		double[][] totalQoS = QoSAttribute.calculateTotalQoS(mQoSAttributes);

		for (int i = 0; i < totalQoS.length; i++) {
			System.out.println(Arrays.toString(totalQoS[i]));
		}

		double[] objectiveFunction = new double[mNoConcreteServices + 1];
		int accum = 0;
		for (int i = 0; i < mNoAbstractServices; i++) {
			for (int j = 0; j < totalQoS[i].length; j++) {
				objectiveFunction[accum + 1] = totalQoS[i][j];
				accum++;
			}
		}

		s.setObjectiveFuntion(objectiveFunction, Simplex.MAXIMIZE);

		/* Adding the common constraints. */
		accum = 0;
		for (int i = 0; i < mNoAbstractServices; i++) {
			double[] a = new double[mNoConcreteServices];
			for (int j = 0; j < totalQoS[i].length; j++) {
				s.addBinaryVariableConstraint(accum + 1);
				a[accum] = 1;
				accum++;
			}

			s.addConstraint(a, Simplex.EQUALS, 1);
		}

		/* Adding the user-defined constraints. */
		for (Constraint c : mConstraints) {
			s.addConstraint(c);
		}

		boolean[] integerVariables = new boolean[mNoConcreteServices];
		Arrays.fill(integerVariables, true);

		BranchAndBound bb = new BranchAndBound(s, integerVariables);

		return bb;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		double[][] values = { { 1.0, 0.2, 0.5 }, { 0.5, 1.0}, { 0.2, 0.9, 1.0 } };

		QoSAttribute attrSum = new QoSAttribute(values,
				QoSAttribute.AGGREGATE_BY_SUM, 0.2);
		QoSAttribute attrProd = new QoSAttribute(values,
				QoSAttribute.AGGREGATE_BY_PRODUCT, 0.2);
		QoSAttribute attrAvg = new QoSAttribute(values,
				QoSAttribute.AGGREGATE_BY_AVERAGE, 0.6);

		QoSAttribute[] attrs = new QoSAttribute[] { attrSum, attrProd, attrAvg };

		WscHelper h = new WscHelper(attrs);
		h.addConstraintOnAttribute(0, Simplex.LTE, 1.2);

		BranchAndBound bb = h.getProblem();

		System.out.println(bb);

		bb.solve();

		System.out.println(Arrays.toString(bb.getSolution()));
	}
}
