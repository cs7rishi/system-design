package Structural.Facade;

public class InventoryService {

    public boolean isAvailable(
            String productId,
            int quantity
    ) {
        System.out.println(
                "Checking inventory"
        );

        return true;
    }

    public void reserve(
            String productId,
            int quantity
    ) {
        System.out.println(
                "Inventory reserved"
        );
    }
}
