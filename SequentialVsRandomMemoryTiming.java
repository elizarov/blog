import java.util.Arrays;
import java.util.Locale;

/**
 * Compare sequential vs random memory access of 32 bits ints on the same simple code.
 * See {@link #run} method.
 *
 * @author Roman Elizarov
 */
public class SequentialVsRandomMemoryTiming {
	private static final int N = 1 << 25;
	private static final int RND_STEP = (int)((Math.sqrt(5) - 1) / 2 * N) | 1;

	private static final int STABLE_PASS = 3;
	private static final int PASSES = 20;

	private final int[] a = new int[N];

	private final Stats seqStats = new Stats();
	private final Stats rndStats = new Stats();

	public static void main(String[] args) {
		new SequentialVsRandomMemoryTiming().go();
	}

	public SequentialVsRandomMemoryTiming() {
		Arrays.fill(a, 1);
	}

	private void go() {
		assert (N & (N - 1)) == 0 : "N must be power of 2";
		for (int pass = 1; pass <= PASSES; pass++) {
			System.out.printf("=== PASS %d ===%n", pass);
			time(pass, seqStats, "seq", 1);
			time(pass, rndStats, "rnd", RND_STEP);
		}
		System.out.println("=== DONE ===");
		System.out.printf(Locale.US, "Ratio rnd/seq = %.2f%n", rndStats.mean() / seqStats.mean());
	}

	private void time(int pass, Stats stats, String desc, int step) {
		long start = System.nanoTime();
		int sum = run(step);
		double time = (double)(System.nanoTime() - start) / N;
		String ss = "";
		if (pass >= STABLE_PASS) {
			stats.add(time);
			ss = stats.toString();
		}
		System.out.printf(Locale.US, "%s: %6.3f ns per iteration %s (%d)%n", desc, time, ss, sum);
	}

	private int run(int step) {
		int sum = 0;
		int i = 0;
		do {
			sum += a[i];
			i = (i + step) & (N - 1);
		} while (i != 0);
		return sum;
	}
}
