package gregtech.api.configurator.behavior;

import gregtech.api.configurator.IMachineConfiguratorInteractable;
import gregtech.api.configurator.profile.IMachineConfiguratorProfile;
import gregtech.api.configurator.registry.ConfiguratorDataRegistry;
import gregtech.api.configurator.registry.PlayerConfiguratorData;
import gregtech.api.cover.CoverWithUI;
import gregtech.api.items.gui.ItemUIFactory;
import gregtech.api.items.metaitem.stats.IItemBehaviour;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.mui.GTGuiTextures;
import gregtech.api.mui.GTGuis;
import gregtech.api.mui.factory.MetaItemGuiFactory;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import com.cleanroommc.modularui.api.IPanelHandler;
import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.api.widget.IWidget;
import com.cleanroommc.modularui.drawable.GuiTextures;
import com.cleanroommc.modularui.factory.HandGuiData;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.value.sync.PanelSyncHandler;
import com.cleanroommc.modularui.value.sync.PanelSyncManager;
import com.cleanroommc.modularui.value.sync.StringSyncValue;
import com.cleanroommc.modularui.widgets.ButtonWidget;
import com.cleanroommc.modularui.widgets.ListWidget;
import com.cleanroommc.modularui.widgets.layout.Flow;
import com.cleanroommc.modularui.widgets.layout.Row;
import com.cleanroommc.modularui.widgets.textfield.TextFieldWidget;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class MachineConfiguratorBehavior implements IItemBehaviour, ItemUIFactory {

    private static final String SELECTED_SLOT_KEY = "SelectedSlot";

    private final Map<String, IPanelHandler> panelCache = new Object2ObjectOpenHashMap<>();

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

        if (mci.getProfile() != configuratorData.getSlotProfile(slotName)) {
            return EnumActionResult.PASS;
        }

        if (player.isSneaking()) {
            configuratorData.setSlotConfig(slotName, mci.writeProfileData());
        } else {
            mci.readProfileData(configuratorData.getSlotConfig(slotName));
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
    public ModularPanel buildUI(HandGuiData guiData, PanelSyncManager guiSyncManager) {
        UUID playerID = guiData.getPlayer().getUniqueID();

        return GTGuis.createPanel(guiData.getUsedItemStack(), 176, 120)
                .child(CoverWithUI.createTitleRow(guiData.getUsedItemStack()))
                .child(createWidgets(guiData, guiSyncManager, playerID));
    }

    private Flow createWidgets(HandGuiData guiData, PanelSyncManager syncManager, UUID playerID) {
        ItemStack configuratorStack = guiData.getUsedItemStack();
        StringSyncValue selectedSlot = new StringSyncValue(
                () -> getSlotFromConfigurator(configuratorStack),
                str -> setSlotToConfigurator(configuratorStack, str));

        IPanelHandler slotSelector = syncManager.panel("slot_selector", slotSelector(playerID), true);

        return Flow.column().coverChildrenHeight().top(24)
                .margin(7, 0).widthRel(1f)
                .child(new Row().marginBottom(2)
                        .coverChildrenHeight()
                        .child(new TextFieldWidget()
                                .height(18)
                                .value(selectedSlot)
                                .widthRel(0.5f)
                                .marginRight(2)))
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
                        .background(GTGuiTextures.MC_BUTTON, GTGuiTextures.FILTER_SETTINGS_OVERLAY.asIcon().size(16))
                        .hoverBackground(GuiTextures.MC_BUTTON_HOVERED,
                                GTGuiTextures.FILTER_SETTINGS_OVERLAY.asIcon().size(16))
                        .onMousePressed(mb -> {
                            IPanelHandler panel = panelCache.computeIfAbsent(selectedSlot.getValue(),
                                    slotName -> makeConfigPanel(slotName, syncManager, playerID));

                            if (panel != null) {
                                if (panel.isPanelOpen()) {
                                    panel.closePanel();
                                } else {
                                    panel.openPanel();
                                }
                            }

                            return true;
                        }));
    }

    @Nullable
    private static IPanelHandler makeConfigPanel(String slotName, PanelSyncManager syncManager, UUID playerID) {
        PlayerConfiguratorData configuratorData = ConfiguratorDataRegistry.getPlayerData(playerID);
        IMachineConfiguratorProfile profile = configuratorData.getSlotProfile(slotName);
        if (profile == null) return null;

        return syncManager.panel(profile.getName(), (panelSyncManager, panelHandler) -> profile
                .createConfiguratorPanel(panelSyncManager, configuratorData.getSlotConfig(slotName)), true);
    }

    private PanelSyncHandler.IPanelBuilder slotSelector(UUID player) {
        return (syncManager, syncHandler) -> {
            List<IWidget> rows = new ArrayList<>();
            ConfiguratorDataRegistry.getSlots(player).forEach(name -> rows.add(createRow(name, syncManager)));
            return GTGuis.createPopupPanel("slot_selector", 168, 112, true)
                    .child(IKey.lang("slots")
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
        };
    }

    private IWidget createRow(String name, PanelSyncManager syncManager) {
        return IKey.str(name).asWidget();
    }

    private static void setSlotToConfigurator(ItemStack stack, String slotName) {
        NBTTagCompound newNBT = new NBTTagCompound();
        newNBT.setString(SELECTED_SLOT_KEY, slotName);
        stack.setTagCompound(newNBT);
    }

    private static String getSlotFromConfigurator(ItemStack stack) {
        NBTTagCompound configuratorNBT = stack.getTagCompound();
        if (configuratorNBT != null) {
            return configuratorNBT.getString(SELECTED_SLOT_KEY);
        } else {
            return "";
        }
    }
}
