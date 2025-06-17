package gregtech.api.configurator.behavior;

import gregtech.api.configurator.IMachineConfiguratorInteractable;
import gregtech.api.configurator.playerdata.ConfiguratorDataRegistry;
import gregtech.api.configurator.playerdata.PlayerConfiguratorData;
import gregtech.api.configurator.profile.ConfiguratorProfileRegistry;
import gregtech.api.configurator.profile.IMachineConfiguratorProfile;
import gregtech.api.cover.CoverWithUI;
import gregtech.api.items.gui.ItemUIFactory;
import gregtech.api.items.metaitem.stats.IItemBehaviour;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.mui.GTGuiTextures;
import gregtech.api.mui.GTGuis;
import gregtech.api.mui.factory.MetaItemGuiFactory;
import gregtech.api.util.GTUtility;

import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import com.cleanroommc.modularui.api.IPanelHandler;
import com.cleanroommc.modularui.api.drawable.IDrawable;
import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.api.widget.IWidget;
import com.cleanroommc.modularui.drawable.GuiTextures;
import com.cleanroommc.modularui.factory.HandGuiData;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.value.StringValue;
import com.cleanroommc.modularui.value.sync.PanelSyncHandler;
import com.cleanroommc.modularui.value.sync.PanelSyncManager;
import com.cleanroommc.modularui.value.sync.StringSyncValue;
import com.cleanroommc.modularui.value.sync.SyncHandler;
import com.cleanroommc.modularui.widgets.ButtonWidget;
import com.cleanroommc.modularui.widgets.ListWidget;
import com.cleanroommc.modularui.widgets.layout.Flow;
import com.cleanroommc.modularui.widgets.textfield.TextFieldWidget;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.cleanroommc.modularui.value.sync.PanelSyncManager.makeSyncKey;

public class MachineConfiguratorBehavior implements IItemBehaviour, ItemUIFactory {

    private static final String SELECTED_SLOT_KEY = "SelectedSlot";
    private static final String SYNC_HANDLER_NAME = "configuratorSync";

    @Override
    public EnumActionResult onItemUseFirst(EntityPlayer player, World world, BlockPos pos, EnumFacing side, float hitX,
                                           float hitY, float hitZ, EnumHand hand) {
        TileEntity tileEntity = world.getTileEntity(pos);
        NBTTagCompound configuratorNBT = player.getHeldItem(hand).getTagCompound();

        if (!(tileEntity instanceof IGregTechTileEntity gte)) {
            return EnumActionResult.PASS;
        }

        if (!(gte.getMetaTileEntity() instanceof IMachineConfiguratorInteractable mci)) {
            return EnumActionResult.PASS;
        }

        if (world.isRemote) {
            return EnumActionResult.SUCCESS;
        }

        if (configuratorNBT == null) {
            return EnumActionResult.PASS;
        }

        if (!configuratorNBT.hasKey(SELECTED_SLOT_KEY)) {
            return EnumActionResult.PASS;
        }

        PlayerConfiguratorData configuratorData = ConfiguratorDataRegistry.getPlayerData(player.getUniqueID());
        String slotName = configuratorNBT.getString(SELECTED_SLOT_KEY);

        if (!configuratorData.hasSlot(slotName)) {
            return EnumActionResult.PASS;
        }

        IMachineConfiguratorProfile profile = configuratorData.getSlotProfile(slotName);
        if (profile == null || !mci.getProfiles().contains(profile)) {
            return EnumActionResult.PASS;
        }

        if (player.isSneaking()) {
            configuratorData.setSlotConfig(slotName, mci.writeProfileData(profile));
        } else {
            mci.readProfileData(profile, configuratorData.getSlotConfig(slotName));
        }

        return EnumActionResult.SUCCESS;
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
        ItemStack heldItem = player.getHeldItem(hand);
        if (!world.isRemote) {
            MetaItemGuiFactory.open(player, hand);
        }
        return ActionResult.newResult(EnumActionResult.SUCCESS, heldItem);
    }

