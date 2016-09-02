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

public class Main {

  private static final String S_PROPERTIES_FILENAME = "settings.properties";

  private final Properties m_props = new Properties();
  final Set<String> m_macAdresses = new HashSet<>();
  Scanner m_stdIn = null;

  public static void main(String[] args) throws PCapException {
    try {
      new Main().start();
    }catch (Exception e) {
      e.printStackTrace();
    }
  }

  public void start() throws PCapException {
    loadProperties();
    m_stdIn = new Scanner(System.in);
    String interfaceName = getInterfaceName();
    String macAdress = m_props.getProperty("dashMacAdress", null);
    if(macAdress==null){
      sniffMacAdresses(interfaceName);
    }
    macAdress = m_props.getProperty("dashMacAdress", null);
    if(macAdress==null){
      System.out.println("There's no dash mac adress :/");
    } else {
      startMainSniffingLoop(interfaceName, macAdress);
    }
    System.out.println("bye!");
  }

  private void startMainSniffingLoop(String interfaceName, String macAdress) throws PCapException {
    Pcap pcap = openPcap(interfaceName);
    Thread t = new Thread(new Runnable() {
      @Override
      public void run() {
        pcap.loop(Pcap.LOOP_INFINITE, new ARPMacAddressEventonator(macAdress, new WakeOnLanEvent()), "");
      }
    });
    t.start();
    
    System.out.println("started main loop, type 'exit' to exit");
    String input = null;
    do {
      input = m_stdIn.nextLine();
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
      m_stdIn.nextLine();
      pcap.breakloop();
      // t.stop();
      persistMacAdress();

      // Close the pcap
      pcap.close();
    } catch (Exception ex) {
      System.out.println(ex);
    }
  }

  private Pcap openPcap(String interfaceName) throws PCapException {
    int snaplen = 64 * 1024; // Capture all packets, no truncation
    int flags = Pcap.MODE_PROMISCUOUS; // capture all packets
    int timeout = 10 * 1000; // 10 seconds in milliseconds
    // Open the selected device to capture packets
    System.out.println("opening interface " + interfaceName);
    StringBuilder errbuf = new StringBuilder();
    final Pcap pcap = Pcap.openLive(interfaceName, snaplen, flags, timeout, errbuf);
    if (pcap == null) {
      throw new PCapException("Error while opening device for capture: " + errbuf.toString());
      
    }
    return pcap;
  }

  private void persistMacAdress() {
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
      ch = m_stdIn.nextInt();
    } while(ch<0 || ch>=l.size());
    m_props.setProperty("dashMacAdress", l.get(ch));
    saveProperties();
  }

  private String chooseDevice() throws PCapException {
    List<PcapIf> alldevs = new ArrayList<PcapIf>();
    StringBuilder errbuf = new StringBuilder();

    // This loops is used to calculate the list of devices that can be used
    int r = -1;
    try {
      r = Pcap.findAllDevs(alldevs, errbuf);
    } catch (UnsatisfiedLinkError e) {
      throw new PCapException(
          "The native library cannot find the other library. Please install winpcap/libpcap to resolve this", e);
    }

    if (r != Pcap.OK) {
      throw new PCapException(String.format("Can't read list of devices, error is %s", errbuf.toString()));
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
      ch = m_stdIn.nextInt();
    } while (ch < 0 || ch >= alldevs.size());
    return alldevs.get(ch).getName();
  }

  private void loadProperties() {
    try {
      m_props.load(new FileInputStream(S_PROPERTIES_FILENAME));
    } catch (FileNotFoundException e) {
      System.out.println("no properties file found");
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private void saveProperties() {
    try {
      m_props.store(new FileWriter(S_PROPERTIES_FILENAME), "c1");
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private String getInterfaceName() throws PCapException {
    String interfaceName = m_props.getProperty("interfaceName");
    if (interfaceName == null || interfaceName.isEmpty()) {
      interfaceName = chooseDevice();
      m_props.setProperty("interfaceName", interfaceName);
      saveProperties();
    }
    return interfaceName;
  }

  static String toString(byte[] bytes) {
    StringBuilder sb = new StringBuilder();
    for (byte b : bytes) {
      sb.append(String.format("%02X ", b));
    }
    return sb.toString().trim();
  }

}