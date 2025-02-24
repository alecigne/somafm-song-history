package net.lecigne.somafm.domain;

public class UnknownChannelException extends RuntimeException {

  public UnknownChannelException(String channel) {
    super("Unknown channel: " + channel);
  }

}
