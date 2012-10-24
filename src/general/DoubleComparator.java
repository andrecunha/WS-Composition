package general;

/**
 * A class for making double-precision comparisons within an epsilon interval.
 * 
 * @author Andre Luiz Verucci da Cunha.
 * 
 */
public class DoubleComparator {
	/**
	 * The precision of the comparison operation.
	 */
	private static final double EPSILON = 1E-6;

	/**
	 * Compares its two arguments for order. Returns a negative integer, zero,
	 * or a positive integer as the first argument is less than, equal to, or
	 * greater than the second.
	 * 
	 * @param n1
	 *            The first number to be compared.
	 * @param n2
	 *            The second number to be compared.
	 * @return A negative integer, zero, or a positive integer as the first
	 *         argument is less than, equal to, or greater than the second.
	 */
	public static int compare(Double n1, Double n2) {

		if (Math.abs(n1 - n2) <= EPSILON) {
			return 0;
		} else if (n1 - n2 > EPSILON) {
			return 1;
		} else {
			return -1;
		}
	}
}