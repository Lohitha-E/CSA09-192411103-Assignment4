/**
 * Task 1 - Alternative Concept: time to dispatch one event to the right code for k buttons.
 *   chain    : if (cmd.equals("Cmd0")) ... else if (cmd.equals("Cmd1")) ...   up to k comparisons
 *   listener : the button already holds its own listener object                  O(1)
 * The chain is measured for the LAST button (worst case). Run it yourself and use your own numbers.
 */
public class DispatchBench {
    interface Handler { void run(); }

    static volatile int sink;

    public static void main(String[] args) {
        int[] ks = {3, 10, 100, 1000};
        System.out.printf("%6s %18s %18s%n", "k", "if-else chain (ns)", "own listener (ns)");
        for (int k : ks) {
            String[] names = new String[k];
            Handler[] handlers = new Handler[k];
            for (int i = 0; i < k; i++) { names[i] = "Cmd" + i; handlers[i] = () -> sink++; }
            String command = new String("Cmd" + (k - 1));       // equal text, different object (like getActionCommand)
            int reps = 2_000_000;

            for (int w = 0; w < 3; w++) { chain(names, command, reps); listener(handlers, k - 1, reps); }   // warm-up
            long t0 = System.nanoTime(); chain(names, command, reps);
            long t1 = System.nanoTime(); listener(handlers, k - 1, reps);
            long t2 = System.nanoTime();
            System.out.printf("%6d %18.2f %18.2f%n", k, (t1 - t0) / (double) reps, (t2 - t1) / (double) reps);
        }
        System.out.println("Space: k listener objects, each a few tens of bytes; the chain needs one object.");
    }

    static void chain(String[] names, String command, int reps) {
        for (int r = 0; r < reps; r++) {
            for (int i = 0; i < names.length; i++) {
                if (names[i].equals(command)) { sink++; break; }
            }
        }
    }

    static void listener(Handler[] handlers, int index, int reps) {
        for (int r = 0; r < reps; r++) handlers[index].run();
    }
}
