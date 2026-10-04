package Structural.Facade;

public class NotificationService {

    public void sendConfirmation(
            String customerId
    ) {
        System.out.println(
                "Order confirmation sent"
        );
    }
}
