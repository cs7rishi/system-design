package Behavioural.Observer;

public class Client {
    public static void main(String[] args) {
        Order order = new Order();

        OrderObserver emailObserver =
                new EmailObserver();

        OrderObserver smsObserver =
                new SmsObserver();

        OrderObserver analyticsObserver =
                new AnalyticsObserver();

        order.addObserver(emailObserver);
        order.addObserver(smsObserver);
        order.addObserver(analyticsObserver);

        order.setStatus("SHIPPED");
    }
}
