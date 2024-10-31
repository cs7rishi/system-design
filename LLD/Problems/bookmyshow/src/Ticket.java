import java.util.Date;

public class Ticket {
    private static int idCounter = 0;
    private int id;
    private String ownerName;
    private Date bookingTime;
    private int noOFSeats;
    private Show bookedShow;

    public Ticket(String ownerName, Date bookingTime, int noOFSeats, Show bookedShow) {
        this.id = ++idCounter;
        this.ownerName = ownerName;
        this.bookingTime = bookingTime;
        this.noOFSeats = noOFSeats;
        this.bookedShow = bookedShow;
    }

    public String getTicketInfo() {
        return "Ticket ID: " + id
            + "\nOwner Name: " + ownerName
            + "\nBooking Time: " + bookingTime
            + "\nNumber of Seats: " + noOFSeats
            + "\nBooked Show: " + bookedShow.getMovie().getName() + " - " + bookedShow.getShowTime();
    }

    public int cancelTicker(){
        this.ownerName = null;
        this.noOFSeats = 0;
        this.bookedShow = null;
        System.out.println("Ticket got cancelled successfully");
        return 1;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public void setOwnerName(String ownerName) {
        this.ownerName = ownerName;
    }

    public Date getBookingTime() {
        return bookingTime;
    }

    public void setBookingTime(Date bookingTime) {
        this.bookingTime = bookingTime;
    }

    public int getNoOFSeats() {
        return noOFSeats;
    }

    public void setNoOFSeats(int noOFSeats) {
        this.noOFSeats = noOFSeats;
    }

    public Show getBookedShow() {
        return bookedShow;
    }

    public void setBookedShow(Show bookedShow) {
        this.bookedShow = bookedShow;
    }
}
