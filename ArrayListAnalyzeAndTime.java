
import java.util.*;

/**
 * Examine the speed of iteration on {@code ArrayList<Integer>} at various sizes.
 * @author Roman Elizarov
 */
public class ArrayListAnalyzeAndTime {
	private static final int STABLE_PASS = 2;

	private static final int MIN_PAGE_SHIFT = 6;
	private static final int MAX_PAGE_SHIFT = 12;

	private static final int MIN_SIZE = 1000;
	private static final int MAX_SIZE = 10000000;
	private static final int TOTAL_ITERATIONS = 100000000;

	private static final boolean SHUFFLE = Boolean.getBoolean("shuffle");

	private final IntList.ViaArrayList list;
	private final long[] address;

	private int dummy; // to avoid HotSpot optimizing away iteration

	private ArrayListAnalyzeAndTime(int size) throws Exception {
		list = new IntList.ViaArrayList();
		// fill in list with random values
		Random random = new Random(1);
		for	(int i = 0; i < size; i++)
			list.add(random.nextInt());
		// optionally shuffle
		if (SHUFFLE)
			Collections.shuffle(list, new Random(1));
		// analyze object addresses
		address = new long[size];
		for (int i = 0; i < size; i++)
			address[i] = getCurrentObjectAddress(list.get(i));
	}

	private int countPages(int shift, int size) {
		int count = 0;
		long page = 0;
		for (int i = 0; i < size; i++) {
			long cp = address[i] >>> shift;
			if (cp != page) {
				count++;
				page = cp;
			}
		}
		return count;
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
		if (args.length != 1) {
			System.err.println("Usage: " + ArrayListAnalyzeAndTime.class + " <passes>");
			System.err.println("Where: <passes>  is the number of passes to run the test for.");
			return;
		}

		int passes = Integer.decode(args[0]);

		ArrayListAnalyzeAndTime instance = new ArrayListAnalyzeAndTime(MAX_SIZE);

		System.out.println("----- MEMORY PAGES ANALYSIS -----");
		System.out.println("Avg no of accessed pages per object times page size (e.g. effective object size)");
		System.out.print("[pagesize]: ");
		for (int shift = MIN_PAGE_SHIFT; shift <= MAX_PAGE_SHIFT; shift++) {
			System.out.printf("%9d", 1 << shift);
		}
		System.out.println();
		for (int size = MIN_SIZE; size <= MAX_SIZE; size *= 10) {
			System.out.printf("[%8d]: ", size);
			for (int shift = MIN_PAGE_SHIFT; shift <= MAX_PAGE_SHIFT; shift++) {
				System.out.printf(Locale.US, "%9.1f", (double)instance.countPages(shift, size) * (1 << shift) / size);
			}
			System.out.println();
		}

		Map<Integer, TimeStats> stats = new HashMap<Integer, TimeStats>();
		for (int pass = 1; pass <= passes; pass++) {
			System.out.printf("----- PASS %d -----%n", pass);
			for (int size = MIN_SIZE; size <= MAX_SIZE; size *= 10) {
				double time = instance.time(size);
				TimeStats stat = stats.get(size);
				if (stat == null)
					stats.put(size, stat = new TimeStats());
				if (pass >= STABLE_PASS)
					stat.add(time);
				System.out.printf(Locale.US, "[%8d]: %.2f %s ns per item (%d)%n",
						size, time, stat, instance.dummy);
			}
		}
	}

	private static native long getCurrentObjectAddress(Object obj);

	static {
		System.loadLibrary(ArrayListAnalyzeAndTime.class.getName());
	}
}
