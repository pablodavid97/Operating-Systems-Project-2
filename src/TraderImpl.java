import java.util.*;

public class TraderImpl implements Trader {
    private Grain specialty;
    private Map<Grain, Integer> bushels;

    TraderImpl(Grain specialty){
        this.specialty = specialty;
        bushels = new EnumMap(Grain.class);

        for(Grain g : Grain.values()){
            bushels.put(g, 0);
        }
    }

    public synchronized void setBushelAmount(Grain g, int amt){
        bushels.put(g, new Integer(amt));
    }

    public synchronized void outOfStock(Grain g, int amt){
        if(g.equals(specialty)){
            deliver(Math.abs(amt));
        } else {
            swap(g, Math.abs(amt));
        }
    }


    public void get(Order order){
        int diff = 0;
        for(Grain g : bushels.keySet()){
            diff = bushels.get(g) - order.get(g);
            if(diff < 0){
                outOfStock(g, diff);
            }
        }
    }

    public void deliver(int amt){
        int qnty = bushels.get(specialty) + amt;
        setBushelAmount(specialty, qnty);
    }

    public void swap(Grain what, int amt){
        int qnty1 = bushels.get(specialty) - amt;
        int qnty2 = bushels.get(what) + amt;

        setBushelAmount(specialty, qnty1);
        Order order = new Order();
        order.set(what, amt);

        try {
            P2.specialist(what).get(order);
        } catch (InterruptedException e) {

        }

        requestSwap(what, qnty2);
    }

    private synchronized void requestSwap(Grain g, int amt){
        Order order = new Order();
        order.set(g, amt);
        try {
            P2.specialist(g).get(order);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        try {
            wait();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        setBushelAmount(g, amt);
    }

    public Order getAmountOnHand(){
        Order order = new Order();

        bushels.forEach((key, value) -> {
            order.set(key, value);
        });

        return order;
    }
}
