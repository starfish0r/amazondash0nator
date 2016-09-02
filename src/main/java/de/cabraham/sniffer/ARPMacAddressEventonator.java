package de.cabraham.sniffer;

import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.jnetpcap.packet.PcapPacket;
import org.jnetpcap.packet.PcapPacketHandler;
import org.jnetpcap.protocol.network.Arp;

final class ARPMacAddressEventonator implements PcapPacketHandler<String> {
  private static final HashMap<String,String> dict = new HashMap<>();
  
  private final Arp arp = new Arp();
  //private volatile boolean m_stop = false;
  private final String m_macAdress;
  private MacAddressAwareRunnable m_todo;
  
  static{
    dict.put("74 D4 35 FE A9 60", "starfish2");
    dict.put("5C 49 79 35 C7 88", "fritzbox");
    dict.put("AC 0D 1B FD 62 37", "LG G5");
    dict.put("9C FC 01 66 F3 77", "Julia iPhone 6");
  }
  
  ExecutorService s = Executors.newCachedThreadPool();

  ARPMacAddressEventonator(String macAdress, MacAddressAwareRunnable todo) {
    m_macAdress = macAdress;
    m_todo = todo;
  }


  @Override
  public void nextPacket(PcapPacket packet, String user) {
    if (packet.hasHeader(arp)) {
      String sourceMac = Main.toString(arp.sha());
      //System.out.println("arp packet: " + Main.toString(arp.getHeader()));
      System.out.println("ARP packet from " + sourceMac + " "+dict.get(sourceMac));
      if(m_macAdress.equals(sourceMac)){
        m_todo.setMacAdress(sourceMac);
        s.execute(m_todo);
      }
    }
  }
  public static interface MacAddressAwareRunnable extends Runnable {
    public void setMacAdress(String macAddress);
  }
}