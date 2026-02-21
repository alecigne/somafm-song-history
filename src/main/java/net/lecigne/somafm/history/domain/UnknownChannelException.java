package net.lecigne.somafm.history.domain;

public class UnknownChannelException extends RuntimeException {

  public UnknownChannelException(String channel) {
    super("Unknown channel: " + channel);
  }

}
