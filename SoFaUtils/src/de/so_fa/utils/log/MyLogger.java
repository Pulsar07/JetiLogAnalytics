package de.so_fa.utils.log;

import java.io.File;
import java.io.IOException;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;


public class MyLogger {
  public static void setup(String aFilename) throws SecurityException,
      IOException {
    setup(aFilename, null);
  }

  public static void setup(String aFilename, String aFilePath)
      throws SecurityException, IOException {

    String logdir;
    if (aFilePath == null) {
      logdir = System.getProperty("user.home") + File.separator + "log";
      File theDir = new File(logdir);


      // if the directory does not exist, create it
      if (!theDir.exists()) {
        try {
          theDir.mkdir();
        } catch (SecurityException se) {
          se.printStackTrace();
        }
      }
    } else {
      File theDir = new File(aFilePath);
      logdir = theDir.getAbsolutePath();
    }

    FileHandler fh = null;
    ConsoleHandler ch = null;
    String logFile = logdir + "/" + aFilename + "-%g.log";
    fh = new FileHandler(logFile, 50000, 5, true);
    System.out.println("logging to " + logFile);
    ch = new ConsoleHandler();
    Logger l = Logger.getLogger("");
    l.removeHandler(l.getHandlers()[0]);
    fh.setFormatter(new SimpleFormatter());
    ch.setFormatter(new ShortLoggerFormatter());
    l.addHandler(fh);
    l.addHandler(ch);
    // SEVERE (highest)
    // WARNING
    // INFO
    // CONFIG
    // FINE
    // FINER
    // FINEST
    l.setLevel(Level.CONFIG);
  }

}
