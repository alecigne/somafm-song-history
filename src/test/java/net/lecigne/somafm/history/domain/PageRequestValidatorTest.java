package net.lecigne.somafm.history.domain;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import net.lecigne.somafm.history.domain.services.PageRequestValidator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("A page request validator")
class PageRequestValidatorTest {

  @Test
  void should_accept_valid_page_request() {
    // Given
    var validator = new PageRequestValidator(200);

    // Then
    assertThatCode(() -> validator.validate(1, 50)).doesNotThrowAnyException();
  }

  @Test
  void should_reject_page_lower_than_one() {
    // Given
    var validator = new PageRequestValidator(200);

    // Then
    assertThatThrownBy(() -> validator.validate(0, 50))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("page must be >= 1");
  }

  @Test
  void should_reject_page_size_above_max() {
    // Given
    var validator = new PageRequestValidator(200);

    // Then
    assertThatThrownBy(() -> validator.validate(1, 201))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("size must be <= 200");
  }

}
