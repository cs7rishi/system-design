package Structural.Facade;

public class OrderFacade {
    private final InventoryService inventoryService;
    private final PaymentService paymentService;
    private final ShippingService shippingService;
    private final NotificationService notificationService;

    public OrderFacade(InventoryService inventoryService, PaymentService paymentService, ShippingService shippingService, NotificationService notificationService) {
        this.inventoryService = inventoryService;
        this.paymentService = paymentService;
        this.shippingService = shippingService;
        this.notificationService = notificationService;
    }

    public void placeOrder(
            String customerId,
            String productId,
            int quantity,
            double amount
    ) {

        if (!inventoryService.isAvailable(
                productId,
                quantity
        )) {

            throw new RuntimeException(
                    "Product unavailable"
            );
        }

        inventoryService.reserve(
                productId,
                quantity
        );

        paymentService.processPayment(
                customerId,
                amount
        );

        shippingService.createShipment(
                productId,
                quantity
        );

        notificationService.sendConfirmation(
                customerId
        );
    }
}
