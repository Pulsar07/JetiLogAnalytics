package de.so_fa.modellflug.jeti.jla.datamodel;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import de.so_fa.modellflug.jeti.jla.jetilog.JetiLogDataScanner;
import de.so_fa.modellflug.jeti.jla.jetilog.TimeDuration;
import de.so_fa.modellflug.jeti.jla.lang.NLS;
import de.so_fa.modellflug.jeti.jla.lang.NLS.NLSKey;

public class Model {
  private static Logger ourLogger = Logger.getLogger(Model.class.getName());
  private int myLogCount = 0;
  private int myLogTime = 0;

  private static Map<String, Model> ourModels;
  private String myName;
  private List<Flight> myFlightList = new ArrayList<Flight>();
  private int myMaxHeight;
  private int myMaxSpeed;
  private float myMinVoltage;
  int mySigDuraMax = 0;
  Map<String, Integer> myAlarms = new HashMap<String, Integer>();
  List<SensorAttribute> myAttributes = new ArrayList<SensorAttribute>();

  public static void init() {
	ourModels = new HashMap<String, Model>();
  }

  public void setSigDuraMax(int aSigDuraMax) {
	mySigDuraMax = Math.max(mySigDuraMax, aSigDuraMax);
  }

  public void setAlarm(String aName) {
	setAlarm(aName, 1);
  }

  public void setAlarm(String aName, int aCount) {
	int alarmCount = 0;
	if (myAlarms.containsKey(aName)) {
	  alarmCount = myAlarms.get(aName);
	}
	myAlarms.put(aName, alarmCount + aCount);
  }

  public Map<String, Integer> getAlarms() {
	return Collections.unmodifiableMap(myAlarms);
  }

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

