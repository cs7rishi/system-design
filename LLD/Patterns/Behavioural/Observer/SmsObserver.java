package Behavioural.Observer;

public class SmsObserver implements OrderObserver {
    @Override
    public void update(Order order) {
        System.out.println(
                "SMS notification: order status = " + order.getStatus()
        );
    }
}
