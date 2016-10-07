package de.cabraham.sniffer.event;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

import de.cabraham.sniffer.util.Logger;
import de.cabraham.sniffer.util.Util;

public class WakeOnLanRunnable implements Runnable {

  private static final int PORT = 9;
  private static final String IPNETWORK = "192.168.1.255"; // todo: to config file

  

  private static byte[] getMacBytes(String macStr) throws IllegalArgumentException {
    byte[] bytes = new byte[6];
    String[] hex = macStr.split("(\\:|\\-| )");
    if (hex.length != 6) {
      throw new IllegalArgumentException("Invalid MAC address.");
    }
    try {
      for (int i = 0; i < 6; i++) {
        bytes[i] = (byte) Integer.parseInt(hex[i], 16);
      }
    } catch (NumberFormatException e) {
      throw new IllegalArgumentException("Invalid hex digit in MAC address.");
    }
    return bytes;
  }

  @Override
  public void run() {
    String wolMacAdress = Util.getProperties().getProperty("wolTargetMac");//"74 D4 35 FE A9 60";
    if(wolMacAdress == null) {
      Logger.output("wol mac not configured. Place it in the settings.properties using the name wolTargetMac");
    } else {
      wolMacAdress = wolMacAdress.trim();
    }
    Logger.debug("sending wol to " + wolMacAdress);
    try {
      byte[] macBytes = getMacBytes(wolMacAdress);
      byte[] bytes = new byte[6 + 16 * macBytes.length];
      for (int i = 0; i < 6; i++) {
        bytes[i] = (byte) 0xff;
      }
      for (int i = 6; i < bytes.length; i += macBytes.length) {
        System.arraycopy(macBytes, 0, bytes, i, macBytes.length);
      }

      InetAddress address = InetAddress.getByName(IPNETWORK);
      DatagramPacket packet = new DatagramPacket(bytes, bytes.length, address, PORT);
      DatagramSocket socket = new DatagramSocket();
      socket.send(packet);
      socket.close();

      Logger.output("Wake-on-LAN packet sent.");
    } catch (Exception e) {
      Logger.error("Failed to send Wake-on-LAN packet", e);
    }
  }

  
}