package de.so_fa.modellflug.jeti.jla.datamodel;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import de.so_fa.modellflug.jeti.jla.detectors.IFlightListener;
import de.so_fa.modellflug.jeti.jla.lang.NLS;
import de.so_fa.modellflug.jeti.jla.lang.NLS.NLSKey;
import de.so_fa.modellflug.jeti.jla.log.TimeDuration;

public class Flight {
  public static final Logger ourLogger = Logger.getLogger(Flight.class.getName());
  public static List<IFlightListener> ourFlightListeners = new ArrayList<IFlightListener>();

  public enum FlightDetection {
	HEIGHT, SPEED, SIGNAL, NA
  }
  // static List<Flight> ourO = new ArrayList<Flight>();
  Model myModel;
  LocalDateTime myStartTime;
  int myFlightDuration;
  int myMaxHeight = 0;
  int myMaxSpeed = 0;
  int myAvgSpeed = 0;
  int myVnorm = 0;
  FlightDetection myDetectionType;
  Map<String, Integer> myAlarms;

  public int getAvgSpeed() {
	return this.myAvgSpeed;
  }

  public void setAvgSpeed(int aAvgSpeed) {
	this.myAvgSpeed = aAvgSpeed;
  }

  public int getMaxHeight() {
	return myMaxHeight;
  }

  public void setMaxHeight(int aMaxHeight) {
	myMaxHeight = aMaxHeight;
	myModel.setMaxHeight(myMaxHeight);
  }

  public static Flight createFlight(FlightDetection aType, Model aModel, LocalDateTime aTime, int aFlightDuration) {
	Flight f = new Flight(aType, aModel);
	f.setStartTime(aTime);
	f.setFlightDuration(aFlightDuration);

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

  public void setMaxSpeed(int aMaxSpeed) {
	myModel.setMaxSpeed(aMaxSpeed);
	myMaxSpeed = aMaxSpeed;
  }

  public int getMaxSpeed() {
	return myMaxSpeed;
  }
  
  public void setVnorm(int aVnorm) {
	myVnorm = aVnorm;
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

	int identation = 22;
	StringBuffer out = new StringBuffer();
	out.append("    " + NLS.get(NLSKey.FLIGHT, identation) + "  : ");
	out.append(myStartTime.format(timeFormat));
	out.append("\n");
	out.append("      " + NLS.get(NLSKey.FLIGHT_DETECTION_TYPE, identation) + ": ");
	out.append(myDetectionType);
	out.append("\n");
	out.append("      " + NLS.get(NLSKey.FLIGHTDURATION, identation) + ": ");
	out.append((new TimeDuration(myFlightDuration)).toString());
	out.append("\n");
	if (myMaxHeight > 20) {
	  out.append("      " + NLS.get(NLSKey.MAX_HEIGHT, identation) + ": ");
	  out.append(myMaxHeight);
	  out.append("\n");
	}
	if (myAvgSpeed > 10) {
	  out.append("      " + NLS.get(NLSKey.NORM_SPEED, identation) + ": ");
	  out.append(myVnorm);
	  out.append("\n");
	  out.append("      " + NLS.get(NLSKey.AVG_SPEED, identation) + ": ");
	  out.append(myAvgSpeed);
	  out.append("\n");
	  out.append("      " + NLS.get(NLSKey.MAX_SPEED, identation) + ": ");
	  out.append(myMaxSpeed);
	  out.append("\n");
	  
	}
	if (myAlarms != null && !myAlarms.isEmpty()) {
	  out.append("      " + NLS.get(NLSKey.ALARMS) + ":\n");
	  for (String alarm: myAlarms.keySet()) {
		  out.append("        " + NLS.fillWithBlanks(alarm, 20) + ": " + myAlarms.get(alarm));
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
	for (String alarm: myAlarms.keySet()) {
	  myModel.setAlarm(alarm, myAlarms.get(alarm));
	}
  }
}
