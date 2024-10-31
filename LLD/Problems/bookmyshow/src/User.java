public abstract class User {
    private static int idCounter = 0;
    private int id;
    private String name;

    public User(String name) {
        this.id = ++idCounter;
        this.name = name;
    }

    public static int getIdCounter() {
        return idCounter;
    }

    public static void setIdCounter(int idCounter) {
        User.idCounter = idCounter;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
