package net.lecigne.somafm.history.adapters.out;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javax.sql.DataSource;
import lombok.extern.slf4j.Slf4j;
import net.lecigne.somafm.history.application.ports.out.SongRepository;
import net.lecigne.somafm.history.bootstrap.config.SomaFmConfig;
import net.lecigne.somafm.history.bootstrap.config.SomaFmConfig.DbConfig;
import net.lecigne.somafm.history.domain.model.Song;
import net.lecigne.somafm.history.domain.model.SongBroadcast;
import net.lecigne.somafm.history.domain.model.SongDetails;
import net.lecigne.somafm.recentlib.PredefinedChannel;

@Slf4j
public class SqlSongRepository implements SongRepository {

  private final DataSource dataSource;

  SqlSongRepository(DataSource dataSource) {
    this.dataSource = dataSource;
  }

  @Override
  public long countSongs() {
    var sql = "SELECT COUNT(*) FROM songs";
    try (Connection connection = dataSource.getConnection();
        PreparedStatement statement = connection.prepareStatement(sql);
        ResultSet resultSet = statement.executeQuery()) {
      if (resultSet.next()) return resultSet.getLong(1);
      return 0;
    } catch (SQLException e) {
      log.error("Error while counting songs", e);
      throw new IllegalStateException("Could not count songs in database", e);
    }
  }

  @Override
  public List<Song> getSongs(int page, int size) {
    var sql = """
        SELECT id, artist, title, album FROM songs
        ORDER BY LOWER(artist), LOWER(title), LOWER(album), id
        LIMIT ? OFFSET ?;
        """;
    int offset = (page - 1) * size;
    List<Song> songs = new ArrayList<>();
    try (Connection connection = dataSource.getConnection();
        PreparedStatement statement = connection.prepareStatement(sql)) {
      statement.setInt(1, size);
      statement.setInt(2, offset);
      try (ResultSet rs = statement.executeQuery()) {
        while (rs.next()) {
          songs.add(mapSong(rs));
        }
      }
      return songs;
    } catch (SQLException e) {
      log.error("Error while reading songs page {} with size {}", page, size, e);
      throw new IllegalStateException("Could not read songs from database", e);
    }
  }

  @Override
  public Optional<SongDetails> getSong(long id) {
    var sql = """
        SELECT s.id, s.artist, s.title, s.album, b.utc_time, b.channel, b.id AS broadcast_id
        FROM songs s
        LEFT JOIN broadcasts b ON b.song_id = s.id
        WHERE s.id = ?
        ORDER BY b.utc_time DESC, b.id DESC;
        """;
    try (Connection connection = dataSource.getConnection();
        PreparedStatement statement = connection.prepareStatement(sql)) {
      statement.setLong(1, id);
      try (ResultSet rs = statement.executeQuery()) {
        Song song = null;
        List<SongBroadcast> broadcasts = new ArrayList<>();
        while (rs.next()) {
          if (song == null) {
            song = mapSong(rs);
          }
          if (rs.getObject("broadcast_id") != null) {
            String channelName = rs.getString("channel");
            var channel = PredefinedChannel
                .getByPublicName(channelName)
                .orElseThrow(() -> new IllegalStateException("Unknown channel in database: " + channelName));
            broadcasts.add(new SongBroadcast(rs.getTimestamp("utc_time").toInstant(), channel));
          }
        }
        if (song == null) {
          return Optional.empty();
        }
        return Optional.of(new SongDetails(song, broadcasts));
      }
    } catch (SQLException e) {
      log.error("Error while reading song {}", id, e);
      throw new IllegalStateException("Could not read song from database", e);
    }
  }

  private Song mapSong(ResultSet rs) throws SQLException {
    return new Song(
        rs.getLong("id"),
        rs.getString("artist"),
        rs.getString("title"),
        rs.getString("album"));
  }

  public static SongRepository init(SomaFmConfig config) {
    var hikariConfig = new HikariConfig();
    DbConfig db = config.getDb();
    hikariConfig.setJdbcUrl(db.getUrl());
    hikariConfig.setUsername(db.getUser());
    hikariConfig.setPassword(db.getPassword());
    var hikariDataSource = new HikariDataSource(hikariConfig);
    return new SqlSongRepository(hikariDataSource);
  }

  public static SongRepository init(DataSource dataSource) {
    return new SqlSongRepository(dataSource);
  }

}
