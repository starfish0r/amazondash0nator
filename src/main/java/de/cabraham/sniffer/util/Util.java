package de.cabraham.sniffer.util;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Properties;
import java.util.regex.Pattern;

public class Util {
  
  private static final String S_PROPERTIES_FILENAME = "settings.properties";
  private static final Properties m_props = new Properties();
  {
    m_props.clear();
  }
  
  public final static Pattern PATTERN_MAC_ADDRESS = Pattern.compile("[0-z]{2}[-: ][0-z]{2}[-: ][0-z]{2}[-: ][0-z]{2}[-: ][0-z]{2}[-: ][0-z]{2}");

  public static Properties getProperties() {
    return m_props;
  }
  
  public static void loadProperties() {
    try {
      m_props.load(new FileInputStream(S_PROPERTIES_FILENAME));
    } catch (FileNotFoundException e) {
      System.out.println("no properties file found");
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public static void saveProperties() {
    try {
      m_props.store(new FileWriter(S_PROPERTIES_FILENAME), "properties for the amazondash0nator");
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
  
  public static void setAndSaveProperty(String key, String value){
    if(value == null){
      getProperties().remove(key);
    } else {
      getProperties().setProperty(key, value);
    }
    saveProperties();
  }
  
  public static String macAddresstoString(byte[] bytes) {
    StringBuilder sb = new StringBuilder();
    for (byte b : bytes) {
      sb.append(String.format("%02X ", b));
    }
    return sb.toString().trim();
  }
  
  /**
   * makes sure to have it in a standard format 00:aa:00:62:c6:09
   * @param mac
   * @return
   */
  public static String toStandardMac(String mac){
    return mac.replaceAll("[- ]", ":").toLowerCase();
  }

  public static void threadSleep(long l) {
    try {
      Thread.sleep(l);
    } catch (InterruptedException e) {}
  }




}
