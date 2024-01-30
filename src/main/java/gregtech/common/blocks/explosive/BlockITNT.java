package gregtech.common.blocks.explosive;

import gregtech.common.entities.EntityGTExplosive;
import gregtech.common.entities.ITNTEntity;

import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class BlockITNT extends BlockGTExplosive {

    public BlockITNT() {
        super(Material.TNT, true, true, 40);
        setHardness(0);
        setSoundType(SoundType.PLANT);
    }

    @Override
    protected EntityGTExplosive createEntity(World world, BlockPos pos, EntityLivingBase exploder) {
        float x = pos.getX() + 0.5F, y = pos.getY(), z = pos.getZ() + 0.5F;
        return new ITNTEntity(world, x, y, z, exploder);
    }

    @Override
    public void addInformation(@NotNull ItemStack stack, @Nullable World world, @NotNull List<String> tooltip,
                               @NotNull ITooltipFlag flag) {
        tooltip.add(I18n.format("tile.itnt.drops_tooltip"));
        super.addInformation(stack, world, tooltip, flag);
    }
}
