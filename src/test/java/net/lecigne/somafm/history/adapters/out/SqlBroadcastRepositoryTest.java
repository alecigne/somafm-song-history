package net.lecigne.somafm.history.adapters.out;

import static net.lecigne.somafm.history.fixtures.Fixtures.dirkSerriesSix;
import static net.lecigne.somafm.recentlib.PredefinedChannel.DRONE_ZONE;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

import java.sql.SQLException;
import java.time.Instant;
import java.util.List;
import javax.sql.DataSource;
import net.lecigne.somafm.history.domain.model.Broadcast;
import org.assertj.core.api.ThrowableAssert.ThrowingCallable;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

@DisplayName("The SQL broadcast repository")
class SqlBroadcastRepositoryTest {

  @Test
  void should_fail_when_broadcast_update_cannot_access_database() throws SQLException {
    // Given
    DataSource dataSource = Mockito.mock(DataSource.class);
    given(dataSource.getConnection()).willThrow(new SQLException("database unavailable"));
    var repository = new SqlBroadcastRepository(dataSource);
    var broadcast = Broadcast.builder()
        .time(Instant.parse("2021-01-01T11:00:00Z"))
        .channel(DRONE_ZONE)
        .song(dirkSerriesSix())
        .build();

    // "When"
    ThrowingCallable call = () -> repository.updateBroadcasts(List.of(broadcast));

    // Then
    assertThatThrownBy(call)
        .isInstanceOf(IllegalStateException.class)
        .hasMessage("Could not update broadcasts in database")
        .hasCauseInstanceOf(SQLException.class);
  }

}
