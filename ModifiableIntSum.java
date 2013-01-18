import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * @author Roman Elizarov
 */
public class ModifiableIntSum {
    private static final int PASSES = 5;

    private static final int N = 100000;
    private static final int REPS = 1000;

    public static void main(String[] args) {
        ModifiableIntSum mis = new ModifiableIntSum();
        for (int pass = 1; pass <= PASSES; pass++) {
            System.out.printf("----- PASS %d -----%n", pass);
            long startTime = System.currentTimeMillis();
            int sum = mis.run();
            System.out.printf("Done %d ms (sum=%d)%n", System.currentTimeMillis() - startTime, sum);
        }
    }

    private final List<ModifiableIntContainer> list = new ArrayList<>();

    private ModifiableIntSum() {
        Random r = new Random(1);
        for (int i = 0; i < N; i++)
            list.add(new ModifiableIntContainer(new ModifiableInt(r.nextInt())));
    }

    private int run() {
        int sum = 0;
        for (int rep = 0; rep < REPS; rep++)
            sum += runIteration();
        return sum;
    }

    private int runIteration() {
        int sum = 0;
        for (ModifiableIntContainer container : list)
            sum += container.getC().getValue();
        return sum;
    }
}
