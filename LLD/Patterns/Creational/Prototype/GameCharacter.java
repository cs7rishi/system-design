package Creational.Prototype;

public class GameCharacter {
    private String name;
    private int health;
    private int attach;
    private int defence;

    public GameCharacter(String name, int health, int attach, int defence) {
        this.name = name;
        this.health = health;
        this.attach = attach;
        this.defence = defence;
    }

    public GameCharacter copy(){
        return new GameCharacter(this.name, this.health, this.attach, this.defence);
    }

    @Override
    public String toString() {
        return "GameCharacter{" +
                "name='" + name + '\'' +
                ", health=" + health +
                ", attach=" + attach +
                ", defence=" + defence +
                '}';
    }
}
