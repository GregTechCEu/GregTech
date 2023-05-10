package gregtech.api.util;

import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;

import javax.annotation.Nonnull;
import java.util.regex.Pattern;

@SuppressWarnings("deprecation")
public class LocalizationUtils {

    private static final Pattern NEW_LINE_PATTERN = Pattern.compile("/n");

    /**
     * This function calls `net.minecraft.client.resources.I18n.format` when called on client
     * or `net.minecraft.util.text.translation.I18n.translateToLocalFormatted` when called on server.
     * <ul>
     *  <li>It is intended that translations should be done using `I18n` on the client.</li>
     *  <li>For setting up translations on the server you should use `TextComponentTranslatable`.</li>
     *  <li>`LocalisationUtils` is only for cases where some kind of translation is required on the server and there is no client/player in context.</li>
     *  <li>`LocalisationUtils` is "best effort" and will probably only work properly with en-us.</li>
     * </ul>
     *
     * @param key    the localization key passed to the underlying format function
     * @param format the substitutions passed to the underlying format function
     * @return the localized string.
     */
    public static String format(String key, Object... format) {
        if (FMLCommonHandler.instance().getSide() == Side.SERVER) {
            return net.minecraft.util.text.translation.I18n.translateToLocalFormatted(key, format);
        } else {
            return net.minecraft.client.resources.I18n.format(key, format);
        }
    }

    /**
     * This function calls `net.minecraft.client.resources.I18n.hasKey` when called on client
     * or `net.minecraft.util.text.translation.I18n.canTranslate` when called on server.
     * <ul>
     *  <li>It is intended that translations should be done using `I18n` on the client.</li>
     *  <li>For setting up translations on the server you should use `TextComponentTranslatable`.</li>
     *  <li>`LocalisationUtils` is only for cases where some kind of translation is required on the server and there is no client/player in context.</li>
     *  <li>`LocalisationUtils` is "best effort" and will probably only work properly with en-us.</li>
     * </ul>
     *
     * @param key the localization key passed to the underlying hasKey function
     * @return a boolean indicating if the given localization key has localisations
     */
    public static boolean hasKey(String key) {
        if (FMLCommonHandler.instance().getSide() == Side.SERVER) {
            return net.minecraft.util.text.translation.I18n.canTranslate(key);
        } else {
            return net.minecraft.client.resources.I18n.hasKey(key);
        }
    }

    /**
     * Returns translated text corresponding to given key {@code key} on current locale,
     * split with text {@code '\n'}.
     *
     * @param key  localization key
     * @param args substitutions
     * @return translated text split with text {@code '\n'}
     */
    @Nonnull
    public static String[] formatLines(String key, Object... args) {
        return NEW_LINE_PATTERN.split(format(key, args));
    }
}
