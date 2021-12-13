package gregtech.common.covers.facade;

import com.google.common.collect.ImmutableList;
import gregtech.api.util.GTUtility;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.NonNullList;

import java.util.List;
import java.util.stream.StreamSupport;

public class FacadeHelper {

    private static ImmutableList<ItemStack> validFacadeItems = null;

    private static ImmutableList<ItemStack> retrieveValidItemsList() {
        return StreamSupport.stream(Item.REGISTRY.spliterator(), false)
                .filter(item -> item instanceof ItemBlock)
                .flatMap(item -> getSubItems(item).stream())
                .filter(FacadeHelper::isValidFacade)
                .collect(GTUtility.toImmutableList());
    }

    public static ImmutableList<ItemStack> getValidFacadeItems() {
        if (validFacadeItems == null) {
            validFacadeItems = retrieveValidItemsList();
        }
        return validFacadeItems;
    }

    private static List<ItemStack> getSubItems(Item item) {
        NonNullList<ItemStack> list = NonNullList.create();
        item.getSubItems(CreativeTabs.SEARCH, list);
        return list;
    }

    public static boolean isValidFacade(ItemStack itemStack) {
        IBlockState rawBlockState = lookupBlockForItemUnsafe(itemStack);
        //noinspection deprecation
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
            //noinspection deprecation
            return block.getStateFromMeta(blockMetadata);
        } catch (Throwable e) {
            return null;
        }
    }
}
