package gtrmcore.api.util;

import gtrmcore.api.GTRMValues;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class GTRMLog {

    private GTRMLog() {}

    public static Logger logger = LogManager.getLogger(GTRMValues.MODID);
}
