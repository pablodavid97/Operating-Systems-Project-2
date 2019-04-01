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

    public void outOfStock(Grain g, int amt){
        if(g.equals(specialty)){
            deliver(Math.abs(amt));
        } else {
            try {
                swap(g, Math.abs(amt));
            }catch(Exception e){}
        }
    }


    public void get(Order order){
        int diff = 0;
        try {
            for (Grain g : bushels.keySet()) {
                
                diff = bushels.get(g) - order.get(g);
                if (diff < 0) {
                    synchronized (order) {
                        order.wait(1200);
                    }

                    synchronized (order) {
                        outOfStock(g, diff);
                        order.notifyAll();
                    }
                  //  outOfStock(g, diff);
                }
            }
        }catch(Exception e){e.printStackTrace();}
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
        synchronized (order){
            order.set(what, amt);
        }


        try {
            P2.specialist(what).get(order);
        } catch (InterruptedException e) {

        }

        setBushelAmount(what, qnty2);
    }

    public Order getAmountOnHand(){
        Order order = new Order();

        bushels.forEach((key, value) -> {
            order.set(key, value);
        });

        return order;
    }
}
