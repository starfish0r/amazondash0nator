package de.cabraham.sniffer;

import java.io.FileInputStream;

import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Scanner;
import java.util.Set;

import org.jnetpcap.Pcap;
import org.jnetpcap.PcapIf;

import de.cabraham.sniffer.event.WakeOnLanRunnable;
import de.cabraham.sniffer.impl.PacketSniffer;
import de.cabraham.sniffer.impl.pcap.PCapImpl;
import de.cabraham.sniffer.impl.tcpdump.TCPDumpImpl;


public class Main {

  final Set<String> m_macAdresses = new HashSet<>();
  Scanner m_stdIn = null;

  public static void main(String[] args) throws SniffingException {
    try {
      new Main().start();
    }catch (Exception e) {
      e.printStackTrace();
    }
  }

  public void start() throws SniffingException {
    Util.loadProperties();
    m_stdIn = new Scanner(System.in);
    
    SniffingImplementations chosen = chooseImpl();
    PacketSniffer impl = null;
    try {
      impl = chosen.getImplementationClass().newInstance();
    } catch (InstantiationException e) {
      e.printStackTrace();
    } catch (IllegalAccessException e) {
      e.printStackTrace();
    }
    
    impl.setStdIn(m_stdIn);
    
    String macAdress = Util.getProperties().getProperty("dashMacAdress", null);
    if(macAdress==null){
      String mac = impl.chooseMacAdress();
      Util.getProperties().setProperty("dashMacAdress", mac);
      Util.saveProperties();
    }
    
    macAdress = Util.getProperties().getProperty("dashMacAdress", null);
    if(macAdress==null){
      System.out.println("There's no dash mac adress :/");
    } else {
      impl.startSniffing(macAdress, new WakeOnLanRunnable());
    }
    
    System.out.println("bye!");
  }

  private SniffingImplementations chooseImpl() {
    String impl = Util.getProperties().getProperty("sniffingImpl", null);
    
    if(impl != null) {
      return SniffingImplementations.valueOf(impl);
    } 

    int pick = -1;
    SniffingImplementations[] arrImpls = SniffingImplementations.values();
    do {
      System.out.println("Please choose how packets will be sniffed:");
      for(int i=0;i<arrImpls.length;i++){
        System.out.println("#"+i+": "+arrImpls[i]);
      }
      pick = m_stdIn.nextInt();
    } while(pick <0 || pick >= arrImpls.length);
    return arrImpls[pick];
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