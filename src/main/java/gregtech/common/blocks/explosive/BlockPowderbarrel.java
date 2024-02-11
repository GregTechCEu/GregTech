package gregtech.common.blocks.explosive;

import gregtech.api.items.toolitem.ToolClasses;
import gregtech.common.blocks.material.GTBlockMaterials;
import gregtech.common.entities.EntityGTExplosive;
import gregtech.common.entities.PowderbarrelEntity;

import net.minecraft.block.SoundType;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class BlockPowderbarrel extends BlockGTExplosive {

    public BlockPowderbarrel() {
        super(GTBlockMaterials.POWDERBARREL, false, true, 100);
        setHarvestLevel(ToolClasses.AXE, 1);
        setHardness(0.5F);
        setSoundType(SoundType.WOOD);
    }

    @Override
    protected EntityGTExplosive createEntity(World world, BlockPos pos, EntityLivingBase exploder) {
        float x = pos.getX() + 0.5F, y = pos.getY(), z = pos.getZ() + 0.5F;
        return new PowderbarrelEntity(world, x, y, z, exploder);
    }

    @Override
    public void addInformation(@NotNull ItemStack stack, @Nullable World world, @NotNull List<String> tooltip,
                               @NotNull ITooltipFlag flag) {
        tooltip.add(I18n.format("tile.powderbarrel.drops_tooltip"));
        super.addInformation(stack, world, tooltip, flag);
    }
}
