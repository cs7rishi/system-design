package Behavioural.Command;

public class LightOnCommand implements Command {

    private final Light light;

    LightOnCommand(Light light){
        this.light = light;
    }
    @Override
    public void execute() {
        light.turnOn();
    }
}
