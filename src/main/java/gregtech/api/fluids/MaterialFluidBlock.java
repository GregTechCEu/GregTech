package gregtech.api.fluids;

import gregtech.api.GTValues;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.material.info.MaterialFlags;
import net.minecraft.block.BlockFire;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockRenderLayer;
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

    public MaterialFluidBlock(Fluid fluid, net.minecraft.block.material.Material material, Material gtMaterial) {
        super(fluid, material);
        this.gtMaterial = gtMaterial;

        boolean displaces = fluid.getDensity() > 1000; // water density
        this.displacements.put(Blocks.WATER, displaces);
        this.displacements.put(Blocks.FLOWING_WATER, displaces);
        displaces = fluid.getDensity() > 3000; // lava density
        this.displacements.put(Blocks.LAVA, displaces);
        this.displacements.put(Blocks.FLOWING_LAVA, displaces);

        this.renderLayer = BlockRenderLayer.SOLID;
    }

    @Nullable
    @Override
    public Boolean isEntityInsideMaterial(@Nonnull IBlockAccess world, @Nonnull BlockPos blockpos, @Nonnull IBlockState iblockstate, @Nonnull Entity entity, double yToTest, @Nonnull net.minecraft.block.material.Material materialIn, boolean testingHead) {
        return materialIn == net.minecraft.block.material.Material.WATER ? true : null;
    }

    @Override
    public int getFlammability(@Nonnull IBlockAccess world, @Nonnull BlockPos pos, @Nonnull EnumFacing face) {
        return this.gtMaterial.hasFlag(MaterialFlags.FLAMMABLE) ? 200 : 0;
    }

    @Override
    public void onNeighborChange(@Nonnull IBlockAccess blockAccess, @Nonnull BlockPos pos, @Nonnull BlockPos neighbor) {
        if (this.gtMaterial.hasFlag(MaterialFlags.EXPLOSIVE) && blockAccess instanceof World &&
                blockAccess.getBlockState(neighbor).getBlock() instanceof BlockFire) {
            if (GTValues.RNG.nextInt(10) == 0) {
                ((World) blockAccess).setBlockToAir(pos);
                ((World) blockAccess).createExplosion(null, pos.getX() + 0.5F, pos.getY() + 0.5F, pos.getZ() + 0.5F, 1.0F, true);
            }
        }
    }

    @Override
    public void onEntityCollision(@Nonnull World worldIn, @Nonnull BlockPos pos, @Nonnull IBlockState state, @Nonnull Entity entityIn) {
        if (this.gtMaterial.hasFlag(MaterialFlags.STICKY)) entityIn.setInWeb();
    }
}
