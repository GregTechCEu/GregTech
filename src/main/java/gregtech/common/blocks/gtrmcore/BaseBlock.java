package gregtech.common.blocks.gtrmcore;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class BaseBlock extends Block {

    public BaseBlock(Material material, String registryName) {
        super(material);
        setRegistryName(registryName);
        setTranslationKey(registryName);
        setDefaultState(blockState.getBaseState());
    }

    public ItemStack getItemStack() {
        return Item.getItemFromBlock(this).getDefaultInstance();
    }
}
