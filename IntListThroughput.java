import java.util.*;
import java.util.concurrent.Phaser;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Multi-threaded benchmark for implementations of simple {@link IntList} interface.
 * @author Roman Elizarov
 */
public class IntListThroughput {
	private static final int MIN_SIZE = 1000;
	private static final int MAX_SIZE = 10000000;
	private static final int DURATION_SECS = 20;
	private static final int AVG_FROM_SEC = 3;

	private static Phaser phaser;
	private static List<Test> tests;

	static class Test implements Runnable {
		final IntList list;
		final IntOp op;

		final AtomicInteger counter = new AtomicInteger();

		int size;
		int dummy; // to avoid HotSpot optimizing away iteration

		volatile boolean done;

		private Test(Class<?> listImplClass, IntOp op, int size) throws Exception {
			this.op = op;
			list = (IntList)listImplClass.newInstance();
			Random random = new Random(1);
			for	(int i = 0; i < size; i++)
				list.add(random.nextInt(1 << random.nextInt(31)));
		}

		public void run() {
			try {
				while (true) {
					done = false;
					phaser.arriveAndAwaitAdvance();
					test();
					phaser.arriveAndAwaitAdvance();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		private void test() {
			int counter = 0;
			while (!done) {
				dummy += runIteration(size);
				this.counter.lazySet(++counter);
			}
		}

		private int runIteration(int size) {
			int sum = 0;
			for (int i = 0; i < size; i++)
				sum += op.compute(list.getInt(i));
			return sum;
		}
	}

	private static List<String> getListImplClassNames() {
		List<String> list = new ArrayList<String>();
		for (Class c : IntList.class.getDeclaredClasses())
			list.add(c.getSimpleName());
		Collections.sort(list);
		return list;
	}

	private static Class<?> getListImplClass(String className) throws ClassNotFoundException {
		return Class.forName(IntList.class.getName() + "$" + className);
	}

	public static void main(String[] args) throws Exception {
		if (args.length != 4) {
			System.err.println("Usage: " + IntListIterationTiming.class + " <min-threads> <max-threads> <list-impl> <op>");
			System.err.println("Where: <min-threads> the minimal number of threads.");
			System.err.println("       <max-threads> the maximal number of threads.");
			System.err.println("       <list-impl>   is one of " + getListImplClassNames());
			System.err.println("       <op>          is one of " + Arrays.asList(IntOp.values()));
			return;
		}

		int minThreads = Integer.decode(args[0]);
		int maxThreads = Integer.decode(args[1]);
		Class<?> listImplClass = getListImplClass(args[2]);
		IntOp op = IntOp.valueOf(args[3].toUpperCase(Locale.US));

		phaser = new Phaser(1);
		tests = new ArrayList<Test>(maxThreads);

		for (int i = 0; i < maxThreads; i++)
			tests.add(new Test(listImplClass, op, MAX_SIZE));
		for (int i = 0; i < minThreads - 1; i++)
			startThread(i);

		for (int threads = minThreads; threads <= maxThreads; threads++) {
			startThread(threads - 1);
			for (int size = MIN_SIZE; size <= MAX_SIZE; size *= 10) {
				System.out.printf(Locale.US, "#%d [%,10d]: ", threads, size);
				launchTest(threads, size);
				Stats stats = gatherTestStats(threads, size);
				stopTest(threads);
				System.out.printf(Locale.US, " done %s x 10^9 ops/sec%n", stats);
			}
		}
	}

	private static void startThread(int i) {
		phaser.register();
		Thread t = new Thread(tests.get(i));
		t.setDaemon(true);
		t.start();
	}

	private static void launchTest(int threads, int size) {
		for (int i = 0; i < threads; i++)
			tests.get(i).size = size;
		phaser.arriveAndAwaitAdvance();
	}

	private static Stats gatherTestStats(int threads, int size) throws InterruptedException {
		Stats stats = new Stats();
		for (int sec = 0; sec < DURATION_SECS; sec++) {
			long prevCount = totalCount(threads);
			long prevTime = System.nanoTime();
			Thread.sleep(1000);
			long count = totalCount(threads);
			long time = System.nanoTime();
			if (sec >= AVG_FROM_SEC)
				stats.add((double)size * (count - prevCount) / (time - prevTime));
			System.out.print('.');
		}
		return stats;
	}

	private static long totalCount(int threads) {
		long total = 0;
		for (int i = 0; i < threads; i++)
			total += tests.get(i).counter.get();
		return total;
	}

	private static void stopTest(int threads) {
		for (int i = 0; i < threads; i++)
			tests.get(i).done = true;
		phaser.arriveAndAwaitAdvance();
	}
}
