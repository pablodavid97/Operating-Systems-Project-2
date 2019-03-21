import java.util.*;
import static java.lang.System.*;


public class P2 {
	/** Source version number. */
	private static final int VERSION = 1;

    /** Number of Brewers. */
    private static int brewerCount;

    /** Traders.  traders.get(g) is a Trader specializing in grain g. */
    private static EnumMap<Grain, Trader> traders;

    /** Brewers. */
    private static Brewer[] brewers;

    /** Brewer threads. */
    private static Thread[] brewerThreads;

    /** The unique supplier. */
    private static Supplier supplier;

    /** Flag to control debugging output. */
    private static boolean verbose = false;

    /** Turns debugging output on or off.
     * @param on if true, turn debugging on; otherwise turn it off.
     */
    public static void setVerbose(boolean on) {
        verbose = on;
    } // setVerbose(boolean)

    /** Returns the specialist for a given grain.
     * @param g the grain.
     * @return the Trader that specializes in grain g.
     */
    public static Trader specialist(Grain g) {
        return traders.get(g);
    } // specialist(int)

    /** If the debugging flag is on, prints a message, preceded by the
     * name of the current thread.  If it is off, does nothing.
     * @param message the message to print.
     */
    public static void debug(Object message) {
        if (verbose) {
            out.printf("%s: %s%n", Thread.currentThread().getName(), message);
        }
    } // debug(Object)

    /** If the debugging flag is on, prints a message, preceded by the
     * name of the current thread.  If it is off, does nothing.
     * @param format a printf-like format for the message
     * @param args arguments to the printf
     */
    public static void debug(String format, Object... args) {
        if (verbose) {
            String message = String.format(format, args);
            out.printf("%s: %s%n", Thread.currentThread().getName(), message);
        }
    } // debug(String,Object...)

    /** Random number generator. */
    private static Random rand;
        
    /** Utility to generate a random non-negative integer less than max.
     * @param max integer one greater than the largest possible result.
     * @return a non-negative integer strictly less than max.
     */
    public static int randInt(int max) {
        if (0 >= max) {
            throw new IllegalArgumentException("randInt");
        }
        return (rand.nextInt(max));
    } // randInt(int)

    /** Utility to generate a random integer between min and max (inclusive).
     * @param min the smallest possible result.
     * @param max the largest possible result.
     * @return an integer between min and max, inclusive.
     */
    public static int randInt(int min, int max) {
        if (min >= max) {
            throw new IllegalArgumentException("randInt");
        }
        return min + rand.nextInt(max - min + 1);
    } // randInt(int,int)

    /** Prints a usage message and terminates. */
    private static void usage() {
        err.println(
            "usage: P2 [-v][-r] brewerCount iterations");
        exit(1);
    } // usage()

    /** Main program for project 2.
     * @param args the command-line arguments.
     */
    public static void main(String[] args) {
        // Parse command-line arguments
        GetOpt options = new GetOpt("P2", args, "vr");
        int opt;
        while ((opt = options.nextOpt()) != -1) {
            switch (opt) {
            default:
                usage();
                break;
            case 'v':
                verbose = true;
                break;
            case 'r':
                rand = new Random(0);
                break;
            }
        }
        if (rand == null) {
            rand = new Random();
        }
        if (options.optind != args.length - 2) {
            usage();
        }
        brewerCount = Integer.parseInt(args[options.optind]);
        int iterations = Integer.parseInt(args[options.optind + 1]);

        // Create Traders
        traders = new EnumMap<Grain, Trader>(Grain.class);
        for (Grain g : Grain.values()) {
            traders.put(g, new TraderImpl(g));
        }

        // Create the unique Supplier
        supplier = new Supplier(iterations);

        Thread supplierThread = new Thread(supplier, "Supplier");

        brewers = new Brewer[brewerCount];
        brewerThreads = new Thread[brewerCount];
        for (int i = 0; i < brewerCount; i++) {
            brewers[i] = new Brewer();
            brewerThreads[i] = new Thread(brewers[i], "Brewer" + i);
        }

        // Start the threads running
        // They all have lower priority than the main thread so none of them
        // will run until we are done starting them all.
        supplierThread.setPriority(Thread.NORM_PRIORITY - 1);
        supplierThread.start();
        for (Thread t : brewerThreads) {
            t.setPriority(Thread.NORM_PRIORITY - 1);
            t.start();
        }

        // Wait for all the threads to finish
        try {
            // The supplier thread returns when it has completed the requested
            // number of iterations.
            supplierThread.join();

            // Wait three seconds to give the brewers a chance to finish
            // whatever they're doing, then kill them all off.
            Thread.sleep(3000);

            for (Thread t : brewerThreads) {
                System.out.println("Brewer Threads");
                t.interrupt();
                t.join();
            }
        } catch (InterruptedException e) {
            System.out.println("InterruptedException");
            e.printStackTrace();
        }

        // Display final state.
        out.printf("**** Program terminating%n");

        Order amt;
        Order balance = new Order();
        int produced = 0;
        int atTraders = 0;
        int consumed = 0;

        amt = supplier.getProduction();
        out.printf("Produced %s%n", amt);

        for (Grain g : Grain.values()) {
            int n = amt.get(g);
            balance.change(g, n);
            produced += n;
        }

        for (Grain g : Grain.values()) {
            amt = traders.get(g).getAmountOnHand();
            for (Grain c1 : Grain.values()) {
                int n = amt.get(c1);
                balance.change(c1, -n);
                atTraders += n;
            }
        }

        for (int i = 0; i < brewerCount; i++) {
            amt = brewers[i].getConsumption();
            out.printf("Brewer %d consumed %s%n", i, amt);
            for (Grain g : Grain.values()) {
                int n = amt.get(g);
                balance.change(g, -n);
                consumed += n;
            }
        }
        out.printf("Net excess (deficit) is %s%n", balance);
        out.printf(
            "Total: produced = %d, consumed = %d,"
                    + " remaining at traders = %d, net = %d%n",
            produced, consumed, atTraders, (produced - consumed - atTraders));
    } // main(String[])
} // P2
