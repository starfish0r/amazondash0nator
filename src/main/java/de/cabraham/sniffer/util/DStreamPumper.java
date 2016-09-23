package de.cabraham.sniffer.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;

import org.codehaus.plexus.util.IOUtil;
import org.codehaus.plexus.util.cli.AbstractStreamHandler;
import org.codehaus.plexus.util.cli.StreamConsumer;

public class DStreamPumper extends AbstractStreamHandler {
  private final BufferedReader in;

  private final StreamConsumer consumer;

  private final PrintWriter out;

  private volatile Exception exception = null;

  private static final int SIZE = 1024;

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
    this.in = new BufferedReader(new InputStreamReader(in), SIZE);
    this.out = writer;
    this.consumer = consumer;
  }

  public void run() {
    /*char[] buf = new char[SIZE];
    int count = -1;*/
    try {
      /*while((count = in.read(buf)) != -1){
        //this assumes a whole line is read... i know.
        String strRead = new String(Arrays.copyOfRange(buf, 0, count));
        String[] lines = strRead.split("\\r?\\n");
        for(String line:lines){
          if (out != null) {
            out.println(line);
            out.flush();
          }
          consumeLine(line);
        }
      }*/
      
      for (String line = in.readLine(); line != null; line = in.readLine()) {
        System.out.println("[d] read a line: " + line);
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
      IOUtil.close(in);
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
    System.out.println("disabled="+isDisabled()+", line="+line+", consumer="+consumer);
    
    if (consumer != null && !isDisabled()) {
      consumer.consumeLine(line);
    }
  }
}
