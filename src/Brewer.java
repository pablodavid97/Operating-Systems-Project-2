
public class Brewer implements Runnable {
	/** Source version number. */
	private static final int VERSION = 1;

    // Current consumer state:

    /** Total consumed thus far. */
    private Order consumed = new Order();

    /** Reports on the total amount consumed thus far.
     * @return the amount consumed thus far.
     */
    public synchronized Order getConsumption() {
        return consumed;
    } // getConsumption(int[])

    /** Consumes the indicated amounts of resources.
     * @param result a vector of amounts, one for each grain.
     */
    private synchronized void consume(Order amount) {
        for (Grain g : Grain.values()) {
            consumed.change(g, amount.get(g));
        }
    } // consume(Order)

    /** Main loop.
     * Repeatedly generates random orders to random brokers.
     */
    public void run() {
        Order order = new Order();
        for (;;) {
            try {
                Thread.sleep(P2.randInt(500));
            } catch (InterruptedException e) {
                P2.setVerbose(true);
                P2.debug("interrupted while sleeping");
                return;
            }

            for (Grain g : Grain.values()) {
                order.set(g, P2.randInt(1,10));
            }
            Grain g = Grain.randChoice();
            P2.debug("requesting %s from the %s trader", order, g);
            try {
                P2.specialist(g).get(order);
            } catch (InterruptedException e) {
                P2.setVerbose(true);
                P2.debug("interrupted while requesting%n"
                        + "     %s from the %s trader",
                    order, g);
                return;
            }
            P2.debug("got %s from the %s trader", order, g);
            consume(order);
        }
    } // run()
} // Brewer
