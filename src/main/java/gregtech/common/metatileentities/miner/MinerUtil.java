package gregtech.common.metatileentities.miner;

import gregtech.api.GTValues;
import gregtech.api.recipes.Recipe;
import gregtech.api.recipes.RecipeMap;
import gregtech.api.unification.OreDictUnifier;
import gregtech.api.unification.ore.OrePrefix;
import gregtech.api.util.GTLog;
import gregtech.api.util.GTUtility;
import gregtech.common.ConfigHolder;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.AxisAlignedBB;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class MinerUtil {

    private MinerUtil() {}

    /**
     * Maximum amount of blocks individual miners can scan in one tick
     */
    public static final int MAX_BLOCK_SCAN = 200;

    public static final String DISPLAY_CLICK_AREA_PREVIEW = "preview_area";
    public static final String DISPLAY_CLICK_AREA_PREVIEW_HIDE = "hide_preview_area";
    public static final String DISPLAY_CLICK_AREA_DECR = "decr_area";
    public static final String DISPLAY_CLICK_AREA_INCR = "incr_area";
    public static final String DISPLAY_CLICK_Y_LIMIT_DECR = "decr_y_limit";
    public static final String DISPLAY_CLICK_Y_LIMIT_INCR = "incr_y_limit";
    public static final String DISPLAY_CLICK_REPEAT_ENABLE = "enable_repeat";
    public static final String DISPLAY_CLICK_REPEAT_DISABLE = "disable_repeat";
    public static final String DISPLAY_CLICK_REPLACE_ORE_ENABLE = "enable_replace_ore";
    public static final String DISPLAY_CLICK_REPLACE_ORE_DISABLE = "disable_replace_ore";

    public static final AxisAlignedBB EMPTY_AABB = new AxisAlignedBB(0, 0, 0, 0, 0, 0);

    private static String oreReplacementConfigCache;
    private static IBlockState oreReplacement;

    @Nonnull
    @SuppressWarnings("deprecation")
    public static IBlockState getOreReplacement() {
        String config = ConfigHolder.machines.replaceMinedBlocksWith;
        if (Objects.equals(oreReplacementConfigCache, config)) {
            return oreReplacement;
        }

        oreReplacementConfigCache = config;

        String[] blockDescription = StringUtils.split(config, ":");
        String blockName = blockDescription.length <= 2 ? config : blockDescription[0] + ":" + blockDescription[1];
        Block block = Block.getBlockFromName(blockName);

        if (block == null) {
            GTLog.logger.error("Invalid configuration on entry 'machines/replaceMinedBlocksWith': Cannot find block with name '{}', using cobblestone as fallback.", blockName);
            return oreReplacement = Blocks.COBBLESTONE.getDefaultState();
        } else if (blockDescription.length <= 2 || blockDescription[2].isEmpty()) {
            return oreReplacement = block.getDefaultState();
        } else {
            try {
                return oreReplacement = block.getDefaultState().getBlock().getStateFromMeta(Integer.parseInt(blockDescription[2]));
            } catch (NumberFormatException ex) {
                GTLog.logger.error("Invalid configuration on entry 'machines/replaceMinedBlocksWith': Cannot parse metadata value '{}' as integer, using cobblestone as fallback.", blockDescription[2]);
                return oreReplacement = Blocks.COBBLESTONE.getDefaultState();
            }
        }
    }

    /**
     * Applies a fortune hammer to block drops based on a tier value.
     *
     * @param stack the item stack to check for recipes
     * @param drops where the drops are stored to
     * @return amount of items inserted to {@code drops}
     */
    public static int applyTieredHammerDrops(@Nonnull ItemStack stack, @Nonnull List<ItemStack> drops,
                                             int energyTier, @Nonnull RecipeMap<?> blockDropRecipeMap,
                                             int oreMultiplier) {
        Recipe recipe = blockDropRecipeMap.findRecipe(
                GTValues.V[energyTier],
                Collections.singletonList(stack),
                Collections.emptyList());
        if (recipe == null || recipe.getOutputs().isEmpty()) return 0;
        int c = 0;
        for (ItemStack output : recipe.getResultItemOutputs(GTUtility.getTierByVoltage(recipe.getEUt()), energyTier, blockDropRecipeMap)) {
            output = output.copy();
            if (oreMultiplier > 0 && OreDictUnifier.getPrefix(output) == OrePrefix.crushed) {
                output.grow(output.getCount() * oreMultiplier);
            }
            drops.add(output);
            c++;
        }
        return c;
    }
}
