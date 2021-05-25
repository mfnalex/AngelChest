package de.jeff_media.angelchest;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.MessageFormat;
import java.util.Date;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

public class DebugFormatter extends Formatter {

    private final Date date = new Date();
    private static final String format = "{0,time}";
    private MessageFormat formatter;
    private final Object[] args = new Object[1];
    private final String newLine = System.lineSeparator();

    @Override
    public synchronized String format(final LogRecord record) {
        final StringBuilder sb = new StringBuilder();
        date.setTime(record.getMillis());
        args[0] = date;

        // Date and time
        final StringBuffer text = new StringBuffer("[");
        if(formatter == null) {
            formatter = new MessageFormat(format);
        }
        formatter.format(args,text,null);
        sb.append(text).append("] ");



        final String message = formatMessage(record);

        sb.append(message);
        sb.append(newLine);
        if (record.getThrown() != null) {
            try {
                final StringWriter sw = new StringWriter();
                final PrintWriter pw = new PrintWriter(sw);
                record.getThrown().printStackTrace(pw);
                pw.close();
                sb.append(sw);
            } catch (final Exception ignored) {
            }
        }
        return sb.toString();
    }
}
