package net.lecigne.somafm.history.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import net.lecigne.somafm.history.domain.services.PaginationService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("The page request service")
class PaginationServiceTest {

  @Test
  void should_accept_valid_page_request() {
    // Given
    var service = new PaginationService(200);

    // Then
    assertThatCode(() -> service.validate(1, 50)).doesNotThrowAnyException();
  }

  @Test
  void should_reject_page_lower_than_one() {
    // Given
    var service = new PaginationService(200);

    // Then
    assertThatThrownBy(() -> service.validate(0, 50))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("page must be >= 1");
  }

  @Test
  void should_reject_page_size_above_max() {
    // Given
    var service = new PaginationService(200);

    // Then
    assertThatThrownBy(() -> service.validate(1, 201))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("size must be <= 200");
  }

  @Test
  void should_count_pages_correctly() {
    // Given
    var service = new PaginationService(100);

    // When
    int numberOfPages = service.countPages(2761, 13);

    // Then
    assertThat(numberOfPages).isEqualTo(213);
  }

}
