package de.so_fa.modellflug.jeti.jla;

import java.io.File;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.logging.Level;
import java.util.logging.Logger;

import de.so_fa.modellflug.jeti.jla.datamodel.Flight;
import de.so_fa.modellflug.jeti.jla.datamodel.Model;
import de.so_fa.modellflug.jeti.jla.datamodel.Total;
import de.so_fa.modellflug.jeti.jla.detectors.AlarmDetector;
import de.so_fa.modellflug.jeti.jla.detectors.DistanceDetector;
import de.so_fa.modellflug.jeti.jla.detectors.FlightDetectorSignalStrength;
import de.so_fa.modellflug.jeti.jla.detectors.FlightHeightDetector;
import de.so_fa.modellflug.jeti.jla.detectors.RXQDetector;
import de.so_fa.modellflug.jeti.jla.detectors.SpeedDetector;
import de.so_fa.modellflug.jeti.jla.detectors.VoltageDetector;
import de.so_fa.modellflug.jeti.jla.gui.JLAGui;
import de.so_fa.modellflug.jeti.jla.jetilog.JetiLogDataScanner;
import de.so_fa.modellflug.jeti.jla.lang.NLS;
import de.so_fa.modellflug.jeti.jla.lang.NLS.NLSKey;
import de.so_fa.modellflug.jeti.jla.lang.NLS.NLSLang;
import de.so_fa.utils.log.MyLogger;

public class JetiLogAnalytics {

  /*
   * Version history:
   * 
   * 0.1.7 : 08/2019 RS : refactored JetiLogDataScanner with support for
   * ISensorObserver interface
   * 
   * 0.1.8 : 08/2019 RS : some ignore/fix "holes" in time stamp and sensor id as
   * in some old log files existing recalculate different sensor value units (m/s,
   * mpt, ft) to the ISO system units km/h and m
   * 
   * 0.1.9 : 09/2019 RS : added AlarmDetector to show alarms per flight / model /
   * overall
   * 
   * 0.1.10 : 09/2019 RS : some beautifications in format of result output
   * 
   * 0.1.11 : 09/2019 RS : bugfix in class FlightDetectorSignalStrength, fixed
   * initialization bug for switching log files
   * 
   * 0.2.0 : 03/2020 RS : added JavaFX GUI and RXQ
   * (http://www.so-fa.de/nh/JetiSensorRXQ) Detector
   * 
   * 0.2.1 : 03/2020 RS : more controls to filter result data (model, flight,
   * devices)
   *
   * 0.2.2 : 03/2020 RS : more controls to filter result data (model, flight,
   * devices)
   *
   * 0.2.3 : 03/2020 RS : some minor fixes and doc
   * 
   * 0.2.4 : 03/2020 RS : major bug for negative sensor values fixed,
   * FlightHeightDetector optimized
   * 
   * 0.2.5 : 03/2020 RS : more details in total statistic
   * 
   * 0.2.6 : 01/2023 RS : fixed confusion with height detection of height sensor names
   * 
   */
  public static final String VERSION = "0.2.6";
  public static final String APP_NAME = "JetiLogAnalytics";
  private final static Logger ourLogger = Logger.getLogger(JetiLogAnalytics.class.getName());
  static File ourLogFolder;
  static LocalDate ourFromDate = null;
  static LocalDate ourToDate = null;

  static private boolean ourGUISupport = true;
  private static boolean ourInterruptProcessing;

  public static void main(String[] aArgs) {
	try {
	  MyLogger.setup(APP_NAME);

	  for (int i = 0; i < aArgs.length; i++) {
		if (aArgs[i].equals("-nls") || aArgs[i].equals("-NLS")) {
		  i++;
		  if (aArgs[i].equals("DE")) {
			NLS.getInstance().setLang(NLSLang.DE);
		  }
		}
		if (aArgs[i].equals("-d") || aArgs[i].equals("-dir") || aArgs[i].equals("--dir")) {
		  i++;
		  ourLogFolder = new File(aArgs[i]);
		  if (!ourLogFolder.isDirectory()) {
			ourLogger.severe("--dir argument is not a directory");
			System.exit(-1);
		  }
		  // System.out.println("using log folder: " + args[0]);
		  MyLogger.setup(APP_NAME, "/var/log/jla");
		}
		if (aArgs[i].equals("-nogui") || aArgs[i].equals("--nogui")) {
		  ourGUISupport = false;
		}

		if (aArgs[i].equals("--from") | aArgs[i].equals("-from")) {
		  i++;
		  try {
			ourFromDate = LocalDate.parse(aArgs[i]);
		  } catch (DateTimeParseException e) {
			ourLogger.severe("--from - argument with invalid format, only \"2016-08-16\" is supported");
			System.exit(-1);
		  }
		}
		if (aArgs[i].equals("--to") | aArgs[i].equals("-to")) {
		  i++;
		  try {
			ourToDate = LocalDate.parse(aArgs[i]);
		  } catch (DateTimeParseException e) {
			ourLogger.severe("--to - argument with invalid format, only \"2016-08-16\" is supported");
			System.exit(-1);
		  }
		}
		if (aArgs[i].equals("-?") || aArgs[i].equals("-help") || aArgs[i].equals("--help") || aArgs[i].equals("?")) {
		  System.out.println("usage: " + APP_NAME + " [option]");
		  System.out.println(
			  "scans JETI log files found in folder and printout the results of total, model, flight statistic");
		  System.out.println("Example: java -jar " + APP_NAME + "-nls DE -nogui -dir ./testData/ ");
		  System.out.println("");
		  System.out.println("options:");
		  System.out.println(" --nogui                      commndline mode and textoutput only application");
		  System.out.println(" --dir <path to log-folder>   path used in command line mode");
		  System.out.println(
			  " --from <YYYY-MM-DD>          date to start analysing log files, if omitted all log files found are analysed");
		  System.out.println(
			  " --to <YYYY-MM-DD>            date to end analysing log files, if omitted all log files found are analysed");
		  System.exit(0);
		}
	  }
	  System.out.println(APP_NAME + " (JLA)" + " Version: " + VERSION);

	  ourLogger.info("START/RESTART of " + APP_NAME + " " + VERSION);

//	  if (false) {
//		testCode();
//	  }

	  if (ourGUISupport) {
		JLAGui.startGUI(aArgs);
	  } else {
		if (ourLogFolder == null || !ourLogFolder.isDirectory()) {
		  ourLogger.severe("either no log folder given or not a directory");
		  System.exit(-1);
		}
		JetiLogAnalyticsController.getInstance().setFromRange(ourFromDate);
		JetiLogAnalyticsController.getInstance().setToRange(ourToDate);
		JetiLogAnalytics.startAnalysis(ourLogFolder);
	  }
	} catch (Throwable e) {
	  ourLogger.log(Level.SEVERE, "unexpected error:", e);
	}
  }
  

