package Behavioural.Iterator;

import java.util.Iterator;

public class Playlist implements Iterable<String>{
    private final String[] songs;

    Playlist(String[] songs){
        this.songs = songs;
    }

    @Override
    public Iterator<String> iterator() {
        return new PlaylistIterator(songs);
    }
}
