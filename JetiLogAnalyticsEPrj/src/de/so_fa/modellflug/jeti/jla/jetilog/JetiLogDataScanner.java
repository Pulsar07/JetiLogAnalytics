package de.so_fa.modellflug.jeti.jla.jetilog;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.so_fa.modellflug.jeti.jla.JetiLogAnalytics;
import de.so_fa.modellflug.jeti.jla.JetiLogAnalyticsController;
import de.so_fa.modellflug.jeti.jla.datamodel.Flight;
import de.so_fa.modellflug.jeti.jla.datamodel.IFlightListener;
import de.so_fa.modellflug.jeti.jla.datamodel.Model;
import de.so_fa.modellflug.jeti.jla.detectors.ISensorObserver;
import de.so_fa.modellflug.jeti.jla.detectors.ISensorValueHandler;
import de.so_fa.modellflug.jeti.jla.lang.NLS;
import de.so_fa.modellflug.jeti.jla.lang.NLS.NLSKey;

public class JetiLogDataScanner implements Comparable<JetiLogDataScanner>, IFlightListener {

  private final static Logger ourLogger = Logger.getLogger(JetiLogDataScanner.class.getName());

  private static List<JetiLogDataScanner> ourLogDataList;
  private static List<ISensorObserver> ourSensorObservers;

  static private final long HEADER = 0;

  File myLogFile;
  File myDateFolder;
  boolean isJetiLog = false;
  String myModelName;
  boolean isHeaderScanFinished = false;
  LocalDateTime myLogTime = null;
  int myLogDuration = 0;
  int myFlightCnt = 0;
  long myStartOffset = 0;
  long myActualTime = 0;
  boolean myIgnoreMark = false;
  
  public boolean getIgnoreMark() {
	return myIgnoreMark;
  }

  public static void addSensorObserver(ISensorObserver aObserver) {
	ourSensorObservers.add(aObserver);
  }

  public static void init() {
	ourSensorObservers = new ArrayList<ISensorObserver>();
	ourLogDataList = new ArrayList<JetiLogDataScanner>();
  }

  public static void addNewLog(File aFile, File aFolder) {
	// "20180531\\15-57-44.log";
	// ToDo if (aFolder.getName().matches([]))
	ourLogger.info("add new LogData: " + aFolder + "/" + aFile.getName());
	if (aFile == null || !JetiLogDataScanner.validateLogFile(aFolder, aFile)) {
	  ourLogger.info("ignoring log file : " + aFile);
	  // String folderString = null == aFolder ? "" : aFolder.getName();
	  // System.out.println(NLS.get(NLSKey.CO_LOG_FILE_NOT_AS_EXPECTED) + ": " + folderString + "/" + aFile.getName());
	  return;
	}
	ourLogDataList.add(new JetiLogDataScanner(aFile, aFolder));
  }

  public int getFlightCnt() {
	return myFlightCnt;
  }

  public int getLogDuration() {
	return myLogDuration;
  }

  public JetiLogDataScanner(File aFile, File aFolder) {
	myLogFile = aFile;
	myDateFolder = aFolder;
	myLogTime = parseLogFileTime(aFolder, aFile);
  }

  public LocalDateTime getLogTime() {
	return myLogTime;
  }

  public boolean isJetiLogfile() {
	return isJetiLog;
  }

  public String getModelName() {
	return myModelName;
  }

  public void analyse() {
	Flight.addFlightListener(this);
	// Open the file
	FileInputStream fstream;
	try {
	  fstream = new FileInputStream(myLogFile);
	  BufferedReader br = new BufferedReader(new InputStreamReader(fstream));

	  String strLine;

	  // Read File Line By Line
	  int lineCnt = 0;
	  while ((strLine = br.readLine()) != null) {
		if (0 == lineCnt) {
		  if (strLine.startsWith("# ")) {
			isJetiLog = true;
			myModelName = strLine.substring(2);
			  Matcher m = JetiLogAnalyticsController.getInstance().getModelFilter().matcher(myModelName);
			  if (!m.matches()) {
				// if the model name does not match with the fitler pattern, try the next one ;-)
				ourLogger.info("skipping model: " + myModelName);
				myIgnoreMark = true;
				break;
			  }
			for (ISensorObserver sensorObserver : ourSensorObservers) {
			  sensorObserver.resetValueHandler();
			  sensorObserver.newLogData(this);
			  SensorValueDescription.newHeader();
			}
		  }
		} else {
		  scanLogLine(strLine, lineCnt + 1);
		}

		lineCnt++;
	  }

	  myLogDuration = (int) (myActualTime / 1000);

	  for (ISensorObserver sensorObserver : ourSensorObservers) {
		sensorObserver.endOfLog();
	  }

	  // Close the input stream
	  fstream.close();
	  Flight.removeFlightListener(this);
	} catch (IOException e) {
	  ourLogger.log(Level.SEVERE, "problems analyising log data: " + myLogFile.getAbsolutePath(), e);
	}
  }

