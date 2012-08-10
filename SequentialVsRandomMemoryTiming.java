import java.util.*;
import java.util.concurrent.*;

/**
 * Compare sequential vs random memory access of 32 bits ints on the same simple code.
 *
 * @author Roman Elizarov
 */
public class SequentialVsRandomMemoryTiming {
	private static final int N = 1 << 25;
	private static final int RND_STEP = (int)((Math.sqrt(5) - 1) / 2 * N) | 1;

	private static final int REP = 10;
	private static final int STABLE_PASS = 3;
	private static final int PASSES = 20;

	private static final boolean SMALL_STEPS = Boolean.getBoolean("smallSteps");
	private static final int MAX_SMALL_STEP = 25;

	private final int[] a = new int[N];

	AnyStep rnd = new AnyStep(RND_STEP);
	AnyStep seq = new AnyStep(1);

	private final Test[] pos = new Test[MAX_SMALL_STEP + 1];
	private final Test[] neg = new Test[MAX_SMALL_STEP + 1];

	private final Test ss0 = new SimpleSum();
	private final Test sp2 = new Split2();
	private final Test sp3 = new Split3();
	private final Test par = new Parallel();

	private final static ForkJoinPool FJP = new ForkJoinPool(4);
	private static final int SEQ_LIMIT = N / 4;

	public static void main(String[] args) {
		new SequentialVsRandomMemoryTiming().go();
	}

	public SequentialVsRandomMemoryTiming() {
		Random r = new Random(1);
		for (int i = 0; i < N; i++)
			a[i] = r.nextInt();
	}

	private void go() {
		assert (N & (N - 1)) == 0 : "N must be power of 2";
		for (int pass = 1; pass <= PASSES; pass++) {
			System.out.printf("=== PASS %d ===%n", pass);
			time(pass, "rnd  ", rnd);
			time(pass, "seq  ", seq);
			if (SMALL_STEPS) {
				for (int step = 3; step <= MAX_SMALL_STEP; step += 2) {
					if (pos[step] == null)
						pos[step] = new AnyStep(step);
					time(pass, String.format("+%02d", step), pos[step]);
				}
				for (int step = 1; step <= MAX_SMALL_STEP; step += 2) {
					if (neg[step] == null)
						neg[step] = new AnyStep(-step);
					time(pass, String.format("-%02d", step), neg[-step]);
				}
			}
			time(pass, "ss0  ", ss0);
			time(pass, "sp2  ", sp2);
			time(pass, "sp3  ", sp3);
			time(pass, "par  ", par);
		}
		System.out.println("=== DONE ===");
		System.out.printf(Locale.US, "Ratio rnd/seq = %.2f%n", rnd.stats.mean() / seq.stats.mean());
	}

	private void time(int pass, String desc, Test test) {
		long start = System.nanoTime();
		int sum = runRep(test);
		double time = (double)(System.nanoTime() - start) / (N * REP);
		String ss = "";
		double avgTime = time;
		if (pass >= STABLE_PASS) {
			test.stats.add(time);
			ss = test.stats.toString();
			avgTime = test.stats.mean();
		}
		System.out.printf(Locale.US, "%s: %6.3f ns per iteration %s == %.2f GB/s (%d)%n",
			desc, time, ss, 4 / avgTime, sum);
	}

	private int runRep(Test test) {
		int sum = 0;
		for (int i = 0; i < REP; i++)
			sum += test.run();
		return sum;
	}

	abstract class Test {
		final Stats stats = new Stats();
		final int[] a = SequentialVsRandomMemoryTiming.this.a;

		public abstract int run();
	}

	class AnyStep extends Test {
		final int step;

		AnyStep(int step) {
			this.step = step;
		}

		public int run() {
			int sum = 0;
			int i = 0;
			do {
				sum += a[i];
				i = (i + step) & (N - 1);
			} while (i != 0);
			return sum;
		}
	}

	class SimpleSum extends Test {
		public int run() {
			int sum = 0;
			for (int i = 0; i < N; i++)
				sum += a[i];
			return sum;
		}
	}

	class Split2 extends Test {
		public int run() {
			int sum0 = 0;
			int sum1 = 0;
			for (int i = 0; i < N / 2; i++) {
				sum0 += a[i];
				sum1 += a[i + N / 2];
			}
			return sum0 + sum1;
		}
	}

	class Split3 extends Test {
		public int run() {
			int sum0 = 0;
			int sum1 = 0;
			int sum2 = 0;
			for (int i = 0; i < N / 3; i++) {
				sum0 += a[i];
				sum1 += a[i + N / 3];
				sum1 += a[i + N / 3 * 2];
			}
			for (int i = N / 3 * 3; i < N; i++)
				sum0 += a[i];
			return sum0 + sum1 + sum2;
		}
	}

	class Parallel extends Test {
		public int run() {
			SumTask task = new SumTask(0, N);
			FJP.invoke(task);
			return task.sum;
		}
	}

	class SumTask extends RecursiveAction {
		final int from;
		final int to;
		final int[] a = SequentialVsRandomMemoryTiming.this.a;

		int sum;

		public SumTask(int from, int to) {
			this.from = from;
			this.to = to;
		}

		@Override
		protected void compute() {
			if (to - from <= SEQ_LIMIT) {
				int sum = 0;
				for (int i = from; i < to; i++)
					sum += a[i];
				this.sum = sum;
			} else {
				int mid = (from + to) / 2;
				SumTask lo = new SumTask(from, mid);
				SumTask hi = new SumTask(mid, to);
				invokeAll(lo, hi);
				sum = lo.sum + hi.sum;
			}
		}
	}
}
