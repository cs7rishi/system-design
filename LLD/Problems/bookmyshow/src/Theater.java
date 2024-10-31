import java.util.ArrayList;

public class Theater {
    private static int idCounter = 0;
    private int id;
    private String name;
    private String location;
    private Integer capacity;

    private ArrayList<Show> shows;

    public Theater(String name, String location, Integer capacity) {
        this.id = ++idCounter;
        this.name = name;
        this.location = location;
        this.capacity = capacity;
        this.shows = new ArrayList<>();
    }
    public void updateShow(Show oldShow, Show newShow){
        shows.remove(oldShow);
        shows.add(newShow);
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

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public Integer getCapacity() {
        return capacity;
    }

    public void setCapacity(Integer capacity) {
        this.capacity = capacity;
    }

    public ArrayList<Show> getShows() {
        return shows;
    }

    public void setShows(ArrayList<Show> shows) {
        this.shows = shows;
    }
}
