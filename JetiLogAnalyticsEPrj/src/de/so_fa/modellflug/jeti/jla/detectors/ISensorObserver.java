package de.so_fa.modellflug.jeti.jla.detectors;

import java.util.Collection;
import java.util.Map;
import java.util.regex.Pattern;

import de.so_fa.modellflug.jeti.jla.log.JetiLogDataScanner;
import de.so_fa.modellflug.jeti.jla.log.SensorValue;
import de.so_fa.modellflug.jeti.jla.log.SensorValueDescription;

public interface ISensorObserver {
  /**
   * this method is called before a log file is scanned
   * e.g. Pattern.compile(".*speed");
   * @return a Pattern matching the name of the sensor
   */
  public Pattern getSensorNamePattern();
  
  /**
   * this method is called if a sensor descriptor with the name given in 
   * getSensorNamePattern() matches from the header section of the log file 
   * Here a detailed "selection" of sensor value descriptions has to be created, 
   * which has to be given in getSensorDescr()
   * 
   * @param aDescr is the sensor descriptor matching
   */
  public void nameMatch(SensorValueDescription aDescr);
  
  /**
   * this method is called in the log file scan process, to identify the sensors values
   * @return
   */
  public Collection<SensorValueDescription> getSensorDescr();
  
  /**
   * this method is called if a sensor value mathces the given sensor value 
   * descriptions given in getSensorDescr()
   * @param aValue the value matching
   */
  public void valueMatch(SensorValue aValue);
  
  /** 
   * this method is called in case a new log file is scanned, to reset some flight data
   * @param aLogData
   */
  public void newLogData(JetiLogDataScanner aLogData);
  
  /**
   * this method is called in case a log file scan is finished, to reset some flight data
   */
  public void endOfLog();
}
