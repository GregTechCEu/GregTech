package gregtech.api.fluids;

import gregtech.api.GTValues;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.material.info.MaterialFlags;
import net.minecraft.block.Block;
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
    private final boolean isFlammable;
    private final boolean isExplosive;
    private final boolean isSticky;

    public MaterialFluidBlock(@Nonnull Fluid fluid, @Nonnull net.minecraft.block.material.Material material, @Nonnull Material gtMaterial) {
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

        this.renderLayer = BlockRenderLayer.SOLID;
    }

    @Nullable
    @Override
    public Boolean isEntityInsideMaterial(@Nonnull IBlockAccess world, @Nonnull BlockPos blockpos, @Nonnull IBlockState iblockstate, @Nonnull Entity entity, double yToTest, @Nonnull net.minecraft.block.material.Material materialIn, boolean testingHead) {
        return materialIn == net.minecraft.block.material.Material.WATER ? true : null;
    }

    @Override
    public int getFlammability(@Nonnull IBlockAccess world, @Nonnull BlockPos pos, @Nonnull EnumFacing face) {
        return this.isFlammable ? 200 : 0;
    }

    @Override
    public void neighborChanged(@Nonnull IBlockState state, @Nonnull World world, @Nonnull BlockPos pos, @Nonnull Block neighborBlock, @Nonnull BlockPos neighbourPos) {
        super.neighborChanged(state, world, pos, neighborBlock, neighbourPos);
        if (this.isExplosive) {
            // explosive fluids are unstable, can just explode whenever disrupted
            // flammable explosives are way more likely to blow up
            if (GTValues.RNG.nextInt(this.isFlammable && neighborBlock instanceof BlockFire ? 5 : 10) == 0) {
                world.setBlockToAir(pos);
                world.createExplosion(null, pos.getX() + 0.5F, pos.getY() + 0.5F, pos.getZ() + 0.5F, 3.0F, true);
            }
        }
    }

    @Override
    public void onEntityCollision(@Nonnull World worldIn, @Nonnull BlockPos pos, @Nonnull IBlockState state, @Nonnull Entity entityIn) {
        if (this.isSticky) entityIn.setInWeb();
    }

    @Nonnull
    public Material getGTMaterial() {
        return this.gtMaterial;
    }
}
