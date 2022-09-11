package gregtech.common.metatileentities.multi;

import codechicken.lib.raytracer.CuboidRayTraceResult;
import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import gregtech.api.capability.impl.FluidHandlerProxy;
import gregtech.api.capability.impl.FluidTankList;
import gregtech.api.gui.ModularUI;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.IMultiblockAbilityPart;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.api.metatileentity.multiblock.MultiblockControllerBase;
import gregtech.api.util.GTTransferUtils;
import gregtech.client.renderer.ICubeRenderer;
import gregtech.client.renderer.texture.Textures;
import gregtech.common.metatileentities.multi.multiblockpart.MetaTileEntityMultiblockPart;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public class MetaTileEntityTankValve extends MetaTileEntityMultiblockPart implements IMultiblockAbilityPart<IFluidHandler> {

    private final boolean isMetal;

    public MetaTileEntityTankValve(ResourceLocation metaTileEntityId, boolean isMetal) {
        super(metaTileEntityId, 0);
        this.isMetal = isMetal;
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity tileEntity) {
        return new MetaTileEntityTankValve(metaTileEntityId, isMetal);
    }

    @Override
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        super.renderMetaTileEntity(renderState, translation, pipeline);
        Textures.PIPE_IN_OVERLAY.renderSided(getFrontFacing(), renderState, translation, pipeline);
    }

    @Override
    public ICubeRenderer getBaseTexture() {
        if (getController() == null) {
            if (isMetal)
                return Textures.SOLID_STEEL_CASING;
            return Textures.WOOD_WALL;
        }
        return super.getBaseTexture();
    }

    @Override
    public int getDefaultPaintingColor() {
        return 0xFFFFFF;
    }

    @Override
    public void update() {
        super.update();
        if (!getWorld().isRemote && getOffsetTimer() % 5 == 0L && isAttachedToMultiBlock() && getFrontFacing() == EnumFacing.DOWN) {
            TileEntity tileEntity = getWorld().getTileEntity(getPos().offset(getFrontFacing()));
            IFluidHandler fluidHandler = tileEntity == null ? null : tileEntity.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, getFrontFacing().getOpposite());
            if (fluidHandler != null) {
                GTTransferUtils.transferFluids(fluidInventory, fluidHandler);
            }
        }
    }

    @Override
    protected void initializeInventory() {
        super.initializeInventory();
        this.fluidInventory = new FluidHandlerProxy(new FluidTankList(false), new FluidTankList(false));
    }

    @Override
    public boolean onWrenchClick(EntityPlayer playerIn, EnumHand hand, EnumFacing wrenchSide, CuboidRayTraceResult hitResult) {
        boolean wasRotated = super.onWrenchClick(playerIn, hand, wrenchSide, hitResult);
        if (wasRotated && !getWorld().isRemote) {
            reinitializeFluidInventory(getFrontFacing());
        }
        return wasRotated;
    }

    @Override
    public void addToMultiBlock(MultiblockControllerBase controllerBase) {
        super.addToMultiBlock(controllerBase);
        if (getFrontFacing() == EnumFacing.DOWN) {
            this.fluidInventory = new FluidHandlerProxy(new FluidTankList(false), controllerBase.getImportFluids());
        } else {
            this.fluidInventory = new FluidHandlerProxy(new FluidTankList(false, controllerBase.getImportFluids()), controllerBase.getImportFluids());
        }
    }

    private void reinitializeFluidInventory(EnumFacing facing) {
        FluidHandlerProxy proxy = (FluidHandlerProxy) fluidInventory;
        if (facing == EnumFacing.DOWN) {
            proxy.reinitializeHandler(new FluidTankList(false), proxy.output);
        } else {
            proxy.reinitializeHandler(proxy.output, proxy.output);
        }
    }

    @Override
    public void removeFromMultiBlock(MultiblockControllerBase controllerBase) {
        super.removeFromMultiBlock(controllerBase);
        this.fluidInventory = new FluidHandlerProxy(new FluidTankList(false), new FluidTankList(false));
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
    public boolean canPartShare() {
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

    @Override
    public void addInformation(ItemStack stack, @Nullable World player, List<String> tooltip, boolean advanced) {
        super.addInformation(stack, player, tooltip, advanced);
        tooltip.add(I18n.format("gregtech.tank_valve.tooltip"));
    }

    @Override
    public boolean needsSneakToRotate() {
        return true;
    }
}
