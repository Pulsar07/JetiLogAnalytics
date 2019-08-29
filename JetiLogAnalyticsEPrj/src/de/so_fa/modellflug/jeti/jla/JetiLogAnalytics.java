package de.so_fa.modellflug.jeti.jla;

/*
 * Version history:
 * 0.1.6: 
 * 0.1.7: signal flight detection added
 */

import java.io.File;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.logging.Level;
import java.util.logging.Logger;

import de.so_fa.modellflug.jeti.jla.NLS.NLSKey;
import de.so_fa.modellflug.jeti.jla.NLS.NLSLang;
import de.so_fa.modellflug.jeti.jla.datamodel.Model;
import de.so_fa.modellflug.jeti.jla.detectors.FlightDetectorHeight;
import de.so_fa.modellflug.jeti.jla.detectors.FlightDetectorSignalStrength;
import de.so_fa.modellflug.jeti.jla.detectors.MaxSpeedDetector;
import de.so_fa.modellflug.jeti.jla.log.JetiLogDataScanner;
import de.so_fa.utils.log.MyLogger;

public class JetiLogAnalytics {

  /*
   * Version history:
   * 
   * 0.1.7 : 08/2019 RS :  refactored JetiLogDataScanner with support for ISensorObserver interface
   * 0.1.8 : 08/2019 RS :  some ignore/fix "holes" in time stamp and sensor id as in some old log files existing 
   *                       recalculate different sensor value units (m/s, mpt, ft) to the ISO system units km/h and m
   */
  private static final String VERSION = "0.1.8";
  private static final String APP_NAME = "JetiLogAnalytics";
  private final static Logger ourLogger = Logger.getLogger(JetiLogAnalytics.class.getName());
  static File ourFolder;

  public static void main(String[] args) {
	try {

	  String logFolderName = "/home/stransky/Links/Modellflug/JETI/log/Log";
	  logFolderName = "/home/stransky/Downloads/jls/Logs";
	  logFolderName = "/home/stransky/Links/Modellflug/JETI/log/Log";
	  logFolderName = "/home/stransky/Links/Modellflug/JETI/logTest";

	  boolean externCall = false;
	  MyLogger.setup(APP_NAME);
	  NLS.getInstance().setLang(NLSLang.EN);

	  for (int i = 0; i < args.length; i++) {
		if (args[i].equals("-nls") || args[i].equals("-NLS")) {
		  i++;
		  if (args[i].equals("DE")) {
			NLS.getInstance().setLang(NLSLang.DE);
		  }
		}
		if (args[i].equals("-d") || args[i].equals("-dir")) {
		  i++;
		  externCall = true;
		  logFolderName = args[i];
		  // System.out.println("using log folder: " + args[0]);
		  MyLogger.setup(APP_NAME, "/var/log/jla");
		}

	  }
	  System.out.println(APP_NAME + " (JLA)" + " Version: " + VERSION);

	  ourLogger.info("START/RESTART of " + APP_NAME + " " + VERSION);

	  if (false) {
		testCode();
	  }

	  File[] files = new File(logFolderName).listFiles();

	  LocalDate fromDate = LocalDate.of(2019, 5, 2);
	  LocalDate toDate = LocalDate.of(2019, 5, 2);
	  if (true) {
		fromDate = null;
		toDate = null;

	  }

	  if (true) {
		fromDate = LocalDate.of(2019, 1, 5);
		toDate = LocalDate.of(2019, 8, 5);
		toDate = null;
		toDate = null;
		
		
		toDate = LocalDate.of(2018, 5, 30);
		fromDate = LocalDate.of(2019, 8, 20);
		fromDate = null;
		toDate = null;
	  }
	
//		LocalDate fromDate = LocalDate.of(2018, 01, 01);
//		LocalDate toDate = LocalDate.of(2018,12, 31);
	  if (externCall) {
		fromDate = null;
		toDate = null;

	  }

	  traverseJetiLogFiles(files, null, fromDate, toDate);

	  JetiLogDataScanner.addSensorObserver(new FlightDetectorHeight());
	  JetiLogDataScanner.addSensorObserver(new FlightDetectorSignalStrength());
	  JetiLogDataScanner.addSensorObserver(new MaxSpeedDetector());
	  
	  System.out.println("\n" + NLS.get(NLSKey.KEY_READLOG) + ":");
	  JetiLogDataScanner.analyseData();
	  Model.printResult();
	} catch (Throwable e) {
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
}