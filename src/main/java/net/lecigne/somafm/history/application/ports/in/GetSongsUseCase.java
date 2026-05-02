package net.lecigne.somafm.history.application.ports.in;

import net.lecigne.somafm.history.domain.model.Page;
import net.lecigne.somafm.recentlib.Song;

public interface GetSongsUseCase {
  Page<Song> getSongs(int page, int size);
}
