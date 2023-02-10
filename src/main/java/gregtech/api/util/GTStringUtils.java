package gregtech.api.util;

import gregtech.api.block.machines.MachineItemBlock;
import gregtech.api.items.materialitem.MetaPrefixItem;
import gregtech.api.items.metaitem.MetaItem;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.pipenet.block.material.BlockMaterialPipe;
import gregtech.api.recipes.ingredients.IntCircuitIngredient;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.ore.OrePrefix;
import gregtech.common.blocks.BlockCompressed;
import gregtech.common.blocks.BlockFrame;
import gregtech.common.items.MetaItems;
import net.minecraft.block.Block;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;

public final class GTStringUtils {

    private GTStringUtils() {/**/}

    @Nonnull
    public static String prettyPrintItemStack(@Nonnull ItemStack stack) {
        if (stack.getItem() instanceof MetaItem) {
            MetaItem<?> metaItem = (MetaItem<?>) stack.getItem();
            MetaItem.MetaValueItem metaValueItem = metaItem.getItem(stack);
            if (metaValueItem == null) {
                if (metaItem instanceof MetaPrefixItem) {
                    Material material = ((MetaPrefixItem) metaItem).getMaterial(stack);
                    OrePrefix orePrefix = ((MetaPrefixItem) metaItem).getOrePrefix();
                    return "(MetaItem) OrePrefix: " + orePrefix.name + ", Material: " + material + " * " + stack.getCount();
                }
            } else {
                if (MetaItems.INTEGRATED_CIRCUIT.isItemEqual(stack)) {
                    return "Config circuit #" + IntCircuitIngredient.getCircuitConfiguration(stack);
                }
                return "(MetaItem) " + metaValueItem.unlocalizedName + " * " + stack.getCount();
            }
        } else if (stack.getItem() instanceof MachineItemBlock) {
            MetaTileEntity mte = GTUtility.getMetaTileEntity(stack);
            if (mte != null) {
                String id = mte.metaTileEntityId.toString();
                if (mte.metaTileEntityId.getNamespace().equals("gregtech"))
                    id = mte.metaTileEntityId.getPath();
                return "(MetaTileEntity) " + id + " * " + stack.getCount();
            }
        } else {
            Block block = Block.getBlockFromItem(stack.getItem());
            String id = null;
            if (block instanceof BlockCompressed) {
                id = "block" + ((BlockCompressed) block).getGtMaterial(stack.getMetadata()).toCamelCaseString();
            } else if (block instanceof BlockFrame) {
                id = "frame" + ((BlockFrame) block).getGtMaterial(stack.getMetadata()).toCamelCaseString();
            } else if (block instanceof BlockMaterialPipe) {
                id = ((BlockMaterialPipe<?, ?, ?>) block).getPrefix().name + ((BlockMaterialPipe<?, ?, ?>) block).getItemMaterial(stack).toCamelCaseString();
            }

            if (id != null) {
                return "(MetaBlock) " + id + " * " + stack.getCount();
            }
        }
        //noinspection ConstantConditions
        return stack.getItem().getRegistryName().toString() + " * " + stack.getCount() + " (Meta " + stack.getItemDamage() + ")";
    }

    /**
     * Copied and pasted from {@link net.minecraft.util.StringUtils#ticksToElapsedTime(int)} in order to be accessible
     * Server-Side
     *
     * @param ticks the amount of ticks to convert
     * @return the time elapsed for the given number of ticks, in "mm:ss" format.
     */
    @Nonnull
    public static String ticksToElapsedTime(int ticks) {
        int seconds = ticks / 20;
        int minutes = seconds / 60;
        seconds = seconds % 60;
        //noinspection StringConcatenationMissingWhitespace
        return seconds < 10 ? minutes + ":0" + seconds : minutes + ":" + seconds;
    }

    /**
     * Draws a String centered within a given width.
     * If the String exceeds the given width, it is cutoff
     *
     * @param stringToDraw The String to draw
     * @param fontRenderer An instance of the MC FontRenderer
     * @param maxLength The maximum width of the String
     */
    public static void drawCenteredStringWithCutoff(String stringToDraw, FontRenderer fontRenderer, int maxLength) {

        //Account for really long names
        if (fontRenderer.getStringWidth(stringToDraw) > maxLength) {
            stringToDraw = fontRenderer.trimStringToWidth(stringToDraw, maxLength - 3, false) + "...";
        }

        //Ensure that the string is centered
        int startPosition = (maxLength - fontRenderer.getStringWidth(stringToDraw)) / 2;

        fontRenderer.drawString(stringToDraw, startPosition, 1, 0x111111);
    }
}
