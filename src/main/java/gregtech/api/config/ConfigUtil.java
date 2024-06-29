package gregtech.api.config;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.util.regex.Pattern;

public final class ConfigUtil {

    private static final Pattern COLON_REGEX = Pattern.compile(":");

    private ConfigUtil() {}

    /**
     * Parse an ItemStack from a string in format {@code modid:registry_name} or {@code modid:registry_name:metadata}.
     *
     * @param string the string to parse
     * @param logger the logger used to log errors
     * @return the parsed ItemStack, or {@link ItemStack#EMPTY} if parsing failed
     */
    public static @NotNull ItemStack parseItemStack(@NotNull String string, @NotNull Logger logger) {
        String[] split = COLON_REGEX.split(string);
        if (split.length < 2) {
            logger.error("Invalid string '{}', must be in format 'modid:registry_name:metadata'", string);
            return ItemStack.EMPTY;
        }

        String modid = split[0];
        String registryName = split[1];

        Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(modid, registryName));
        if (item == null) {
            logger.error("Could not find item with name '{}:{}' for string {}", modid, registryName, string);
            return ItemStack.EMPTY;
        }

        int meta = 0;

        if (split.length > 2) {
            try {
                meta = Integer.parseInt(split[2]);
            } catch (NumberFormatException e) {
                logger.error("Failed to parse ItemStack metadata for string '{}'", string, e);
                return ItemStack.EMPTY;
            }
        }

        return new ItemStack(item, 1, meta);
    }
}
