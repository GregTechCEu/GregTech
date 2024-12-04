package gregtech.common.entities;

import gregtech.common.blocks.MetaBlocks;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.world.World;

import org.jetbrains.annotations.NotNull;

public class ITNTEntity extends EntityGTExplosive {

    public ITNTEntity(World world, double x, double y, double z, EntityLivingBase exploder) {
        super(world, x, y, z, exploder);
    }

    @SuppressWarnings("unused")
    public ITNTEntity(World world) {
        super(world);
    }

    @Override
    protected float getStrength() {
        return 5.0F;
    }

    @Override
    public boolean dropsAllBlocks() {
        return true;
    }

    @Override
    protected int getRange() {
        return 3;
    }

    @Override
    public @NotNull IBlockState getExplosiveState() {
        return MetaBlocks.ITNT.getDefaultState();
    }
}
