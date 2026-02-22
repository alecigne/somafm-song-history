package net.lecigne.somafm.history.adapters.out;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import javax.sql.DataSource;
import lombok.extern.slf4j.Slf4j;
import net.lecigne.somafm.history.application.ports.out.BroadcastRepository;
import net.lecigne.somafm.history.bootstrap.config.SomaFmConfig;
import net.lecigne.somafm.history.bootstrap.config.SomaFmConfig.DbConfig;
import net.lecigne.somafm.recentlib.Artist;
import net.lecigne.somafm.recentlib.Broadcast;
import net.lecigne.somafm.recentlib.PredefinedChannel;
import net.lecigne.somafm.recentlib.Song;

@Slf4j
public class SqlBroadcastRepository implements BroadcastRepository {

  private final DataSource dataSource;

  SqlBroadcastRepository(DataSource dataSource) {
    this.dataSource = dataSource;
  }

  @Override
  public long countBroadcasts() {
    var sql = "SELECT COUNT(*) FROM broadcasts";
    try (Connection connection = dataSource.getConnection();
        PreparedStatement statement = connection.prepareStatement(sql);
        ResultSet resultSet = statement.executeQuery()) {
      if (resultSet.next()) {
        return resultSet.getLong(1);
      }
      return 0;
    } catch (SQLException e) {
      log.error("Error while counting broadcasts", e);
      throw new IllegalStateException("Could not count broadcasts in database", e);
    }
  }

  @Override
  public List<Broadcast> getBroadcasts(int page, int size) {
    var sql = """
        SELECT broadcasts.utc_time, broadcasts.channel, songs.artist, songs.title, songs.album
        FROM broadcasts
        JOIN songs ON broadcasts.song_id = songs.id
        ORDER BY broadcasts.utc_time DESC, broadcasts.id DESC
        LIMIT ? OFFSET ?;""";
    int offset = (page - 1) * size;
    List<Broadcast> broadcasts = new ArrayList<>();
    try (Connection connection = dataSource.getConnection();
        PreparedStatement statement = connection.prepareStatement(sql)) {
      statement.setInt(1, size);
      statement.setInt(2, offset);
      try (ResultSet resultSet = statement.executeQuery()) {
        while (resultSet.next()) {
          broadcasts.add(mapBroadcast(resultSet));
        }
      }
      return broadcasts;
    } catch (SQLException e) {
      log.error("Error while reading broadcasts page {} with size {}", page, size, e);
      throw new IllegalStateException("Could not read broadcasts from database", e);
    }
  }

  /**
   * Update broadcasts with new data from SomaFM.
   * <p>
   * The SQL code inserts a song only if it doesn't exist, but always return its ID. This ID is then
   * referenced in the broadcasts table.
   */
  @Override
  public void updateBroadcasts(List<Broadcast> broadcasts) {
    var sql = """
        WITH upsert_song AS(
            INSERT INTO songs (artist, title, album)
            VALUES (?, ?, ?)
            ON CONFLICT DO NOTHING
            RETURNING id AS new_song_id
        ),
        select_song_id AS(
            SELECT new_song_id FROM upsert_song
            UNION
            SELECT id FROM songs WHERE artist=? AND title=? AND album=?
        )
        INSERT INTO broadcasts (utc_time, channel, song_id)
        SELECT ?, ?, new_song_id FROM select_song_id
        ON CONFLICT DO NOTHING;""";
    try (Connection connection = dataSource.getConnection();
        PreparedStatement statement = connection.prepareStatement(sql)) {
      for (Broadcast broadcast : broadcasts) {
        Song song = broadcast.song();
        String artistName = (song.artist() == null || song.artist().name() == null) ? "n/a" : song.artist().name();
        String albumName = song.album() == null ? "n/a" : song.album();
        statement.setString(1, artistName);
        statement.setString(2, song.title());
        statement.setString(3, albumName);
        statement.setString(4, artistName);
        statement.setString(5, song.title());
        statement.setString(6, albumName);
        statement.setTimestamp(7, Timestamp.from(broadcast.time()));
        statement.setString(8, broadcast.channel().publicName());
        statement.addBatch();
      }
      statement.executeBatch();
    } catch (SQLException e) {
      log.error("Error", e);
    }
  }

  private Broadcast mapBroadcast(ResultSet resultSet) throws SQLException {
    String channelName = resultSet.getString("channel");
    var channel = PredefinedChannel
        .getByPublicName(channelName)
        .orElseThrow(() -> new IllegalStateException("Unknown channel in database: " + channelName));
    return Broadcast.builder()
        .time(resultSet.getTimestamp("utc_time").toInstant())
        .channel(channel)
        .song(Song.builder()
            .artist(Artist.builder().name(resultSet.getString("artist")).build())
            .title(resultSet.getString("title"))
            .album(resultSet.getString("album"))
            .build())
        .build();
  }

  public static BroadcastRepository init(SomaFmConfig config) {
    var hikariConfig = new HikariConfig();
    DbConfig db = config.getDb();
    hikariConfig.setJdbcUrl(db.getUrl());
    hikariConfig.setUsername(db.getUser());
    hikariConfig.setPassword(db.getPassword());
    var hikariDataSource = new HikariDataSource(hikariConfig);
    return new SqlBroadcastRepository(hikariDataSource);
  }

  public static BroadcastRepository init(DataSource dataSource) {
    return new SqlBroadcastRepository(dataSource);
  }

}
