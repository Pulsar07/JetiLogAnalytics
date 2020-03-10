package de.so_fa.modellflug.jeti.jla.datamodel;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import de.so_fa.modellflug.jeti.jla.JetiLogAnalyticsController;
import de.so_fa.modellflug.jeti.jla.jetilog.TimeDuration;
import de.so_fa.modellflug.jeti.jla.lang.NLS;
import de.so_fa.modellflug.jeti.jla.lang.NLS.NLSKey;
import javafx.util.converter.LocalDateStringConverter;

public class Total {
  private static Logger ourLogger = Logger.getLogger(Total.class.getName());

  public String toString() {
	int cntLog = 0;
	int cntFlights = 0;
	int timeFlights = 0;
	int timeLogs = 0;
	int modelCount = 0;
	Map<String, Integer> allAlarms = new HashMap<String, Integer>();
	StringBuffer modelOut = new StringBuffer();
	modelOut.append("\n" + NLS.get(NLSKey.CO_MODEL_STATISTIC) + " (" + Model.getModelCollection().size() + " "
		+ NLS.get(NLSKey.CO_MODELS) + "):\n");
	// System.out.println(out);
	modelCount = Model.getModelCollection().size();
	for (Model model : Model.getModelCollection()) {
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
	StringBuffer out = new StringBuffer();
	out.append("\n" + NLS.get(NLSKey.CO_STATISTIC_TOTAL, indentation + 2) + "");
	out.append("\n");
	out.append("  " + NLS.fillWithBlanks(NLS.get(NLSKey.CO_GEN_TIME_RANGE), indentation) + ": "
		+ JetiLogAnalyticsController.getInstance().getRange());
	out.append("\n");
	out.append("  " + NLS.fillWithBlanks(NLS.get(NLSKey.CO_LOG_COUNT) + " " + NLS.get(NLSKey.CO_TOTAL), indentation)
		+ ": " + cntLog);
	out.append("\n");
	out.append("  " + NLS.fillWithBlanks(NLS.get(NLSKey.CO_LOGDURATION) + " " + NLS.get(NLSKey.CO_TOTAL), indentation)
		+ ": " + TimeDuration.getString(timeLogs) + " " + NLS.get(NLSKey.CO_GEN_INHOURS));
	out.append("\n");
	out.append("  " + NLS.fillWithBlanks(NLS.get(NLSKey.CO_MODEL_COUNT), indentation) + ": " + modelCount);
	out.append("\n");
	out.append("  " + NLS.fillWithBlanks(NLS.get(NLSKey.CO_FLIGHT_COUNT) + " " + NLS.get(NLSKey.CO_TOTAL), indentation)
		+ ": " + cntFlights);
	out.append("\n");
	out.append(
		"  " + NLS.fillWithBlanks(NLS.get(NLSKey.CO_FLIGHTDURATION) + " " + NLS.get(NLSKey.CO_TOTAL), indentation)
			+ ": " + TimeDuration.getString(timeFlights) + " " + NLS.get(NLSKey.CO_GEN_INHOURS));
	out.append("\n");
	if (cntFlights != 0) {
	  out.append(
		  "  " + NLS.fillWithBlanks(NLS.get(NLSKey.CO_FLIGHTDURATION) + "-" + NLS.get(NLSKey.CO_GEN_AVG), indentation)
			  + ": " + TimeDuration.getString(timeFlights / cntFlights) + " " + NLS.get(NLSKey.CO_GEN_INHOURS));
	  out.append("\n");
	}

	out.append("  "
		+ NLS.fillWithBlanks(
			NLS.get(NLSKey.CO_FLIGHTDURATION) + " " + NLS.get(NLSKey.CO_GEN_PER_DAY) + "-" + NLS.get(NLSKey.CO_GEN_AVG),
			indentation)
		+ ": " + TimeDuration.getString(timeFlights / JetiLogAnalyticsController.getInstance().getRange()) + " "
		+ NLS.get(NLSKey.CO_GEN_INHOURS));
	out.append("\n");
	out.append("  "
		+ NLS.fillWithBlanks(NLS.get(NLSKey.CO_FLIGHTDURATION) + " " + NLS.get(NLSKey.CO_GEN_PER_WEEK) + "-"
			+ NLS.get(NLSKey.CO_GEN_AVG), indentation)
		+ ": " + TimeDuration.getString(7 * timeFlights / JetiLogAnalyticsController.getInstance().getRange()) + " "
		+ NLS.get(NLSKey.CO_GEN_INHOURS));
	out.append("\n");
	out.append("  "
		+ NLS.fillWithBlanks(NLS.get(NLSKey.CO_FLIGHTDURATION) + " " + NLS.get(NLSKey.CO_GEN_PER_MONTH) + "-"
			+ NLS.get(NLSKey.CO_GEN_AVG), indentation)
		+ ": " + TimeDuration.getString(30 * timeFlights / JetiLogAnalyticsController.getInstance().getRange()) + " "
		+ NLS.get(NLSKey.CO_GEN_INHOURS));
	out.append("\n");
	out.append("  "
		+ NLS.fillWithBlanks(NLS.get(NLSKey.CO_FLIGHTDURATION) + " " + NLS.get(NLSKey.CO_GEN_PER_YEAR) + "-"
			+ NLS.get(NLSKey.CO_GEN_AVG), indentation)
		+ ": " + TimeDuration.getString(365 * timeFlights / JetiLogAnalyticsController.getInstance().getRange()) + " "
		+ NLS.get(NLSKey.CO_GEN_INHOURS));
	out.append("\n");

	if (allAlarms != null && !allAlarms.isEmpty()) {
	  out.append("  " + NLS.get(NLSKey.CO_ALARMS, indentation) + ":\n");
	  List<String> alarmList = new ArrayList<String>(allAlarms.keySet());
	  alarmList.sort(Comparator.naturalOrder());
	  for (String alarm : alarmList) {
		out.append("    " + NLS.fillWithBlanks(alarm, indentation - 2) + ": " + allAlarms.get(alarm));
		out.append(System.getProperty("line.separator"));
	  }
	}
	ourLogger.info(out.toString());

	return out.toString();
  }

  public static void printResult() {
	Total t = new Total();
	System.out.println(t);
  }
}
