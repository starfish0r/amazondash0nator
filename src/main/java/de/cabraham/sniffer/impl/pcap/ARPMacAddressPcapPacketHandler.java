package de.cabraham.sniffer.impl.pcap;

import java.util.HashMap;

import org.jnetpcap.packet.PcapPacket;
import org.jnetpcap.packet.PcapPacketHandler;
import org.jnetpcap.protocol.network.Arp;

import de.cabraham.sniffer.event.EventCallback;
import de.cabraham.sniffer.util.Util;

public final class ARPMacAddressPcapPacketHandler implements PcapPacketHandler<String> {
  private static final HashMap<String,String> dict = new HashMap<>();
  
  private final Arp arp = new Arp();
  private final String m_macAdress;
  private EventCallback<Runnable> m_callback;
  
  static{
    dict.put("74 D4 35 FE A9 60", "starfish2");
    dict.put("5C 49 79 35 C7 88", "fritzbox");
    dict.put("AC 0D 1B FD 62 37", "LG G5");
    dict.put("9C FC 01 66 F3 77", "Julia iPhone 6");
  }
  

  public ARPMacAddressPcapPacketHandler(String macAdress, EventCallback<Runnable> callback) {
    m_macAdress = macAdress;
    m_callback = callback;
  }


  @Override
  public void nextPacket(PcapPacket packet, String user) {
    if (packet.hasHeader(arp)) {
      String sourceMac = Util.macAddresstoString(arp.sha());
      //System.out.println("arp packet: " + Main.toString(arp.getHeader()));
      System.out.println("ARP packet from " + sourceMac + " "+dict.get(sourceMac));
      if(m_macAdress.equals(sourceMac)){
        //m_todo.setMacAdress(sourceMac);
        m_callback.callback();
      }
    }
  }
}