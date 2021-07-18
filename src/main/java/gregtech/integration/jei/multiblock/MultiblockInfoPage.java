package gregtech.integration.jei.multiblock;

import gregtech.api.GTValues;
import gregtech.api.metatileentity.multiblock.MultiblockControllerBase;
import gregtech.api.util.ItemStackHashStrategy;
import gregtech.common.metatileentities.MetaTileEntities;
import it.unimi.dsi.fastutil.Hash;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenCustomHashMap;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public abstract class MultiblockInfoPage {

    private final Hash.Strategy<ItemStack> strategy = ItemStackHashStrategy.comparingAllButCount();

    private Map<ItemStack, List<ITextComponent>> blockTooltips = new Object2ObjectOpenCustomHashMap<>(strategy);

    private static final ITextComponent defaultText = new TextComponentTranslation("gregtech.multiblock.preview.any_hatch").setStyle(new Style().setColor(TextFormatting.GREEN));

    public abstract MultiblockControllerBase getController();

    public abstract List<MultiblockShapeInfo> getMatchingShapes();

    public abstract String[] getDescription();

    public float getDefaultZoom() {
        return 1.0f;
    }

    public List<String> informationText() {

        return Stream.of("gregtech.multiblock.preview.tilt", "gregtech.multiblock.preview.zoom",
                "gregtech.multiblock.preview.pan", "gregtech.multiblock.preview.move", "gregtech.multiblock.preview.reset")
                .map(I18n::format)
                .collect(Collectors.toList());
    }

    /**
     * Gets the Map containing all tooltips for Blocks that have had tooltips applied.
     * Will generate the map if it does not exist
     * @return - The Map containing Tooltips and Formatting for specific Blocks
     */
    public Map<ItemStack, List<ITextComponent>> getBlockTooltipMap() {
        // Generate on first use
        if (this.blockTooltips.size() == 0) {
            generateBlockTooltips();
        }
        return this.blockTooltips;
    }

    /**
     * Contains the default tooltips that will be applied to all default Blocks
     */
    protected void generateBlockTooltips() {

        for(int i  = 0; i < GTValues.V.length; i++) {
            addBlockTooltip(MetaTileEntities.ITEM_EXPORT_BUS[i].getStackForm(), defaultText);
            addBlockTooltip(MetaTileEntities.ITEM_IMPORT_BUS[i].getStackForm(),  defaultText);
            addBlockTooltip(MetaTileEntities.FLUID_EXPORT_HATCH[i].getStackForm(),  defaultText);
            addBlockTooltip(MetaTileEntities.FLUID_IMPORT_HATCH[i].getStackForm(),  defaultText);
        }
    }

    /**
     * A Helper method for adding tooltips to Blocks in the multiblock preview screen.
     * Can be called if {@link MultiblockInfoPage#generateBlockTooltips()} is not overridden
     * Will add tooltips to MultiblockAbilities with existing tooltips
     * @param itemStack - The ItemStack form of the Block to add a Tooltip too
     * @param tooltip - An ITextComponent object consisting of the tooltip and format to add to the block
     */
    protected void addBlockTooltip(ItemStack itemStack, ITextComponent tooltip) {

        List<ITextComponent> tooltipList = this.blockTooltips.getOrDefault(itemStack, null);

        if(tooltipList == null) {
            List<ITextComponent> tooltipToAdd = new ArrayList<>();
            tooltipToAdd.add(tooltip);
            this.blockTooltips.put(itemStack, tooltipToAdd);
        }
        else {
            tooltipList.add(tooltip);
            this.blockTooltips.put(itemStack, tooltipList);

        }
    }
}
