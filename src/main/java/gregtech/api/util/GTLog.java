package gregtech.api.util;

import org.apache.logging.log4j.Logger;

/**
 * GregTech logger
 * One edit to this class and you're not alive anymore
 */
public class GTLog {

    public static Logger logger;

    public static void init(Logger modLogger) {
        logger = modLogger;
    }

}
