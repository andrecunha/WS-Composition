package bb;

import java.util.Arrays;

public class Constraint {

	public double[] a;
	public int rel;
	public double b;
	
	public Constraint(double[] a, int rel, double b) {
		this.a = a;
		this.rel = rel;
		this.b = b;
	}
	
	/**
	 * Copy constructor.
	 * @param c The constraint to copy.
	 */
	public Constraint(Constraint c) {
		a = Arrays.copyOf(c.a, c.a.length);
		rel = c.rel;
		b = c.b;
	}
}
