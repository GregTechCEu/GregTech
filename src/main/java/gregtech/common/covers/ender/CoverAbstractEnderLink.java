package gregtech.common.covers.ender;

import gregtech.api.capability.GregtechDataCodes;
import gregtech.api.capability.IControllable;
import gregtech.api.cover.CoverBase;
import gregtech.api.cover.CoverDefinition;
import gregtech.api.cover.CoverWithUI;
import gregtech.api.cover.CoverableView;
import gregtech.api.mui.GTGuiTextures;
import gregtech.api.mui.GTGuis;
import gregtech.api.util.virtualregistry.EntryTypes;
import gregtech.api.util.virtualregistry.VirtualEntry;
import gregtech.api.util.virtualregistry.VirtualRegistryBase;
import gregtech.common.mui.widget.InteractableText;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ITickable;

import codechicken.lib.raytracer.CuboidRayTraceResult;
import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.api.widget.IWidget;
import com.cleanroommc.modularui.api.widget.Interactable;
import com.cleanroommc.modularui.drawable.DynamicDrawable;
import com.cleanroommc.modularui.drawable.GuiTextures;
import com.cleanroommc.modularui.drawable.Rectangle;
import com.cleanroommc.modularui.factory.SidedPosGuiData;
import com.cleanroommc.modularui.network.NetworkUtils;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.utils.Color;
import com.cleanroommc.modularui.value.sync.BooleanSyncValue;
import com.cleanroommc.modularui.value.sync.GuiSyncManager;
import com.cleanroommc.modularui.value.sync.PanelSyncHandler;
import com.cleanroommc.modularui.value.sync.StringSyncValue;
import com.cleanroommc.modularui.widgets.ButtonWidget;
import com.cleanroommc.modularui.widgets.ListWidget;
import com.cleanroommc.modularui.widgets.ToggleButton;
import com.cleanroommc.modularui.widgets.layout.Column;
import com.cleanroommc.modularui.widgets.layout.Row;
import com.cleanroommc.modularui.widgets.textfield.TextFieldWidget;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.regex.Pattern;

