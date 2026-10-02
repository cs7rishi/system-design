package Behavioural.Observer;

public class AnalyticsObserver implements OrderObserver {
    @Override
    public void update(Order order) {
        System.out.println(
                "Analytics updated for status = " + order.getStatus()
        );

    }
}