    @Override
    public ModularPanel buildUI(HandGuiData guiData, PanelSyncManager syncManager) {
        PlayerConfiguratorData playerData = ConfiguratorDataRegistry.getPlayerData(guiData.getPlayer().getUniqueID());
        ItemStack configuratorStack = guiData.getUsedItemStack();
        StringSyncValue selectedSlot = new StringSyncValue(
                () -> getSlotFromConfigurator(configuratorStack),
                str -> setSlotToConfigurator(configuratorStack, str));
        syncManager.syncValue("selected_slot", 0, selectedSlot);

        return GTGuis.createPanel(guiData.getUsedItemStack(), 140, 75)
                .child(CoverWithUI.createTitleRow(guiData.getUsedItemStack()))
                .child(createWidgets(syncManager, playerData, selectedSlot))
                .child(IKey.lang(() -> selectedSlot.getValue().isEmpty() ?
                        "metaitem.tool.machine_configurator.no_slot" :
                        "metaitem.tool.machine_configurator.active_slot",
                        () -> {
                            String slotName = selectedSlot.getValue();
                            return slotName.isEmpty() ? null : new Object[] { slotName };
                        })
                        .asWidget()
                        .left(7)
                        .top(24 + 20))
                .child(IKey.lang(() -> {
                    IMachineConfiguratorProfile profile = playerData.getSlotProfile(selectedSlot.getValue());
                    return profile == null ? "metaitem.tool.machine_configurator.no_profile" :
                            "metaitem.tool.machine_configurator.active_profile";
                }, () -> {
                    IMachineConfiguratorProfile profile = playerData.getSlotProfile(selectedSlot.getValue());
                    return profile == null ? null : new Object[] { profile.getProfileName() };
                })
                        .asWidget()
                        .left(7)
                        .top(24 + 18 + 12));
    }

    private Flow createWidgets(@NotNull PanelSyncManager syncManager, @NotNull PlayerConfiguratorData playerData,
                               @NotNull StringSyncValue selectedSlot) {
        ConfiguratorSyncHandler configuratorSyncHandler = new ConfiguratorSyncHandler(playerData);
        syncManager.syncValue(SYNC_HANDLER_NAME, 0, configuratorSyncHandler);

        IPanelHandler slotSelector = syncManager.panel("slot_selector",
                slotSelector(configuratorSyncHandler, playerData, selectedSlot), true);
        IPanelHandler profileSelector = syncManager.panel("profile_selector", profileSelector(configuratorSyncHandler),
                true);

        Map<IMachineConfiguratorProfile, IPanelHandler> configPanels = new Object2ObjectOpenHashMap<>();
        ConfiguratorProfileRegistry.getConfiguratorProfiles().forEach(profile -> configPanels.put(profile,
                syncManager.panel(profile.getName(), (profileSyncManager, profilePanelHandler) -> profile
                        .createConfiguratorPanel(profileSyncManager, playerData, selectedSlot::getValue),
                        true)));

        return Flow.row().coverChildrenHeight().top(24)
                .margin(7, 0).widthRel(1f)
                .child(new ButtonWidget<>()
                        .overlay(GTGuiTextures.MENU_OVERLAY)
                        .background(GTGuiTextures.MC_BUTTON)
                        .disableHoverBackground()
                        .onMousePressed(i -> {
                            if (slotSelector.isPanelOpen()) {
                                slotSelector.closePanel();
                            } else {
                                slotSelector.openPanel();
                            }

                            return true;
                        }))
                .child(new ButtonWidget<>()
                        .onMousePressed(i -> {
                            if (profileSelector.isPanelOpen()) {
                                profileSelector.closePanel();
                            } else {
                                profileSelector.openPanel();
                            }

                            return true;
                        }))
                .child(new ButtonWidget<>()
                        .background(GTGuiTextures.MC_BUTTON, GTGuiTextures.FILTER_SETTINGS_OVERLAY.asIcon().size(16))
                        .hoverBackground(GuiTextures.MC_BUTTON_HOVERED,
                                GTGuiTextures.FILTER_SETTINGS_OVERLAY.asIcon().size(16))
                        .onMousePressed(mb -> {
                            IMachineConfiguratorProfile profile = playerData.getSlotProfile(selectedSlot.getValue());
                            IPanelHandler configPH = configPanels.get(profile);
                            if (configPH == null) return false;

                            if (configPH.isPanelOpen()) {
                                configPH.closePanel();
                            } else {
                                configPH.openPanel();
                            }

                            return true;
                        }));
    }

