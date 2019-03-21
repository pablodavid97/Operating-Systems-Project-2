
public interface Trader {
    // VERSION = 1;

    /** Reports on the total amount of resources held by this Trader.
     * @return an indication of the amount of each type of grain stocked by this
     * Trader.
     */
    Order getAmountOnHand();

    /** A request from a brewer.
     * The caller is blocked until the request can be completely filled.
     * @param order the number of bushels needed of each type of grain.
     * @throws InterruptedException if the current thread is interrupted while
     *            waiting for the order to be filled.
     */
    void get(Order order) throws InterruptedException;

    /** Responds to a swap request from another Trader.
     * The other Trader calls this method to request a trade of a particular
     * type of grain for this Trader's specialty.  Delays the caller until this
     * Trader can complete the swap.
     * @param what the type of grain the other Trader wants to swap.
     * @param amt the number of bushels to swap.
     * @throws InterruptedException if the current thread is interrupted while
     *            waiting for the swap to succeed.
     */
    void swap(Grain what, int amt) throws InterruptedException;

    /** Accepts a delivery from the supplier.
     * The supplier calls this method to deliver some of this Trader's
     * specialty grain.
     * @param amt the number of ounces of this Trader's specialty
     *               being delivered.
     */
    void deliver(int amt) throws InterruptedException;
} // Trader
