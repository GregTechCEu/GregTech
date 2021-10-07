package gregtech.common.covers;

import codechicken.lib.raytracer.CuboidRayTraceResult;
import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Cuboid6;
import codechicken.lib.vec.Matrix4;
import gregtech.api.cover.CoverBehavior;
import gregtech.api.cover.CoverWithUI;
import gregtech.api.cover.ICoverable;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.ModularUI;
import gregtech.api.gui.widgets.*;
import gregtech.api.render.Textures;
import gregtech.api.util.FluidTankSwitchShim;
import gregtech.api.util.GTFluidUtils;
import gregtech.api.util.VirtualTankRegistry;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.*;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;

public class CoverEnderFluidLink extends CoverBehavior implements CoverWithUI, ITickable {

    private final int TRANSFER_RATE = 8000; // mB/t

    protected CoverPump.PumpMode pumpMode;
    private int color;
    private final FluidTankSwitchShim linkedTank;

    public CoverEnderFluidLink(ICoverable coverHolder, EnumFacing attachedSide) {
        super(coverHolder, attachedSide);
        pumpMode = CoverPump.PumpMode.IMPORT;
        //todo argb?
        color = 0xFFFFFF;
        this.linkedTank = new FluidTankSwitchShim(VirtualTankRegistry.getTankCreate(makeTankName()));
        VirtualTankRegistry.addRef(makeTankName());
    }

    private String makeTankName() {
        return "EFLink#" + Integer.toHexString(color).toUpperCase();
    }

    @Override
    public boolean canAttach() {
        return this.coverHolder.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, attachedSide) != null;
    }

    @Override
    public void renderCover(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline, Cuboid6 plateBox, BlockRenderLayer layer) {
        Textures.ENDER_FLUID_LINK.renderSided(attachedSide, plateBox, renderState, pipeline, translation);
    }

    @Override
    public EnumActionResult onScrewdriverClick(EntityPlayer playerIn, EnumHand hand, CuboidRayTraceResult hitResult) {
        if (!coverHolder.getWorld().isRemote) {
            openUI((EntityPlayerMP) playerIn);
        }
        return EnumActionResult.SUCCESS;
    }

    @Override
    public void onRemoved() {
        VirtualTankRegistry.delRef(makeTankName());
    }

    @Override
    public void update() {
        transferFluids();
    }

    protected void transferFluids() {
        IFluidHandler fluidHandler = coverHolder.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, attachedSide);
        if (pumpMode == CoverPump.PumpMode.IMPORT) {
            GTFluidUtils.transferFluids(fluidHandler, linkedTank, TRANSFER_RATE);
        } else if (pumpMode == CoverPump.PumpMode.EXPORT) {
            GTFluidUtils.transferFluids(linkedTank, fluidHandler, TRANSFER_RATE);
        }
    }

    public void setPumpMode(CoverPump.PumpMode pumpMode) {
        this.pumpMode = pumpMode;
        coverHolder.markDirty();
    }

    public CoverPump.PumpMode getPumpMode() {
        return pumpMode;
    }

    @Override
    public ModularUI createUI(EntityPlayer player) {
        return ModularUI.defaultBuilder()
                .widget(new LabelWidget(10, 5, "cover.ender_fluid_link.title"))
                .widget(new TextFieldWidget(10, 20, 72, 18, true,
                        () -> Integer.toHexString(color).toUpperCase(), this::updateColor, 6)
                        //todo allow empty string somehow?
                        .setValidator(str -> str.matches("[0-9a-fA-F]+")))
                .widget(new CycleButtonWidget(10, 63, 75, 18,
                        CoverPump.PumpMode.class, this::getPumpMode, this::setPumpMode))
                .widget(new TankWidget(this.linkedTank, 90, 20, 18, 18)
                        .setContainerClicking(true, true)
                        .setBackgroundTexture(GuiTextures.FLUID_SLOT).setAlwaysShowFull(true))
                .bindPlayerInventory(player.inventory)
        .build(this, player);
    }

    public void updateColor(String str) {
        VirtualTankRegistry.delRef(makeTankName());
        this.color = Integer.parseInt(str.toUpperCase(), 16);
        updateTankLink();
    }

    public void updateTankLink() {
        this.linkedTank.changeTank(VirtualTankRegistry.getTankCreate(makeTankName()));
        VirtualTankRegistry.addRef(makeTankName());
        coverHolder.markDirty();
    }

    @Override
    public void writeToNBT(NBTTagCompound tagCompound) {
        super.writeToNBT(tagCompound);
        tagCompound.setInteger("Frequency", color);
        tagCompound.setInteger("PumpMode", pumpMode.ordinal());
    }

    @Override
    public void readFromNBT(NBTTagCompound tagCompound) {
        super.readFromNBT(tagCompound);
        VirtualTankRegistry.delRef(makeTankName());
        this.color = tagCompound.getInteger("Frequency");
        this.pumpMode = CoverPump.PumpMode.values()[tagCompound.getInteger("PumpMode")];
        updateTankLink();
    }
}