    private PanelSyncHandler.IPanelBuilder profileSelector(@NotNull ConfiguratorSyncHandler configuratorSyncHandler) {
        return ((syncManager, syncHandler) -> {
            List<IWidget> rows = new ArrayList<>();
            ConfiguratorProfileRegistry.getConfiguratorProfiles().forEach(profile -> rows.add(
                    createProfileRow(profile, configuratorSyncHandler, syncHandler)));
            return GTGuis.createPopupPanel("profile_selector", 168, 112, false)
                    .child(IKey.lang("metaitem.tool.machine_configurator.profiles")
                            .color(CoverWithUI.UI_TITLE_COLOR)
                            .asWidget()
                            .top(6)
                            .left(4))
                    .child(new ListWidget<>()
                            .children(rows).background(GTGuiTextures.DISPLAY.asIcon()
                                    .width(168 - 8)
                                    .height(112 - 20))
                            .paddingTop(1)
                            .size(168 - 12, 112 - 24)
                            .left(4)
                            .bottom(6));
        });
    }

    private Flow createProfileRow(@NotNull IMachineConfiguratorProfile profile,
                                  @NotNull ConfiguratorSyncHandler configuratorSyncHandler,
                                  @NotNull IPanelHandler parentPanel) {
        return Flow.row()
                .height(25)
                .margin(5, 0, 0, 0)
                .child(new ButtonWidget<>()
                        .size(10)
                        .alignY(0.5f)
                        .onMousePressed(i -> {
                            configuratorSyncHandler.setSelectedSlotProfile(profile);

                            if (parentPanel.isPanelOpen()) {
                                parentPanel.closePanel();
                            }

                            return true;
                        })
                        .overlay(IKey.str("✓")))
                .child(profile.getProfileName()
                        .asWidget()
                        .alignY(0.5f));
    }

    private PanelSyncHandler.IPanelBuilder slotSelector(@NotNull ConfiguratorSyncHandler configuratorSyncHandler,
                                                        @NotNull PlayerConfiguratorData playerData,
                                                        @NotNull StringSyncValue selectedSlot) {
        return (syncManager, syncHandler) -> {
            var slotList = new ListWidget<>();
            Runnable createRows = () -> {
                Iterator<IWidget> slotListIterator = slotList.getChildren().iterator();
                while (slotListIterator.hasNext()) {
                    IWidget child = slotListIterator.next();
                    slotListIterator.remove();
                    child.dispose();
                    slotList.onChildRemove(child);
                }

                configuratorSyncHandler.getSlots()
                        .forEach(name -> slotList.child(createSlotRow(name, playerData, selectedSlot, syncHandler)));
            };
            createRows.run();
            configuratorSyncHandler.onSlotsChanged(createRows);

            IPanelHandler newSlotPopup = syncManager.panel("new_slot_popup",
                    createNewSlotPopup(configuratorSyncHandler, selectedSlot, syncHandler), true);

            return GTGuis.createPopupPanel("slot_selector", 168, 112, false)
                    .child(IKey.lang("metaitem.tool.machine_configurator.slots")
                            .color(CoverWithUI.UI_TITLE_COLOR)
                            .asWidget()
                            .left(4)
                            .top(6))
                    .child(new ButtonWidget<>()
                            .size(10)
                            .left(4 + 23 + 5)
                            .top(6)
                            .onMousePressed(mouse -> {
                                if (!newSlotPopup.isPanelOpen()) {
                                    newSlotPopup.openPanel();
                                }

                                return true;
                            })
                            .overlay(IKey.str("+"))
                            .addTooltipLine(IKey.lang("metaitem.tool.machine_configurator.add_slot")))
                    .child(slotList.background(GTGuiTextures.DISPLAY.asIcon()
                            .width(168 - 8)
                            .height(112 - 20))
                            .paddingTop(1)
                            .size(168 - 12, 112 - 24)
                            .left(4)
                            .bottom(6));
        };
    }

