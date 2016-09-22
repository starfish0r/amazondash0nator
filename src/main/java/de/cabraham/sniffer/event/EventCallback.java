package de.cabraham.sniffer.event;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class EventCallback<T extends Runnable>{
  private static final ExecutorService s = Executors.newCachedThreadPool();
  
  private final T m_job;
  public EventCallback(T job){
    m_job = job;
  }
  
  public void callback(){
    s.submit(m_job);
  }
  
  public T getJob(){
    return m_job;
  }
  

}
