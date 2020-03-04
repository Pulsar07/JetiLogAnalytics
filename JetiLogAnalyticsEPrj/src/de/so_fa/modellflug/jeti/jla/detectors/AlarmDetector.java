package de.so_fa.modellflug.jeti.jla.detectors;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import de.so_fa.modellflug.jeti.jla.datamodel.Flight;
import de.so_fa.modellflug.jeti.jla.datamodel.IFlightListener;
import de.so_fa.modellflug.jeti.jla.jetilog.JetiLogDataScanner;
import de.so_fa.modellflug.jeti.jla.jetilog.SensorValue;
import de.so_fa.modellflug.jeti.jla.jetilog.SensorValueDescription;

public class AlarmDetector extends SensorObserverAdapter implements IFlightListener {

  public static Logger ourLogger = Logger.getLogger(AlarmDetector.class.getName());
  Map<String, Integer> myAlarmsByFlight;
  AlarmHandler myHandler;
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
  
  
  public void setAlarm(SensorValue aValue) {
	String alarmName = aValue.getAlarm();
	if (myAlarmsByFlight != null) {
	  int numAlarms = myAlarmsByFlight.get(alarmName) == null ? 1 : myAlarmsByFlight.get(alarmName) + 1;
	  myAlarmsByFlight.put(alarmName, numAlarms);
	  ourLogger.info(
		  "match: " + " [" + myAlarmsByFlight.get(alarmName) + "] " + aValue.getAlarm() + " at: " + aValue.getTime());
	} else {
	  ourLogger.info("match, but not \"in flight\": " + aValue.getAlarm() + "/" + aValue.getTime());
	}
  }
//  @Override
//  public void valueMatch(SensorValue aValue) {
//	String alarmName = aValue.getAlarm();
//	if (myAlarmsByFlight != null) {
//	  int numAlarms = myAlarmsByFlight.get(alarmName) == null ? 1 : myAlarmsByFlight.get(alarmName) + 1;
//	  myAlarmsByFlight.put(alarmName, numAlarms);
//	  ourLogger.info(
//		  "match: " + " [" + myAlarmsByFlight.get(alarmName) + "] " + aValue.getAlarm() + " at: " + aValue.getTime());
//	} else {
//	  ourLogger.info("match, but not \"in flight\": " + aValue.getAlarm() + "/" + aValue.getTime());
//	}
//
//  }

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
	SensorValueDescription descr = new SensorValueDescription(0, 15, "Alarm", "-");
	myHandler = new AlarmHandler(this, descr);
	addValueHandler(myHandler);

	myAlarmsByFlight = new HashMap<String, Integer>();
  }

  @Override
  public void flightEnd(Flight aF) {
	aF.setAlarms(myAlarmsByFlight);
	myAlarmsByFlight = null;
  }

  

}

class AlarmHandler extends SensorValueHandlerAdapter {
  private AlarmDetector myDetector;

  public AlarmHandler(AlarmDetector aDetector, SensorValueDescription aDescr) {
	super(aDescr);
	myDetector = aDetector;
  }

  @Override
  public void handle(SensorValue aValue) {
	myDetector.setAlarm(aValue);
  }

}
