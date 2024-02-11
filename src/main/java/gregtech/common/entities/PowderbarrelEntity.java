package gregtech.common.entities;

import gregtech.common.blocks.MetaBlocks;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.world.World;

import org.jetbrains.annotations.NotNull;

public class PowderbarrelEntity extends EntityGTExplosive {

    public PowderbarrelEntity(World world, double x, double y, double z, EntityLivingBase exploder) {
        super(world, x, y, z, exploder);
    }

    @SuppressWarnings("unused")
    public PowderbarrelEntity(World world) {
        super(world);
    }

    @Override
    protected float getStrength() {
        return 3.5F;
    }

    @Override
    public boolean dropsAllBlocks() {
        return true;
    }

    @Override
    public @NotNull IBlockState getExplosiveState() {
        return MetaBlocks.POWDERBARREL.getDefaultState();
    }
}
