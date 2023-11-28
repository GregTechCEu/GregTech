package gregtech.integration.jei.utils;

import gregtech.api.util.GTLog;
import gregtech.api.worldgen.config.OreDepositDefinition;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.resources.I18n;
import net.minecraft.world.DimensionType;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.fml.common.Loader;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.IntSortedSet;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;

import static gregtech.api.GTValues.MODID_AR;

/**
 * Common util methods shared between {@link gregtech.integration.jei.basic.GTOreCategory}
 * and {@link gregtech.integration.jei.basic.GTFluidVeinCategory}
 */
public class JEIResourceDepositCategoryUtils {

    /**
     * Creates a list of biomes whose weight, as described by the passed Function, differs from the passed original
     * weight
     * For use in the JEI Ore Spawn Page and JEI Fluid Spawn Page
     *
     * @param biomeFunction  A Function describing the modified weights of biomes
     * @param originalWeight The original weight of the biome
     * @return A List containing all the modified biomes and their value they are modified by
     */
    public static List<String> createSpawnPageBiomeTooltip(Function<Biome, Integer> biomeFunction, int originalWeight) {
        if (biomeFunction == OreDepositDefinition.NO_BIOME_INFLUENCE) {
            return Collections.emptyList();
        }

        Object2IntMap<Biome> modifiedBiomeMap = new Object2IntOpenHashMap<>();

        // Tests biomes against all registered biomes to find which biomes have had their weights modified
        for (Biome biome : Biome.REGISTRY) {
            int biomeWeight = biomeFunction.apply(biome);
            // Check if the biomeWeight is modified
            // Don't show non changed weights, to save room
            if (biomeWeight != 0) {
                modifiedBiomeMap.put(biome, originalWeight + biomeWeight);
            }
        }

        if (modifiedBiomeMap.isEmpty()) {
            return Collections.emptyList();
        }

        List<String> tooltip = new ArrayList<>();
        tooltip.add("");
        tooltip.add("");
        tooltip.add(I18n.format("gregtech.jei.ore.biome_weighting_title"));
        for (Object2IntMap.Entry<Biome> entry : modifiedBiomeMap.object2IntEntrySet()) {
            String biomeName = entry.getKey().getBiomeName();
            int weight = entry.getIntValue();
            if (weight <= 0) {
                tooltip.add(I18n.format("gregtech.jei.ore.biome_weighting_no_spawn", biomeName));
            } else {
                tooltip.add(I18n.format("gregtech.jei.ore.biome_weighting", biomeName, weight));
            }
        }

        return tooltip;
    }

    /**
     * Attempts to draw a comma separated list of Dimension IDs, adding dimension names to specified dimension ids
     * Will create a new line if the list would overflow the page
     *
     * @param namedDimensions A Map containing the special dimensions that should have their names added to the display
     * @param dimensionIDs    A List of all dimension IDs to draw
     * @param dimStartYPos    The Starting Y Position of the displayed dimension names
     * @param dimDisplayPosX  The Starting X Position of the displayed dimension names
     */
    public static void drawMultiLineCommaSeparatedDimensionList(Int2ObjectMap<String> namedDimensions,
                                                                int[] dimensionIDs,
                                                                FontRenderer fontRenderer,
                                                                int dimStartXPos,
                                                                int dimStartYPos,
                                                                int dimDisplayPosX) {
        for (int i = 0; i < dimensionIDs.length; i++) {
            StringBuilder stb = new StringBuilder().append(dimensionIDs[i]);
            String dimName = namedDimensions.get(dimensionIDs[i]);
            if (dimName != null) {
                stb.append(" (").append(dimName).append(')');
            }
            if (i < dimensionIDs.length - 1) {
                stb.append(", ");
            }

            String fullDimName = stb.toString();

            // Find the length of the dimension name string
            int dimDisplayLength = fontRenderer.getStringWidth(fullDimName);

            // If the length of the string would go off the edge of screen, instead increment the y position
            if (dimDisplayLength > (176 - dimDisplayPosX)) {
                dimStartYPos = dimStartYPos + Minecraft.getMinecraft().fontRenderer.FONT_HEIGHT;
                dimDisplayPosX = dimStartXPos;
            }

            fontRenderer.drawString(fullDimName, dimDisplayPosX, dimStartYPos, 0x111111);

            // Increment the dimension name display position
            dimDisplayPosX = dimDisplayPosX + dimDisplayLength;
        }
    }

    /**
     * Gather a list of all registered dimensions. Done as a Supplier so that it can be called at any time and catch
     * dimensions that are registered late
     *
     * @param filter An Optional filter to restrict the returned dimensions
     * @return A Supplier containing a list of all registered dimensions
     */
    public static int[] getAllRegisteredDimensions(@Nullable Predicate<WorldProvider> filter) {
        IntList dims = new IntArrayList();

        Map<DimensionType, IntSortedSet> dimMap = DimensionManager.getRegisteredDimensions();
        // to remove AR space dims from the dimension list
        DimensionType arSpaceDimensionType = null;

        if (Loader.isModLoaded(MODID_AR)) {
            try {
                arSpaceDimensionType = DimensionType.byName("space");
            } catch (IllegalArgumentException e) {
                GTLog.logger.error("Something went wrong with AR JEI integration, No DimensionType found");
            }
        }

        for (Map.Entry<DimensionType, IntSortedSet> e : dimMap.entrySet()) {
            if (e.getKey() == arSpaceDimensionType) continue;
            for (int num : e.getValue()) {
                if (filter == null || filter.test(DimensionManager.createProviderFor(num))) {
                    dims.add(num);
                }
            }
        }

        return dims.toIntArray();
    }
}
