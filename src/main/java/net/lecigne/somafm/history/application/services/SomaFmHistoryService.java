package net.lecigne.somafm.history.application.services;

import java.util.List;
import java.util.Optional;
import java.util.function.LongSupplier;
import java.util.function.Supplier;
import net.lecigne.somafm.history.application.ports.in.GetBroadcastsUseCase;
import net.lecigne.somafm.history.application.ports.in.GetSongDetailsUseCase;
import net.lecigne.somafm.history.application.ports.in.GetSongsUseCase;
import net.lecigne.somafm.history.application.ports.out.BroadcastRepository;
import net.lecigne.somafm.history.application.ports.out.SongRepository;
import net.lecigne.somafm.history.application.model.Page;
import net.lecigne.somafm.history.domain.model.Broadcast;
import net.lecigne.somafm.history.domain.model.Song;
import net.lecigne.somafm.history.domain.model.SongDetails;

public class SomaFmHistoryService implements GetBroadcastsUseCase, GetSongsUseCase, GetSongDetailsUseCase {

  private final BroadcastRepository broadcastRepo;
  private final SongRepository songRepo;
  private final PaginationService paginationService;

  SomaFmHistoryService(
      BroadcastRepository broadcastRepo,
      SongRepository songRepo,
      PaginationService paginationService) {
    this.broadcastRepo = broadcastRepo;
    this.songRepo = songRepo;
    this.paginationService = paginationService;
  }

  SomaFmHistoryService(BroadcastRepository broadcastRepo, SongRepository songRepo) {
    this(broadcastRepo, songRepo, PaginationService.defaultValidator());
  }

  @Override
  public Page<Broadcast> getBroadcasts(int page, int size) {
    return getPage(broadcastRepo::countBroadcasts, () -> broadcastRepo.getBroadcasts(page, size), page, size);
  }

  @Override
  public Page<Song> getSongs(int page, int size) {
    return getPage(songRepo::countSongs, () -> songRepo.getSongs(page, size), page, size);
  }

  @Override
  public Optional<SongDetails> getSongDetails(long id) {
    return songRepo.getSong(id);
  }

  private <T> Page<T> getPage(LongSupplier counter, Supplier<List<T>> supplier, int page, int size) {
    paginationService.validate(page, size);
    long totalNumberOfElements = counter.getAsLong();
    int numberOfPages = paginationService.countPages(totalNumberOfElements, size);
    List<T> elements = supplier.get();
    return new Page<>(page, size, totalNumberOfElements, numberOfPages, elements);
  }

  public static SomaFmHistoryService init(BroadcastRepository broadcastRepo, SongRepository songRepo) {
    return new SomaFmHistoryService(broadcastRepo, songRepo);
  }

}
