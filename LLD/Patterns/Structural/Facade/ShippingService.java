package Structural.Facade;

public class ShippingService {

    public void createShipment(
            String productId,
            int quantity
    ) {
        System.out.println(
                "Shipment created"
        );
    }
}
