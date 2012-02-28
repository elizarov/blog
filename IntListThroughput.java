import java.util.*;
import java.util.concurrent.CyclicBarrier;
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

	static class Test implements Runnable {
		final IntList list;
		final CyclicBarrier startBarrier;
		final CyclicBarrier stopBarrier;

		final AtomicInteger counter = new AtomicInteger();

		int size;
		int dummy; // to avoid HotSpot optimizing away iteration

		volatile boolean done;

		private Test(Class<?> implClass, int size, CyclicBarrier startBarrier, CyclicBarrier stopBarrier) throws Exception {
			this.startBarrier = startBarrier;
			this.stopBarrier = stopBarrier;
			list = (IntList)implClass.newInstance();
			Random random = new Random(1);
			for	(int i = 0; i < size; i++)
				list.add(random.nextInt());
		}

		public void run() {
			try {
				while (true) {
					done = false;
					startBarrier.await();
					test();
					stopBarrier.await();
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
				sum += list.getInt(i);
			return sum;
		}
	}

	private static Class<?> getImplClass(String className) throws ClassNotFoundException {
		return Class.forName(IntList.class.getName() + "$" + className);
	}

	public static void main(String[] args) throws Exception {
		if (args.length != 2) {
			System.err.println("Usage: " + IntListIterationTiming.class + " <threads> <impl>");
			System.err.println("Where: <threads> the number of threads.");
			System.err.println("       <impl>    is one of: ");
			for (Class c : IntList.class.getDeclaredClasses())
				System.err.println("                    " + c.getSimpleName());
			return;
		}

		int threads = Integer.decode(args[0]);
		Class<?> implClass = getImplClass(args[1]);

		CyclicBarrier startBarrier = new CyclicBarrier(threads + 1);
		CyclicBarrier stopBarrier = new CyclicBarrier(threads + 1);
		List<Test> tests = new ArrayList<Test>(threads);
		for (int i = 0; i < threads; i++) {
			Test test = new Test(implClass, MAX_SIZE, startBarrier, stopBarrier);
			Thread thread = new Thread(test);
			thread.setDaemon(true);
			thread.start();
			tests.add(test);
		}

		Map<Integer, Double> results = new HashMap<Integer, Double>();
		for (int size = MIN_SIZE; size <= MAX_SIZE; size *= 10) {
			System.out.printf("--- Running test for size %,d: ", size);
			for (Test test : tests)
				test.size = size;
			startBarrier.await();

			long from = 0;
			long to = 0;
			for (int sec = 0; sec < DURATION_SECS; sec++) {
				Thread.sleep(1000);
				long total = 0;
				for (int i = 0; i < threads; i++)
					total += tests.get(i).counter.get();
				if (sec == AVG_FROM_SEC)
					from = total;
				to = total;
				System.out.print('.');
			}
			double result = (to - from) * size / 1e9 / (DURATION_SECS - AVG_FROM_SEC);
			System.out.printf(Locale.US, " done %.2f x 10^9 ops/sec%n", result);
			results.put(size, result);

			for (Test test : tests)
				test.done = true;
			stopBarrier.await();
		}

		for (int size = MIN_SIZE; size <= MAX_SIZE; size *= 10)
			System.out.printf(Locale.US, "[%,10d] Average throughput: %.2f x 10^9 ops/sec%n", size, results.get(size));

	}
}
