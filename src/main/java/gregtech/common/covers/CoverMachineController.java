package gregtech.common.covers;

import codechicken.lib.raytracer.CuboidRayTraceResult;
import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Cuboid6;
import codechicken.lib.vec.Matrix4;
import com.cleanroommc.modularui.api.drawable.ItemDrawable;
import com.cleanroommc.modularui.api.drawable.Text;
import com.cleanroommc.modularui.api.math.Alignment;
import com.cleanroommc.modularui.api.math.Color;
import com.cleanroommc.modularui.api.screen.ModularWindow;
import com.cleanroommc.modularui.api.screen.UIBuildContext;
import com.cleanroommc.modularui.common.widget.ButtonWidget;
import com.cleanroommc.modularui.common.widget.CycleButtonWidget;
import com.cleanroommc.modularui.common.widget.SliderWidget;
import com.cleanroommc.modularui.common.widget.TextWidget;
import gregtech.api.capability.GregtechTileCapabilities;
import gregtech.api.capability.IControllable;
import gregtech.api.cover.CoverBehavior;
import gregtech.api.cover.CoverWithUI;
import gregtech.api.cover.ICoverable;
import gregtech.api.gui.GregTechUI;
import gregtech.api.gui.GuiTextures;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.client.renderer.texture.Textures;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.*;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public class CoverMachineController extends CoverBehavior implements CoverWithUI {

    private int minRedstoneStrength;
    private boolean isInverted;
    private ControllerMode controllerMode;

    public CoverMachineController(ICoverable coverHolder, EnumFacing attachedSide) {
        super(coverHolder, attachedSide);
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
        coverHolder.markDirty();
    }

    public void setInverted(boolean inverted) {
        isInverted = inverted;
        updateRedstoneStatus();
        coverHolder.markDirty();
    }

    public void setControllerMode(ControllerMode controllerMode) {
        resetCurrentControllable();
        this.controllerMode = controllerMode;
        updateRedstoneStatus();
        coverHolder.markDirty();
    }

    private void cycleNextControllerMode() {
        List<ControllerMode> allowedModes = getAllowedModes();
        int nextIndex = allowedModes.indexOf(controllerMode) + 1;
        if (!allowedModes.isEmpty()) {
            setControllerMode(allowedModes.get(nextIndex % allowedModes.size()));
        }
    }

    public List<ControllerMode> getAllowedModes() {
        ArrayList<ControllerMode> results = new ArrayList<>();
        for (ControllerMode controllerMode : ControllerMode.values()) {
            IControllable controllable = null;
            if (controllerMode.side == null) {
                controllable = coverHolder.getCapability(GregtechTileCapabilities.CAPABILITY_CONTROLLABLE, attachedSide);
            } else {
                CoverBehavior coverBehavior = coverHolder.getCoverAtSide(controllerMode.side);
                if (coverBehavior != null) {
                    controllable = coverBehavior.getCapability(GregtechTileCapabilities.CAPABILITY_CONTROLLABLE, null);
                }
            }
            if (controllable != null) {
                results.add(controllerMode);
            }
        }
        return results;
    }

    @Override
    public boolean canAttach() {
        return !getAllowedModes().isEmpty();
    }

    @Override
    public EnumActionResult onScrewdriverClick(EntityPlayer playerIn, EnumHand hand, CuboidRayTraceResult hitResult) {
        if (!coverHolder.getWorld().isRemote) {
            GregTechUI.getCoverUi(attachedSide).open(playerIn, coverHolder.getWorld(), coverHolder.getPos());
        }
        return EnumActionResult.SUCCESS;
    }

    @Override
    public ModularWindow createWindow(UIBuildContext buildContext) {
        ItemDrawable controlledItemDisplay = new ItemDrawable(getControlledItem());
        ModularWindow.Builder builder = ModularWindow.builder(176, 85);
        builder.setBackground(GuiTextures.VANILLA_BACKGROUND)
                .widget(new TextWidget(Text.localised("cover.machine_controller.title"))
                        .setPos(10, 5))
                .widget(new SliderWidget()
                        .setGetter(() -> (float) minRedstoneStrength)
                        .setSetter(val -> setMinRedstoneStrength((int) (val + 0.5f)))
                        .setBounds(0, 15)
                        .setSize(156, 20)
                        .setPos(10, 18))
                .widget(TextWidget.dynamicText(() -> Text.localised("cover.machine_controller.redstone", minRedstoneStrength).color(Color.WHITE.normal))
                        .setTextAlignment(Alignment.Center)
                        .setSize(156, 20)
                        .setPos(10, 18))
                .widget(new ButtonWidget()
                        .setOnClick((clickData, widget) -> {
                            cycleNextControllerMode();
                            controlledItemDisplay.setItem(getControlledItem());
                        })
                        .setBackground(GuiTextures.BASE_BUTTON)
                        .setSize(134, 18)
                        .setPos(10, 40))
                .widget(TextWidget.dynamicText(() -> Text.localised(getControllerMode().getName()).color(Color.WHITE.normal).shadow())
                        .setTextAlignment(Alignment.Center)
                        .setSize(134, 18)
                        .setPos(10, 40))
                .widget(controlledItemDisplay.asWidget().setPos(148, 41))
                .widget(new CycleButtonWidget()
                        .setToggle(this::isInverted, this::setInverted)
                        .setTextureGetter(val -> Text.localised(val == 0 ? "cover.machine_controller.normal" : "cover.machine_controller.inverted").color(Color.WHITE.normal).shadow())
                        .addTooltip(1, Text.localised("cover.machine_controller.inverted.description").color(Color.WHITE.normal).shadow())
                        .addTooltip(0, Text.localised("cover.machine_controller.normal.description").color(Color.WHITE.normal).shadow())
                        .setBackground(GuiTextures.BASE_BUTTON)
                        .setSize(80, 18)
                        .setPos(48, 60));
        return builder.build();
    }

    @Override
    public void onAttached(ItemStack itemStack) {
        super.onAttached(itemStack);
        this.controllerMode = getAllowedModes().iterator().next();
        updateRedstoneStatus();
    }

    @Override
    public void onRemoved() {
        super.onRemoved();
        resetCurrentControllable();
    }

    @Override
    public void renderCover(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline, Cuboid6 plateBox, BlockRenderLayer layer) {
        Textures.MACHINE_CONTROLLER_OVERLAY.renderSided(attachedSide, plateBox, renderState, pipeline, translation);
    }

    @Override
    public boolean canConnectRedstone() {
        return true;
    }

    @Override
    public void onRedstoneInputSignalChange(int newSignalStrength) {
        updateRedstoneStatus();
    }

    private ItemStack getControlledItem() {
        EnumFacing side = getControllerMode().side;
        if (side == null) {
            if (this.coverHolder instanceof MetaTileEntity) {
                return coverHolder.getStackForm();
            }
        } else {
            CoverBehavior coverBehavior = coverHolder.getCoverAtSide(side);
            if (coverBehavior != null) {
                return coverBehavior.getCoverDefinition().getDropItemStack();
            }
        }
        return ItemStack.EMPTY;
    }

    private IControllable getControllable() {
        EnumFacing side = controllerMode.side;
        if (side == null) {
            return coverHolder.getCapability(GregtechTileCapabilities.CAPABILITY_CONTROLLABLE, attachedSide);
        } else {
            CoverBehavior coverBehavior = coverHolder.getCoverAtSide(side);
            if (coverBehavior == null) {
                return null;
            }
            return coverBehavior.getCapability(GregtechTileCapabilities.CAPABILITY_CONTROLLABLE, null);
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
        boolean shouldAllowWorking = getRedstoneSignalInput() < minRedstoneStrength;
        //noinspection SimplifiableConditionalExpression
        return isInverted ? !shouldAllowWorking : shouldAllowWorking;
    }

    private boolean doesOtherAllowingWork() {
        boolean otherAllow = true;
        CoverMachineController cover;
        for (EnumFacing side : EnumFacing.values()) {
            if (side != attachedSide && coverHolder.getCoverAtSide(side) instanceof CoverMachineController) {
                cover = (CoverMachineController) coverHolder.getCoverAtSide(side);
                otherAllow = otherAllow && cover.controllerMode == controllerMode && cover.shouldAllowWorking();
            }
        }
        return otherAllow;
    }

    @Override
    public void writeToNBT(NBTTagCompound tagCompound) {
        super.writeToNBT(tagCompound);
        tagCompound.setInteger("MinRedstoneStrength", minRedstoneStrength);
        tagCompound.setBoolean("Inverted", isInverted);
        tagCompound.setInteger("ControllerMode", controllerMode.ordinal());
    }

    @Override
    public void readFromNBT(NBTTagCompound tagCompound) {
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


        @Nonnull
        @Override
        public String getName() {
            return localeName;
        }
    }
}
