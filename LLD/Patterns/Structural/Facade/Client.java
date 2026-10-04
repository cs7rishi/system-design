package Structural.Facade;

public class Client {

    public static void main(String[] args) {

        OrderFacade facade =
                new OrderFacade(
                        new InventoryService(),
                        new PaymentService(),
                        new ShippingService(),
                        new NotificationService()
                );

        facade.placeOrder(
                "C100",
                "P100",
                2,
                1000
        );
    }
}
