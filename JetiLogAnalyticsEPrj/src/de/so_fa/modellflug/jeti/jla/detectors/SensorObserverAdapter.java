package de.so_fa.modellflug.jeti.jla.detectors;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import de.so_fa.modellflug.jeti.jla.jetilog.JetiLogDataScanner;
import de.so_fa.modellflug.jeti.jla.jetilog.SensorValue;
import de.so_fa.modellflug.jeti.jla.jetilog.SensorValueDescription;

public abstract  class SensorObserverAdapter implements ISensorObserver {

  private static Logger ourLogger = Logger.getLogger(SensorObserverAdapter.class.getName());

  Map<String, SensorValueDescription> myValueDescrMap;
  List<ISensorValueHandler> myValueHandlerList;
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

  public void addSensorDescr(String aKey, SensorValueDescription aDescr) {
	myValueDescrMap.put(aKey, aDescr);
  }

  public void addValueHandler(ISensorValueHandler aHandler) {
	myValueHandlerList.add(aHandler);
  }
  
  @Override
  public Collection<ISensorValueHandler> getValueHandler() {
	return myValueHandlerList;
  }
  
  public void resetValueHandler() {
	myValueHandlerList = new ArrayList<ISensorValueHandler>();
  }

}
