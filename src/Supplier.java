
import java.util.logging.Level;
import java.util.logging.Logger;


public class Supplier implements Runnable {
	/** Source version number. */
	private static final int VERSION = 1;

    /** Number of times to iterate before terminating. */
    private int iterations;

    /** Total delivered thus far */
    private Order delivered = new Order();

    /** Creates a new Supplier.
     * @param iterations the number of times to iterate before terminating.
     */
    public Supplier(int iterations) {
        this.iterations = iterations;
    } // Supplier(int)

    /** Indicates the amount of each grain delivered.
     * @return an indication of the total amount of each grain delivered
     *         to traders thus far.
     */
    public synchronized Order getProduction() {
        return delivered;
    } // getProduction()

    /** Main loop.
     * Repeatedly generates orders to random brokers.
     */
    public void run() {
        for (int i = 0; i < iterations; i++) {
            try {
                Thread.sleep(P2.randInt(100));
            } catch (Exception e) {
                e.printStackTrace();
            }
            Grain g = Grain.randChoice();
            int amount = P2.randInt(1,10);
            delivered.change(g, amount);
            P2.debug("delivering %d %s of %s to the %s broker",
                amount, (amount == 1 ? "bushel" : "bushels"), g, g);
            try {
                P2.specialist(g).deliver(amount);
            } catch (InterruptedException ex) {
                Logger.getLogger(Supplier.class.getName()).log(Level.SEVERE, null, ex);
            }

        }
        P2.debug("Supplier shutting down ...");
    } // run()
} // Supplier
