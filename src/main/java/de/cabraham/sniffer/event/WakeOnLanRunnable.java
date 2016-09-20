package de.cabraham.sniffer.event;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class WakeOnLanRunnable implements MacAddressAwareRunnable {

  private static final int PORT = 9;
  private static final String IPNETWORK = "192.168.1.255"; // todo: to config file

  private String m_macAdress = "74 D4 35 FE A9 60";

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
    System.out.println("sending wol to " + m_macAdress);
    try {
      byte[] macBytes = getMacBytes(m_macAdress);
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

      System.out.println("Wake-on-LAN packet sent.");
    } catch (Exception e) {
      System.out.println("Failed to send Wake-on-LAN packet");
      e.printStackTrace();
    }
  }

  @Override
  public void setMacAdress(String macAddress) {
    m_macAdress = macAddress;
  }
  
}