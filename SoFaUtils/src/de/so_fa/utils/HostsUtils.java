package de.so_fa.utils;

public class HostsUtils {
  static public String getLocalHostname() {
    String OS = System.getProperty("os.name").toLowerCase();
    String result = null;

    if (OS.indexOf("win") >= 0) {
      result = System.getenv("COMPUTERNAME");
    } else {
      if (OS.indexOf("nix") >= 0 || OS.indexOf("nux") >= 0) {
        result = System.getenv("HOSTNAME");
      }
    }
    return result;
  }

  private static final String OS = System.getProperty("os.name").toLowerCase();

  public static boolean isWindows() {
    return (OS.indexOf("win") >= 0);
  }

  public static boolean isMac() {
    return (OS.indexOf("mac") >= 0);
  }

  public static boolean isUnix() {
    return (OS.indexOf("nux") >= 0);
  }
}