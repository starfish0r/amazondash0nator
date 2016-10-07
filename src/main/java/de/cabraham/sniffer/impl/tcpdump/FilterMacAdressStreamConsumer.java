package de.cabraham.sniffer.impl.tcpdump;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;

import org.codehaus.plexus.util.cli.StreamConsumer;

import de.cabraham.sniffer.util.Logger;
import de.cabraham.sniffer.util.Util;

public class FilterMacAdressStreamConsumer implements StreamConsumer {
  
  private final Set<String> m_macs = new HashSet<>();
  private final String m_prefix;

  public FilterMacAdressStreamConsumer(String prefix) {
    m_prefix = prefix;
  }

  public FilterMacAdressStreamConsumer() {
    this("");
  }

  @Override
  public void consumeLine(String line) {
    Matcher m = Util.PATTERN_MAC_ADDRESS.matcher(line);
    while(m.find()){
      String mac = m.group(0);
      String standardMac = Util.toStandardMac(mac);
      if(m_macs.add(standardMac)){
        Logger.output("new mac: "+standardMac);
      }
    }
    Logger.debug(m_prefix+line);
  }
  
  public Set<String> getCapturedMacs(){
    return m_macs;
  }
}