  private void scanLogLine(String aLine, int aLineNumber) {
	try {
	  StringTokenizer tokenizer = new StringTokenizer(aLine, ";");
	  long timeStamp = Long.parseLong(tokenizer.nextToken());
	  String idToken = tokenizer.nextToken();
	  idToken = idToken.replace(' ', '0');
	  long sensorId = Long.parseLong(idToken);
	  if (HEADER == timeStamp) { // == 0000000000
		parseLogHeader(timeStamp, sensorId, tokenizer);
	  } else {
		if (!isHeaderScanFinished) {
		  myStartOffset = timeStamp;
		}
		isHeaderScanFinished = true;
		myActualTime = timeStamp - myStartOffset;

		// read all 4-tupels till end of line / end of tokens
		while (tokenizer.hasMoreTokens()) {
		  SensorValue value = new SensorValue(myActualTime, sensorId, tokenizer);
		  if (!value.isValid()) {
			// ourLogger.severe(""+ getLogName()+ ":" + aLineNumber + ": invalid " + value);
			continue;
		  }

		  // now call all sensor observers, if the sensor value is matching
		  for (ISensorObserver sensorObserver : ourSensorObservers) {
			for (ISensorValueHandler handler : sensorObserver.getValueHandler()) {
			  for (SensorValueDescription descr : handler.getSensorDescr()) {
				if (value.is(descr)) {
				  handler.handle(value);
				}
			  }
			}
		  }
		}
		// DATA
		return;
	  }
	} catch (NumberFormatException e) {
	  ourLogger.severe("ignoring reading line: " + aLine);
	}

  }

  private void parseLogHeader(long aTimeStamp, long aSensorId, StringTokenizer aTokenizer) {
	// Example:
	// # F-Swift 3.2m
	// 000000000;4291922503;0;Tx;
	// 000000000;4390522048;0;Rx REX7A;
	// 000000000;4390522048;1;U Rx;V
	// 000000000;4390522048;2;A1;
	// 000000000;4390522048;3;A2;
	// 000000000;4390522048;4;Q;%
	// 000000000;4384121665;0;RxB RSat900;
	// 000000000;4384121665;1;U Rx;V
	// 000000000;4384121665;2;A1;
	// 000000000;4384121665;3;A2;
	// 000000000;4199312918;0;VarioGPS;
	// 000000000;4199312918;1;Latitude;
	// 000000000;4199312918;2;Longitude;
	// 000000000;4199312918;3;GPS Speed;km/h
	// 000000000;4199312918;4;Hoehe;m
	// 000000000;4199312918;5;Abs. Hoehe;m
	// 000000000;4199312918;6;Vario;m/s
	int sensorValueIdx = Integer.parseInt(aTokenizer.nextToken());
	String sensorValueName = aTokenizer.nextToken();
	String sensorValueUnit = null;
	if (aTokenizer.hasMoreElements()) {
	  sensorValueUnit = aTokenizer.nextToken();
	}
	SensorValueDescription descr = new SensorValueDescription(aSensorId, sensorValueIdx, sensorValueName,
		sensorValueUnit);
	if (descr.getIndex() == 0) {
	  // this is the one of sometimes more lines for a sensor device
	  SensorValueDescription.updateSensorDevice(descr);
	}

	for (ISensorObserver sensorObserver : ourSensorObservers) {
	  Pattern p = sensorObserver.getSensorNamePattern();
	  if (null != p) {
		Matcher m = p.matcher(descr.getName().toLowerCase());
		if (m.matches()) {
		  sensorObserver.nameMatch(descr);
		}
	  }
	}
  }

