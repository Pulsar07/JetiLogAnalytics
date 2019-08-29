package de.so_fa.utils.log;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;



// this custom formatter formats parts of a log record to a single line

class ShortLoggerFormatter extends Formatter {
    private static final String LINE_SEPARATOR = System.getProperty("line.separator");

    @Override
    public String format(LogRecord record) {
        StringBuilder sb = new StringBuilder();

        sb.append(calcDate(record.getMillis()))
            .append(" ")
            .append(record.getLevel())
            .append(" [")
            .append(record.getSourceClassName().substring(record.getSourceClassName().lastIndexOf('.')+1))
            .append(".")
            .append(record.getSourceMethodName())
            .append("()]: ")
            .append(formatMessage(record))
            .append(LINE_SEPARATOR);

        if (record.getThrown() != null) {
            try {
                StringWriter sw = new StringWriter();
                PrintWriter pw = new PrintWriter(sw);
                record.getThrown().printStackTrace(pw);
                pw.close();
                sb.append(sw.toString());
            } catch (Exception ex) {
                // ignore
            }
        }

        return sb.toString();
    }




  private String calcDate(long millisecs) {

    SimpleDateFormat date_format = new SimpleDateFormat("yy.MM.dd-HH:mm:ss:mmm");

    Date resultdate = new Date(millisecs);

    return date_format.format(resultdate);

  }

} 

