package gregtech.common.covers;

import gregtech.api.capability.GregtechTileCapabilities;
import gregtech.api.capability.IControllable;
import gregtech.api.cover.*;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.ModularUI;
import gregtech.api.gui.widgets.*;
import gregtech.client.renderer.texture.Textures;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.*;
import net.minecraftforge.items.ItemStackHandler;

import codechicken.lib.raytracer.CuboidRayTraceResult;
import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Cuboid6;
import codechicken.lib.vec.Matrix4;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class CoverMachineController extends CoverBase implements CoverWithUI {

    private int minRedstoneStrength;
    private boolean isInverted;
    private ControllerMode controllerMode;
    private final ItemStackHandler displayInventory = new ItemStackHandler(1);

    public CoverMachineController(@NotNull CoverDefinition definition, @NotNull CoverableView coverableView,
                                  @NotNull EnumFacing attachedSide) {
        super(definition, coverableView, attachedSide);
        this.minRedstoneStrength = 1;
        this.isInverted = false;
        this.controllerMode = ControllerMode.MACHINE;
    }

    public int getMinRedstoneStrength() {
        return minRedstoneStrength;
    }

    public ControllerMode getControllerMode() {
        return controllerMode;
    }

    public boolean isInverted() {
        return isInverted;
    }

    public void setMinRedstoneStrength(int minRedstoneStrength) {
        this.minRedstoneStrength = minRedstoneStrength;
        updateRedstoneStatus();
        getCoverableView().markDirty();
    }

    public void setInverted(boolean inverted) {
        isInverted = inverted;
        updateRedstoneStatus();
        getCoverableView().markDirty();
    }

    public void setControllerMode(ControllerMode controllerMode) {
        resetCurrentControllable();
        this.controllerMode = controllerMode;
        updateRedstoneStatus();
        updateDisplayInventory();
        getCoverableView().markDirty();
    }

    private void cycleNextControllerMode() {
        List<ControllerMode> allowedModes = getAllowedModes(getCoverableView(), getAttachedSide());
        int nextIndex = allowedModes.indexOf(controllerMode) + 1;
        if (!allowedModes.isEmpty()) {
            setControllerMode(allowedModes.get(nextIndex % allowedModes.size()));
        }
    }

    public List<ControllerMode> getAllowedModes(@NotNull CoverableView coverable, @NotNull EnumFacing side) {
        List<ControllerMode> results = new ArrayList<>();
        for (ControllerMode controllerMode : ControllerMode.values()) {
            IControllable controllable = null;
            if (controllerMode.side == null) {
                controllable = coverable.getCapability(GregtechTileCapabilities.CAPABILITY_CONTROLLABLE, side);
            } else {
                Cover cover = coverable.getCoverAtSide(controllerMode.side);
                if (cover != null) {
                    controllable = cover.getCapability(GregtechTileCapabilities.CAPABILITY_CONTROLLABLE, null);
                }
            }
            if (controllable != null) {
                results.add(controllerMode);
            }
        }
        return results;
    }

    @Override
    public boolean canAttach(@NotNull CoverableView coverable, @NotNull EnumFacing side) {
        return !getAllowedModes(coverable, side).isEmpty();
    }

    @Override
    public @NotNull EnumActionResult onScrewdriverClick(@NotNull EntityPlayer playerIn, @NotNull EnumHand hand,
                                                        @NotNull CuboidRayTraceResult hitResult) {
        if (!getCoverableView().getWorld().isRemote) {
            openUI((EntityPlayerMP) playerIn);
        }
        return EnumActionResult.SUCCESS;
    }

    @Override
    public ModularUI createUI(EntityPlayer player) {
        updateDisplayInventory();
        return ModularUI.builder(GuiTextures.BACKGROUND, 176, 95)
                .image(4, 4, 16, 16, GuiTextures.COVER_MACHINE_CONTROLLER)
                .label(24, 8, "cover.machine_controller.title")
                .widget(new SliderWidget("cover.machine_controller.redstone", 10, 24, 156, 20, 1.0f, 15.0f,
                        minRedstoneStrength, it -> setMinRedstoneStrength((int) it)))
                .widget(new ClickButtonWidget(10, 48, 134, 18, "", data -> cycleNextControllerMode()))
                .widget(new SimpleTextWidget(77, 58, "", 0xFFFFFF, () -> getControllerMode().getName()).setShadow(true))
                .widget(new SlotWidget(displayInventory, 0, 148, 48, false, false)
                        .setBackgroundTexture(GuiTextures.SLOT))
                .widget(new CycleButtonWidget(48, 70, 80, 18, this::isInverted, this::setInverted,
                        "cover.machine_controller.normal", "cover.machine_controller.inverted")
                                .setTooltipHoverString("cover.machine_controller.inverted.description"))
                .build(this, player);
    }

    @Override
    public void onAttachment(@NotNull CoverableView coverableView, @NotNull EnumFacing side,
                             @Nullable EntityPlayer player, @NotNull ItemStack itemStack) {
        super.onAttachment(coverableView, side, player, itemStack);
        this.controllerMode = getAllowedModes(getCoverableView(), getAttachedSide()).iterator().next();
        updateRedstoneStatus();
    }

    @Override
    public void onRemoval() {
        super.onRemoval();
        resetCurrentControllable();
    }

    @Override
    public void renderCover(@NotNull CCRenderState renderState, @NotNull Matrix4 translation,
                            IVertexOperation[] pipeline, @NotNull Cuboid6 plateBox, @NotNull BlockRenderLayer layer) {
        Textures.MACHINE_CONTROLLER_OVERLAY.renderSided(getAttachedSide(), plateBox, renderState, pipeline,
                translation);
    }

    @Override
    public boolean canConnectRedstone() {
        return true;
    }

    @Override
    public void onRedstoneInputSignalChange(int newSignalStrength) {
        updateRedstoneStatus();
    }

    private void updateDisplayInventory() {
        EnumFacing controlledSide = getControllerMode().side;
        ItemStack resultStack = ItemStack.EMPTY;
        if (controlledSide != null) {
            Cover cover = getCoverableView().getCoverAtSide(controlledSide);
            if (cover != null) {
                resultStack = cover.getDefinition().getDropItemStack();
            }
        }
        this.displayInventory.setStackInSlot(0, resultStack);
    }

    private @Nullable IControllable getControllable() {
        EnumFacing side = controllerMode.side;
        if (side == null) {
            return getCoverableView().getCapability(GregtechTileCapabilities.CAPABILITY_CONTROLLABLE,
                    getAttachedSide());
        } else {
            Cover cover = getCoverableView().getCoverAtSide(side);
            if (cover == null) {
                return null;
            }
            return cover.getCapability(GregtechTileCapabilities.CAPABILITY_CONTROLLABLE, null);
        }
    }

    private void resetCurrentControllable() {
        IControllable controllable = getControllable();
        if (controllable != null) {
            controllable.setWorkingEnabled(doesOtherAllowingWork());
        }
    }

    private void updateRedstoneStatus() {
        IControllable controllable = getControllable();
        if (controllable != null) {
            controllable.setWorkingEnabled(shouldAllowWorking() && doesOtherAllowingWork());
        }
    }

    private boolean shouldAllowWorking() {
        boolean shouldAllowWorking = getCoverableView().getInputRedstoneSignal(getAttachedSide(), true) <
                minRedstoneStrength;
        // noinspection SimplifiableConditionalExpression
        return isInverted ? !shouldAllowWorking : shouldAllowWorking;
    }

    private boolean doesOtherAllowingWork() {
        boolean otherAllow = true;
        CoverMachineController cover;
        EnumFacing attachedSide = getAttachedSide();
        CoverableView coverable = getCoverableView();
        for (EnumFacing side : EnumFacing.values()) {
            if (side != attachedSide &&
                    coverable.getCoverAtSide(side) instanceof CoverMachineController machineController) {
                cover = machineController;
                otherAllow = otherAllow && cover.controllerMode == controllerMode && cover.shouldAllowWorking();
            }
        }
        return otherAllow;
    }

    @Override
    public void writeToNBT(@NotNull NBTTagCompound tagCompound) {
        super.writeToNBT(tagCompound);
        tagCompound.setInteger("MinRedstoneStrength", minRedstoneStrength);
        tagCompound.setBoolean("Inverted", isInverted);
        tagCompound.setInteger("ControllerMode", controllerMode.ordinal());
    }

    @Override
    public void readFromNBT(@NotNull NBTTagCompound tagCompound) {
        super.readFromNBT(tagCompound);
        this.minRedstoneStrength = tagCompound.getInteger("MinRedstoneStrength");
        this.isInverted = tagCompound.getBoolean("Inverted");
        this.controllerMode = ControllerMode.values()[tagCompound.getInteger("ControllerMode")];
    }

    public enum ControllerMode implements IStringSerializable {

        MACHINE("cover.machine_controller.mode.machine", null),
        COVER_UP("cover.machine_controller.mode.cover_up", EnumFacing.UP),
        COVER_DOWN("cover.machine_controller.mode.cover_down", EnumFacing.DOWN),
        COVER_NORTH("cover.machine_controller.mode.cover_north", EnumFacing.NORTH),
        COVER_SOUTH("cover.machine_controller.mode.cover_south", EnumFacing.SOUTH),
        COVER_EAST("cover.machine_controller.mode.cover_east", EnumFacing.EAST),
        COVER_WEST("cover.machine_controller.mode.cover_west", EnumFacing.WEST);

        public final String localeName;
        public final EnumFacing side;

        ControllerMode(String localeName, EnumFacing side) {
            this.localeName = localeName;
            this.side = side;
        }

        @NotNull
        @Override
        public String getName() {
            return localeName;
        }
    }
}