@SuppressWarnings("SameParameterValue")
public abstract class CoverAbstractEnderLink<T extends VirtualEntry> extends CoverBase
                                            implements CoverWithUI, ITickable, IControllable {

    protected static final Pattern COLOR_INPUT_PATTERN = Pattern.compile("[0-9a-fA-F]*");
    public static final int UPDATE_PRIVATE = GregtechDataCodes.assignId();

    protected T activeEntry = null;
    protected String color = VirtualEntry.DEFAULT_COLOR;
    protected UUID playerUUID = null;
    private boolean isPrivate = false;
    private boolean workingEnabled = true;
    private boolean ioEnabled = false;

    public CoverAbstractEnderLink(@NotNull CoverDefinition definition, @NotNull CoverableView coverableView,
                                  @NotNull EnumFacing attachedSide) {
        super(definition, coverableView, attachedSide);
        updateLink();
    }

    protected void updateLink() {
        this.activeEntry = VirtualRegistryBase.getOrCreateEntry(getOwner(), getType(), createName());
        this.activeEntry.setColor(this.color);
        markDirty();
    }

    protected abstract EntryTypes<T> getType();

    public String getColorStr() {
        return this.color;
    }

    protected final String createName() {
        ;
        return identifier() + this.color;
    }

    protected abstract String identifier();

    protected final UUID getOwner() {
        return isPrivate ? playerUUID : null;
    }

    @Override
    public void readCustomData(int discriminator, @NotNull PacketBuffer buf) {
        super.readCustomData(discriminator, buf);
        if (discriminator == UPDATE_PRIVATE) {
            setPrivate(buf.readBoolean());
            updateLink();
        }
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
    public void onAttachment(@NotNull CoverableView coverableView, @NotNull EnumFacing side,
                             @Nullable EntityPlayer player, @NotNull ItemStack itemStack) {
        super.onAttachment(coverableView, side, player, itemStack);
        if (player != null) {
            this.playerUUID = player.getUniqueID();
        }
    }

    public void updateColor(String str) {
        if (str.length() == 8) {
            this.color = str.toUpperCase();
            updateLink();
        }
    }

    @Override
    public boolean usesMui2() {
        return true;
    }

    @Override
    public ModularPanel buildUI(SidedPosGuiData guiData, GuiSyncManager guiSyncManager) {
        var panel = GTGuis.createPanel(this, 176, 192);

        return panel.child(CoverWithUI.createTitleRow(getPickItem()))
                .child(createWidgets(panel, guiSyncManager))
                .bindPlayerInventory();
    }

    protected Column createWidgets(ModularPanel panel, GuiSyncManager syncManager) {
        var name = new StringSyncValue(this::getColorStr, this::updateColor);

        var entrySelectorSH = createEntrySelector(panel);
        syncManager.syncValue("entry_selector", entrySelectorSH);

        return new Column().coverChildrenHeight().top(24)
                .margin(7, 0).widthRel(1f)
                .child(new Row().marginBottom(2)
                        .coverChildrenHeight()
                        .child(createPrivateButton())
                        .child(createColorIcon())
                        .child(new TextFieldWidget()
                                .height(18)
                                .value(name)
                                .setPattern(COLOR_INPUT_PATTERN)
                                .widthRel(0.5f)
                                .marginRight(2))
                        .child(createEntrySlot())
                        .child(new ButtonWidget<>()
                                .overlay(GTGuiTextures.MENU_OVERLAY)
                                .background(GTGuiTextures.MC_BUTTON)
                                .disableHoverBackground()
                                .addTooltipLine(IKey.lang("cover.generic.ender.open_selector"))
                                .onMousePressed(i -> {
                                    if (entrySelectorSH.isPanelOpen()) {
                                        entrySelectorSH.closePanel();
                                    } else {
                                        entrySelectorSH.openPanel();
                                    }
                                    Interactable.playButtonClickSound();
                                    return true;
                                })))
                .child(createIoRow());
    }

    protected abstract PanelSyncHandler createEntrySelector(ModularPanel panel);

    protected abstract IWidget createEntrySlot();

    protected IWidget createColorIcon() {
        // todo color selector popup panel
        return new DynamicDrawable(() -> new Rectangle()
                .setColor(this.activeEntry.getColor())
                .asIcon().size(16))
                        .asWidget()
                        .background(GTGuiTextures.SLOT)
                        .size(18)
                        .marginRight(2);
    }

    protected IWidget createPrivateButton() {
        var isPrivate = new BooleanSyncValue(this::isPrivate, this::setPrivate);
        isPrivate.updateCacheFromSource(true);

        return new ToggleButton()
                .tooltip(tooltip -> tooltip.setAutoUpdate(true))
                .background(GTGuiTextures.PRIVATE_MODE_BUTTON[0])
                .hoverBackground(GTGuiTextures.PRIVATE_MODE_BUTTON[0])
                .selectedBackground(GTGuiTextures.PRIVATE_MODE_BUTTON[1])
                .selectedHoverBackground(GTGuiTextures.PRIVATE_MODE_BUTTON[1])
                .tooltipBuilder(tooltip -> tooltip.addLine(IKey.lang(this.isPrivate ?
                        "cover.ender_fluid_link.private.tooltip.enabled" :
                        "cover.ender_fluid_link.private.tooltip.disabled")))
                .marginRight(2)
                .value(isPrivate);
    }

    protected IWidget createIoRow() {
        var ioEnabled = new BooleanSyncValue(this::isIoEnabled, this::setIoEnabled);

        return new Row().marginBottom(2)
                .coverChildrenHeight()
                .child(new ToggleButton()
                        .value(ioEnabled)
                        .overlay(IKey.dynamic(() -> IKey.lang(this.ioEnabled ?
                                "behaviour.soft_hammer.enabled" :
                                "behaviour.soft_hammer.disabled").get())
                                .color(Color.WHITE.darker(1)))
                        .widthRel(0.6f)
                        .left(0));
    }

    @Override
    public boolean isWorkingEnabled() {
        return workingEnabled;
    }

    @Override
    public void setWorkingEnabled(boolean isActivationAllowed) {
        this.workingEnabled = isActivationAllowed;
    }

    public boolean isIoEnabled() {
        return ioEnabled;
    }

    protected void setIoEnabled(boolean ioEnabled) {
        this.ioEnabled = ioEnabled;
    }

    private boolean isPrivate() {
        return isPrivate;
    }

    private void setPrivate(boolean isPrivate) {
        this.isPrivate = isPrivate;
        updateLink();
        writeCustomData(UPDATE_PRIVATE, buffer -> buffer.writeBoolean(this.isPrivate));
    }

    @Override
    public void writeInitialSyncData(PacketBuffer packetBuffer) {
        packetBuffer.writeString(this.playerUUID == null ? "null" : this.playerUUID.toString());
    }

    @Override
    public void readInitialSyncData(PacketBuffer packetBuffer) {
        // does client even need uuid info? just in case
        String uuidStr = packetBuffer.readString(36);
        this.playerUUID = uuidStr.equals("null") ? null : UUID.fromString(uuidStr);
        // client does not need the actual tank reference, the default one will do just fine
    }

    @Override
    public void readFromNBT(@NotNull NBTTagCompound nbt) {
        super.readFromNBT(nbt);
        this.ioEnabled = nbt.getBoolean("IOAllowed");
        this.isPrivate = nbt.getBoolean("Private");
        this.workingEnabled = nbt.getBoolean("WorkingAllowed");
        this.playerUUID = UUID.fromString(nbt.getString("PlacedUUID"));
        int color = nbt.getInteger("Frequency");
        this.color = Integer.toHexString(color).toUpperCase();
        updateLink();
    }

    @Override
    public void writeToNBT(@NotNull NBTTagCompound nbt) {
        super.writeToNBT(nbt);
        nbt.setBoolean("IOAllowed", ioEnabled);
        nbt.setBoolean("Private", isPrivate);
        nbt.setBoolean("WorkingAllowed", workingEnabled);
        nbt.setString("PlacedUUID", playerUUID.toString());
        nbt.setInteger("Frequency", activeEntry.getColor());
    }

    protected abstract class EntrySelectorSH extends PanelSyncHandler {

        private final EntryTypes<T> type;
        private final List<String> names = new ArrayList<>();

        protected EntrySelectorSH(ModularPanel mainPanel, EntryTypes<T> type) {
            super(mainPanel);
            this.type = type;
        }

        @Override
        public ModularPanel createUI(ModularPanel mainPanel, GuiSyncManager syncManager) {
            this.names.clear();
            this.names.addAll(VirtualRegistryBase.getEntryNames(getOwner(), type));
            return GTGuis.createPopupPanel("entry_selector", 168, 112)
                    .child(IKey.lang("cover.generic.ender.known_channels")
                            .color(UI_TITLE_COLOR).asWidget()
                            .top(6)
                            .left(4))
                    .child(ListWidget.builder(this.names, name -> createRow(name, mainPanel, syncManager))
                            .background(GTGuiTextures.DISPLAY.asIcon()
                                    .width(168 - 8)
                                    .height(112 - 20))
                            .paddingTop(1)
                            .size(168 - 12, 112 - 24)
                            .left(4)
                            .bottom(6));
        }

        protected IWidget createRow(String name, ModularPanel mainPanel, GuiSyncManager syncManager) {
            T entry = VirtualRegistryBase.getEntry(getOwner(), this.type, name);
            var entryDescriptionSH = new EntryDescriptionSH(mainPanel, entry);
            syncManager.syncValue(String.format("entry#%s_description", entry.getColorStr()), isPrivate ? 1 : 0,
                    entryDescriptionSH);

            return new Row()
                    .left(4)
                    .marginBottom(2)
                    .height(18)
                    .widthRel(0.98f)
                    .setEnabledIf(row -> VirtualRegistryBase.hasEntry(getOwner(), this.type, name))
                    .child(new Rectangle()
                            .setColor(entry.getColor())
                            .asWidget()
                            .marginRight(4)
                            .size(16)
                            .background(GTGuiTextures.SLOT.asIcon().size(18))
                            .top(1))
                    .child(new InteractableText<>(entry, CoverAbstractEnderLink.this::updateColor)
                            .tooltip(tooltip -> tooltip.setAutoUpdate(true))
                            .tooltipBuilder(tooltip -> {
                                String desc = entry.getDescription();
                                if (!desc.isEmpty())
                                    tooltip.addLine(desc);
                            })
                            .width(64)
                            .height(16)
                            .top(1)
                            .marginRight(4))
                    .child(new ButtonWidget<>()
                            .overlay(GuiTextures.GEAR)
                            .addTooltipLine(IKey.lang("cover.generic.ender.set_description.tooltip"))
                            .onMousePressed(i -> {
                                // open entry settings
                                if (entryDescriptionSH.isPanelOpen()) {
                                    entryDescriptionSH.closePanel();
                                } else {
                                    entryDescriptionSH.openPanel();
                                }
                                Interactable.playButtonClickSound();
                                return true;
                            }))
                    .child(createSlotWidget(entry))
                    .child(new ButtonWidget<>()
                            .overlay(GTGuiTextures.BUTTON_CROSS)
                            .setEnabledIf(w -> !Objects.equals(entry.getColor(), activeEntry.getColor()))
                            .addTooltipLine(IKey.lang("cover.generic.ender.delete_entry"))
                            .onMousePressed(i -> {
                                // todo option to force delete, maybe as a popup?
                                deleteEntry(name, entry);
                                syncToServer(1, buffer -> NetworkUtils.writeStringSafe(buffer, name));
                                Interactable.playButtonClickSound();
                                return true;
                            }));
        }

        @Override
        public void readOnServer(int i, PacketBuffer packetBuffer) throws IOException {
            super.readOnServer(i, packetBuffer);
            if (i == 1) {
                String name = NetworkUtils.readStringSafe(packetBuffer);
                T entry = VirtualRegistryBase.getEntry(getOwner(), this.type, name);
                deleteEntry(name, entry);
            }
        }

        protected abstract IWidget createSlotWidget(T entry);

        protected abstract void deleteEntry(String name, T entry);
    }

    private static class EntryDescriptionSH extends PanelSyncHandler {

        private final VirtualEntry entry;

        protected EntryDescriptionSH(ModularPanel mainPanel, VirtualEntry entry) {
            super(mainPanel);
            this.entry = entry;
        }

        @Override
        public ModularPanel createUI(ModularPanel mainPanel, GuiSyncManager syncManager) {
            return GTGuis.createPopupPanel("entry_description", 168, 36 + 6)
                    .child(IKey.lang("cover.generic.ender.set_description.title", entry.getColorStr())
                            .color(UI_TITLE_COLOR)
                            .asWidget()
                            .left(4)
                            .top(6))
                    .child(new TextFieldWidget()
                            .setTextColor(Color.WHITE.darker(1))
                            .widthRel(0.95f)
                            .height(18)
                            .value(new StringSyncValue(entry::getDescription, this::updateDescription))
                            .alignX(0.5f)
                            .bottom(6));
        }

        private void updateDescription(String desc) {
            this.entry.setDescription(desc);
            closePanel();
        }
    }
}
