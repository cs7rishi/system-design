package Behavioural.Iterator;

public class Client {
    public static void main(String[] args) {
        Playlist playlist =
                new Playlist(
                        new String[]{
                                "Song A",
                                "Song B",
                                "Song C"
                        }
                );

        for (String song : playlist) {
            System.out.println(song);
        }
    }

}
