package de.so_fa.modellflug.jeti.jla.lang;

import java.util.Locale;
import java.util.ResourceBundle;

public class NLS {

  static ResourceBundle ourLabelBundle;

  public static enum NLSLang {
	DE, EN
  }

  public static enum NLSKey {
	CO_KEY_READLOG, CO_SCAN_LOG, CO_FLIGHT_COUNT, CO_MODEL, CO_MODELS, CO_STATISTIC_TOTAL, CO_FLIGHT, CO_FLIGHTS, CO_FLIGHT_DETECTION_TYPE, 
	CO_FLIGHTDURATION, CO_LOG_COUNT,
	CO_TOTAL, CO_LOGDURATION, CO_MODEL_COUNT, CO_LOG_FOLDER_NOT_AS_EXPECTED, CO_LOG_FILE_NOT_AS_EXPECTED,
	CO_MODEL_STATISTIC, CO_ALARMS, CO_LOG_FILE, 
	CO_SIG_DURA,
	CO_GEN_SPEED, CO_GEN_AIR_SPEED, CO_GEN_GPS_SPEED, CO_GEN_DISTANCE, CO_GEN_HEIGHT,
	CO_GEN_MIN, CO_GEN_MAX, CO_GEN_AVG, CO_GEN_VOLTAGE,
	FX_HelpHeader, FX_HelpContent
  }

//	
//	ourEN.put(NLSKey.LOG_FILE, "log file");
//	ourDE.put(NLSKey.LOG_FILE, "Log-Datei");
//  }

  private static NLS ourInstance = null;

  public static NLS getInstance() {
	if (ourInstance == null) {
	  ourInstance = new NLS();
	}
	return ourInstance;
  }

  NLS() {
	Locale locale = Locale.getDefault();
	ourLabelBundle = ResourceBundle.getBundle("de.so_fa.modellflug.jeti.jla.gui.JLAGui", locale);

  }

  public void setLang(NLSLang aLang) {
	Locale locale = new Locale(aLang.toString().toLowerCase());
	ourLabelBundle = ResourceBundle.getBundle("de.so_fa.modellflug.jeti.jla.gui.JLAGui", locale);
  }

  public static String get(NLSKey aKey) {
	return getInstance().getString(aKey);
  }

  public static String get(NLSKey aKey, int aFillBlanksTill) {
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
	return ourLabelBundle.getString(aKey.toString());
//	throw new RuntimeException("no translation given for " + aKey);
  }
}
