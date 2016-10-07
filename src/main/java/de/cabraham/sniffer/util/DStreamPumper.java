package de.cabraham.sniffer.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.Arrays;

import org.codehaus.plexus.util.IOUtil;
import org.codehaus.plexus.util.cli.AbstractStreamHandler;
import org.codehaus.plexus.util.cli.StreamConsumer;

public class DStreamPumper extends AbstractStreamHandler {
  private final BufferedReader inReader;

  private final StreamConsumer consumer;

  private final PrintWriter out;

  private volatile Exception exception = null;

  private InputStream inStream;

  private static final int SIZE = 10;

  public DStreamPumper(InputStream in) {
    this(in, (StreamConsumer) null);
  }

  public DStreamPumper(InputStream in, StreamConsumer consumer) {
    this(in, null, consumer);
  }

  public DStreamPumper(InputStream in, PrintWriter writer) {
    this(in, writer, null);
  }

  public DStreamPumper(InputStream in, PrintWriter writer, StreamConsumer consumer) {
    this.inStream = in;
    this.inReader = new BufferedReader(new InputStreamReader(in), SIZE);
    this.out = writer;
    this.consumer = consumer;
    setDaemon(true);
  }

  public void run() {
    /*char[] buf = new char[SIZE];
    int count = -1;*/
    try {
      /*while(true){
        //System.out.println(inStream+" "+inStream.getClass().getName());
        while(true){
          System.out.println("available: "+inStream.available());
          Util.threadSleep(1000l);
          if(inStream.available() > 10000){
            break;
          }
        }
        
        //System.out.println("1");
        while(inStream.available()==0){
          Util.threadSleep(10l);
        }
        System.out.println("available: "+inStream.available());
        if((count = inReader.read(buf)) == -1){
          break;
        }
        System.out.println("2");
        //this assumes a whole line is read... i know.
        String strRead = new String(Arrays.copyOfRange(buf, 0, count));
        System.out.println(strRead);
        System.out.println("3");
        String[] lines = strRead.split("\\r?\\n");
        for(String line:lines){
          if (out != null) {
            out.println(line);
            out.flush();
          }
          consumeLine(line);
        }
      }*/
      
      for (String line = inReader.readLine(); line != null; line = inReader.readLine()) {
        System.out.println("[d] " + line);
        try {
          if (exception == null) {
            consumeLine(line);
          }
        } catch (Exception t) {
          t.printStackTrace();
          exception = t;
        }

        if (out != null) {
          out.println(line);
          out.flush();
        }
      }
    } catch (IOException e) {
      e.printStackTrace();
      exception = e;
    } finally {
      IOUtil.close(inReader);
      synchronized (this) {
        setDone();
        this.notifyAll();
      }
    }
  }

  public void flush() {
    if (out != null) {
      out.flush();
    }
  }

  public void close() {
    IOUtil.close(out);
  }

  public Exception getException() {
    return exception;
  }

  private void consumeLine(String line) {
    //System.out.println("disabled="+isDisabled()+", line="+line+", consumer="+consumer);
    if (consumer != null && !isDisabled()) {
      consumer.consumeLine(line);
    }
  }
  
}
