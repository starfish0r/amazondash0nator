package de.cabraham.sniffer.util;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;

import org.codehaus.plexus.util.cli.CommandLineException;
import org.codehaus.plexus.util.cli.Commandline;
import org.codehaus.plexus.util.cli.StreamConsumer;
import org.codehaus.plexus.util.cli.StreamFeeder;

public class NonTerminatingCommandLine extends Commandline {
  private Process m_nonTerminatingProcess;
  private StreamFeeder m_inputFeeder;
  private DStreamPumper m_outputPumper, m_errorPumper;
  
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
    m_outputPumper = new DStreamPumper(m_nonTerminatingProcess.getInputStream(), systemOut);
    m_errorPumper = new DStreamPumper(m_nonTerminatingProcess.getErrorStream(), systemErr);

    if (m_inputFeeder != null) {
      m_inputFeeder.start();
    }

    m_outputPumper.start();
    m_errorPumper.start();
  }
}
