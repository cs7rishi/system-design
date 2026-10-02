package Behavioural.Command;

public class Client {
    public static void main(String[] args) {
        Light light = new Light();

        Command command = new LightOnCommand(light);

        Remote remote = new Remote();

        remote.setCommand(command);
        remote.pressButton();
    }
}
