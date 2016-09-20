package de.cabraham.sniffer;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Properties;

public class Util {
  
  private static final String S_PROPERTIES_FILENAME = "settings.properties";
  private static final Properties m_props = new Properties();
  

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
      m_props.store(new FileWriter(S_PROPERTIES_FILENAME), "c1");
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
  
  
  public static String macAddresstoString(byte[] bytes) {
    StringBuilder sb = new StringBuilder();
    for (byte b : bytes) {
      sb.append(String.format("%02X ", b));
    }
    return sb.toString().trim();
  }




}
