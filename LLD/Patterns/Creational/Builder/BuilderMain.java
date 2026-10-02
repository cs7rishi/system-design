package Creational.Builder;

public class BuilderMain {
    public static void main(String[] args) {
        User user = new User.Builder().name("Rishi").build();
        System.out.println(user);
    }
}
