package de.cabraham.sniffer;

import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;

import de.cabraham.sniffer.event.EventCallbackRunnable;
import de.cabraham.sniffer.event.WakeOnLanRunnable;
import de.cabraham.sniffer.impl.PacketSniffer;
import de.cabraham.sniffer.impl.pcap.PCapImpl;
import de.cabraham.sniffer.impl.tcpdump.TCPDumpImpl;
import de.cabraham.sniffer.util.Logger;
import de.cabraham.sniffer.util.Util;


public class Main {

  final Set<String> m_macAdresses = new HashSet<>();
  Scanner m_stdIn = null;

  public static void main(String[] args) throws SniffingException {
    try {
      new Main().start();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public void start() throws SniffingException {
    Util.loadProperties();
    Logger.setDebug(Boolean.valueOf(Util.getProperties().getProperty("debug", "false")));
    m_stdIn = new Scanner(System.in);
    
    SniffingImplementations chosen = chooseImpl();
    PacketSniffer impl = null;
    try {
      impl = chosen.getImplementationClass().newInstance();
    } catch (Exception e) {
      Logger.error("Cannot instantiate class", e);
      return;
    }
    
    impl.setStdIn(m_stdIn);
    String macAdress = Util.getProperties().getProperty("dashMacAdress", null);
    if(macAdress==null){
      String mac = impl.chooseMacAdress();
      Util.setAndSaveProperty("dashMacAdress", mac);
    }
    
    macAdress = Util.getProperties().getProperty("dashMacAdress", null);
    if(macAdress==null){
      Logger.output("There's no dash mac adress :/");
    } else {
      Logger.output("starting to sniff, looking for "+macAdress);
      impl.startSniffing(macAdress, new EventCallbackRunnable<Runnable>(new WakeOnLanRunnable()));
    }
    Logger.output("done!");
  }

  private SniffingImplementations chooseImpl() {
    String impl = Util.getProperties().getProperty("sniffingImpl", null);
    
    if(impl != null) {
      try {
        return SniffingImplementations.valueOf(impl);
      } catch (Exception e) {
        //invalid value in settings
        Logger.output("invalid value '"+impl+"' for key sniffingImpl");
      }
    } 

    int pick = -1;
    SniffingImplementations[] arrImpls = SniffingImplementations.values();
    do {
      Logger.output("Please choose how packets will be sniffed:");
      for(int i=0;i<arrImpls.length;i++){
        System.out.println("#"+i+": "+arrImpls[i]);
      }
      pick = m_stdIn.nextInt();
    } while(pick <0 || pick >= arrImpls.length);
    SniffingImplementations pickedImpl = arrImpls[pick];
    Util.setAndSaveProperty("sniffingImpl", pickedImpl.name());
    return pickedImpl;
  }
  
  enum SniffingImplementations {
    PCAP("jnetpcap - uses the native pcap library, not available for the arm platform", PCapImpl.class),
    TCPDUMP("tcpdump - uses the unix binary. Not available on windows.", TCPDumpImpl.class);
    String m_desc;
    
    private Class<? extends PacketSniffer> m_class;
    private SniffingImplementations(String desc, Class<? extends PacketSniffer> clazz) {
      m_desc = desc;
      m_class = clazz;
    }
    public Class<? extends PacketSniffer> getImplementationClass() {
      return m_class;
    }
    public String getDesc(){
      return m_desc;
    }
  }


}
