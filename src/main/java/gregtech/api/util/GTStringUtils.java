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
import net.minecraft.item.ItemStack;

public class GTStringUtils {

    public static String prettyPrintItemStack(ItemStack stack) {
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
}
