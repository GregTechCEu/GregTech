package gregtech.common.metatileentities.multi.steam;


import gregtech.api.block.VariantActiveBlock;
import gregtech.api.capability.GregtechDataCodes;
import gregtech.api.capability.impl.SteamMultiWorkable;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.IMultiblockPart;
import gregtech.api.metatileentity.multiblock.RecipeMapSteamMultiblockController;
import gregtech.api.pattern.BlockPattern;
import gregtech.api.pattern.FactoryBlockPattern;
import gregtech.api.pattern.PatternMatchContext;
import gregtech.api.recipes.RecipeMaps;
import gregtech.client.renderer.ICubeRenderer;
import gregtech.client.renderer.texture.Textures;
import gregtech.common.ConfigHolder;
import gregtech.common.blocks.BlockFireboxCasing;
import gregtech.common.blocks.BlockMetalCasing;
import gregtech.common.blocks.MetaBlocks;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.block.state.IBlockState;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nonnull;

import static gregtech.api.capability.GregtechDataCodes.IS_WORKING;

public class MetaTileEntitySteamOven extends RecipeMapSteamMultiblockController {

    private boolean isActive;

    public MetaTileEntitySteamOven(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId, RecipeMaps.FURNACE_RECIPES, CONVERSION_RATE);
        this.recipeMapWorkable = new SteamMultiWorkable(this, CONVERSION_RATE);
        this.recipeMapWorkable.setParallelLimit(8);
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity tileEntity) {
        return new MetaTileEntitySteamOven(metaTileEntityId);
    }

    private void setActive(boolean active) {
        this.isActive = active;
        if (!getWorld().isRemote) {
            if (isStructureFormed()) {
                replaceFireboxAsActive(active);
            }
            writeCustomData(IS_WORKING, buf -> buf.writeBoolean(isActive));
            markDirty();
        }
    }

    @Override
    protected BlockPattern createStructurePattern() {
        return FactoryBlockPattern.start()
                .aisle("XXX", "CCC", "#C#")
                .aisle("XXX", "C#C", "#C#")
                .aisle("XXX", "CSC", "#C#")
                .where('S', selfPredicate())
                .where('X', states(getFireboxState())
                        .or(autoAbilities(true, false, false, false, false).setMinGlobalLimited(1).setMaxGlobalLimited(3)))
                .where('C', states(getCasingState()).setMinGlobalLimited(6)
                        .or(autoAbilities(false, false, true, true, false)))
                .where('#', any())
                .build();
    }

    @Override
    protected void formStructure(PatternMatchContext context) {
        super.formStructure(context);
        VariantActiveBlock.ACTIVE_BLOCKS.putIfAbsent(getWorld().provider.getDimension(), new ObjectOpenHashSet<>());
    }

    public IBlockState getCasingState() {
        return ConfigHolder.machines.steelSteamMultiblocks ?
                MetaBlocks.METAL_CASING.getState(BlockMetalCasing.MetalCasingType.STEEL_SOLID) :
                MetaBlocks.METAL_CASING.getState(BlockMetalCasing.MetalCasingType.BRONZE_BRICKS);
    }

    public IBlockState getFireboxState() {
        return ConfigHolder.machines.steelSteamMultiblocks ?
                MetaBlocks.BOILER_FIREBOX_CASING.getState(BlockFireboxCasing.FireboxCasingType.STEEL_FIREBOX) :
                MetaBlocks.BOILER_FIREBOX_CASING.getState(BlockFireboxCasing.FireboxCasingType.BRONZE_FIREBOX);
    }

    private boolean isFireboxPart(IMultiblockPart sourcePart) {
        return isStructureFormed() && (((MetaTileEntity) sourcePart).getPos().getY() < getPos().getY());
    }

    @Override
    public ICubeRenderer getBaseTexture(IMultiblockPart sourcePart) {
        if (ConfigHolder.machines.steelSteamMultiblocks) {
            if (sourcePart != null && isFireboxPart(sourcePart)) {
                return isActive ? Textures.STEEL_FIREBOX_ACTIVE : Textures.STEEL_FIREBOX;
            }
            return Textures.SOLID_STEEL_CASING;

        } else {
            if (sourcePart != null && isFireboxPart(sourcePart)) {
                return isActive ? Textures.BRONZE_FIREBOX_ACTIVE : Textures.BRONZE_FIREBOX;
            }
            return Textures.BRONZE_PLATED_BRICKS;
        }
    }

    //FIXME this is basically the same method in RecipeMapMultiblockController
    public void replaceFireboxAsActive(boolean isActive) {
        int id = getWorld().provider.getDimension();
        BlockPos centerPos = getPos().offset(getFrontFacing().getOpposite()).down();
        writeCustomData(GregtechDataCodes.VARIANT_RENDER_UPDATE, buf -> {
            buf.writeInt(id);
            buf.writeBoolean(isActive);
            //9 is the number of blocks we will be passing as 'active'
            buf.writeInt(9);
            for (int x = -1; x <= 1; x++) {
                for (int z = -1; z <= 1; z++) {
                    BlockPos blockPos = centerPos.add(x, 0, z);
                    if (isActive) {
                        VariantActiveBlock.ACTIVE_BLOCKS.get(id).add(blockPos);
                    } else {
                        VariantActiveBlock.ACTIVE_BLOCKS.get(id).remove(blockPos);
                    }
                    buf.writeBlockPos(blockPos);
                }
            }
        });
    }

    @Override
    protected void updateFormedValid() {
        super.updateFormedValid();
        if (isActive != recipeMapWorkable.isActive()) {
            setActive(recipeMapWorkable.isActive());
        }
    }

    @Override
    public void onRemoval() {
        super.onRemoval();
        if (!getWorld().isRemote && isStructureFormed()) {
            replaceFireboxAsActive(false);
        }
    }

    @Override
    public void invalidateStructure() {
        super.invalidateStructure();
        this.isActive = false;
        replaceFireboxAsActive(false);
    }

    @Override
    public int getLightValueForPart(IMultiblockPart sourcePart) {
        return sourcePart == null ? 0 : (isActive ? 15 : 0);
    }

    @Override
    public void writeInitialSyncData(PacketBuffer buf) {
        super.writeInitialSyncData(buf);
        buf.writeBoolean(isActive);
    }

    @Override
    public void receiveInitialSyncData(PacketBuffer buf) {
        super.receiveInitialSyncData(buf);
        this.isActive = buf.readBoolean();
    }

    @Override
    public void receiveCustomData(int dataId, PacketBuffer buf) {
        super.receiveCustomData(dataId, buf);
        if (dataId == IS_WORKING) {
            this.isActive = buf.readBoolean();
        }
        if (dataId == GregtechDataCodes.VARIANT_RENDER_UPDATE) {
            int minX;
            int minY;
            int minZ;
            minX = minY = minZ = Integer.MAX_VALUE;
            int maxX;
            int maxY;
            int maxZ;
            maxX = maxY = maxZ = Integer.MIN_VALUE;

            int id = buf.readInt();
            boolean isActive = buf.readBoolean();
            int size = buf.readInt();
            for (int i = 0; i < size; i++) {
                BlockPos blockPos = buf.readBlockPos();
                if (isActive) {
                    VariantActiveBlock.ACTIVE_BLOCKS.get(id).add(blockPos);
                } else {
                    VariantActiveBlock.ACTIVE_BLOCKS.get(id).remove(blockPos);
                }
                minX = Math.min(minX, blockPos.getX());
                minY = Math.min(minY, blockPos.getY());
                minZ = Math.min(minZ, blockPos.getZ());
                maxX = Math.max(maxX, blockPos.getX());
                maxY = Math.max(maxY, blockPos.getY());
                maxZ = Math.max(maxZ, blockPos.getZ());
            }
            if (getWorld().provider.getDimension() == id) {
                getWorld().markBlockRangeForRenderUpdate(new BlockPos(minX, minY, minZ), new BlockPos(maxX, maxY, maxZ));
            }
        }
    }

    @Nonnull
    @Override
    protected ICubeRenderer getFrontOverlay() {
        return Textures.ELECTRIC_FURNACE_OVERLAY;
    }

    @Override
    public boolean hasMaintenanceMechanics() {
        return false;
    }

    @Override
    public int getItemOutputLimit() {
        return 1;
    }
}
