package experimental.logging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FirstLog {
    public static final Logger logger = LoggerFactory.getLogger(FirstLog.class);

    static void main() {
        logger.info("Hello World!");
        logger.trace("Trace message");
        logger.debug("Debug message");
        logger.warn("Warn message");
        logger.error("Error message");
    }
}
