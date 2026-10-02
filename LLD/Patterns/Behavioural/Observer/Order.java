package Behavioural.Observer;

import java.util.ArrayList;
import java.util.List;
import java.util.Observer;

public class Order {
    private final List<OrderObserver> orderObservers
            = new ArrayList<>();
    private String status;

    public void setStatus(String status) {
        this.status = status;
        notifyObservers();
    }

    public String getStatus() {
        return status;
    }

    public void addObserver(OrderObserver orderObserver){
        orderObservers.add(orderObserver);
    }

    public void removeObserver(OrderObserver orderObserver){
        orderObservers.remove(orderObserver);
    }
    private void notifyObservers(){
        for(OrderObserver orderObserver : orderObservers){
            orderObserver.update(this);
        }
    }

}
