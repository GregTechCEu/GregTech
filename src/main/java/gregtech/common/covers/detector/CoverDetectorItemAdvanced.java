package gregtech.common.covers.detector;

import gregtech.api.cover.CoverDefinition;
import gregtech.api.cover.CoverWithUI;
import gregtech.api.cover.CoverableView;
import gregtech.api.gui.widgets.*;
import gregtech.api.mui.GTGuis;
import gregtech.api.util.RedstoneUtil;
import gregtech.client.renderer.texture.Textures;
import gregtech.common.covers.filter.ItemFilterContainer;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

import codechicken.lib.raytracer.CuboidRayTraceResult;
import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Cuboid6;
import codechicken.lib.vec.Matrix4;
import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.drawable.DynamicDrawable;
import com.cleanroommc.modularui.factory.SidedPosGuiData;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.value.sync.BooleanSyncValue;
import com.cleanroommc.modularui.value.sync.PanelSyncManager;
import com.cleanroommc.modularui.widgets.ToggleButton;
import com.cleanroommc.modularui.widgets.layout.Flow;
import org.jetbrains.annotations.NotNull;

public class CoverDetectorItemAdvanced extends CoverDetectorItem implements CoverWithUI {

    private static final int PADDING = 3;
    private static final int SIZE = 18;

    private static final int DEFAULT_MIN = 64;
    private static final int DEFAULT_MAX = 512;

    private int min = DEFAULT_MIN;
    private int max = DEFAULT_MAX;
    private int outputAmount;
    private boolean isLatched = false;
    protected ItemFilterContainer itemFilter;

    public CoverDetectorItemAdvanced(@NotNull CoverDefinition definition, @NotNull CoverableView coverableView,
                                     @NotNull EnumFacing attachedSide) {
        super(definition, coverableView, attachedSide);
        this.itemFilter = new ItemFilterContainer(this);
    }

    @Override
    public void renderCover(@NotNull CCRenderState renderState, @NotNull Matrix4 translation,
                            IVertexOperation[] pipeline, @NotNull Cuboid6 plateBox, @NotNull BlockRenderLayer layer) {
        Textures.DETECTOR_ITEM_ADVANCED.renderSided(getAttachedSide(), plateBox, renderState, pipeline, translation);
    }

    @Override
    public ModularPanel buildUI(SidedPosGuiData guiData, PanelSyncManager guiSyncManager) {
        return GTGuis.defaultPanel(this)
                .height(202)
                .child(CoverWithUI.createTitleRow(getPickItem()))
                .child(Flow.column()
                        .top(28)
                        .left(5).right(5)
                        .coverChildrenHeight()
                        .child(createMinMaxRow("cover.advanced_item_detector.min", this::getMinValue,
                                this::setMinValue))
                        .child(createMinMaxRow("cover.advanced_item_detector.max", this::getMaxValue,
                                this::setMaxValue))
                        .child(Flow.row()
                                .widthRel(1f)
                                .coverChildrenHeight()
                                .marginBottom(5)
                                .child(new ToggleButton()
                                        .size(72, 18)
                                        .overlay(new DynamicDrawable(() -> {
                                            String lang = "cover.advanced_energy_detector.";
                                            lang += isInverted() ? "inverted" : "normal";
                                            return IKey.lang(lang).format(TextFormatting.WHITE);
                                        }))
                                        .addTooltipLine(IKey.lang("cover.generic.advanced_detector.invert_tooltip"))
                                        .value(new BooleanSyncValue(this::isInverted, this::setInverted)))
                                .child(new ToggleButton()
                                        .size(72, 18)
                                        .right(0)
                                        .overlay(new DynamicDrawable(() -> {
                                            String lang = "cover.generic.advanced_detector.";
                                            lang += isLatched() ? "latched" : "continuous";
                                            return IKey.lang(lang).format(TextFormatting.WHITE);
                                        }))
                                        .addTooltipLine(IKey.lang("cover.generic.advanced_detector.latch_tooltip"))
                                        .value(new BooleanSyncValue(this::isLatched, this::setLatched))))
                        .child(itemFilter.initUI(guiData, guiSyncManager)))
                .bindPlayerInventory();
    }

    private String getMinValue() {
        return String.valueOf(min);
    }

    private String getMaxValue() {
        return String.valueOf(max);
    }

    private void setMinValue(String val) {
        this.min = CoverDetectorBase.parseCapped(val, 0, this.max - 1, DEFAULT_MIN);
    }

    private void setMaxValue(String val) {
        this.max = CoverDetectorBase.parseCapped(val, this.min + 1, Integer.MAX_VALUE, DEFAULT_MAX);
    }

    private void setLatched(boolean isLatched) {
        this.isLatched = isLatched;
    }

    public boolean isLatched() {
        return this.isLatched;
    }

    @Override
    public @NotNull EnumActionResult onScrewdriverClick(@NotNull EntityPlayer playerIn, @NotNull EnumHand hand,
                                                        @NotNull CuboidRayTraceResult hitResult) {
        if (!getWorld().isRemote) {
            openUI((EntityPlayerMP) playerIn);
        }
        return EnumActionResult.SUCCESS;
    }

    @Override
    public void update() {
        if (getOffsetTimer() % 20 != 0) return;

        IItemHandler itemHandler = getCoverableView().getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY,
                null);
        if (itemHandler == null) return;

        int storedItems = 0;

        for (int i = 0; i < itemHandler.getSlots(); i++) {
            if (itemFilter.test(itemHandler.getStackInSlot(i)))
                storedItems += itemHandler.getStackInSlot(i).getCount();
        }

        if (isLatched) {
            outputAmount = RedstoneUtil.computeLatchedRedstoneBetweenValues(storedItems, max, min, isInverted(),
                    outputAmount);
        } else {
            outputAmount = RedstoneUtil.computeRedstoneBetweenValues(storedItems, max, min, isInverted());
        }

        setRedstoneSignalOutput(outputAmount);
    }

    @Override
    public void writeToNBT(@NotNull NBTTagCompound tagCompound) {
        super.writeToNBT(tagCompound);
        tagCompound.setInteger("min", this.min);
        tagCompound.setInteger("max", this.max);
        tagCompound.setBoolean("isLatched", this.isLatched);
        tagCompound.setTag("filter", this.itemFilter.serializeNBT());
    }

    @Override
    public void readFromNBT(@NotNull NBTTagCompound tagCompound) {
        super.readFromNBT(tagCompound);
        this.min = tagCompound.getInteger("min");
        this.max = tagCompound.getInteger("max");
        this.isLatched = tagCompound.getBoolean("isLatched");
        this.itemFilter.deserializeNBT(tagCompound.getCompoundTag("filter"));
    }

    @Override
    public void writeInitialSyncData(@NotNull PacketBuffer packetBuffer) {
        super.writeInitialSyncData(packetBuffer);
        packetBuffer.writeInt(this.min);
        packetBuffer.writeInt(this.max);
        packetBuffer.writeBoolean(this.isLatched);
    }

    @Override
    public void readInitialSyncData(@NotNull PacketBuffer packetBuffer) {
        super.readInitialSyncData(packetBuffer);
        this.min = packetBuffer.readInt();
        this.max = packetBuffer.readInt();
        this.isLatched = packetBuffer.readBoolean();
    }
}
