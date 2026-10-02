package Behavioural.Observer;

public class EmailObserver implements OrderObserver {
    @Override
    public void update(Order order) {
        System.out.println(
                "Email notification: order status = " + order.getStatus()
        );
    }
}
