package gregtech.api.recipes.crafttweaker;

import crafttweaker.annotations.ZenRegister;
import gregtech.api.block.machines.MachineItemBlock;
import gregtech.api.items.materialitem.MetaPrefixItem;
import gregtech.api.items.metaitem.MetaItem;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.pipenet.block.material.BlockMaterialPipe;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.ore.OrePrefix;
import gregtech.api.unification.stack.UnificationEntry;
import gregtech.api.util.GTLog;
import gregtech.common.blocks.BlockCompressed;
import gregtech.common.blocks.BlockFrame;
import gregtech.common.blocks.CompressedItemBlock;
import gregtech.common.blocks.FrameItemBlock;
import net.minecraft.block.Block;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenMethod;

import javax.annotation.Nullable;

import static gregtech.integration.jei.multiblock.MultiblockInfoCategory.*;

@ZenClass("mods.gregtech.general.utils")
@ZenRegister
public class CTUtilities {

    @ZenMethod("RemoveMultiblockPreviewFromJei")
    public static void removeMulti(String name) {

        REGISTER.removeIf(multi -> multi.metaTileEntityId.toString().equals(name));

    }

    @Nullable
    public static String getMetaItemId(ItemStack item) {
        if (item.getItem() instanceof MetaItem) {
            MetaItem<?> metaItem = (MetaItem<?>) item.getItem();
            if (item.getItem() instanceof MetaPrefixItem) {
                Material material = ((MetaPrefixItem) metaItem).getMaterial(item);
                OrePrefix orePrefix = ((MetaPrefixItem) metaItem).getOrePrefix();
                return new UnificationEntry(orePrefix, material).toString();
            }
            return metaItem.getItem(item).unlocalizedName;
        }
        if (item.getItem() instanceof ItemBlock) {
            Block block = ((ItemBlock) item.getItem()).getBlock();
            if (item.getItem() instanceof MachineItemBlock) {
                MetaTileEntity mte = MachineItemBlock.getMetaTileEntity(item);
                if (mte != null) {
                    return (mte.metaTileEntityId.getNamespace().equals("gregtech") ? mte.metaTileEntityId.getPath() : mte.metaTileEntityId.toString());
                }
            }
            if (block instanceof BlockCompressed) {
                return "block" + ((BlockCompressed) block).getGtMaterial(item.getMetadata()).toCamelCaseString();
            }
            if (block instanceof BlockFrame) {
                return "frame" + ((BlockFrame) block).getGtMaterial(item.getMetadata()).toCamelCaseString();
            }
            if (block instanceof BlockMaterialPipe) {
                return ((BlockMaterialPipe<?, ?, ?>) block).getPrefix().name + ((BlockMaterialPipe<?, ?, ?>) block).getItemMaterial(item).toCamelCaseString();
            }
        }

        return null;
    }

    public static String getItemIdFor(ItemStack item) {
        String id = getMetaItemId(item);
        if (id != null)
            return id;
        if (item.getItem().getRegistryName() == null)
            return "null";
        return item.getItem().getRegistryName().toString();
    }
}
