package de.so_fa.modellflug.jeti.jla.detectors;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import de.so_fa.modellflug.jeti.jla.log.JetiLogDataScanner;
import de.so_fa.modellflug.jeti.jla.log.SensorValueDescription;

public abstract class SensorObserverAdapter implements ISensorObserver {

  private static Logger ourLogger = Logger.getLogger(SensorObserverAdapter.class.getName());
  
  Map<String, SensorValueDescription> myValueDescrMap;
  JetiLogDataScanner myLogData;

  public String toString() {
	StringBuffer retVal = new StringBuffer();
	retVal.append(this.getClass().getSimpleName());
	retVal.append("<");
	for (SensorValueDescription descr : myValueDescrMap.values()) {
	  retVal.append(descr);
	  retVal.append(",");
	}
	retVal.deleteCharAt(retVal.length() - 1);
	retVal.append(">");
	return retVal.toString();
  }

  @Override
  public void newLogData(JetiLogDataScanner aLogData) {
	myLogData = aLogData;
	myValueDescrMap = new HashMap<String, SensorValueDescription>();
  }
  
  @Override
  public Collection<SensorValueDescription> getSensorDescr() {
	return myValueDescrMap.values();
  }
  

}
