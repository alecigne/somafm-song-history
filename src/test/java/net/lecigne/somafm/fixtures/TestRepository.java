package net.lecigne.somafm.fixtures;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import javax.sql.DataSource;
import lombok.RequiredArgsConstructor;
import net.lecigne.somafm.model.Broadcast;
import net.lecigne.somafm.model.Channel;
import net.lecigne.somafm.model.Song;

@RequiredArgsConstructor
public class TestRepository {

  private final DataSource testDataSource;

  public List<Broadcast> readAllBroadcasts() throws IOException {
    var sql = """
        SELECT utc_time, channel, artist, title, album FROM broadcasts
        JOIN songs on broadcasts.song_id = songs.id
        ORDER BY utc_time DESC;""";
    List<Broadcast> results = new ArrayList<>();
    try (Connection connection = testDataSource.getConnection();
        PreparedStatement preparedStatement = connection.prepareStatement(sql);
        ResultSet resultSet = preparedStatement.executeQuery()) {
      while (resultSet.next()) {
        var time = resultSet.getTimestamp(1);
        var channel = resultSet.getString(2);
        var artist = resultSet.getString(3);
        var title = resultSet.getString(4);
        var album = resultSet.getString(5);
        var broadcast = Broadcast.builder()
            .time(time.toInstant())
            .channel(Channel.valueOf(channel))
            .song(Song.builder()
                .artist(artist)
                .title(title)
                .album(album)
                .build()).build();
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
