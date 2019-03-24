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

    public void get(Order order){
        int diff = 0;
        for(Grain g : bushels.keySet()){
            diff = bushels.get(g) - order.get(g);
            if(diff < 0){
                if(g.equals(specialty)){
                    deliver(Math.abs(diff));
                } else {
                    swap(g, Math.abs(diff));
                }
            }
        }
    }

    public void deliver(int amt){
        int qnty = bushels.get(specialty) + amt;
        bushels.put(specialty, new Integer(qnty));
    }

    public void swap(Grain what, int amt){
        int qnty1 = bushels.get(specialty) - amt;
        int qnty2 = bushels.get(what) + amt;

        bushels.put(specialty, new Integer(qnty1));
        Order order = new Order();
        order.set(what, amt);

        try {
            P2.specialist(what).get(order);
        } catch (InterruptedException e) {

        }
        bushels.put(what, new Integer(qnty2));
    }

    public Order getAmountOnHand(){
        Order order = new Order();

        bushels.forEach((key, value) -> {
            order.set(key, value);
        });

        return order;
    }
}