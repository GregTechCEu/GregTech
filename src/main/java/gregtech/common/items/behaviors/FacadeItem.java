package gregtech.common.items.behaviors;

import gregtech.api.items.metaitem.stats.IItemNameProvider;
import gregtech.api.items.metaitem.stats.ISubItemHandler;
import gregtech.api.util.LocalizationUtils;
import gregtech.common.ConfigHolder;
import gregtech.common.covers.facade.FacadeHelper;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.util.Constants.NBT;

import com.google.common.collect.ImmutableList;

import java.util.List;
import java.util.Objects;

public class FacadeItem implements IItemNameProvider, ISubItemHandler {

    @Override
    public String getItemStackDisplayName(ItemStack itemStack, String unlocalizedName) {
        ItemStack facadeStack = getFacadeStack(itemStack);
        String name = facadeStack.getItem().getItemStackDisplayName(facadeStack);
        return LocalizationUtils.format(unlocalizedName, name);
    }

    @Override
    public void getSubItems(ItemStack itemStack, CreativeTabs creativeTab, NonNullList<ItemStack> subItems) {
        List<ItemStack> validFacades;
        if (creativeTab == CreativeTabs.SEARCH && !ConfigHolder.compat.hideFacadesInJEI) {
            validFacades = FacadeHelper.getValidFacadeItems();
        } else {
            validFacades = ImmutableList.of(new ItemStack(Blocks.STONE));
        }
        for (ItemStack facadeStack : validFacades) {
            ItemStack resultStack = itemStack.copy();
            setFacadeStack(resultStack, facadeStack);
            subItems.add(resultStack);
        }
    }

    @Override
    public String getItemSubType(ItemStack itemStack) {
        ItemStack facadeStack = getFacadeStack(itemStack);
        ResourceLocation registryName = Objects.requireNonNull(facadeStack.getItem().getRegistryName());
        return String.format("%s:%s@%d", registryName.getNamespace(), registryName.getPath(),
                Items.FEATHER.getDamage(facadeStack));
    }

    public static void setFacadeStack(ItemStack itemStack, ItemStack facadeStack) {
        facadeStack = facadeStack.copy();
        facadeStack.setCount(1);
        if (!FacadeHelper.isValidFacade(facadeStack)) {
            facadeStack = new ItemStack(Blocks.STONE);
        }
        if (!itemStack.hasTagCompound()) {
            itemStack.setTagCompound(new NBTTagCompound());
        }
        NBTTagCompound tagCompound = Objects.requireNonNull(itemStack.getTagCompound());
        tagCompound.setTag("Facade", facadeStack.writeToNBT(new NBTTagCompound()));
    }

    public static ItemStack getFacadeStack(ItemStack itemStack) {
        ItemStack unsafeStack = getFacadeStackUnsafe(itemStack);
        if (unsafeStack == null) {
            return new ItemStack(Blocks.STONE);
        }
        return unsafeStack;
    }

    private static ItemStack getFacadeStackUnsafe(ItemStack itemStack) {
        NBTTagCompound tagCompound = itemStack.getTagCompound();
        if (tagCompound == null || !tagCompound.hasKey("Facade", NBT.TAG_COMPOUND)) {
            return null;
        }
        ItemStack facadeStack = new ItemStack(tagCompound.getCompoundTag("Facade"));
        if (facadeStack.isEmpty() || !FacadeHelper.isValidFacade(facadeStack)) {
            return null;
        }
        return facadeStack;
    }
}