  public static void startAnalysis(File aJetiLogFolder) {
	try {
	  ourInterruptProcessing = false;
	  JetiLogDataScanner.init();
	  Model.init();
	  Flight.init();
	  File[] files = aJetiLogFolder.listFiles();

	  traverseJetiLogFiles(files, null, JetiLogAnalyticsController.getInstance().getFromRange(),
		  JetiLogAnalyticsController.getInstance().getToRange());

	  if (JetiLogAnalyticsController.getInstance().getFromRange() == null
		  || JetiLogAnalyticsController.getInstance().getFromRange().equals(LocalDate.MIN)) {
		JetiLogAnalyticsController.getInstance().setFromRange(JetiLogDataScanner.getFirstDate());
	  }
	  if (JetiLogAnalyticsController.getInstance().getToRange() == null
		  || JetiLogAnalyticsController.getInstance().getToRange().equals(LocalDate.MAX)) {
		JetiLogAnalyticsController.getInstance().setToRange(JetiLogDataScanner.getLastDate());
	  }
	  JetiLogDataScanner.addSensorObserver(new FlightHeightDetector());
	  JetiLogDataScanner.addSensorObserver(new FlightDetectorSignalStrength());
	  JetiLogDataScanner.addSensorObserver(new SpeedDetector());
	  JetiLogDataScanner.addSensorObserver(new AlarmDetector());
	  JetiLogDataScanner.addSensorObserver(new RXQDetector());
	  JetiLogDataScanner.addSensorObserver(new DistanceDetector());
	  JetiLogDataScanner.addSensorObserver(new VoltageDetector());

	  System.out.println("\n" + NLS.get(NLSKey.CO_KEY_READLOG) + ":");
	  JetiLogDataScanner.analyseData();
	  Model.printResult();
	  Total.printResult();
	} catch (

	Throwable e) {
	  ourLogger.log(Level.SEVERE, "unexpected error in Navigator:", e);
	}

  }

  public static void traverseJetiLogFiles(File[] files, File aFolder, LocalDate aFromDate, LocalDate aToDate) {
	if (null == aFromDate) {
	  aFromDate = LocalDate.MIN;
	}
	if (null == aToDate) {
	  aToDate = LocalDate.MAX;
	}

	for (File file : files) {
	  if (file.isDirectory()) {
		LocalDate folderDate = JetiLogDataScanner.getLocalDate(file);

		if (null == folderDate) {
		  ourLogger.fine("traversing non Jeti log folder: " + file.getName() + ":  " + folderDate);
		  // folder is not a JETI log folder, maybe is is a subfolder with JETI log
		  // folders, so traverse further ;-)
		  traverseJetiLogFiles(file.listFiles(), file, aFromDate, aToDate); // Calls same method again.
		} else if (!folderDate.isBefore(aFromDate) && !folderDate.isAfter(aToDate)) {
		  // JETI log folder is in time range so traverse furhter
		  ourLogger.fine("traversing Jeti log folder: " + file.getName() + ":  " + folderDate);
		  traverseJetiLogFiles(file.listFiles(), file, aFromDate, aToDate); // Calls same method again.
		} else {
		  ourLogger.fine("ignoring Jeti log folder due to time conditions: " + file.getName() + ":  " + folderDate);
		}
	  } else {
		ourLogger.fine("adding Jeti log: ################" + file.getName());
		JetiLogDataScanner.addNewLog(file, aFolder);
	  }
	}
  }

  public static void testCode() {
	String ts = "20180531\\15-57-44.log";
	ts = ts.substring(0, 17).replace('\\', ':').replace('/', ':');

	System.out.println("date : " + ts);
	DateTimeFormatter parseFormatter = DateTimeFormatter.ofPattern("yyyyMMdd:HH-mm-ss");
	DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
	LocalDateTime ld = LocalDateTime.parse(ts, parseFormatter);
	System.out.println("date : " + ts + " = " + ld.format(outputFormatter));
	System.exit(0);
  }

  public static void stop() {
	ourInterruptProcessing = true;
  }

  public static boolean isStopped() {
	return ourInterruptProcessing;
  }
}