  public static boolean validateLogFile(File aLogFolder, File aLogFile) {
	boolean retVal = false;
	if (aLogFile.getName().toLowerCase().endsWith(".log")) {
	  if (null != parseLogFileTime(aLogFolder, aLogFile)) {
		return true;
	  }
	}
	return retVal;
  }

  private static LocalDateTime parseLogFileTime(File aLogFolder, File aLogFile) {
	LocalDateTime logTime = null;
	try {
	  String logFile = aLogFolder.getName() + File.separator + aLogFile.getName(); // "20180531\\15-57-44.log";
	  logFile = logFile.substring(0, 17).replace('\\', ':').replace('/', ':');

	  // System.out.println("date : " + logFile);
	  DateTimeFormatter parseFormatter = DateTimeFormatter.ofPattern("yyyyMMdd:HH-mm-ss");
	  // DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd
	  // HH:mm:ss");
	  // System.out.println("date : " + logFile + " = " +
	  // logTime.format(outputFormatter));
	  logTime = LocalDateTime.parse(logFile, parseFormatter);
	} catch (DateTimeParseException | NullPointerException | StringIndexOutOfBoundsException e) {
	}
	return logTime;
  }

  public String getLogName() {
	return myDateFolder.getName() + File.separator + myLogFile.getName();
  }

  public static void analyseData() {
	Collections.sort(ourLogDataList);
	for (JetiLogDataScanner data : ourLogDataList) {
	  try {
		if (JetiLogAnalytics.isStopped()) {
		  System.out.println(NLS.get(NLSKey.CO_ANALYSIS_STOPPED));
		  return;
		}
		ourLogger.info("scanning :" + data.getLogFolder().getName() + "/" + data.getLogFile().getName());
		data.analyse();
		if (data.getIgnoreMark()) {
		  continue;
		}
		ourLogger.info("scanning data : model: " + data.getModelName() + ", flightcnt: " + data.getFlightCnt());
		System.out.println("  " + NLS.get(NLSKey.CO_SCAN_LOG) + ": " + data.getLogFolder().getName() + "/"
			+ data.getLogFile().getName() + " : " + NLS.get(NLSKey.CO_MODEL) + ": " + data.getModelName() + ", "
			+ NLS.get(NLSKey.CO_FLIGHT_COUNT) + ": " + data.getFlightCnt());

		if (data.isJetiLogfile()) {
		  ourLogger.fine("using Jeti log file: " + data.getLogFolder().getName() + "/" + data.getLogFile().getName());
		  Model.addLogData(data);
		} else {
		  ourLogger.warning("no Jeti log file: " + data.getLogFolder().getName() + "/" + data.getLogFile().getName());
		}
	  } catch (Exception e) {
		ourLogger.severe(
			"problems analyising log file : " + data.getLogFolder().getName() + "/" + data.getLogFile().getName());
		throw e;
	  }
	}
  }

  private File getLogFolder() {
	return myDateFolder;
  }

  private File getLogFile() {
	return myLogFile;
  }

  @Override
  public int compareTo(JetiLogDataScanner aT) {
	return myLogTime.compareTo(aT.myLogTime);
  }

  /**
   * returns a LocalDate object in case the given folder is a JETI log folder with
   * format "20180531"
   * 
   * @param aFolder
   * @return LocalDate object or null if aFolder is not a folder with the expected
   *         format
   */
  public static LocalDate getLocalDate(File aFolder) {
	try {
	  DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
	  return LocalDate.parse(aFolder.getName(), formatter);
	} catch (DateTimeParseException e) {
	}
	return null;
  }

  public static boolean validateLogFolder(File aFolder) {
	final LocalDate min = LocalDate.of(1990, 1, 1);
	final LocalDate now = LocalDate.now();
	LocalDate d = getLocalDate(aFolder);
	if (null != d) {
	  if (d.isAfter(min) && d.isBefore(now)) {
		return true;
	  }
	}
	return false;
  }

  @Override
  public void flightStart() {
	// TODO Auto-generated method stub

  }

  @Override
  public void flightEnd(Flight aF) {
	myFlightCnt++;
  }

}