  public void addAttribute(SensorAttribute aAttr) {
	for (SensorAttribute attr : myAttributes) {
	  if (aAttr.getNameKey() == attr.getNameKey()) {
		attr.merge(aAttr);
		return;
	  }
	}
	SensorAttribute attribute = new SensorAttribute(aAttr);
	attribute.noAvgValue();
	myAttributes.add(attribute);
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

  public Flight addFlight(Flight.FlightDetection aType, LocalDateTime aTime, int aFlightDuration, JetiLogDataScanner aLogData) {
	Flight f = null;
	if (!myFlightList.isEmpty()) {
	  f = myFlightList.get(myFlightList.size() - 1);
	}

	if (null != f && Math.abs((f.getStartTime().atZone(ZoneId.systemDefault()).toEpochSecond()
		- aTime.atZone(ZoneId.systemDefault()).toEpochSecond())) < 20) {
	  // an other detector has created this flight, so ad only the attributes missing
	  ourLogger.info("resusing flight: " + f);
	} else {
	  f = Flight.createFlight(aType, this, aTime, aFlightDuration, aLogData);
	  myFlightList.add(f);
	  ourLogger.info("adding flight: " + f);
	}
	return f;

  }

  public Flight getCurrentFlight() {
	return myFlightList.get(myFlightList.size() - 1);
  }

  public String toString() {
	if (this.getFlightCount() == 0) {
	  return "";
	}
	int identation = 30;
	StringBuffer out = new StringBuffer();
	out.append(NLS.get(NLSKey.CO_MODEL, identation) + "  : ");
	out.append(this.getName());
	out.append(System.getProperty("line.separator"));
	String name;
	out.append("  " + NLS.get(NLSKey.CO_FLIGHT_COUNT, identation) + ": ");
	out.append(this.getFlightCount());
	out.append(System.getProperty("line.separator"));
	out.append("  " + NLS.fillWithBlanks(NLS.get(NLSKey.CO_FLIGHTDURATION) + " " + NLS.get(NLSKey.CO_TOTAL), identation)
		+ ": ");
	out.append(TimeDuration.getString(this.getFlightTime()));
	out.append(System.getProperty("line.separator"));
	out.append(
		"  " + NLS.fillWithBlanks(NLS.get(NLSKey.CO_LOGDURATION) + " " + NLS.get(NLSKey.CO_TOTAL), identation) + ": ");
	out.append(TimeDuration.getString(this.getLogTime()));
	out.append(System.getProperty("line.separator"));
	for (SensorAttribute attr : myAttributes) {
	  out.append("  " + NLS.fillWithBlanks(attr.getName(), identation) + ": ");
	  out.append(attr.getValueString());
	  out.append(System.getProperty("line.separator"));
	}

	if (myAlarms != null && !myAlarms.isEmpty()) {
	  out.append("  " + NLS.get(NLSKey.CO_ALARMS) + ":\n");

	  List<String> alarmList = new ArrayList<String>(myAlarms.keySet());
	  alarmList.sort(Comparator.naturalOrder());
	  for (String alarm : alarmList) {
		out.append("    " + NLS.fillWithBlanks(alarm, identation-2) + ": " + myAlarms.get(alarm));
		out.append(System.getProperty("line.separator"));
	  }
	}
	out.append("  " + NLS.get(NLSKey.CO_FLIGHTS, identation) + "");
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


  public void setVoltageMin(float aValue) {
	myMinVoltage = aValue;
  }

  public float getVoltageMin() {
	return myMinVoltage;
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
	Map<String, Integer> allAlarms = new HashMap<String, Integer>();
	StringBuffer modelOut = new StringBuffer();
	modelOut.append("\n" + NLS.get(NLSKey.CO_MODEL_STATISTIC) + " (" + Model.getModelCollection().size() + " "
		+ NLS.get(NLSKey.CO_MODELS) + "):\n");
	// System.out.println(out);
	for (Model model : Model.getModelCollection()) {
	  modelOut.append(model);
	  // System.out.println(model);
	  ourLogger.info(model.toString());
	  cntLog += model.getLogCount();
	  timeLogs += model.getLogTime();
	  cntFlights += model.getFlightCount();
	  timeFlights += model.getFlightTime();
	  Map<String, Integer> alarmMap = model.getAlarms();
	  if (alarmMap != null && !alarmMap.isEmpty()) {
		for (String alarm : alarmMap.keySet()) {
		  int alarmCount = 0;
		  if (allAlarms.containsKey(alarm)) {
			alarmCount = allAlarms.get(alarm);
		  }
		  allAlarms.put(alarm, alarmCount + alarmMap.get(alarm));
		}
	  }
	}

	int indentation = 30;
	StringBuffer totalOut = new StringBuffer();
	totalOut.append("\n" + NLS.get(NLSKey.CO_STATISTIC_TOTAL, indentation + 2) + "");
	totalOut.append("\n");
	totalOut
		.append("  " + NLS.fillWithBlanks(NLS.get(NLSKey.CO_LOG_COUNT) + " " + NLS.get(NLSKey.CO_TOTAL), indentation)
			+ ": " + cntLog);
	totalOut.append("\n");
	totalOut
		.append("  " + NLS.fillWithBlanks(NLS.get(NLSKey.CO_LOGDURATION) + " " + NLS.get(NLSKey.CO_TOTAL), indentation)
			+ ": " + TimeDuration.getString(timeLogs));
	totalOut.append("\n");
	totalOut.append(
		"  " + NLS.fillWithBlanks(NLS.get(NLSKey.CO_MODEL_COUNT), indentation) + ": " + ourModels.entrySet().size());
	totalOut.append("\n");
	totalOut
		.append("  " + NLS.fillWithBlanks(NLS.get(NLSKey.CO_FLIGHT_COUNT) + " " + NLS.get(NLSKey.CO_TOTAL), indentation)
			+ ": " + cntFlights);
	totalOut.append("\n");
	totalOut.append(
		"  " + NLS.fillWithBlanks(NLS.get(NLSKey.CO_FLIGHTDURATION) + " " + NLS.get(NLSKey.CO_TOTAL), indentation)
			+ ": " + TimeDuration.getString(timeFlights));
	totalOut.append("\n");
	if (allAlarms != null && !allAlarms.isEmpty()) {
	  totalOut.append("  " + NLS.get(NLSKey.CO_ALARMS, indentation) + ":\n");
	  List<String> alarmList = new ArrayList<String>(allAlarms.keySet());
	  alarmList.sort(Comparator.naturalOrder());
	  for (String alarm : alarmList) {
		totalOut.append("    " + NLS.fillWithBlanks(alarm, indentation - 2) + ": " + allAlarms.get(alarm));
		totalOut.append(System.getProperty("line.separator"));
	  }
	}
	ourLogger.info(totalOut.toString());

	System.out.println(modelOut);
	System.out.println(totalOut);

  }
}
