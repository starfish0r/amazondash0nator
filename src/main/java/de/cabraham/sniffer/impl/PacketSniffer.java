package de.cabraham.sniffer.impl;

import java.util.Scanner;

import de.cabraham.sniffer.SniffingException;
import de.cabraham.sniffer.event.EventCallbackRunnable;

public abstract class PacketSniffer {
  
  private Scanner m_stdIn;
  
  public final void setStdIn(Scanner stdIn) {
    m_stdIn = stdIn;
  }
  protected final Scanner getStdIn(){
    return m_stdIn;
  }
  
  
  public abstract String chooseMacAdress() throws SniffingException;
  public abstract void startSniffing(String macAddress, EventCallbackRunnable<Runnable> eventCallback) throws SniffingException;


}
