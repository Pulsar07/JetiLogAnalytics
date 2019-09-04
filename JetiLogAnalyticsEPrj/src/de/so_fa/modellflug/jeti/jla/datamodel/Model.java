package de.so_fa.modellflug.jeti.jla.datamodel;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import de.so_fa.modellflug.jeti.jla.datamodel.Flight.FlightDetection;
import de.so_fa.modellflug.jeti.jla.lang.NLS;
import de.so_fa.modellflug.jeti.jla.lang.NLS.NLSKey;
import de.so_fa.modellflug.jeti.jla.log.JetiLogDataScanner;
import de.so_fa.modellflug.jeti.jla.log.TimeDuration;
import de.so_fa.utils.log.MyLogger;

public class Model {
  private static Logger ourLogger = Logger.getLogger(Model.class.getName());
  private int myLogCount = 0;
  private int myLogTime = 0;

  private static Map<String, Model> ourModels = new HashMap<String, Model>();
  private String myName;
  private List<Flight> myFlightList = new ArrayList<Flight>();
  private int myMaxHeight;
  private int myMaxSpeed;

  private Model(String aName) {
	myName = aName;
  }

  public String getName() {
	return myName;
  }

  public int getLogCount() {
	return myLogCount;
  }

  static public Model get(String aName) {
	Model model = ourModels.get(aName);
	if (null == model) {
	  model = new Model(aName);
	  ourModels.put(aName, model);
	}
	return model;
  }

  public static void addLogData(JetiLogDataScanner aData) {
	String name = aData.getModelName();
	Model model = get(name);
	if (null == model) {
	  model = new Model(name);
	  ourModels.put(name, model);
	}
	ourLogger.info(
		"Modell <" + model.getName() + "> add log data: " + aData.getLogName() + " [" + aData.getLogDuration() + "]");
	// System.out.println("Modell <" + model.getName() + "> add log data: " +
	// aData.getLogName());
	model.myLogCount++;
	model.myLogTime += aData.getLogDuration();
  }

  public static Collection<Model> getModelCollection() {
	return ourModels.values();
  }

  public int getLogTime() {
	return myLogTime;
  }

  public int getFlightCount() {
	return myFlightList.size();
  }

  public int getFlightTime() {
	int ftime = 0;
	for (Flight f : myFlightList) {
	  ftime += f.getFlightDuration();
	}
	return ftime;
  }

  public Flight addFlight(Flight.FlightDetection aType, LocalDateTime aTime, int aFlightDuration) {
	Flight f = null;
	if (!myFlightList.isEmpty()) {
	  f = myFlightList.get(myFlightList.size() - 1);
	}

	if (null != f && Math.abs((f.getStartTime().atZone(ZoneId.systemDefault()).toEpochSecond() - aTime.atZone(ZoneId.systemDefault()).toEpochSecond())) < 20) {
	  // an other detector has created this flight, so ad only the attributes missing
	  ourLogger.info("resusing flight: " + f);
	} else {
	  f = Flight.createFlight(aType, this, aTime, aFlightDuration);
	  myFlightList.add(f);
	  ourLogger.info("adding flight: " + f);
	}
	return f;

  }

  public Flight getCurrentFlight() {
	return myFlightList.get(myFlightList.size() - 1);
  }

  public String toString() {
	int identation = 26;
	StringBuffer out = new StringBuffer();
	out.append(NLS.get(NLSKey.MODEL, identation) + "  : ");
	out.append(this.getName());
	out.append(System.getProperty("line.separator"));
	out.append("  " + NLS.get(NLSKey.MAX_HEIGHT, identation) + ": ");
	out.append(this.getMaxHeight());
	out.append(System.getProperty("line.separator"));
	out.append("  " + NLS.get(NLSKey.MAX_SPEED, identation) + ": ");
	out.append(this.getMaxSpeed());
	out.append(System.getProperty("line.separator"));
	out.append("  " + NLS.get(NLSKey.FLIGHT_COUNT, identation) + ": ");
	out.append(this.getFlightCount());
	out.append(System.getProperty("line.separator"));
	out.append(
		"  " + NLS.fillWithBlanks(NLS.get(NLSKey.FLIGHTDURATION) + " " + NLS.get(NLSKey.TOTAL), identation) + ": ");
	out.append(TimeDuration.getString(this.getFlightTime()));
	out.append(System.getProperty("line.separator"));
	out.append("  " + NLS.fillWithBlanks(NLS.get(NLSKey.LOGDURATION) + " " + NLS.get(NLSKey.TOTAL), identation) + ": ");
	out.append(TimeDuration.getString(this.getLogTime()));
	out.append(System.getProperty("line.separator"));
	for (Flight f : this.getFlights()) {
	  out.append(f);
	}
	return out.toString();
  }

  public List<Flight> getFlights() {
	return Collections.unmodifiableList(myFlightList);
  }

  public void setMaxSpeed(int aMaxSpeed) {
	myMaxSpeed = Math.max(myMaxSpeed, aMaxSpeed);
  }

  public int getMaxSpeed() {
	return myMaxSpeed;
  }

  public void setMaxHeight(int aMaxHeight) {
	myMaxHeight = Math.max(myMaxHeight, aMaxHeight);
  }

  public int getMaxHeight() {
	return myMaxHeight;
  }

  public static void printResult() {
	int cntLog = 0;
	int cntFlights = 0;
	int timeFlights = 0;
	int timeLogs = 0;
	StringBuffer result = new StringBuffer();
	result.append("\n" + NLS.get(NLSKey.MODEL_STATISTIC) + " (" + Model.getModelCollection().size() + " "
		+ NLS.get(NLSKey.MODELS) + "):");
	System.out.println(result);
	for (Model model : Model.getModelCollection()) {
	  System.out.println(model);
	  ourLogger.info(model.toString());
	  cntLog += model.getLogCount();
	  timeLogs += model.getLogTime();
	  cntFlights += model.getFlightCount();
	  timeFlights += model.getFlightTime();
	}

	int indentation = 26;
	result = new StringBuffer();
	result.append("\n" + NLS.get(NLSKey.STATISTIC_TOTAL) + ":");
	result.append("\n");
	result.append("  " + NLS.fillWithBlanks(NLS.get(NLSKey.LOG_COUNT) + " " + NLS.get(NLSKey.TOTAL), indentation) + ": "
		+ cntLog);
	result.append("\n");
	result.append("  " + NLS.fillWithBlanks(NLS.get(NLSKey.LOGDURATION) + " " + NLS.get(NLSKey.TOTAL), indentation)
		+ ": " + TimeDuration.getString(timeLogs));
	result.append("\n");
	result.append(
		"  " + NLS.fillWithBlanks(NLS.get(NLSKey.MODEL_COUNT), indentation) + ": " + ourModels.entrySet().size());
	result.append("\n");
	result.append("  " + NLS.fillWithBlanks(NLS.get(NLSKey.FLIGHT_COUNT) + " " + NLS.get(NLSKey.TOTAL), indentation)
		+ ": " + cntFlights);
	result.append("\n");
	result.append("  " + NLS.fillWithBlanks(NLS.get(NLSKey.FLIGHTDURATION) + " " + NLS.get(NLSKey.TOTAL), indentation)
		+ ": " + TimeDuration.getString(timeFlights));
	result.append("\n");
	System.out.println(result);
	ourLogger.info(result.toString());

  }

}
