package de.cabraham.sniffer;

public class SniffingException extends Exception {
  private static final long serialVersionUID = 1L;
  
  public SniffingException(String string, Throwable e) {
    super(string, e);
  }

  public SniffingException(String string) {
    super(string);
  }


}
