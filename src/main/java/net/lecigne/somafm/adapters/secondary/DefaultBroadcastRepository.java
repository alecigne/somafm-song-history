package net.lecigne.somafm.adapters.secondary;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;
import javax.sql.DataSource;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.lecigne.somafm.application.spi.SomaFmSongHistorySpi;
import net.lecigne.somafm.recentlib.Broadcast;
import net.lecigne.somafm.recentlib.Channel;
import net.lecigne.somafm.recentlib.SomaFm;

/**
 * Handle most recent SomaFM broadcasts for a given channel.
 */
@AllArgsConstructor
@Slf4j
public class DefaultBroadcastRepository implements SomaFmSongHistorySpi {

  private SomaFm somaFm;
  private final DataSource dataSource;

  /**
   * Fetch recent broadcasts from SomaFM.
   */
  @Override
  public List<Broadcast> getRecentBroadcasts(Channel channel) {
    return somaFm.fetchRecent(channel);
  }

  /**
   * Update broadcasts with new data from SomaFM.
   * <p>
   * The SQL code inserts a song only if it doesn't exist, but always return its ID. This ID is then referenced in the
   * broadcasts table.
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
        statement.setString(1, broadcast.getSong().getArtist());
        statement.setString(2, broadcast.getSong().getTitle());
        statement.setString(3, broadcast.getSong().getAlbum());
        statement.setString(4, broadcast.getSong().getArtist());
        statement.setString(5, broadcast.getSong().getTitle());
        statement.setString(6, broadcast.getSong().getAlbum());
        statement.setTimestamp(7, Timestamp.from(broadcast.getTime()));
        statement.setString(8, broadcast.getChannel().publicName());
        statement.addBatch();
      }
      statement.executeBatch();
    } catch (SQLException e) {
      log.error("Error", e);
    }
  }

}