    private PanelSyncHandler.IPanelBuilder createNewSlotPopup(@NotNull ConfiguratorSyncHandler configuratorSyncHandler,
                                                              @NotNull StringSyncValue selectedSlot,
                                                              @NotNull IPanelHandler parentPanel) {
        return (syncManager, syncHandler) -> {
            StringValue name = new StringValue("");
            return GTGuis.blankPopupPanel("new_slot_panel", 100, 30)
                    .child(new TextFieldWidget()
                            .left(2)
                            .top(2)
                            .widthRel(0.75f)
                            .height(18)
                            .value(name)
                            .setMaxLength(32))
                    .child(new ButtonWidget<>()
                            .right(2)
                            .alignY(0.5f)
                            .size(10)
                            .onMousePressed(mouse -> {
                                String newName = name.getValue();
                                if (newName.isEmpty()) return false;

                                configuratorSyncHandler.addNewConfig(newName);
                                selectedSlot.setValue(newName);

                                if (syncHandler.isPanelOpen()) {
                                    syncHandler.closePanel();
                                }

                                if (parentPanel.isPanelOpen()) {
                                    parentPanel.closePanel();
                                }

                                return true;
                            })
                            .overlay(IKey.str("✓")));
        };
    }

    private Flow createSlotRow(@NotNull String name, @NotNull PlayerConfiguratorData playerData,
                               @NotNull StringSyncValue selectedSlot, @NotNull IPanelHandler panelHandler) {
        IMachineConfiguratorProfile slotProfile = playerData.getSlotProfile(name);
        IKey hoverText = slotProfile == null ? IKey.lang("metaitem.tool.machine_configurator.no_profile") :
                IKey.lang("metaitem.tool.machine_configurator.active_profile", slotProfile.getProfileName());
        // TODO: replace with KeyUtil#setHover when #2672 merges
        IDrawable nameDrawable = IKey.str(name)
                .asTextIcon()
                .asHoverable()
                .addTooltipDrawableLines(Collections.singleton(hoverText));

        return Flow.row()
                .height(25)
                .margin(5, 0, 0, 0)
                .child(new ButtonWidget<>()
                        .size(10)
                        .alignY(0.5f)
                        .onMousePressed(mouse -> {
                            selectedSlot.setValue(name);
                            if (panelHandler.isPanelOpen()) {
                                panelHandler.closePanel();
                            }
                            return true;
                        })
                        .overlay(IKey.str("✓"))
                        .addTooltipLine(IKey.lang("metaitem.tool.machine_configurator.select_slot")))
                .child(nameDrawable.asWidget()
                        .alignY(0.5f))
                .child(new ButtonWidget<>()
                        .size(10)
                        .right(5)
                        .alignY(0.5f).onMousePressed(mouse -> {
                            selectedSlot.setValue("");
                            playerData.deleteSlot(name);
                            if (panelHandler.isPanelOpen()) {
                                panelHandler.closePanel();
                            }
                            return true;
                        })
                        .overlay(IKey.str("x"))
                        .addTooltipLine(IKey.lang("metaitem.tool.machine_configurator.delete_slot")));
    }

