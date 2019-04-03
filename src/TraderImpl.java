import org.omg.CORBA.ORB;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class TraderImpl implements Trader {
    private Grain specialty;
    private Map<Grain, Integer> bushels = new ConcurrentHashMap<>();//new EnumMap(Grain.class);

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

    public synchronized void setBushelAmount(Grain g, int amt) {
        bushels.put(g, amt);
        notifyAll();
    }


    public void get(Order order) throws InterruptedException {
        int diff = 0;
        Order remainingStock = getAmountOnHand();

        for (Grain g : bushels.keySet()) {

            diff = remainingStock.get(g) - order.get(g);

           // synchronized (bushels){
                while (getAmountOnHand().get(g)-order.get(g)<=0) {
                    {
                        diff=getAmountOnHand().get(g)-order.get(g);
                    }
                }
            //}

            if (diff < 0) {
                synchronized (bushels) {
                    System.out.println("Trader " + specialty + " Out of stock");
                    //order.wait(1000);
                    //wait();

                    for(Grain g1: tradersP.keySet())
                    {
                        if(g.equals(tradersP.keySet().iterator()))
                        {
                            if (g.equals(specialty)) {
                                wait();
                            } else {
                                try {
                                    System.out.println("Swapping.....");
                                    P2.specialist(g).swap(specialty, Math.abs(diff));
                                } catch (Exception e) {}
                            }

                        }
                    }
                    setBushelAmount(g, 0);
                }
            } else {
                synchronized (bushels){
                   // wait();
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
                //bushels.wait(1000);
                deliver(Math.abs(qnty1));
            }
        }

        setBushelAmount(specialty, qnty1);

        int qnty2 = bushels.get(what) + amt;
        setBushelAmount(what, qnty2);

        notifyAll();

    }

    public Order getAmountOnHand() {
        Order order = new Order();

        //bushels.forEach(order::set);
        for(Grain g: bushels.keySet())
        {
            order.set(g,bushels.get(g).intValue());
        }

        System.out.println("..............................."+ specialty +order);
        return order;
    }
}
