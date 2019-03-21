import java.util.*;


public class Order {
	/** Source version number. */
	private static final int VERSION = 1;

    /** The actual mapping. */
    private Map<Grain,Integer> amt;

    /** Creates a new Order with all amounts zero. */
    public Order() {
        amt = new EnumMap<Grain,Integer>(Grain.class);
        for (Grain g : Grain.values()) {
            amt.put(g, 0);
        }
    } // Order()

    /** Returns a readable version of this order.
     * @return a readable version of this order.
     */
    public String toString() {
        StringBuffer sb = new StringBuffer();
        String sep = "[";
        for (Grain g : Grain.values()) {
            sb.append(String.format("%s%d %s", sep, amt.get(g), g));
            sep = ", ";
        }
        sb.append("]");
        return sb.toString();
    } // toString()

    /** Gets the amount of "g" in this order.
     * @param g the grain
     * @return the amount of grain g
     */
    public int get(Grain g) {
        return amt.get(g).intValue();
    } // get(Grain)

    /** Sets the amount of "g" to "n".
     * @param g a grain
     * @param n the amount of grain g
     */
    public void set(Grain g, int n) {
        amt.put(g, n);
    } // set(Grain,int)

    /** Changes the mount of "g" by "diff".
     * @param g a grain
     * @param diff the change in the amount of grain g
     */
    public void change(Grain g, int diff) {
        amt.put(g, amt.get(g) + diff);
    } // change(Grain,int)

    /** Returns a copy of this Order.
     * @return a copy of this Order.
     */
    public Order copy() {
        Order result = new Order();
        for (Grain g : Grain.values()) {
            result.amt.put(g, amt.get(g));
        }
        return result;
    } // copy()
        
} // class Order
