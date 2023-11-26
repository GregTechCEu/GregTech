package gregtech.api.fluids;

import gregtech.api.GTValues;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.material.info.MaterialFlags;

import net.minecraft.block.Block;
import net.minecraft.block.BlockFire;
import net.minecraft.block.material.MaterialLiquid;
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

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class GTFluidBlock extends BlockFluidClassic {

    private final boolean isFlammable;
    private final boolean isExplosive;
    private final boolean isSticky;
    private @Nullable Material gtMaterial;

    public GTFluidBlock(@NotNull Fluid fluid, @NotNull MaterialLiquid material, boolean isFlammable,
                        boolean isExplosive, boolean isSticky) {
        super(fluid, material);
        this.isFlammable = isFlammable;
        this.isExplosive = isExplosive;
        this.isSticky = isSticky;

        boolean displaces = fluid.getDensity() > 1000; // water density
        this.displacements.put(Blocks.WATER, displaces);
        this.displacements.put(Blocks.FLOWING_WATER, displaces);
        displaces = fluid.getDensity() > 3000; // lava density
        this.displacements.put(Blocks.LAVA, displaces);
        this.displacements.put(Blocks.FLOWING_LAVA, displaces);
    }

    public GTFluidBlock(@NotNull Fluid fluid, @NotNull MaterialLiquid material, @NotNull Material gtMaterial) {
        this(fluid, material, gtMaterial.hasFlag(MaterialFlags.FLAMMABLE),
                gtMaterial.hasFlag(MaterialFlags.EXPLOSIVE), gtMaterial.hasFlag(MaterialFlags.STICKY));
        this.gtMaterial = gtMaterial;
    }

    public boolean isFlammable() {
        return isFlammable;
    }

    public boolean isExplosive() {
        return isExplosive;
    }

    public boolean isSticky() {
        return isSticky;
    }

    public @Nullable Material getGtMaterial() {
        return gtMaterial;
    }

    @Nullable
    @Override
    public Boolean isEntityInsideMaterial(@NotNull IBlockAccess world, @NotNull BlockPos blockpos,
                                          @NotNull IBlockState iblockstate, @NotNull Entity entity, double yToTest,
                                          @NotNull net.minecraft.block.material.Material materialIn,
                                          boolean testingHead) {
        return materialIn == net.minecraft.block.material.Material.WATER ? true : null;
    }

    @Override
    public int getFireSpreadSpeed(@NotNull IBlockAccess world, @NotNull BlockPos pos, @NotNull EnumFacing face) {
        return this.isFlammable ? 5 : 0;
    }

    @Override
    public boolean isFlammable(@NotNull IBlockAccess world, @NotNull BlockPos pos, @NotNull EnumFacing face) {
        return this.isFlammable;
    }

    @Override
    public int getFlammability(@NotNull IBlockAccess world, @NotNull BlockPos pos, @NotNull EnumFacing face) {
        return this.isFlammable ? 200 : 0;
    }

    @Override
    public void neighborChanged(@NotNull IBlockState state, @NotNull World world, @NotNull BlockPos pos,
                                @NotNull Block neighborBlock, @NotNull BlockPos neighbourPos) {
        super.neighborChanged(state, world, pos, neighborBlock, neighbourPos);
        if (this.isExplosive && this.isFlammable && neighborBlock instanceof BlockFire &&
                GTValues.RNG.nextInt(5) == 0) {
            world.setBlockToAir(pos);
            world.createExplosion(null, pos.getX() + 0.5F, pos.getY() + 0.5F, pos.getZ() + 0.5F, 1.5F, true);
        }
    }

    @Override
    public void onEntityCollision(@NotNull World worldIn, @NotNull BlockPos pos, @NotNull IBlockState state,
                                  @NotNull Entity entityIn) {
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
    public boolean canDisplace(@NotNull IBlockAccess world, @NotNull BlockPos pos) {
        if (this.isFlammable && world.getBlockState(pos).getMaterial() == net.minecraft.block.material.Material.FIRE) {
            return false;
        }
        return super.canDisplace(world, pos);
    }

    @Override
    public boolean displaceIfPossible(@NotNull World world, @NotNull BlockPos pos) {
        if (this.isFlammable && world.getBlockState(pos).getMaterial() == net.minecraft.block.material.Material.FIRE) {
            return false;
        }
        return super.displaceIfPossible(world, pos);
    }
}
