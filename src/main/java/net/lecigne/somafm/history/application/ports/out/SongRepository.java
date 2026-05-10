package net.lecigne.somafm.history.application.ports.out;

import java.util.List;
import java.util.Optional;
import net.lecigne.somafm.history.domain.model.SongDetails;
import net.lecigne.somafm.history.domain.model.Song;

public interface SongRepository {
  long countSongs();
  List<Song> getSongs(int page, int size);
  Optional<SongDetails> getSong(long id);
}
