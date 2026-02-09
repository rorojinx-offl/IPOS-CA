package experimental.logging;

import ch.qos.logback.core.FileAppender;
import ch.qos.logback.core.rolling.RollingFileAppender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LogVendor {
    public static final Logger logger = LoggerFactory.getLogger(LogVendor.class);

    protected void CreateLog(String msg, String logTier) {
        switch (logTier) {
            case "INFO":
                logger.info(msg);
                break;
            case "WARN":
                logger.warn(msg);
                break;
            case "ERROR":
                logger.error(msg);
                break;
            case "DEBUG":
                logger.debug(msg);
                break;
            case "TRACE":
                logger.trace(msg);
                break;
        }
    }
}
