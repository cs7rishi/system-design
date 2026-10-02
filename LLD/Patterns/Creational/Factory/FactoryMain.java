package Creational.Factory;

public class FactoryMain {
    public static void main(String[] args) {
        Notification notification = NotificationFactory.createNotification("sms");
        notification.send();

        Notification notification1 = NotificationFactory.createNotification("email");
        notification1.send();;

        Notification notification2 = NotificationFactory.createNotification("some");
        notification2.send();
    }
}
