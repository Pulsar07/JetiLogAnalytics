package de.so_fa.modellflug.jeti.jla.detectors;

import java.util.Collection;

import de.so_fa.modellflug.jeti.jla.jetilog.SensorValue;
import de.so_fa.modellflug.jeti.jla.jetilog.SensorValueDescription;

public interface ISensorValueHandler {
  public void handle(SensorValue aValue);
  public Collection<SensorValueDescription> getSensorDescr();
  public void addSensorDescr(SensorValueDescription aDescr);
}
