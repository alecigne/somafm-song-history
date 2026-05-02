package net.lecigne.somafm.history.domain.services;

public class PaginationService {

  private static final int DEFAULT_MAX_PAGE_SIZE = 50;

  private final int maxPageSize;

  public PaginationService(int maxPageSize) {
    if (maxPageSize <= 0) throw new IllegalArgumentException("maxPageSize must be > 0");
    this.maxPageSize = maxPageSize;
  }

  public void validate(int page, int size) {
    if (page < 1) throw new IllegalArgumentException("page must be >= 1");
    if (size <= 0) throw new IllegalArgumentException("size must be > 0");
    if (size > maxPageSize) throw new IllegalArgumentException("size must be <= " + maxPageSize);
  }

  public int countPages(long nbElements, int pageSize) {
    long numberOfPages = nbElements == 0 ? 0 : ((nbElements - 1) / pageSize) + 1;
    return (int) Math.min(numberOfPages, Integer.MAX_VALUE);
  }

  public static PaginationService defaultValidator() {
    return new PaginationService(DEFAULT_MAX_PAGE_SIZE);
  }

}
