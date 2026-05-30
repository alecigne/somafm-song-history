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
import net.lecigne.somafm.history.domain.model.Broadcast;
import net.lecigne.somafm.history.domain.model.Song;
import net.lecigne.somafm.recentlib.PredefinedChannel;

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
        ResultSet rs = statement.executeQuery()) {
      if (rs.next()) return rs.getLong(1);
      return 0;
    } catch (SQLException e) {
      log.atError()
          .addKeyValue("operation", "db.broadcast.count")
          .setCause(e)
          .log("Error while counting broadcasts");
      throw new IllegalStateException("Could not count broadcasts in database", e);
    }
  }

  @Override
  public List<Broadcast> getBroadcasts(int page, int size) {
    var sql = """
        SELECT b.utc_time, b.channel, s.id, s.artist, s.title, s.album
        FROM broadcasts b
        JOIN songs s ON b.song_id = s.id
        ORDER BY b.utc_time DESC, b.id DESC
        LIMIT ? OFFSET ?;""";
    int offset = (page - 1) * size;
    List<Broadcast> broadcasts = new ArrayList<>();
    try (Connection connection = dataSource.getConnection();
        PreparedStatement statement = connection.prepareStatement(sql)) {
      statement.setInt(1, size);
      statement.setInt(2, offset);
      try (ResultSet rs = statement.executeQuery()) {
        while (rs.next()) broadcasts.add(mapBroadcast(rs));
      }
      return broadcasts;
    } catch (SQLException e) {
      log.atError()
          .addKeyValue("operation", "db.broadcast.read_page")
          .addKeyValue("page", page)
          .addKeyValue("size", size)
          .setCause(e)
          .log("Error while reading broadcasts page");
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
        String artistName = song.artist() == null ? "n/a" : song.artist();
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
      log.atError()
          .addKeyValue("operation", "db.broadcast.update")
          .addKeyValue("count", broadcasts.size())
          .setCause(e)
          .log("Error while updating broadcasts");
      throw new IllegalStateException("Could not update broadcasts in database", e);
    }
  }

  private Broadcast mapBroadcast(ResultSet rs) throws SQLException {
    String channelName = rs.getString("channel");
    var channel = PredefinedChannel
        .getByPublicName(channelName)
        .orElseThrow(() -> new IllegalStateException("Unknown channel in database: " + channelName));
    return Broadcast.builder()
        .time(rs.getTimestamp("utc_time").toInstant())
        .channel(channel)
        .song(Song.builder()
            .id(rs.getLong("id"))
            .artist(rs.getString("artist"))
            .title(rs.getString("title"))
            .album(rs.getString("album"))
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
