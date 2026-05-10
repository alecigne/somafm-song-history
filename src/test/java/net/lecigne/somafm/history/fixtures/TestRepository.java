package net.lecigne.somafm.history.fixtures;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import javax.sql.DataSource;
import lombok.RequiredArgsConstructor;
import net.lecigne.somafm.history.domain.model.Broadcast;
import net.lecigne.somafm.history.domain.model.Song;
import net.lecigne.somafm.recentlib.PredefinedChannel;

@RequiredArgsConstructor
public class TestRepository {

  private final DataSource testDataSource;

  public List<Broadcast> readAllBroadcasts() throws IOException {
    var sql = """
        SELECT b.utc_time, b.channel, s.id, s.artist, s.title, s.album FROM broadcasts b
        JOIN songs s on b.song_id = s.id
        ORDER BY b.utc_time DESC;""";
    List<Broadcast> results = new ArrayList<>();
    try (Connection connection = testDataSource.getConnection();
        PreparedStatement preparedStatement = connection.prepareStatement(sql);
        ResultSet rs = preparedStatement.executeQuery()) {
      while (rs.next()) {
        var time = rs.getTimestamp(1);
        var channel = rs.getString(2);
        var songId = rs.getLong(3);
        var artist = rs.getString(4);
        var title = rs.getString(5);
        var album = rs.getString(6);
        var song = new Song(songId, artist, title, album);
        var broadcast = new Broadcast(PredefinedChannel.getByPublicName(channel).orElseThrow(), time.toInstant(), song);
        results.add(broadcast);
      }
    } catch (Exception e) {
      throw new IOException(e);
    }
    return results;
  }

  public List<Song> readAllSongs() throws IOException {
    var sql = "SELECT artist, title, album FROM songs";
    List<Song> results = new ArrayList<>();
    try (Connection connection = testDataSource.getConnection();
        PreparedStatement preparedStatement = connection.prepareStatement(sql);
        ResultSet resultSet = preparedStatement.executeQuery()) {
      while (resultSet.next()) {
        var artist = resultSet.getString(1);
        var title = resultSet.getString(2);
        var album = resultSet.getString(3);
        var song = Song.builder()
            .artist(artist)
            .title(title)
            .album(album)
            .build();
        results.add(song);
      }
    } catch (Exception e) {
      throw new IOException(e);
    }
    return results;
  }

  public void deleteAllData() throws IOException {
    var sql = "TRUNCATE TABLE songs, broadcasts";
    try (Connection connection = testDataSource.getConnection();
        PreparedStatement statement = connection.prepareStatement(sql)) {
      statement.executeUpdate();
    } catch (Exception e) {
      throw new IOException(e);
    }
  }

}
