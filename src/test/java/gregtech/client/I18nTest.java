package gregtech.client;

import gregtech.Bootstrap;

import net.minecraft.client.resources.I18n;

import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.is;

public class I18nTest {

    @BeforeAll
    public static void bootstrap() {
        Bootstrap.perform();
    }

    @Test
    public void testI18n() {
        MatcherAssert.assertThat(I18n.format("sus"), is("sus"));
        MatcherAssert.assertThat(I18n.format("sus", "Sus"), is("sus"));
    }
}
