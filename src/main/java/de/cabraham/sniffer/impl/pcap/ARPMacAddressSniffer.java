package de.cabraham.sniffer.impl.pcap;

import java.util.Set;

import org.jnetpcap.packet.PcapPacket;
import org.jnetpcap.packet.PcapPacketHandler;
import org.jnetpcap.protocol.network.Arp;

import de.cabraham.sniffer.MakeItStopException;
import de.cabraham.sniffer.util.Util;

final class ARPMacAddressSniffer implements PcapPacketHandler<String> {
  private final Arp arp = new Arp();
  private volatile boolean m_stop = false;
  private final Set<String> m_macAdresses;

  /**
   * @param main
   */
  ARPMacAddressSniffer(Set<String> macAdresses) {
    m_macAdresses = macAdresses;
  }


  @Override
  public void nextPacket(PcapPacket packet, String user) {
    if (packet.hasHeader(arp)) {
      String sourceMac = Util.macAddresstoString(arp.sha());
      //System.out.println("arp packet: " + Main.toString(arp.getHeader()));
      //System.out.println("ARP packet from " + sourceMac);
      if(m_macAdresses.add(sourceMac)){
        System.out.println("new mac adress discovered: "+sourceMac);
      }
    }
    if(m_stop){
      throw new MakeItStopException();
    }
  }
  
  public void makeItStop(){
    m_stop = true;
  }
}