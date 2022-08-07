package gregtech.api.gui;

import gregtech.api.GregTechAPI;
import gregtech.api.gui.impl.ModularUIContainer;
import gregtech.api.gui.impl.ModularUIGui;
import gregtech.api.net.NetworkHandler;
import gregtech.api.net.packets.SPacketUIOpen;
import gregtech.api.net.packets.SPacketUIWidgetUpdate;
import io.netty.buffer.Unpooled;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.entity.player.PlayerContainerEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.ArrayList;
import java.util.List;

/**
 * Implement and register on the {@link GregTechAPI.RegisterEvent<UIFactory>} event to be able to create and open ModularUI's
 * createUITemplate should return equal gui both on server and client side, or sync will break!
 *
 * @param <E> UI holder type
 */
public abstract class UIFactory<E extends IUIHolder> {

    public final void openUI(E holder, EntityPlayerMP player) {
        if (player instanceof FakePlayer) {
            return;
        }
        ModularUI uiTemplate = createUITemplate(holder, player);

        if(uiTemplate == null) {
            // Central monitor Screen can return null if clicked when not powered, maybe other multis too
            return;
        }
        uiTemplate.initWidgets();

        player.getNextWindowId();
        player.closeContainer();
        int currentWindowId = player.currentWindowId;

        PacketBuffer serializedHolder = new PacketBuffer(Unpooled.buffer());
        writeHolderToSyncData(serializedHolder, holder);
        int uiFactoryId = GregTechAPI.UI_FACTORY_REGISTRY.getIDForObject(this);

        ModularUIContainer container = new ModularUIContainer(uiTemplate);
        container.windowId = currentWindowId;
        //accumulate all initial updates of widgets in open packet
        container.accumulateWidgetUpdateData = true;
        uiTemplate.guiWidgets.values().forEach(Widget::detectAndSendChanges);
        container.accumulateWidgetUpdateData = false;
        ArrayList<SPacketUIWidgetUpdate> updateData = new ArrayList<>(container.accumulatedUpdates);
        container.accumulatedUpdates.clear();

        SPacketUIOpen packet = new SPacketUIOpen(uiFactoryId, serializedHolder, currentWindowId, updateData);
        NetworkHandler.channel.sendTo(packet.toFMLPacket(), player);

        container.addListener(player);
        player.openContainer = container;

        //and fire forge event only in the end
        MinecraftForge.EVENT_BUS.post(new PlayerContainerEvent.Open(player, container));
    }

    @SideOnly(Side.CLIENT)
    public final void initClientUI(PacketBuffer serializedHolder, int windowId, List<SPacketUIWidgetUpdate> initialWidgetUpdates) {
        E holder = readHolderFromSyncData(serializedHolder);
        Minecraft minecraft = Minecraft.getMinecraft();
        EntityPlayerSP entityPlayer = minecraft.player;

        ModularUI uiTemplate = createUITemplate(holder, entityPlayer);
        uiTemplate.initWidgets();
        ModularUIGui modularUIGui = new ModularUIGui(uiTemplate);
        modularUIGui.inventorySlots.windowId = windowId;
        for (SPacketUIWidgetUpdate packet : initialWidgetUpdates) {
            modularUIGui.handleWidgetUpdate(packet);
        }
        minecraft.addScheduledTask(() -> {
            minecraft.displayGuiScreen(modularUIGui);
            minecraft.player.openContainer.windowId = windowId;
        });
    }

    protected abstract ModularUI createUITemplate(E holder, EntityPlayer entityPlayer);

    @SideOnly(Side.CLIENT)
    protected abstract E readHolderFromSyncData(PacketBuffer syncData);

    protected abstract void writeHolderToSyncData(PacketBuffer syncData, E holder);

}
