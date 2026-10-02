package Creational.Factory;

class NotificationFactory {
    public static Notification createNotification(String type) {
        if (type.equalsIgnoreCase("sms")) {
            return new SMSNotification();
        }
        if (type.equalsIgnoreCase("email")) {
            return new EmailNotification();
        }

        throw new IllegalArgumentException("Invalid notification type");
    }
}
