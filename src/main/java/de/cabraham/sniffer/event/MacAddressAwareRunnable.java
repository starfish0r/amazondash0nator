package de.cabraham.sniffer.event;

public interface MacAddressAwareRunnable extends Runnable {
  public void setMacAdress(String macAddress);
}