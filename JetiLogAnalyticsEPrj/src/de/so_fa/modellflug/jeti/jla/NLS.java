package de.so_fa.modellflug.jeti.jla;

import java.util.HashMap;
import java.util.Map;

public class NLS {

  public static enum NLSLang {
	DE, EN
  }

  private static Map<NLSKey, String> ourEN = new HashMap<NLSKey, String>();
  private static Map<NLSKey, String> ourDE = new HashMap<NLSKey, String>();

  private static Map<NLSKey, String> ourMLMap = ourEN;

  public static enum NLSKey {
	KEY_READLOG, SCAN_LOG, FLIGHT_COUNT, MODEL, MODELS, MAX_HEIGHT, MAX_SPEED, AVG_SPEED,
	STATISTIC_TOTAL, FLIGHT, FLIGHT_DETECTION_TYPE, FLIGHTDURATION, LOG_COUNT, TOTAL, LOGDURATION, MODEL_COUNT, LOG_FOLDER_NOT_AS_EXPECTED,
	LOG_FILE_NOT_AS_EXPECTED, MODEL_STATISTIC
  }

  {
	ourEN.put(NLSKey.MODEL_STATISTIC, "model statistic");
	ourDE.put(NLSKey.MODEL_STATISTIC, "Modell Statistik");

	ourEN.put(NLSKey.LOG_FILE_NOT_AS_EXPECTED, "log file not as expected");
	ourDE.put(NLSKey.LOG_FILE_NOT_AS_EXPECTED, "Log Datei nicht wie erwartet");

	ourEN.put(NLSKey.LOG_FOLDER_NOT_AS_EXPECTED, "log folder not as expected");
	ourDE.put(NLSKey.LOG_FOLDER_NOT_AS_EXPECTED, "Log Verzeichnis nicht wie erwartet");

	ourEN.put(NLSKey.FLIGHT, "flight");
	ourDE.put(NLSKey.FLIGHT, "Flug");

	ourEN.put(NLSKey.FLIGHT_DETECTION_TYPE, "detection type");
	ourDE.put(NLSKey.FLIGHT_DETECTION_TYPE, "Erkennungstyp");
	
	ourEN.put(NLSKey.STATISTIC_TOTAL, "Statisic total");
	ourDE.put(NLSKey.STATISTIC_TOTAL, "Gesamtstatistik");

	ourEN.put(NLSKey.LOGDURATION, "log duration");
	ourDE.put(NLSKey.LOGDURATION, "Logzeit");

	ourEN.put(NLSKey.FLIGHTDURATION, "flight duration");
	ourDE.put(NLSKey.FLIGHTDURATION, "Flugzeit");

	ourEN.put(NLSKey.MAX_HEIGHT, "max. height (in m)");
	ourDE.put(NLSKey.MAX_HEIGHT, "max. Höhe (in m)");

	ourEN.put(NLSKey.MAX_SPEED, "max. speed (in km/h)");
	ourDE.put(NLSKey.MAX_SPEED, "max. Speed (in km/h)");

	ourEN.put(NLSKey.AVG_SPEED, "avg. speed (in km/h)");
	ourDE.put(NLSKey.AVG_SPEED, "mittl. Speed (in km/h)");

	ourEN.put(NLSKey.KEY_READLOG, "Reading Log-Files");
	ourDE.put(NLSKey.KEY_READLOG, "Lese Log-Dateien");

	ourEN.put(NLSKey.SCAN_LOG, "scanning log file");
	ourDE.put(NLSKey.SCAN_LOG, "scanne Log-Datei");

	ourEN.put(NLSKey.FLIGHT_COUNT, "number flights");
	ourDE.put(NLSKey.FLIGHT_COUNT, "Anzahl Flüge");

	ourEN.put(NLSKey.LOG_COUNT, "number logs");
	ourDE.put(NLSKey.LOG_COUNT, "Anzahl Logdateien");

	ourEN.put(NLSKey.MODEL_COUNT, "number models");
	ourDE.put(NLSKey.MODEL_COUNT, "Anzahl Modelle");

	ourEN.put(NLSKey.MODEL, "model");
	ourDE.put(NLSKey.MODEL, "Modell");
	
	ourEN.put(NLSKey.MODELS, "models");
	ourDE.put(NLSKey.MODELS, "Modelle");
	
	ourEN.put(NLSKey.TOTAL, "total");
	ourDE.put(NLSKey.TOTAL, "gesamt");

  }

  private static NLS ourInstance = null;

  public static NLS getInstance() {
	if (ourInstance == null) {
	  ourInstance = new NLS();
	}
	return ourInstance;
  }

  NLS() {
	ourMLMap = ourEN;
  }
  
  public void setLang(NLSLang aLang) {
	if (aLang == NLSLang.DE) {
	  ourMLMap = ourDE;
	}
  }

  public static  String get(NLSKey aKey) {
    return getInstance().getString(aKey);
  }

  public static  String get(NLSKey aKey, int aFillBlanksTill) {
	return fillWithBlanks(get(aKey), aFillBlanksTill);
  }

  public static String fillWithBlanks(String aS, int aFillBlanksTill) {
	StringBuffer retVal = new StringBuffer();
	retVal.append(aS);
	while (retVal.length() < aFillBlanksTill) {
	  retVal.append(" ");
	}
    return retVal.toString();
  }
  public String getString(NLSKey aKey) {
	if (ourMLMap.containsKey(aKey)) {
	  return ourMLMap.get(aKey);
	}
	if (ourEN.containsKey(aKey)) {
	  return ourEN.get(aKey);
	}
	throw new RuntimeException("no translation given for " + aKey);
  }
}
