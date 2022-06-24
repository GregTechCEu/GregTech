package gregtech.common.blocks.foam;

import gregtech.api.GregTechAPI;
import net.minecraft.block.BlockColored;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public class BlockConstructionFoam extends BlockColored {

    public BlockConstructionFoam() {
        super(Material.ROCK);
        setTranslationKey("construction_foam");
        setSoundType(SoundType.STONE);
        setResistance(4.0f);
        setHardness(1.5f);
        setCreativeTab(GregTechAPI.TAB_GREGTECH);
    }

    @Override
    public void getSubBlocks(@Nonnull CreativeTabs itemIn, @Nonnull NonNullList<ItemStack> items) {
        items.add(new ItemStack(this, 1, 0));
    }

    @Override
    public void addInformation(@Nonnull ItemStack stack, @Nullable World worldIn, @Nonnull List<String> tooltip, @Nonnull ITooltipFlag flagIn) {
        super.addInformation(stack, worldIn, tooltip, flagIn);
        tooltip.add(I18n.format("tile.construction_foam.tooltip.1"));
        tooltip.add(I18n.format("tile.construction_foam_wet.tooltip.2"));
        tooltip.add(I18n.format("tile.construction_foam_wet.tooltip.4"));
    }
}
