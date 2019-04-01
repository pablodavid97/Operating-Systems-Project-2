import org.omg.CORBA.ORB;

import java.util.*;

public class TraderImpl implements Trader {
    private Grain specialty;
    private Map<Grain, Integer> bushels;

    TraderImpl(Grain specialty) {
        this.specialty = specialty;
        bushels = new EnumMap(Grain.class);

        for (Grain g : Grain.values()) {
            bushels.put(g, 0);
        }
    }

    public void setBushelAmount(Grain g, int amt) {
        bushels.put(g, new Integer(amt));
    }

    public void outOfStock(Grain g, int amt) throws InterruptedException {
        if (g.equals(specialty)) {
            deliver(Math.abs(amt));
        } else {
            try {
                swap(g, Math.abs(amt));
            } catch (Exception e) {}
        }
    }


    public void get(Order order) throws InterruptedException {
        int diff = 0;
        for (Grain g : bushels.keySet()) {

            diff = bushels.get(g) - order.get(g);
            if (diff < 0) {
                synchronized (order) {
                    order.wait(1000);

                }

                synchronized (order) {
                    outOfStock(g, diff);
                    order.notify();
                }
            }
        }

    }

    public synchronized void deliver(int amt) throws InterruptedException {
        int qnty = bushels.get(specialty) + amt;
        wait(5);
        setBushelAmount(specialty, qnty);
        notify();
    }

    public void swap(Grain what, int amt) throws InterruptedException {
        int qnty1 = bushels.get(specialty) - amt;
        int qnty2 = bushels.get(what) + amt;

        setBushelAmount(specialty, qnty1);
        Order order = new Order();

        synchronized (order) {
            wait(5);
            order.set(what, amt);
            notify();
        }

        synchronized (order) {
            wait(5);
            try {
                P2.specialist(what).get(order);
            } catch (InterruptedException e) {e.printStackTrace();}
            setBushelAmount(what, qnty2);
            notifyAll();
        }
    }

    public Order getAmountOnHand() {
        Order order = new Order();

        bushels.forEach(order::set);

        return order;
    }
}
