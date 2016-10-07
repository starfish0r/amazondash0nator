package de.cabraham.sniffer.util;

public class Logger {
  
  private static boolean s_debug = false;
  
  public static void setDebug(boolean d){
    s_debug = d;
  }
  
  public static void log(String str){
    System.out.println(str);
  }
  
  public static void debug(String str){
    if(s_debug){
      System.out.println(str);
    }
  }

}
