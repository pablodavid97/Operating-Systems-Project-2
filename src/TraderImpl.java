/*
Adrian Duque y Pablo LLanes
Sistemas Operatuvos
Proyecto 2
 */

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class TraderImpl implements Trader {

    private Grain specialty;
    private Order currentStock;
    private Map<Grain, Integer> bushels;

    private static Map<Grain, Trader> tradersP;

    public TraderImpl(Grain grain) {
        this.specialty = grain;
        currentStock = new Order();
        bushels = new ConcurrentHashMap<>();
        tradersP = new ConcurrentHashMap<>();
        tradersP.put(specialty, this);

        for (Grain g : Grain.values()) {
            bushels.put(g, 0);
        }
        System.out.println("Specialist: " + specialty + " bushels: " + bushels);
    }

    @Override
    public Order getAmountOnHand() {
        Order order = currentStock;
        for (Grain g : bushels.keySet()) {
            order.set(g, currentStock.get(g));
        }
        return order;
    }

    @Override
    public void get(Order order) throws InterruptedException {
        System.out.println("Placing order " + order);

        ArrayList<Grain> grains = new ArrayList(Arrays.asList(Grain.values()));
        synchronized (order) {
            waitStock(order);
        }

        grains.remove(this.specialty);
        synchronized (order) {
            getStock(grains, order);
        }
        grains.add(this.specialty);
        for (Grain grain : grains) {
            updateStock(grain, -order.get(grain));
        }

    }

    @Override
    public synchronized void swap(Grain what, int amt) throws InterruptedException {
        synchronized (currentStock) {
            updateStock(this.specialty, -amt);
            updateStock(what, amt);
        }
        notifyAll();
    }

    @Override
    public synchronized void deliver(int amt) throws InterruptedException {
        updateStock(this.specialty, amt);
        notifyAll();
    }


    // Metodo sincrono que se encarga de hacer el update al Stock actual, accede al metodo change de la clase Order
    private synchronized void updateStock(Grain grain, int amt) {
        synchronized (currentStock) {
            currentStock.change(grain, amt);
        }
        notifyAll();

    }

    /*Metodo sincrono que hace que los threads esperen mientras el stock sea limitado o nulo*/
    public synchronized void waitStock(Order order) throws InterruptedException {

        while (currentStock.get(this.specialty) < order.get(this.specialty)) {
            wait(100);
        }
    }


    /*Metodo sincrono que se encabrga de comprobar la disponibilidad de stock, hacer un wait en caso de que no haya
       y si hay, realiz los swaps pertinentes*/
    public synchronized void getStock(ArrayList<Grain> grains, Order order) throws InterruptedException {
        for (Grain grain : grains) {
            int diff = order.get(grain) - currentStock.get(grain);

            while (currentStock.get(grain) < order.get(grain)) {

                if (currentStock.get(specialty) >= diff) {
                    P2.specialist(grain).swap(specialty, diff);
                    updateStock(grain, diff);
                    updateStock(specialty, -diff);
                } else {
                    wait(100);
                }
            }

        }
    }

}
