package gregtech.common.covers.facade;

import gregtech.api.util.GTUtility;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumBlockRenderType;

import com.google.common.collect.ImmutableList;

public class FacadeHelper {

    private static ImmutableList<ItemStack> validFacadeItems = null;

    public static ImmutableList<ItemStack> getValidFacadeItems() {
        if (validFacadeItems == null) {
            ImmutableList.Builder<ItemStack> b = ImmutableList.builder();
            for (Item item : Item.REGISTRY) {
                if (item instanceof ItemBlock) {
                    for (ItemStack subItem : GTUtility.getAllSubItems(item)) {
                        if (isValidFacade(subItem)) {
                            b.add(subItem);
                        }
                    }
                }
            }
            validFacadeItems = b.build();
        }
        return validFacadeItems;
    }

    public static boolean isValidFacade(ItemStack itemStack) {
        IBlockState rawBlockState = lookupBlockForItemUnsafe(itemStack);
        // noinspection deprecation
        return rawBlockState != null &&
                !rawBlockState.getBlock().hasTileEntity(rawBlockState) &&
                !rawBlockState.getBlock().hasTileEntity() &&
                rawBlockState.getRenderType() == EnumBlockRenderType.MODEL &&
                rawBlockState.isFullCube();
    }

    public static IBlockState lookupBlockForItem(ItemStack itemStack) {
        IBlockState rawBlockState = lookupBlockForItemUnsafe(itemStack);
        if (rawBlockState == null) {
            return Blocks.STONE.getDefaultState();
        }
        return rawBlockState;
    }

    private static IBlockState lookupBlockForItemUnsafe(ItemStack itemStack) {
        if (!(itemStack.getItem() instanceof ItemBlock)) {
            return null;
        }
        Block block = ((ItemBlock) itemStack.getItem()).getBlock();
        int blockMetadata = itemStack.getItem().getMetadata(itemStack);
        try {
            // noinspection deprecation
            return block.getStateFromMeta(blockMetadata);
        } catch (Throwable e) {
            return null;
        }
    }
}
