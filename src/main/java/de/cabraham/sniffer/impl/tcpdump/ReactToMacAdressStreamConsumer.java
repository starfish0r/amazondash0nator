package de.cabraham.sniffer.impl.tcpdump;

import java.util.regex.Matcher;

import org.codehaus.plexus.util.cli.StreamConsumer;

import de.cabraham.sniffer.event.EventCallback;
import de.cabraham.sniffer.util.Util;

public class ReactToMacAdressStreamConsumer implements StreamConsumer {

  private final String m_macAddress;
  private final EventCallback<Runnable> m_callback;

  public ReactToMacAdressStreamConsumer(String macAddress, EventCallback<Runnable> callback) {
    m_macAddress = macAddress;
    m_callback = callback;
  }
  
  @Override
  public void consumeLine(String line) {
    Matcher m = Util.PATTERN_MAC_ADDRESS.matcher(line);
    while(m.find()){
      String standardMac = Util.toStandardMac(m.group(0));
      if(m_macAddress.equals(standardMac)){
        m_callback.callback();
      }
    }
  }

}