package de.cabraham.sniffer.impl.tcpdump;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;

import org.codehaus.plexus.util.cli.StreamConsumer;

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
      m_macs.add(Util.toStandardMac(mac));
    }
    System.out.println(m_prefix+line);
    System.out.flush();
  }
  
  public Set<String> getCapturedMacs(){
    return m_macs;
  }
}