package de.so_fa.modellflug.jeti.jla.detectors;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import de.so_fa.modellflug.jeti.jla.datamodel.Flight;
import de.so_fa.modellflug.jeti.jla.log.JetiLogDataScanner;
import de.so_fa.modellflug.jeti.jla.log.SensorValue;
import de.so_fa.modellflug.jeti.jla.log.SensorValueDescription;

public class AlarmDetector extends SensorObserverAdapter implements IFlightListener {

  public static Logger ourLogger = Logger.getLogger(AlarmDetector.class.getName());
  Map<String, Integer> myAlarmsByFlight;
  /*
   * Alarms are not valid sensor values. They do not exist in the Header section
   * of a Log file they appear in the log only as a sensor value with id ==
   * "0000000000", value index == 15, type == 16, decimal places == 0, value is an
   * string of the alarm message
   * 
   * e.g.:
   * 
   * 002382234;0000000000;15;16;0;Schw. Signal: Q;0;0
   * 004691301;0000000000;15;16;0;900MHz Tx aktiviert;0;0
   * 004613969;0000000000;15;16;0;Signalverlust;0;0
   * 004617035;0000000000;15;16;0;Schw. Signal: Q;0;0
   * 004618069;0000000000;15;16;0;Schw. Signal: Q;0;0
   */

  public AlarmDetector() {
	super();
	Flight.addFlightListener(this);
  }

  @Override
  public Pattern getSensorNamePattern() {
	// not valid for alarms
	return null;
  }

  @Override
  public void nameMatch(SensorValueDescription aDescr) {
	// is not called for alarms
  }

  @Override
  public Collection<SensorValueDescription> getSensorDescr() {
	return myValueDescrMap.values();
  }

  @Override
  public void valueMatch(SensorValue aValue) {
	String alarmName = aValue.getAlarm();
	if (myAlarmsByFlight != null) {
	  if (myAlarmsByFlight.containsKey(alarmName)) {
		int cnt = myAlarmsByFlight.get(alarmName);
		myAlarmsByFlight.put(alarmName, cnt++);
	  } else {
		myAlarmsByFlight.put(alarmName, 1);
	  }
	}
	// ourLogger.severe("match: " +aValue.getAlarm());

  }

  @Override
  public void newLogData(JetiLogDataScanner aLogData) {
	super.newLogData(aLogData);
	SensorValueDescription descr = new SensorValueDescription(0, 15, "Alarm", null);
	myValueDescrMap.put("", descr);
  }

  @Override
  public void endOfLog() {
	// TODO Auto-generated method stub

  }

  @Override
  public void flightStart() {
	myAlarmsByFlight = new HashMap<String, Integer>();
  }

  @Override
  public void flightEnd(Flight aF) {
	aF.setAlarms(myAlarmsByFlight);
	myAlarmsByFlight = null;
  }

}
