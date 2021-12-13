package gregtech.api.items.behavior;

import gregtech.api.capability.GregtechDataCodes;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.IUIHolder;
import gregtech.api.gui.ModularUI;
import gregtech.api.items.gui.ItemUIFactory;
import gregtech.api.items.gui.PlayerInventoryHolder;
import gregtech.api.items.metaitem.MetaItem;
import gregtech.api.items.metaitem.stats.IItemBehaviour;
import gregtech.api.net.packets.CPacketPluginSynced;
import gregtech.api.net.NetworkHandler;
import gregtech.api.util.IDirtyNotifiable;
import gregtech.common.gui.widget.monitor.WidgetPluginConfig;
import gregtech.common.metatileentities.multi.electric.centralmonitor.MetaTileEntityMonitorScreen;
import io.netty.buffer.Unpooled;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.function.Consumer;

public abstract class MonitorPluginBaseBehavior implements IItemBehaviour, ItemUIFactory, IDirtyNotifiable {
    protected MetaTileEntityMonitorScreen screen;
    private NBTTagCompound nbtTagCompound;

    public static MonitorPluginBaseBehavior getBehavior(ItemStack itemStack) {
        if (itemStack.getItem() instanceof MetaItem<?>) {
            MetaItem<?> item = (MetaItem<?>) itemStack.getItem();
            for (IItemBehaviour behaviour : item.getBehaviours(itemStack)) {
                if (behaviour instanceof MonitorPluginBaseBehavior) {
                    return (MonitorPluginBaseBehavior) behaviour;
                }
            }
        }
        return null;
    }

    public MetaTileEntityMonitorScreen getScreen() {
        return screen;
    }

    abstract public MonitorPluginBaseBehavior createPlugin();

    /***
     * Do not override createUI below.
     * @param holder It should be one of PlayerInventoryHolder or MetaTileEntityHolder.
     * @param entityPlayer Player
     * @return WidgetGroup back
     */
    public WidgetPluginConfig customUI(WidgetPluginConfig widgetGroup, IUIHolder holder, EntityPlayer entityPlayer) {
        return widgetGroup;
    }

    /***
     * Can player using item (right-click) to open the customUI.
     */
    public boolean hasUI() {
        return false;
    }

    /***
     * Server / Client. Itemstack will be synced to client when init so... yeah normally you don't need to consider nbt init.
     * this will be called when you markDirty.
     * @param data nbtTag
     */
    public void writeToNBT(NBTTagCompound data) {
    }

    /***
     * Server / Client. Initialization of Server and Client.
     * @param data nbtTag
     */
    public void readFromNBT(NBTTagCompound data) {
        this.nbtTagCompound = data;
    }

    /***
     * Server. Same as writeCustomData in MetaTileEntity.
     * @param id PacketID
     * @param buf PacketBuffer
     */
    public final void writePluginData(int id, @Nonnull Consumer<PacketBuffer> buf) {
        if (screen != null && this.screen.getWorld() != null && !this.screen.getWorld().isRemote) {
            screen.writeCustomData(GregtechDataCodes.UPDATE_PLUGIN_DATA, packetBuffer -> {
                packetBuffer.writeVarInt(id);
                buf.accept(packetBuffer);
            });
        }
    }

    /***
     * Client. Same as receiveCustomData in MetaTileEntity.
     * @param id PacketID
     * @param buf PacketBuffer
     */
    public void readPluginData(int id, PacketBuffer buf) {

    }

    /***
     * Client. Send data to Server.
     * @param id PacketID
     * @param buf PacketBuffer
     */
    public final void writePluginAction(int id, @Nonnull Consumer<PacketBuffer> dataWriter) {
        PacketBuffer buffer = new PacketBuffer(Unpooled.buffer());
        dataWriter.accept(buffer);
        NetworkHandler.channel.sendToServer(new CPacketPluginSynced(
                this.getScreen().getWorld().provider.getDimension(),
                this.getScreen().getPos(),
                id, buffer).toFMLPacket());
    }

    /***
     * Server. receive data from client
     * @param player player
     * @param id PacketID
     * @param buf PacketBuffer
     */
    public void readPluginAction(EntityPlayerMP player, int id, PacketBuffer buf) {

    }

    /***
     * Server. Same as writeInitialSyncData in MetaTileEntity.
     * @param buf PacketBuffer
     */
    public void writeInitialSyncData(PacketBuffer buf) {

    }

    /***
     * Client. Same as receiveInitialSyncData in MetaTileEntity.
     * @param buf PacketBuffer
     */
    public void receiveInitialSyncData(PacketBuffer buf) {

    }

    /***
     * Server / Client (deprecated). Should be called when need to write persistence data to NBT
     */
    public void markAsDirty() {
        if (screen != null) {
            screen.pluginDirty();
        } else if (nbtTagCompound != null) {
            writeToNBT(nbtTagCompound);
        }
    }

    /*** Server / Client. Called when player touch the screen.
     * @param playerIn Player
     * @param hand Hand
     * @param facing Facing
     * @param isRight is Right Click
     * @param x xPos of the screen (0 ~ 1.0)
     * @param y yPos of the screen (0 ~ 1.0)
     * @return trigger result
     */
    public boolean onClickLogic(EntityPlayer playerIn, EnumHand hand, EnumFacing facing, boolean isRight, double x, double y) {
        return false;
    }

    /***
     * Server / Client. Called per tick when structure formed.
     */
    public void update() {

    }

    /***
     * Client. Write rendering here
     */
    @SideOnly(Side.CLIENT)
    public void renderPlugin(float partialTicks, RayTraceResult rayTraceResult) {

    }

    /***
     * Server / Client. Called when plugin is added or removed from the screen.
     * @param screen
     * @param valid
     */
    public void onMonitorValid(MetaTileEntityMonitorScreen screen, boolean valid) {
        if (valid) {
            this.screen = screen;
        } else {
            this.screen = null;
        }
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
        if (!world.isRemote) {
            if (hand != EnumHand.MAIN_HAND)
                return ActionResult.newResult(EnumActionResult.PASS, player.getHeldItem(hand));
            ItemStack itemStack = player.getHeldItem(hand);
            MonitorPluginBaseBehavior behavior = getBehavior(itemStack);
            if (behavior != null && behavior.hasUI()) {
                PlayerInventoryHolder holder = new PlayerInventoryHolder(player, hand);
                holder.openUI();
                return ActionResult.newResult(EnumActionResult.SUCCESS, itemStack);
            }
        }
        return ActionResult.newResult(EnumActionResult.PASS, player.getHeldItem(hand));
    }

    @Override
    public final ModularUI createUI(PlayerInventoryHolder playerInventoryHolder, EntityPlayer entityPlayer) {
        ItemStack itemStack = playerInventoryHolder.getCurrentItem();
        MonitorPluginBaseBehavior behavior = MonitorPluginBaseBehavior.getBehavior(itemStack);
        if (behavior != null) {
            behavior = behavior.createPlugin();
            behavior.readFromNBT(itemStack.getOrCreateSubCompound("monitor_plugin"));
            return ModularUI.builder(GuiTextures.BOXED_BACKGROUND, 260, 210)
                    .widget(behavior.customUI(new WidgetPluginConfig().setBackGround(GuiTextures.BACKGROUND), playerInventoryHolder, entityPlayer))
                    .bindCloseListener(this::markAsDirty)
                    .build(playerInventoryHolder, entityPlayer);
        }
        return null;
    }

    @Override
    public void addInformation(ItemStack itemStack, List<String> lines) {
        lines.add(I18n.format("metaitem.plugin.tooltips.1"));
    }
}
