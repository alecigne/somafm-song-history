package net.lecigne.somafm.history.domain.model;

import java.util.List;
import lombok.Getter;
import lombok.experimental.Accessors;

@Accessors(fluent = true)
@Getter
public class Page<T> {
  int number;
  int size;
  long totalElements;
  int totalPages;
  List<T> items;

  public Page(int number, int size, long totalElements, int totalPages, List<T> items) {
    this.number = number;
    this.size = size;
    this.totalElements = totalElements;
    this.totalPages = totalPages;
    this.items = items;
  }

}
