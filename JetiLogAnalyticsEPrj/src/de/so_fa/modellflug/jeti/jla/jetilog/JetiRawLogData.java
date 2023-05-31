package de.so_fa.modellflug.jeti.jla.jetilog;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.nio.charset.Charset;
import java.nio.file.spi.FileSystemProvider;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
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

public class JetiRawLogData {

  private final static Logger ourLogger = Logger.getLogger(JetiRawLogData.class.getName());
  static private final long HEADER = 0;
  File myLogFile;

  private String myModelName;
  private String myLatitudeIdx;
  private String myLongitudeIdx;
  private String myGPSSensorId;

  public JetiRawLogData(File aFile) {
	myLogFile = aFile;
  }

  public void anonymizeLog() {

	File path = myLogFile.getParentFile();
	ourLogger.info("log file file: " + myLogFile);
	ourLogger.info("log file path: " + path);
//	 addNewLog(file, aFolder);
//	 ourLogDataList.add(new JetiLogDataScanner(aFile, aFolder));

	BufferedReader in = null;
	PrintStream out = null;
	try {
	  out = new PrintStream(new File(myLogFile.getParentFile(), myLogFile.getName().replaceFirst(".log", "_a.log")), "ISO-8859-1");
	  in = new BufferedReader(new InputStreamReader(new FileInputStream(myLogFile), "ISO-8859-1"));
	  String strLine;

	  // Read File Line By Line
	  for (int lineCnt = 0; (strLine = in.readLine()) != null; lineCnt++) {
		if (0 == lineCnt) {
		  if (strLine.startsWith("# ")) {

			myModelName = strLine.substring(2);
			out.print(strLine + "\n");

		  } else {
			throw new RuntimeException("not a valid Jeti Log File Format");
		  }
		} else {
		  out.print(scanAndReturnAnonymizedLog(strLine, lineCnt + 1) + "\n");
		}

	  }
	  in.close();
	  out.close();
	} catch (IOException e) {
	  ourLogger.log(Level.SEVERE, "problems analyising log data: " + myLogFile.getAbsolutePath(), e);
	} finally {

	}
  }

  private String scanAndReturnAnonymizedLog(String aLine, int aLineNumber) {
	StringBuffer retVal = new StringBuffer();

	try {

	  StringTokenizer tokenizer = new StringTokenizer(aLine, ";");

	  String tvTimeStamp;
	  String tvSendorId;

	  tvTimeStamp = tokenizer.nextToken();
	  tvSendorId = tokenizer.nextToken();
	  long timeStamp = Long.parseLong(tvTimeStamp);
	  long sensorId = Long.parseLong(tvSendorId.replace(' ', '0'));

	  String tvIdx;
	  String tvName;
	  String tvUnit;

	  String tvType;
	  String tvDecimals;
	  String tvValue;

	  if (HEADER == timeStamp) { // == 0000000000
		tvIdx = tokenizer.nextToken();
		tvName = tokenizer.nextToken();
		if (tokenizer.hasMoreElements()) {
		  tvUnit = tokenizer.nextToken();
		}

		// 000000000;4199312918;1;Latitude;
		// 000000000;4199312918;2;Longitude;
		if (tvName.equalsIgnoreCase("Latitude")) {
		  myGPSSensorId = tvSendorId;
		  myLatitudeIdx = tvIdx;
		}
		if (tvName.equalsIgnoreCase("Longitude")) {
		  myGPSSensorId = tvSendorId;
		  myLongitudeIdx = tvIdx;
		}
		retVal.append(aLine);
	  } else {
		retVal.append(tvTimeStamp);
		retVal.append(";");
		retVal.append(tvSendorId);

		// read all 4-tupels till end of line / end of tokens
		while (tokenizer.hasMoreTokens()) {
		  tvIdx = tokenizer.nextToken();
		  tvType = tokenizer.nextToken();
		  tvDecimals = tokenizer.nextToken();
		  tvValue = tokenizer.nextToken();

		  if (tvSendorId.equals(myGPSSensorId) && 
			  ( tvIdx.equals(myLatitudeIdx) || tvIdx.equals(myLongitudeIdx))) {
			  retVal.append(";");
			  retVal.append(tvIdx);
			  retVal.append(";");
			  retVal.append(tvType);
			  retVal.append(";");
			  retVal.append(tvDecimals);
			  retVal.append(";");
			  retVal.append(0);
		  } else {
		  retVal.append(";");
		  retVal.append(tvIdx);
		  retVal.append(";");
		  retVal.append(tvType);
		  retVal.append(";");
		  retVal.append(tvDecimals);
		  retVal.append(";");
		  retVal.append(tvValue);
		  }

		}
		if (aLine.endsWith(";")) {
		  retVal.append(";");
		}

		// DATA
	  }
	} catch (NoSuchElementException e) {
	  // Alarm messages have the right format, but has to be returned
	  ourLogger.info("alarm: " + aLine);
	  retVal.delete(0, retVal.length());
	  retVal.append(aLine);
	} catch (NumberFormatException e) {
	  ourLogger.severe("ignoring reading line: " + aLine);
	  throw e;
	} catch (Exception e) {
	  ourLogger.severe("problems reading line: " + aLine);
	  throw e;
	}
	return retVal.toString();

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

  }
}