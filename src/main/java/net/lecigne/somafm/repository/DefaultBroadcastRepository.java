package net.lecigne.somafm.repository;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Clock;
import java.util.Set;
import javax.sql.DataSource;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.lecigne.somafm.client.RecentBroadcastsClient;
import net.lecigne.somafm.client.dto.RecentBroadcastsDto;
import net.lecigne.somafm.config.SomaFmConfig;
import net.lecigne.somafm.mappers.BroadcastMapper;
import net.lecigne.somafm.model.Broadcast;
import net.lecigne.somafm.model.Channel;

/**
 * Handle most recent SomaFM broadcasts for a given channel.
 */
@AllArgsConstructor
@Slf4j
public class DefaultBroadcastRepository implements BroadcastRepository {

  private RecentBroadcastsClient recentBroadcastsClient;
  private BroadcastMapper broadcastMapper;
  private final DataSource dataSource;

  @Override
  public Set<Broadcast> getRecentBroadcasts(Channel channel) throws IOException {
    log.info("Getting recent broadcasts for SomaFM's {}", channel.getPublicName());
    RecentBroadcastsDto recentBroadcastsDto = recentBroadcastsClient.get(channel);
    return broadcastMapper.map(recentBroadcastsDto);
  }

  /**
   * Update broadcasts with new data from SomaFM.
   * <p>
   * The SQL code inserts a song only if it doesn't exist, but always return its ID. This ID is then referenced in the
   * broadcasts table.
   */
  @Override
  public void updateBroadcasts(Set<Broadcast> broadcasts) {
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
        statement.setString(1, broadcast.getSong().getArtist());
        statement.setString(2, broadcast.getSong().getTitle());
        statement.setString(3, broadcast.getSong().getAlbum());
        statement.setString(4, broadcast.getSong().getArtist());
        statement.setString(5, broadcast.getSong().getTitle());
        statement.setString(6, broadcast.getSong().getAlbum());
        statement.setTimestamp(7, Timestamp.from(broadcast.getTime()));
        statement.setString(8, broadcast.getChannel().name());
        statement.addBatch();
      }
      statement.executeBatch();
    } catch (SQLException e) {
      log.error("Error", e);
    }
  }

  public static BroadcastRepository init(SomaFmConfig config) {
    RecentBroadcastsClient client = RecentBroadcastsClient.init(config);
    var mapper = new BroadcastMapper(Clock.systemDefaultZone());
    var hikariConfig = new HikariConfig();
    hikariConfig.setJdbcUrl(config.getDbUrl());
    hikariConfig.setUsername(config.getDbUser());
    hikariConfig.setPassword(config.getDbPassword());
    var hikariDataSource = new HikariDataSource(hikariConfig);
    return new DefaultBroadcastRepository(client, mapper, hikariDataSource);
  }

}
