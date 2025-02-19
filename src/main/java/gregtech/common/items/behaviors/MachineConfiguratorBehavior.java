package gregtech.common.items.behaviors;

import gregtech.api.GregTechAPI;
import gregtech.api.capability.copytool.IMachineConfiguratorInteractable;
import gregtech.api.capability.copytool.IMachineConfiguratorProfile;
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
import com.cleanroommc.modularui.drawable.GuiTextures;
import com.cleanroommc.modularui.factory.HandGuiData;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.value.sync.PanelSyncManager;
import com.cleanroommc.modularui.widgets.ButtonWidget;

public class MachineConfiguratorBehavior implements IItemBehaviour, ItemUIFactory {

    @Override
    public EnumActionResult onItemUseFirst(EntityPlayer player, World world, BlockPos pos, EnumFacing side, float hitX,
                                           float hitY, float hitZ, EnumHand hand) {
        TileEntity tileEntity = world.getTileEntity(pos);
        ItemStack configurator = player.getHeldItem(hand);

        if (!(tileEntity instanceof IGregTechTileEntity gte)) {
            return EnumActionResult.PASS;
        }

        if (!(gte.getMetaTileEntity() instanceof IMachineConfiguratorInteractable mci)) {
            return EnumActionResult.PASS;
        }

        if (world.isRemote) {
            return EnumActionResult.SUCCESS;
        }

        if (player.isSneaking()) {
            NBTTagCompound newTag = new NBTTagCompound();
            newTag.setString("Profile", mci.getProfile().getName());
            newTag.setTag("ConfigData", mci.writeProfileData());

            configurator.setTagCompound(newTag);
        } else {
            NBTTagCompound tag = configurator.getTagCompound();

            if (tag != null && mci.getProfile().getName().equals(tag.getString("Profile"))) {
                mci.readProfileData(configurator.getTagCompound().getCompoundTag("ConfigData"));
            }
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
        var basePanel = GTGuis.createPanel(guiData.getUsedItemStack(), 176, 120);

        ItemStack configurator = guiData.getUsedItemStack();
        NBTTagCompound tag = configurator.getTagCompound();
        IPanelHandler panel;

        if (tag != null && tag.hasKey("Profile")) {
            IMachineConfiguratorProfile profile = GregTechAPI.getMachineConfiguratorProfile(tag.getString("Profile"));
            panel = guiSyncManager.panel("configurator",
                    (syncManager, syncHandler) -> profile.createConfiguratorPanel(guiSyncManager,
                            () -> configurator.getSubCompound("ConfigData")),
                    true);
        } else {
            panel = null;
        }

        basePanel.child(new ButtonWidget<>()
                .background(GTGuiTextures.MC_BUTTON, GTGuiTextures.FILTER_SETTINGS_OVERLAY.asIcon().size(16))
                .hoverBackground(GuiTextures.MC_BUTTON_HOVERED, GTGuiTextures.FILTER_SETTINGS_OVERLAY.asIcon().size(16))
                .setEnabledIf(w -> panel != null)
                .onMousePressed(mb -> {
                    if (panel == null) return false;

                    if (panel.isPanelOpen()) {
                        panel.closePanel();
                    } else {
                        panel.openPanel();
                    }

                    return true;
                }));

        return basePanel;
    }
}
