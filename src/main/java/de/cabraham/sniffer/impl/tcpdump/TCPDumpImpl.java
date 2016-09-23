package de.cabraham.sniffer.impl.tcpdump;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.Set;

import org.codehaus.plexus.util.cli.CommandLineException;

import de.cabraham.sniffer.SniffingException;
import de.cabraham.sniffer.event.EventCallback;
import de.cabraham.sniffer.impl.PacketSniffer;
import de.cabraham.sniffer.util.NonTerminatingCommandLine;
import de.cabraham.sniffer.util.Util;

public class TCPDumpImpl extends PacketSniffer {

  @Override
  public String chooseMacAdress() throws SniffingException {
    NonTerminatingCommandLine nt = new NonTerminatingCommandLine();
    /*nt.setExecutable("tcpdump");
    nt.createArg().setLine("-eqtnni eth0 arp");*/
    nt.setExecutable("ping");
    nt.createArg().setLine("google.de");
    

    FilterMacAdressStreamConsumer out = new FilterMacAdressStreamConsumer("[out] ");
    FilterMacAdressStreamConsumer err = new FilterMacAdressStreamConsumer("[err] ");
    
    try {
      nt.execute(null, out, err);
    } catch (CommandLineException e) {
      throw new SniffingException("exception executing commandline", e);
    }
    
    do {
      System.out.println("type stop to stop");
    } while(nt.isAlive() && !getStdIn().nextLine().equals("stop"));
    nt.terminate();
    
    Util.threadSleep(1000L);
    System.out.println();
    return chooseOne(out.getCapturedMacs());
  }

  private String chooseOne(Set<String> capturedMacs) {
    if(capturedMacs.isEmpty()){
      System.out.println("No mac adresses could be found. Please restart the process and wait for packets.");
      return null;
    }
    
    List<String> l = new ArrayList<>();
    for(String mac:capturedMacs){
      l.add(mac);
    }
    
    System.out.println("The following mac adresses have been found. Pick one:");
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

  @Override
  public void startSniffing(String macAddress, EventCallback<Runnable> callback) throws SniffingException {
    NonTerminatingCommandLine nt = new NonTerminatingCommandLine();
    nt.setExecutable("tcpdump");
    nt.createArg().setLine("-eqtnni eth0 ether host " + macAddress);
    ReactToMacAdressStreamConsumer cons = new ReactToMacAdressStreamConsumer(macAddress, callback);
    try {
      nt.execute(null, cons, cons);
    } catch (CommandLineException e) {
      throw new SniffingException("exception executing commandline", e);
    }
  }

  public static void main(String[] args) throws SniffingException {
    /*FilterMacAdressStreamConsumer a = new FilterMacAdressStreamConsumer();
    a.consumeLine("ping 157.55.85.212   00-aa-00-62-c6-09  asd .... Adds a00-aa-00-62-c6-08");
    System.out.println(a.m_macs);*/
    TCPDumpImpl a = new TCPDumpImpl();
    a.setStdIn(new Scanner(System.in));
    a.chooseMacAdress();
  }
}

