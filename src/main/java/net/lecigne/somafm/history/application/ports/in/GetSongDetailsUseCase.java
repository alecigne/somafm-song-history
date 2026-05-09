package net.lecigne.somafm.history.application.ports.in;

import java.util.Optional;
import net.lecigne.somafm.history.domain.model.SongDetails;

public interface GetSongDetailsUseCase {
  Optional<SongDetails> getSongDetails(long id);
}
