package de.so_fa.modellflug.jeti.jla.datamodel;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import de.so_fa.modellflug.jeti.jla.jetilog.JetiLogDataScanner;
import de.so_fa.modellflug.jeti.jla.jetilog.TimeDuration;
import de.so_fa.modellflug.jeti.jla.lang.NLS;
import de.so_fa.modellflug.jeti.jla.lang.NLS.NLSKey;

public class Flight {
  public static final Logger ourLogger = Logger.getLogger(Flight.class.getName());
  public static List<IFlightListener> ourFlightListeners;

  public enum FlightDetection {
	HEIGHT, SPEED, SIGNAL, NA
  }

  // static List<Flight> ourO = new ArrayList<Flight>();
  Model myModel;
  LocalDateTime myStartTime;
  int myFlightDuration;

  List<SensorAttribute> myAttributes = new ArrayList<SensorAttribute>();

  FlightDetection myDetectionType;
  Map<String, Integer> myAlarms;
  private JetiLogDataScanner myLogData;

  public static void init() {
	ourFlightListeners = new ArrayList<IFlightListener>();
  }

  public void addAttribute(NLSKey aName, String aUnit, Object aMinValue, Object aMaxValue, Object aAvgValue, boolean aAddToModel) {
	SensorAttribute a = new SensorAttribute(aName, aUnit, aMinValue, aMaxValue, aAvgValue, aAddToModel);
	myAttributes.add(a);
	updateModel(a);
  }

  void updateModel(SensorAttribute aAttribute) {
//	if (aAttribute.getNameKey() == NLSKey.CO_GEN_AIR_SPEED) {
//	  myModel.setMaxSpeed(((Integer) aAttribute.getMaxValue()).intValue());
//	}
//	if (aAttribute.getNameKey() == NLSKey.CO_GEN_GPS_SPEED) {
//	  myModel.setMaxSpeed(((Integer) aAttribute.getMaxValue()).intValue());
//	}
//	if (aAttribute.getNameKey() == NLSKey.CO_GEN_HEIGHT) {
//	  myModel.setMaxHeight(((Integer) aAttribute.getMaxValue()).intValue());
//	}
//	if (aAttribute.getNameKey() == NLSKey.CO_SIG_DURA) {
//	  myModel.setSigDuraMax(((Integer) aAttribute.getMaxValue()).intValue());
//	}
	if (aAttribute.getDoAddToModel()) {
	  myModel.addAttribute(aAttribute);
	}

  }

  public static Flight createFlight(FlightDetection aType, Model aModel, LocalDateTime aTime, int aFlightDuration, JetiLogDataScanner aLogData) {
	Flight f = new Flight(aType, aModel);
	f.setStartTime(aTime);
	f.setFlightDuration(aFlightDuration);
	f.setLog(aLogData);

	ourLogger.info("flight end noti");
	for (IFlightListener listener : ourFlightListeners) {
	  listener.flightEnd(f);
	}
	return f;
  }

  private Flight(FlightDetection aType, Model aModel) {
	myDetectionType = aType;
	myModel = aModel;
  }

  public void setStartTime(LocalDateTime aTime) {
	myStartTime = aTime;
  }

  public LocalDateTime getStartTime() {
	return this.myStartTime;
  }

  public void setFlightDuration(int aTime) {
	myFlightDuration = aTime;
  }

  public int getFlightDuration() {
	return myFlightDuration;
  }

  public static void potentialFlightStart() {
	ourLogger.info("flight start noti");
	for (IFlightListener listener : ourFlightListeners) {
	  listener.flightStart();
	}
  }

  public static void addFlightListener(IFlightListener aListener) {
	ourFlightListeners.add(aListener);
  }

  public static void removeFlightListener(IFlightListener aListener) {
	ourFlightListeners.remove(aListener);
  }

  public String toString() {
	DateTimeFormatter timeFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

	int identation = 30;
	StringBuffer out = new StringBuffer();
	out.append("    " + NLS.get(NLSKey.CO_FLIGHT, identation) + "  : ");
	out.append(myStartTime.format(timeFormat));
	out.append("\n");
	out.append("      " + NLS.get(NLSKey.CO_LOG_FILE, identation) + ": ");
	out.append(myLogData.getLogName());
	out.append("\n");
	out.append("      " + NLS.fillWithBlanks(NLS.get(NLSKey.CO_LOGDURATION) + " " + NLS.get(NLSKey.CO_TOTAL), identation) + ": ");
	out.append(TimeDuration.getString(myLogData.getLogDuration()));
	out.append(System.getProperty("line.separator"));
	out.append("      " + NLS.get(NLSKey.CO_FLIGHTDURATION, identation) + ": ");
	out.append((new TimeDuration(myFlightDuration)).toString());
	out.append("\n");
	for (SensorAttribute attr : myAttributes) {
	  out.append("      " + NLS.fillWithBlanks(attr.getName(), identation) + ": ");
	  out.append(attr.getValueString());
	  out.append("\n");
	}
	if (myAlarms != null && !myAlarms.isEmpty()) {
	  out.append("      " + NLS.get(NLSKey.CO_ALARMS) + ":\n");
	  List<String> alarmList = new ArrayList<String>(myAlarms.keySet());
	  alarmList.sort(Comparator.naturalOrder());
	  for (String alarm : alarmList) {
		out.append("        " + NLS.fillWithBlanks(alarm, identation-2) + ": " + myAlarms.get(alarm));
		out.append("\n");
	  }
	}
	return out.toString();

  }

  public void setAlarms(Map<String, Integer> aAlarms) {
	if (null == aAlarms) {
	  return;
	}
	myAlarms = aAlarms;
	for (String alarm : myAlarms.keySet()) {
	  myModel.setAlarm(alarm, myAlarms.get(alarm));
	}
  }

  public void setLog(JetiLogDataScanner aLogData) {
	myLogData = aLogData;
  }
}
