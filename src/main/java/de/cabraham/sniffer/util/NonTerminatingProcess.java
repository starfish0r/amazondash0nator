package de.cabraham.sniffer.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.codehaus.plexus.util.cli.CommandLineException;
import org.codehaus.plexus.util.cli.StreamConsumer;
import org.codehaus.plexus.util.cli.StreamFeeder;
import org.codehaus.plexus.util.cli.StreamPumper;

public class NonTerminatingProcess {
  private Process m_nonTerminatingProcess;
  private StreamFeeder m_inputFeeder;
  private StreamPumper m_outputPumper;
  private StreamPumper m_errorPumper;
  private List<String> m_command;
  
  public NonTerminatingProcess(List<String> command){
    m_command = command;
  }
  
  public void terminate(){
    if(m_nonTerminatingProcess != null){
      m_nonTerminatingProcess.destroy();
    }
  }
  
  public boolean isAlive() {
    return m_nonTerminatingProcess.isAlive();
  }
  
  public void execute(final InputStream inStream, final StreamConsumer outConsumer, final StreamConsumer errConsumer) throws CommandLineException, IOException {
    Logger.debug("Running "+m_command.toString());
    m_nonTerminatingProcess = new ProcessBuilder(m_command).redirectErrorStream(true).start();
  
    m_inputFeeder = inStream != null ? new StreamFeeder(inStream, m_nonTerminatingProcess.getOutputStream()) : null;
    m_outputPumper = new StreamPumper(m_nonTerminatingProcess.getInputStream(), outConsumer);
    m_errorPumper = new StreamPumper(m_nonTerminatingProcess.getErrorStream(), errConsumer);

    if (m_inputFeeder != null) {
      m_inputFeeder.start();
    }
    if(m_outputPumper != null) {
      m_outputPumper.start();
    }
    if(m_errorPumper != null) {
      m_errorPumper.start();
    }
  }
  
}
