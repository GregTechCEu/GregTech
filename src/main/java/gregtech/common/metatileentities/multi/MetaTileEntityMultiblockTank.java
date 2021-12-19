package gregtech.common.metatileentities.multi;

import codechicken.lib.raytracer.CuboidRayTraceResult;
import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import gregtech.api.capability.impl.FluidHandlerProxy;
import gregtech.api.capability.impl.FluidTankList;
import gregtech.api.capability.impl.NotifiableFluidTank;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.ModularUI;
import gregtech.api.gui.widgets.LabelWidget;
import gregtech.api.gui.widgets.TankWidget;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.MetaTileEntityHolder;
import gregtech.api.metatileentity.multiblock.IMultiblockPart;
import gregtech.api.metatileentity.multiblock.MultiblockWithDisplayBase;
import gregtech.api.pattern.BlockPattern;
import gregtech.api.pattern.FactoryBlockPattern;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.material.Materials;
import gregtech.client.renderer.ICubeRenderer;
import gregtech.client.renderer.texture.Textures;
import gregtech.common.blocks.BlockMetalCasing;
import gregtech.common.blocks.MetaBlocks;
import gregtech.common.metatileentities.MetaTileEntities;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.FluidTank;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public class MetaTileEntityMultiblockTank extends MultiblockWithDisplayBase {

    private final Material material;

    public MetaTileEntityMultiblockTank(ResourceLocation metaTileEntityId, Material material) {
        super(metaTileEntityId);
        this.material = material;
        initializeAbilities();
    }

    protected void initializeAbilities() {
        this.importFluids = new FluidTankList(true, makeFluidTanks());
        this.exportFluids = importFluids;
        this.fluidInventory = new FluidHandlerProxy(this.importFluids, this.exportFluids);
    }

    @Nonnull
    private List<FluidTank> makeFluidTanks() {
        List<FluidTank> fluidTankList = new ArrayList<>(1);
        fluidTankList.add(new NotifiableFluidTank(32000, this, false));
        return fluidTankList;
    }

    @Override
    public MetaTileEntity createMetaTileEntity(MetaTileEntityHolder holder) {
        return new MetaTileEntityMultiblockTank(metaTileEntityId, material);
    }

    @Override
    protected void updateFormedValid() {

    }

    @Override
    protected BlockPattern createStructurePattern() {
        return FactoryBlockPattern.start()
                .aisle("XXX", "XXX", "XXX")
                .aisle("XXX", "X X", "XXX")
                .aisle("XXX", "XSX", "XXX")
                .where('S', selfPredicate())
                .where('X', states(getCasingState()).setMinGlobalLimited(23)
                        .or(metaTileEntities(MetaTileEntities.BRONZE_TANK_VALVE)
                                .setMinGlobalLimited(1)
                                .setMaxGlobalLimited(2)
                        ))
                .where(' ', air())
                .build();
    }

    private IBlockState getCasingState() {
        if (material.equals(Materials.Bronze))
            return MetaBlocks.METAL_CASING.getState(BlockMetalCasing.MetalCasingType.BRONZE_BRICKS);
        if (material.equals(Materials.Steel))
            return MetaBlocks.METAL_CASING.getState(BlockMetalCasing.MetalCasingType.STEEL_SOLID);
        if (material.equals(Materials.StainlessSteel))
            return MetaBlocks.METAL_CASING.getState(BlockMetalCasing.MetalCasingType.STAINLESS_CLEAN);
        if (material.equals(Materials.Titanium))
            return MetaBlocks.METAL_CASING.getState(BlockMetalCasing.MetalCasingType.TITANIUM_STABLE);
        if (material.equals(Materials.TungstenSteel))
            return MetaBlocks.METAL_CASING.getState(BlockMetalCasing.MetalCasingType.TUNGSTENSTEEL_ROBUST);
        return MetaBlocks.METAL_CASING.getState(BlockMetalCasing.MetalCasingType.PRIMITIVE_BRICKS);
    }

    @Override
    public ICubeRenderer getBaseTexture(IMultiblockPart sourcePart) {
        if (material.equals(Materials.Bronze))
            return Textures.BRONZE_PLATED_BRICKS;
        if (material.equals(Materials.Steel))
            return Textures.SOLID_STEEL_CASING;
        if (material.equals(Materials.StainlessSteel))
            return Textures.CLEAN_STAINLESS_STEEL_CASING;
        if (material.equals(Materials.Titanium))
            return Textures.STABLE_TITANIUM_CASING;
        if (material.equals(Materials.TungstenSteel))
            return Textures.ROBUST_TUNGSTENSTEEL_CASING;
        return Textures.PRIMITIVE_BRICKS;
    }

    @Override
    public boolean hasMaintenanceMechanics() {
        return false;
    }

    @Override
    public boolean onRightClick(EntityPlayer playerIn, EnumHand hand, EnumFacing facing, CuboidRayTraceResult hitResult) {
        if (!isStructureFormed())
            return false;
        return super.onRightClick(playerIn, hand, facing, hitResult);
    }

    @Override
    protected ModularUI.Builder createUITemplate(@Nonnull EntityPlayer entityPlayer) {
        return ModularUI.builder(GuiTextures.PRIMITIVE_BACKGROUND, 176, 166)
                .widget(new LabelWidget(5, 5, getMetaFullName()))
                .widget(new TankWidget(importFluids.getTankAt(0), 134, 13, 20, 58)
                        .setBackgroundTexture(GuiTextures.PRIMITIVE_LARGE_FLUID_TANK)
                        .setOverlayTexture(GuiTextures.PRIMITIVE_LARGE_FLUID_TANK_OVERLAY)
                        .setContainerClicking(true, true))
                .bindPlayerInventory(entityPlayer.inventory, GuiTextures.PRIMITIVE_SLOT, 0);
    }

    @Override
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        super.renderMetaTileEntity(renderState, translation, pipeline);
        getFrontOverlay().renderSided(getFrontFacing(), renderState, translation, pipeline);
    }

    @Nonnull
    @Override
    protected ICubeRenderer getFrontOverlay() {
        return Textures.PIPE_IN_OVERLAY;
    }
}
