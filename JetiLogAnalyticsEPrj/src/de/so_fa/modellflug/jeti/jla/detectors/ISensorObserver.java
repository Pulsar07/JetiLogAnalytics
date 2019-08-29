package de.so_fa.modellflug.jeti.jla.detectors;

import java.util.Collection;
import java.util.Map;
import java.util.regex.Pattern;

import de.so_fa.modellflug.jeti.jla.log.JetiLogDataScanner;
import de.so_fa.modellflug.jeti.jla.log.SensorValue;
import de.so_fa.modellflug.jeti.jla.log.SensorValueDescription;

public interface ISensorObserver {
  public Pattern getSensorNamePattern();
  public void nameMatch(SensorValueDescription aDescr);
  public void newLogData(JetiLogDataScanner aLogData);
  public Collection<SensorValueDescription> getSensorDescr();
  public void valueMatch(SensorValue aValue);
  public void endOfLog();
}
