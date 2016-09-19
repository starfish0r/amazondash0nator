package de.cabraham.sniffer;

public class PCapException extends Exception {
  private static final long serialVersionUID = 1L;
  
  public PCapException(String string, Throwable e) {
    super(string, e);
  }

  public PCapException(String string) {
    super(string);
  }


}
