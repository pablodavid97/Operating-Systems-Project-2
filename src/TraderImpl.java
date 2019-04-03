import org.omg.CORBA.ORB;

import java.util.*;

public class TraderImpl implements Trader {
    private Grain specialty;
    private Map<Grain, Integer> bushels = new EnumMap(Grain.class);
    private static Map<Grain,Trader> tradersP= new HashMap<>();
//    private  Map<Grain,Integer> quantity = new HashMap<>();

    TraderImpl(Grain specialty) {
        this.specialty = specialty;
        //bushels = new EnumMap(Grain.class);
       // bushels= new HashMap<>();

        tradersP.put(specialty,this);

        for (Grain g : Grain.values()) {
            bushels.put(g, 0);
//            quantity.put(g,0);
        }
        System.out.println("Specialist: " + specialty + " bushels: " +bushels);
//        System.out.println("aquantity: "+quantity);
    }

    public void setBushelAmount(Grain g, int amt) {
        bushels.put(g, amt);
    }

    public void outOfStock(Grain g, int amt) throws InterruptedException {
        for(Grain g1: tradersP.keySet())
        {
            if(g.equals(tradersP.keySet().iterator()))
            {
                if (g.equals(specialty)) {
//                    wait(100);
                    deliver(Math.abs(amt));
//                    notify();
                } else {
                    try {
                        System.out.println("Swapping.....");
//                        wait(100);
                        P2.specialist(g).swap(specialty, Math.abs(amt));
//                        notify();
                    } catch (Exception e) {}
                }

            }
        }

    }


    public void get(Order order) throws InterruptedException {
        int diff = 0;
        Order remainingStock = getAmountOnHand();

        for (Grain g : bushels.keySet()) {

            diff = remainingStock.get(g) - order.get(g);

            if (diff < 0) {
                synchronized (order) {
                    System.out.println("Trader " + specialty + " Out of stock");
                    order.wait(1000);
                    outOfStock(g, diff);
                    setBushelAmount(g, 0);
                }
            } else {
                synchronized (order){
                    order.wait(10);
                    setBushelAmount(g, remainingStock.get(g) - order.get(g));
                }
            }
        }

    }

    public void deliver(int amt) throws InterruptedException {
        int qnty = bushels.get(specialty) + amt;
        setBushelAmount(specialty, qnty);
    }

    public void swap(Grain what, int amt) throws InterruptedException {
        int qnty1 = bushels.get(specialty) - amt;

        if(qnty1 < 0){
            synchronized (bushels) {
                bushels.wait(1000);
                deliver(Math.abs(qnty1));
            }
        }

        setBushelAmount(specialty, qnty1);

        int qnty2 = bushels.get(what) + amt;
        setBushelAmount(what, qnty2);

    }

    public Order getAmountOnHand() {
        Order order = new Order();

        //bushels.forEach(order::set);
        for(Grain g: bushels.keySet())
        {
            order.set(g,bushels.get(g).intValue());
        }

        System.out.println("..............................."+order);
        return order;
    }
}
