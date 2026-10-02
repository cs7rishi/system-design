package Behavioural.State;

public class Client {
    public static void main(String[] args) {
        Order order = new Order();

        order.pay();

        order.ship();

        order.cancel();
    }
}
