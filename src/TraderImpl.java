import org.omg.CORBA.ORB;

import java.util.*;

public class TraderImpl implements Trader {
    private Grain specialty;
    private Map<Grain, Integer> bushels = new EnumMap(Grain.class);
    private Map<Grain,Trader> tradersP= new HashMap<>();
    private  Map<Grain,Integer> quantity = new HashMap<>();

    TraderImpl(Grain specialty) {
        this.specialty = specialty;
        //bushels = new EnumMap(Grain.class);
       // bushels= new HashMap<>();

        tradersP.put(specialty,this);

        for (Grain g : Grain.values()) {
            bushels.put(g, 0);
            quantity.put(g,0);
        }
        System.out.println("bushels: " +bushels);
        System.out.println("aquantity: "+quantity);
    }

    public void setBushelAmount(Grain g, int amt) {
        bushels.put(g, new Integer(amt));
    }

    public synchronized void outOfStock(Grain g, int amt) throws InterruptedException {
        for(Grain g1: tradersP.keySet())
        {
            if(g.equals(tradersP.keySet().iterator()))
            {
                if (g.equals(specialty)) {
                    wait(10);
                    deliver(Math.abs(amt));
                    notify();
                } else {
                    try {
                        System.out.println("Swapping.....");
                        wait(10);
                        P2.specialist(g).swap(specialty, Math.abs(amt));
                        notify();
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
                    order.wait(1000);
                    outOfStock(g, diff);
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

//        int maxTime=0;
//        while (getAmountOnHand().get(what)<amt) {
//            wait();
//            if(maxTime++>100){
//                System.out.println("Max wait");
//
//                return;
//            }
//
//        }
        if(qnty1 < 0){
            deliver(Math.abs(qnty1));
        }
        Order order =getAmountOnHand();

        synchronized (order) {
            wait(5);
            order.set(what, qnty1);
            notify();
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
