package gregtech.common.metatileentities.multi;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import gregtech.api.capability.impl.FluidHandlerProxy;
import gregtech.api.capability.impl.FluidTankList;
import gregtech.api.gui.ModularUI;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.MetaTileEntityHolder;
import gregtech.api.metatileentity.multiblock.IMultiblockAbilityPart;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.api.metatileentity.multiblock.MultiblockControllerBase;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.material.Materials;
import gregtech.api.util.GTFluidUtils;
import gregtech.client.renderer.ICubeRenderer;
import gregtech.client.renderer.texture.Textures;
import gregtech.common.metatileentities.electric.multiblockpart.MetaTileEntityMultiblockPart;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;

import javax.annotation.Nonnull;
import java.util.List;

public class MetaTileEntityTankValve extends MetaTileEntityMultiblockPart implements IMultiblockAbilityPart<IFluidHandler> {

    private final Material material;

    public MetaTileEntityTankValve(ResourceLocation metaTileEntityId, Material material) {
        super(metaTileEntityId, 0);
        this.material = material;
    }

    @Override
    public MetaTileEntity createMetaTileEntity(MetaTileEntityHolder holder) {
        return new MetaTileEntityTankValve(metaTileEntityId, material);
    }

    @Override
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        super.renderMetaTileEntity(renderState, translation, pipeline);
        Textures.HATCH_OVERLAY.renderSided(getFrontFacing(), renderState, translation, pipeline);
    }

    @Override
    public ICubeRenderer getBaseTexture() {
        if (getController() == null) {
            this.setPaintingColor(DEFAULT_PAINTING_COLOR);
            if (material.equals(Materials.Wood))
                return Textures.PRIMITIVE_BRICKS;
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
        }
        return super.getBaseTexture();
    }

    @Override
    public void update() {
        super.update();
        if (!getWorld().isRemote && getOffsetTimer() % 5 == 0L && isAttachedToMultiBlock() && getFrontFacing() == EnumFacing.DOWN) {
            TileEntity tileEntity = getWorld().getTileEntity(getPos().offset(getFrontFacing()));
            IFluidHandler fluidHandler = tileEntity == null ? null : tileEntity.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, getFrontFacing().getOpposite());
            if (fluidHandler != null) {
                GTFluidUtils.transferFluids(fluidInventory, fluidHandler, Integer.MAX_VALUE);
            }
        }
    }

    @Override
    protected void initializeInventory() {
        super.initializeInventory();
        this.fluidInventory = new FluidTankList(false);
    }

    @Override
    public void addToMultiBlock(MultiblockControllerBase controllerBase) {
        super.addToMultiBlock(controllerBase);
        this.fluidInventory = new FluidHandlerProxy(new FluidTankList(false, controllerBase.getImportFluids()), controllerBase.getImportFluids());
    }

    @Override
    public void removeFromMultiBlock(MultiblockControllerBase controllerBase) {
        super.removeFromMultiBlock(controllerBase);
        this.fluidInventory = new FluidTankList(false);
    }

    @Override
    protected ModularUI createUI(EntityPlayer entityPlayer) {
        return null;
    }

    @Override
    protected boolean openGUIOnRightClick() {
        return false;
    }

    @Override
    public MultiblockAbility<IFluidHandler> getAbility() {
        return MultiblockAbility.TANK_VALVE;
    }

    @Override
    public void registerAbilities(@Nonnull List<IFluidHandler> abilityList) {
        abilityList.add(this.getImportFluids());
    }

    @Override
    protected boolean shouldSerializeInventories() {
        return false;
    }
}
