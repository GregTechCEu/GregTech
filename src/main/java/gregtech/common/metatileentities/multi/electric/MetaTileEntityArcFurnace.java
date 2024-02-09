package gregtech.common.metatileentities.multi.electric;

import gregtech.api.GTValues;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.IMultiblockPart;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.api.metatileentity.multiblock.RecipeMapMultiblockController;
import gregtech.api.pattern.BlockPattern;
import gregtech.api.pattern.FactoryBlockPattern;
import gregtech.api.recipes.RecipeMaps;
import gregtech.api.unification.material.Materials;
import gregtech.client.renderer.ICubeRenderer;
import gregtech.client.renderer.texture.Textures;
import gregtech.common.blocks.BlockBoilerCasing;
import gregtech.common.blocks.BlockMachineCasing.MachineCasingType;
import gregtech.common.blocks.BlockMetalCasing;
import gregtech.common.blocks.BlockUniqueCasing;
import gregtech.common.blocks.MetaBlocks;

import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ResourceLocation;

import net.minecraft.util.math.BlockPos;

import org.jetbrains.annotations.NotNull;

import static gregtech.common.blocks.BlockFireboxCasing.FireboxCasingType.BRONZE_FIREBOX;
import static gregtech.common.blocks.BlockFireboxCasing.FireboxCasingType.STEEL_FIREBOX;

public class MetaTileEntityArcFurnace extends RecipeMapMultiblockController {

    public MetaTileEntityArcFurnace(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId, RecipeMaps.ARC_FURNACE_RECIPES);
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity iGregTechTileEntity) {
        return new MetaTileEntityArcFurnace(metaTileEntityId);
    }

    @Override
    protected BlockPattern createStructurePattern() {
        return FactoryBlockPattern.start()
                .aisle(" XXX ", " FFF ", " XXX ", "     ", "     ")
                .aisle("XXXXX", "FG#GF", "XG#GX", " GPG ", " G G ")
                .aisle("XXXXX", "F###F", "X###X", " PPP ", " G G ")
                .aisle("XXXXX", "FG#GF", "XG#GX", " GPG ", " G G ")
                .aisle(" XSX ", " FFF ", " XXX ", "     ", "     ")
                .where('S', selfPredicate())
                .where('X', states(MetaBlocks.METAL_CASING.getState(BlockMetalCasing.MetalCasingType.STEEL_SOLID))
                        .setMinGlobalLimited(24)
                        .or(autoAbilities(true, true, true, true, true, true, false)))
                .where('F', states(MetaBlocks.BOILER_FIREBOX_CASING.getState(STEEL_FIREBOX)))
                .where('P', states(MetaBlocks.BOILER_CASING.getState(BlockBoilerCasing.BoilerCasingType.STEEL_PIPE)))
                .where('G', states(MetaBlocks.UNIQUE_CASING.getState(BlockUniqueCasing.UniqueCasingType.GRAPHITE_ELECTRODE)))
                .where(' ', any())
                .where('#', air())
                .build();
    }

    @Override
    public ICubeRenderer getBaseTexture(IMultiblockPart iMultiblockPart) {
        return Textures.SOLID_STEEL_CASING;
    }

    @NotNull
    @Override
    protected ICubeRenderer getFrontOverlay() {
        return Textures.BLAST_FURNACE_OVERLAY;
    }

    @Override
    public void update() {
        super.update();

        if (this.isActive()) {
            if (getWorld().isRemote) {
                BlockPos pos = getPos();
                EnumFacing facing = getFrontFacing().getOpposite();

                float yPos = pos.getY() + 3F;
                float xPos = pos.getX() + 0.5F;
                float zPos = pos.getZ() + 0.5F;

                float xSpd = (GTValues.RNG.nextFloat() - 0.5f) / 3;
                float ySpd = 0.3F + 0.3F * GTValues.RNG.nextFloat();
                float zSpd = (GTValues.RNG.nextFloat() - 0.5f) / 3;

                if (facing == EnumFacing.NORTH || facing == EnumFacing.SOUTH) {
                    getWorld().spawnParticle(EnumParticleTypes.SMOKE_LARGE, xPos + 1f, yPos, zPos + facing.getZOffset() * 0.5f, xSpd, ySpd, zSpd);
                    getWorld().spawnParticle(EnumParticleTypes.SMOKE_LARGE, xPos - 1f, yPos, zPos + facing.getZOffset() * 0.5f, xSpd, ySpd, zSpd);
                    getWorld().spawnParticle(EnumParticleTypes.SMOKE_LARGE, xPos + 1f, yPos, zPos + facing.getZOffset() * 3.5f, xSpd, ySpd, zSpd);
                    getWorld().spawnParticle(EnumParticleTypes.SMOKE_LARGE, xPos - 1f, yPos, zPos + facing.getZOffset() * 3.5f, xSpd, ySpd, zSpd);
                }

                if (facing == EnumFacing.WEST || facing == EnumFacing.EAST) {
                    getWorld().spawnParticle(EnumParticleTypes.SMOKE_LARGE, xPos + facing.getXOffset() * 0.5f, yPos, zPos + 1f, xSpd, ySpd, zSpd);
                    getWorld().spawnParticle(EnumParticleTypes.SMOKE_LARGE, xPos + facing.getXOffset() * 0.5f, yPos, zPos - 1f, xSpd, ySpd, zSpd);
                    getWorld().spawnParticle(EnumParticleTypes.SMOKE_LARGE, xPos + facing.getXOffset() * 3.5f, yPos, zPos + 1f, xSpd, ySpd, zSpd);
                    getWorld().spawnParticle(EnumParticleTypes.SMOKE_LARGE, xPos + facing.getXOffset() * 3.5f, yPos, zPos - 1f, xSpd, ySpd, zSpd);
                }
            }
        }
    }
}
