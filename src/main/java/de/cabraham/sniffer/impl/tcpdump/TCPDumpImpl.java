package de.cabraham.sniffer.impl.tcpdump;

import java.io.InputStream;

import org.codehaus.plexus.util.cli.CommandLineCallable;
import org.codehaus.plexus.util.cli.CommandLineException;
import org.codehaus.plexus.util.cli.CommandLineTimeOutException;
import org.codehaus.plexus.util.cli.CommandLineUtils;
import org.codehaus.plexus.util.cli.Commandline;
import org.codehaus.plexus.util.cli.ShutdownHookUtils;
import org.codehaus.plexus.util.cli.StreamConsumer;
import org.codehaus.plexus.util.cli.StreamFeeder;
import org.codehaus.plexus.util.cli.StreamPumper;
import org.codehaus.plexus.util.cli.CommandLineUtils.ProcessHook;

import de.cabraham.sniffer.SniffingException;
import de.cabraham.sniffer.event.MacAddressAwareRunnable;
import de.cabraham.sniffer.impl.PacketSniffer;

public class TCPDumpImpl extends PacketSniffer {

  @Override
  public String chooseMacAdress() {
    Commandline c = new Commandline();
    c.setExecutable("tcpdump -eqtnni eth0 arp");
    CommandLineUtils.StringStreamConsumer outputConsumer = new CommandLineUtils.StringStreamConsumer();
    
    try {
      CommandLineUtils.executeCommandLine(c, outputConsumer, outputConsumer);
    } catch (CommandLineException e) {
      
      e.printStackTrace();
    }
    return null;
  }

  @Override
  public void startSniffing(String macAddress, MacAddressAwareRunnable todo) throws SniffingException {
    Commandline c = new Commandline();
    c.setExecutable("tcpdump -eqtnni eth0 ether host " + macAddress);

  }

  public static CommandLineCallable executeCommandLineAsCallable(final Commandline cl, final InputStream systemIn,
      final StreamConsumer systemOut,
      final StreamConsumer systemErr,
      final int timeoutInSeconds)
      throws CommandLineException {
    if (cl == null) {
      throw new IllegalArgumentException("cl cannot be null.");
    }

    final Process p = cl.execute();

    final StreamFeeder inputFeeder = systemIn != null ? new StreamFeeder(systemIn, p.getOutputStream()) : null;

    final StreamPumper outputPumper = new StreamPumper(p.getInputStream(), systemOut);

    final StreamPumper errorPumper = new StreamPumper(p.getErrorStream(), systemErr);

    if (inputFeeder != null) {
      inputFeeder.start();
    }

    outputPumper.start();

    errorPumper.start();

    final ProcessHook processHook = new ProcessHook(p);

    ShutdownHookUtils.addShutDownHook(processHook);

    return new CommandLineCallable() {
      public Integer call() throws CommandLineException {
        try {
          int returnValue;
          if (timeoutInSeconds <= 0) {
            returnValue = p.waitFor();
          } else {
            long now = System.currentTimeMillis();
            long timeoutInMillis = 1000L * timeoutInSeconds;
            long finish = now + timeoutInMillis;
            while (CommandLineUtils.isAlive(p) && (System.currentTimeMillis() < finish)) {
              Thread.sleep(10);
            }
            if (CommandLineUtils.isAlive(p)) {
              throw new InterruptedException("Process timeout out after " + timeoutInSeconds + " seconds");
            }
            returnValue = p.exitValue();
          }

          CommandLineUtils.waitForAllPumpers(inputFeeder, outputPumper, errorPumper);

          if (outputPumper.getException() != null) {
            throw new CommandLineException("Error inside systemOut parser", outputPumper.getException());
          }

          if (errorPumper.getException() != null) {
            throw new CommandLineException("Error inside systemErr parser", errorPumper.getException());
          }

          return returnValue;
        } catch (InterruptedException ex) {
          if (inputFeeder != null) {
            inputFeeder.disable();
          }
          outputPumper.disable();
          errorPumper.disable();
          throw new CommandLineTimeOutException("Error while executing external command, process killed.", ex);
        } finally {
          ShutdownHookUtils.removeShutdownHook(processHook);

          processHook.run();

          if (inputFeeder != null) {
            inputFeeder.close();
          }

          outputPumper.close();

          errorPumper.close();
        }
      }
    };
  }

  private static class ProcessHook extends Thread {
    private final Process process;

    private ProcessHook(Process process) {
      super("CommandlineUtils process shutdown hook");
      this.process = process;
      this.setContextClassLoader(null);
    }

    public void run() {
      process.destroy();
    }
  }

}
