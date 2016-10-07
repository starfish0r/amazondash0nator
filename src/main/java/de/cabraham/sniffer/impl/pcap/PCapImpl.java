package de.cabraham.sniffer.impl.pcap;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jnetpcap.Pcap;
import org.jnetpcap.PcapIf;

import de.cabraham.sniffer.SniffingException;
import de.cabraham.sniffer.event.EventCallbackRunnable;
import de.cabraham.sniffer.impl.PacketSniffer;
import de.cabraham.sniffer.util.Util;

public class PCapImpl extends PacketSniffer {

  private Set<String> m_macAdresses = new HashSet<>();
  
  @Override
  public String chooseMacAdress() throws SniffingException {
    String interfaceName = getInterfaceName();
    sniffMacAdresses(interfaceName);
    
    return chooseMacAdressFromSniffed();
  }

  @Override
  public void startSniffing(String macAddress, EventCallbackRunnable<Runnable> callback) throws SniffingException {
    String interfaceName = getInterfaceName();
    startMainSniffingLoop(interfaceName, macAddress, callback);
  }
  
  /*main{
    String interfaceName = getInterfaceName();
    String macAdress = m_props.getProperty("dashMacAdress", null);
    if(macAdress==null){
      sniffMacAdresses(interfaceName);
    }
    macAdress = m_props.getProperty("dashMacAdress", null);
    if(macAdress==null){
      System.out.println("There's no dash mac adress :/");
    } else {
      
    }
  }*/

  
  
  private void startMainSniffingLoop(String interfaceName, String macAdress, EventCallbackRunnable<Runnable> callback) throws SniffingException {
    Pcap pcap = openPcap(interfaceName);
    Thread t = new Thread(new Runnable() {
      @Override
      public void run() {
        pcap.loop(Pcap.LOOP_INFINITE, new ARPMacAddressPcapPacketHandler(macAdress, callback), "");
      }
    });
    t.start();
    
    System.out.println("started main loop, type 'exit' to exit");
    String input = null;
    do {
      input = getStdIn().nextLine();
    } while(!"exit".equals(input));
    System.out.println("stopping...");
    pcap.breakloop();
    System.out.println("breakloop done");
    try {
      t.join(500);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
    System.out.println("thread joined");
    
  }

  private void sniffMacAdresses(String interfaceName) {
    try {
      final Pcap pcap = openPcap(interfaceName);
      ARPMacAddressSniffer handler = new ARPMacAddressSniffer(m_macAdresses);
      System.out.println("Scanning for mac adresses. Hit Enter when you've had enough.");
      // ExecutorService s = Executors.newFixedThreadPool(1);
      Thread t = new Thread(new Runnable() {
        @Override
        public void run() {
          pcap.loop(Pcap.LOOP_INFINITE, handler, "");
        }
      });
      t.start();
      getStdIn().nextLine();
      pcap.breakloop();
      // t.stop();

      // Close the pcap
      pcap.close();
    } catch (Exception ex) {
      System.out.println(ex);
    }
  }

  private Pcap openPcap(String interfaceName) throws SniffingException {
    int snaplen = 64 * 1024; // Capture all packets, no truncation
    int flags = Pcap.MODE_PROMISCUOUS; // capture all packets
    int timeout = 10 * 1000; // 10 seconds in milliseconds
    // Open the selected device to capture packets
    System.out.println("opening interface " + interfaceName);
    StringBuilder errbuf = new StringBuilder();
    final Pcap pcap = Pcap.openLive(interfaceName, snaplen, flags, timeout, errbuf);
    if (pcap == null) {
      throw new SniffingException("Error while opening device for capture: " + errbuf.toString());
      
    }
    return pcap;
  }

  private String chooseMacAdressFromSniffed() {
    List<String> l = new ArrayList<>();
    for(String mac:m_macAdresses){
      l.add(mac);
    }
    
    System.out.println("The following mac adresses have been found. Pick one to save:");
    int i = 0;
    for(String mac:l){
      System.out.println("#"+(i++)+": "+mac);
    }
    int ch = -1;
    do {
      ch = getStdIn().nextInt();
    } while(ch<0 || ch>=l.size());
    return l.get(ch);
  }

  private String chooseDevice() throws SniffingException {
    List<PcapIf> alldevs = new ArrayList<PcapIf>();
    StringBuilder errbuf = new StringBuilder();

    // This loops is used to calculate the list of devices that can be used
    int r = -1;
    try {
      r = Pcap.findAllDevs(alldevs, errbuf);
    } catch (UnsatisfiedLinkError e) {
      throw new SniffingException(
          "The native library cannot find the other library. Please install winpcap/libpcap to resolve this", e);
    }

    if (r != Pcap.OK) {
      throw new SniffingException(String.format("Can't read list of devices, error is %s", errbuf.toString()));
    }

    System.out.println("Network devices found:");
    int i = 0;
    for (PcapIf device : alldevs) {
      String description = (device.getDescription() != null) ? device.getDescription() : "No description available";
      System.out.printf("#%d: %s [%s]\n", i++, device.getName(), description);
    }

    int ch = -1;
    do {
      System.out.println("Choose the # of the device!");
      ch = getStdIn().nextInt();
    } while (ch < 0 || ch >= alldevs.size());
    return alldevs.get(ch).getName();
  }
  
  private String getInterfaceName() throws SniffingException {
    String interfaceName = Util.getProperties().getProperty("interfaceName");
    if (interfaceName == null || interfaceName.isEmpty()) {
      interfaceName = chooseDevice();
      Util.getProperties().setProperty("interfaceName", interfaceName);
      Util.saveProperties();
    }
    return interfaceName;
  }


}
