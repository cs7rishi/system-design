package Behavioural.Iterator;

import java.util.Iterator;
import java.util.NoSuchElementException;

public class PlaylistIterator implements Iterator<String>{

    private final String[] songs;
    private int position;

    PlaylistIterator(String[] songs){
        this.songs = songs;
        position = 0;
    }

    @Override
    public boolean hasNext() {
        return position < songs.length;
    }

    @Override
    public String next() {
        if(!hasNext()){
            throw new NoSuchElementException();
        }
        return songs[position++];
    }
}
