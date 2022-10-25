package gregtech.api.fluids;

import gregtech.api.GTValues;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.material.info.MaterialFlags;
import net.minecraft.block.Block;
import net.minecraft.block.BlockFire;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fluids.BlockFluidClassic;
import net.minecraftforge.fluids.Fluid;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class MaterialFluidBlock extends BlockFluidClassic {

    private final Material gtMaterial;
    public final boolean isFlammable;
    public final boolean isExplosive;
    public final boolean isSticky;

    public MaterialFluidBlock(@Nonnull Fluid fluid, @Nonnull GTFluidMaterial material, @Nonnull Material gtMaterial) {
        super(fluid, material);
        this.gtMaterial = gtMaterial;
        this.isFlammable = gtMaterial.hasFlag(MaterialFlags.FLAMMABLE);
        this.isExplosive = gtMaterial.hasFlag(MaterialFlags.EXPLOSIVE);
        this.isSticky = gtMaterial.hasFlag(MaterialFlags.STICKY);

        boolean displaces = fluid.getDensity() > 1000; // water density
        this.displacements.put(Blocks.WATER, displaces);
        this.displacements.put(Blocks.FLOWING_WATER, displaces);
        displaces = fluid.getDensity() > 3000; // lava density
        this.displacements.put(Blocks.LAVA, displaces);
        this.displacements.put(Blocks.FLOWING_LAVA, displaces);
    }

    @Nullable
    @Override
    public Boolean isEntityInsideMaterial(@Nonnull IBlockAccess world, @Nonnull BlockPos blockpos, @Nonnull IBlockState iblockstate, @Nonnull Entity entity, double yToTest, @Nonnull net.minecraft.block.material.Material materialIn, boolean testingHead) {
        return materialIn == net.minecraft.block.material.Material.WATER ? true : null;
    }

    @Override
    public int getFireSpreadSpeed(@Nonnull IBlockAccess world, @Nonnull BlockPos pos, @Nonnull EnumFacing face) {
        return this.isFlammable ? 5 : 0;
    }

    @Override
    public boolean isFlammable(@Nonnull IBlockAccess world, @Nonnull BlockPos pos, @Nonnull EnumFacing face) {
        return this.isFlammable;
    }

    @Override
    public int getFlammability(@Nonnull IBlockAccess world, @Nonnull BlockPos pos, @Nonnull EnumFacing face) {
        return this.isFlammable ? 200 : 0;
    }

    @Override
    public void neighborChanged(@Nonnull IBlockState state, @Nonnull World world, @Nonnull BlockPos pos, @Nonnull Block neighborBlock, @Nonnull BlockPos neighbourPos) {
        super.neighborChanged(state, world, pos, neighborBlock, neighbourPos);
        if (this.isExplosive && this.isFlammable && neighborBlock instanceof BlockFire && GTValues.RNG.nextInt(5) == 0) {
            world.setBlockToAir(pos);
            world.createExplosion(null, pos.getX() + 0.5F, pos.getY() + 0.5F, pos.getZ() + 0.5F, 1.5F, true);
        }
    }

    @Override
    public void onEntityCollision(@Nonnull World worldIn, @Nonnull BlockPos pos, @Nonnull IBlockState state, @Nonnull Entity entityIn) {
        if (this.isSticky) {
            if (entityIn instanceof EntityPlayer && ((EntityPlayer) entityIn).isCreative()) {
                return;
            }

            entityIn.motionX *= 0.5;
            entityIn.motionY *= 0.25;
            entityIn.motionZ *= 0.5;
        }
    }

    @Override
    public boolean canDisplace(@Nonnull IBlockAccess world, @Nonnull BlockPos pos) {
        if (this.isFlammable && world.getBlockState(pos).getMaterial() == net.minecraft.block.material.Material.FIRE) {
            return false;
        }
        return super.canDisplace(world, pos);
    }

    @Override
    public boolean displaceIfPossible(@Nonnull World world, @Nonnull BlockPos pos) {
        if (this.isFlammable && world.getBlockState(pos).getMaterial() == net.minecraft.block.material.Material.FIRE) {
            return false;
        }
        return super.displaceIfPossible(world, pos);
    }

    @Nonnull
    public Material getGTMaterial() {
        return this.gtMaterial;
    }
}
