package net.lecigne.somafm.exception;

public class UnknownChannelException extends RuntimeException {

  public UnknownChannelException(String channel) {
    super("Unknown channel: " + channel);
  }

}
