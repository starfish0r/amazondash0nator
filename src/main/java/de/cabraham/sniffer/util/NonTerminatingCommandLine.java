package de.cabraham.sniffer.util;

import java.io.InputStream;

import org.codehaus.plexus.util.cli.CommandLineException;
import org.codehaus.plexus.util.cli.Commandline;
import org.codehaus.plexus.util.cli.StreamConsumer;
import org.codehaus.plexus.util.cli.StreamFeeder;
import org.codehaus.plexus.util.cli.StreamPumper;

public class NonTerminatingCommandLine extends Commandline {
  private Process m_nonTerminatingProcess;
  private StreamFeeder m_inputFeeder;
  private StreamPumper m_outputPumper, m_errorPumper;
  
  public void terminate(){
    if(m_nonTerminatingProcess != null){
      m_nonTerminatingProcess.destroy();
    }
  }
  
  public boolean isAlive() {
    return m_nonTerminatingProcess.isAlive();
  }
  
  public void execute(final InputStream systemIn, final StreamConsumer systemOut, final StreamConsumer systemErr) throws CommandLineException {
    m_nonTerminatingProcess = this.execute();
  
    
    m_inputFeeder = systemIn != null ? new StreamFeeder(systemIn, m_nonTerminatingProcess.getOutputStream()) : null;
    m_outputPumper = new StreamPumper(m_nonTerminatingProcess.getInputStream(), systemOut);
    m_errorPumper = new StreamPumper(m_nonTerminatingProcess.getErrorStream(), systemErr);

    if (m_inputFeeder != null) {
      m_inputFeeder.start();
    }

    m_outputPumper.start();
    m_errorPumper.start();
    /*return new CommandLineCallable() {
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
    };*/
  }

  
}
