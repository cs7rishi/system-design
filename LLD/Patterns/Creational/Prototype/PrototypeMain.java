package Creational.Prototype;

public class PrototypeMain {
    public static void main(String[] args) {

        GameCharacter warrior1 = new GameCharacter("Warrior", 100, 80, 70);
        GameCharacter warrior2 = warrior1.copy();

        System.out.println(warrior1);
        System.out.println(warrior2);

        System.out.println(warrior1 == warrior2);
    }
}
