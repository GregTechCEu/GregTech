package gregtech.client;

import gregtech.Bootstrap;
import gregtech.api.util.GTLog;
import net.minecraft.client.resources.I18n;
import org.junit.BeforeClass;
import org.junit.Test;

public class I18nTest {

    @BeforeClass
    public static void bootstrap() {
        Bootstrap.perform();
    }

    @Test
    public void testI18n() {
        GTLog.logger.info("Test: {}", I18n.format("sus"));
        GTLog.logger.info("Test: {}", I18n.format("sus", "Sus"));
    }

}