    private static void setSlotToConfigurator(@NotNull ItemStack stack, @NotNull String slotName) {
        GTUtility.getOrCreateNbtCompound(stack).setString(SELECTED_SLOT_KEY, slotName);
    }

    @NotNull
    private static String getSlotFromConfigurator(@NotNull ItemStack stack) {
        NBTTagCompound configuratorNBT = stack.getTagCompound();
        return configuratorNBT != null ? configuratorNBT.getString(SELECTED_SLOT_KEY) : "";
    }

    @Override
    public void addInformation(ItemStack itemStack, List<String> lines) {
        String slot = getSlotFromConfigurator(itemStack);
        if (slot.isEmpty()) {
            lines.add(I18n.format("metaitem.tool.machine_configurator.no_slot"));
        } else {
            lines.add(I18n.format("metaitem.tool.machine_configurator.active_slot", slot));
        }
    }

    private static class ConfiguratorSyncHandler extends SyncHandler {

        private static final int newConfigID = 0;
        private static final int slotNamesSyncID = 1;
        private static final int deleteSlotID = 2;
        private static final int setSlotProfileProfileID = 3;

        private final PlayerConfiguratorData playerData;
        private final Set<String> slotNames = new HashSet<>();

        @Nullable
        private Runnable onSlotsChanged;

        public ConfiguratorSyncHandler(PlayerConfiguratorData playerData) {
            this.playerData = playerData;
        }

        @Override
        public void detectAndSendChanges(boolean init) {
            Set<String> realSlotNames = playerData.getSlotNames();

            if (!realSlotNames.equals(slotNames) || init) {
                slotNames.clear();
                slotNames.addAll(realSlotNames);

                syncToClient(slotNamesSyncID, buf -> {
                    buf.writeVarInt(slotNames.size());
                    for (String name : slotNames) {
                        buf.writeString(name);
                    }
                });

                onSlotsChanged();
            }
        }

        @Override
        public void readOnClient(int id, PacketBuffer buf) {
            if (id == slotNamesSyncID) {
                slotNames.clear();
                int size = buf.readVarInt();
                for (int i = 0; i < size; i++) {
                    String name = buf.readString(Short.MAX_VALUE);
                    slotNames.add(name);
                }

                onSlotsChanged();
            }
        }

        @Override
        public void readOnServer(int id, PacketBuffer buf) {
            if (id == newConfigID) {
                playerData.createNewSlot(buf.readString(Short.MAX_VALUE));
            } else if (id == setSlotProfileProfileID) {
                IMachineConfiguratorProfile profile = ConfiguratorProfileRegistry
                        .getConfiguratorProfileByNetworkID(buf.readVarInt());
                if (profile != null) {
                    playerData.setSlotProfile(getSelectedSlot(), profile);
                }
            }
        }

        private boolean onClient() {
            return getSyncManager().isClient();
        }

        private void onSlotsChanged() {
            if (onSlotsChanged != null) {
                onSlotsChanged.run();
            }
        }

        public void onSlotsChanged(@NotNull Runnable onSlotsChanged) {
            this.onSlotsChanged = onSlotsChanged;
        }

        @SideOnly(Side.CLIENT)
        public void addNewConfig(@NotNull String name) {
            syncToServer(newConfigID, buf -> buf.writeString(name));
        }

        @SideOnly(Side.CLIENT)
        public void setSelectedSlotProfile(@NotNull IMachineConfiguratorProfile profile) {
            syncToServer(setSlotProfileProfileID, buf -> buf.writeVarInt(profile.networkID()));
        }

        public @NotNull String getSelectedSlot() {
            return ((StringSyncValue) getSyncManager().getSyncHandler(makeSyncKey("selected_slot", 0))).getValue();
        }

        public Set<String> getSlots() {
            if (onClient()) {
                return slotNames;
            } else {
                return playerData.getSlotNames();
            }
        }
    }
}
