import java.util.*;

/**
 * Benchmark the difference between iteration on {@code ArrayList<Integer>} and regular java array.
 * @author Roman Elizarov
 */
public class IntListIterationTiming {
	private static final int MIN_SIZE = 1000;
	private static final int MAX_SIZE = 10_000_000;
	private static final int TOTAL_ITERATIONS = 1_000_000_000;

	private final IntList list;

	private int dummy; // to avoid HotSpot optimizing away iteration

	private IntListIterationTiming(String className, int size) throws Exception {
		list = (IntList)Class.forName(IntList.class.getName() + "$" + className).newInstance();
		Random random = new Random(1);
		for	(int i = 0; i < size; i++)
			list.add(random.nextInt());
	}

	private double time(int size) {
		dummy = 0;
		int reps = TOTAL_ITERATIONS / size;
		long start = System.nanoTime();
		for	(int rep = 0; rep < reps; rep++)
			dummy += runIteration(size);
		return (double)(System.nanoTime() - start) / reps / size;
	}

	private int runIteration(int size) {
		int sum = 0;
		for (int i = 0; i < size; i++)
			sum += list.getInt(i);
		return sum;
	}

	public static void main(String[] args) throws Exception {
		if (args.length < 2) {
			System.err.println("Usage: " + IntListIterationTiming.class + " <passes> <impl> [<impl> ...]");
			System.err.println("Where: <passes>  is the number of passes to run tests for.");
			System.err.println("       <impl>    is one of: ");
			for (Class c : IntList.class.getDeclaredClasses())
				System.err.println("                    " + c.getSimpleName());
			return;
		}

		int passes = Integer.decode(args[0]);
		String[] classes = Arrays.copyOfRange(args, 1, args.length);

		Map<String, IntListIterationTiming> instances = new HashMap<>();
		for (String className : classes)
			instances.put(className, new IntListIterationTiming(className, MAX_SIZE));

		for (int pass = 1; pass <= passes; pass++) {
			System.out.printf("----- PASS %d -----%n", pass);
			for (int size = MIN_SIZE; size <= MAX_SIZE; size *= 10) {
				for (String className : classes) {
					IntListIterationTiming timing = instances.get(className);
					double time = timing.time(size);
					System.out.printf(Locale.US, "%30s[%8d]: %.2f ns per item (%d)%n",
							className, size, time, timing.dummy);
				}
			}
		}
	}
}
