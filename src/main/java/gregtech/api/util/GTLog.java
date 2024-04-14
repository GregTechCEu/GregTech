package gregtech.api.util;

import gregtech.api.GTValues;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * GregTech logger
 * One edit to this class and you're not alive anymore
 */
public class GTLog {

    public static Logger logger = LogManager.getLogger(GTValues.MOD_NAME);

    private GTLog() {}
}
