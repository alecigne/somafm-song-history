package net.lecigne.somafm.history.application.ports.out;

import java.util.List;
import net.lecigne.somafm.recentlib.Song;

public interface SongRepository {
  long countSongs();
  List<Song> getSongs(int page, int size);
}
