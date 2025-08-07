package gregtech.common.items.behaviors.spray;

import gregtech.api.color.ColoredBlockContainer;
import gregtech.api.items.gui.ItemUIFactory;
import gregtech.api.items.metaitem.stats.IItemColorProvider;
import gregtech.api.items.metaitem.stats.IItemNameProvider;
import gregtech.api.items.metaitem.stats.IMouseEventHandler;
import gregtech.api.mui.GTGuis;
import gregtech.api.mui.factory.MetaItemGuiFactory;
import gregtech.api.util.GTUtility;
import gregtech.common.items.MetaItems;
import gregtech.core.network.packets.PacketItemMouseEvent;

import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.RayTraceResult;
import net.minecraftforge.client.event.MouseEvent;
import net.minecraftforge.common.util.Constants;

import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.drawable.ItemDrawable;
import com.cleanroommc.modularui.factory.HandGuiData;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.value.BoolValue;
import com.cleanroommc.modularui.value.sync.IntSyncValue;
import com.cleanroommc.modularui.value.sync.PanelSyncManager;
import com.cleanroommc.modularui.widgets.SlotGroupWidget;
import com.cleanroommc.modularui.widgets.ToggleButton;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CreativeSprayBehavior extends AbstractSprayBehavior implements ItemUIFactory, IItemColorProvider,
                                   IItemNameProvider, IMouseEventHandler {

    @Override
    public ModularPanel buildUI(HandGuiData guiData, PanelSyncManager guiSyncManager) {
        ItemStack usedStack = guiData.getUsedItemStack();
        IntSyncValue colorSync = new IntSyncValue(() -> getColorOrdinal(usedStack),
                newColor -> setColor(usedStack, newColor));
        guiSyncManager.syncValue("color", 0, colorSync);

        ModularPanel panel = GTGuis.createPanel(usedStack, 176, 120);
        panel.child(SlotGroupWidget.builder()
                .matrix("SCCCCCCCC",
                        "CCCCCCCC")
                .key('S', new ToggleButton()
                        .size(18)
                        .value(new BoolValue.Dynamic(() -> colorSync.getIntValue() == -1,
                                $ -> {
                                    colorSync.setIntValue(-1);
                                    panel.closeIfOpen(true);
                                }))
                        .overlay(new ItemDrawable(MetaItems.SPRAY_SOLVENT.getStackForm())
                                .asIcon()
                                .margin(2))
                        .addTooltipLine(IKey.lang("metaitem.spray.creative.solvent")))
                .key('C', index -> {
                    EnumDyeColor color = EnumDyeColor.values()[index];
                    return new ToggleButton()
                            .size(18)
                            .value(new BoolValue.Dynamic(() -> colorSync.getIntValue() == index,
                                    $ -> {
                                        colorSync.setIntValue(index);
                                        panel.closeIfOpen(true);
                                    }))
                            .overlay(new ItemDrawable(MetaItems.SPRAY_CAN_DYES.get(color).getStackForm())
                                    .asIcon()
                                    .margin(2))
                            .addTooltipLine(IKey.lang("metaitem.spray.creative." + color));
                })
                .build());

        return panel;
    }

    @Override
    public @Nullable EnumDyeColor getColor(@NotNull ItemStack stack) {
        NBTTagCompound tag = GTUtility.getOrCreateNbtCompound(stack);
        if (tag.hasKey("color", Constants.NBT.TAG_INT)) {
            int color = tag.getInteger("color");
            if (color < 0 || color > 15) return null;
            return EnumDyeColor.values()[color];
        }
        return null;
    }

    public static void setColor(@NotNull ItemStack stack, @Nullable EnumDyeColor color) {
        GTUtility.getOrCreateNbtCompound(stack).setInteger("color", color == null ? -1 : color.ordinal());
    }

    public static void setColor(@NotNull ItemStack stack, int color) {
        if (color >= 0 && color <= 15) {
            setColor(stack, EnumDyeColor.values()[color]);
        } else {
            setColor(stack, null);
        }
    }

    @Override
    public int getItemStackColor(ItemStack itemStack, int tintIndex) {
        EnumDyeColor color = getColor(itemStack);
        return color != null && tintIndex == 1 ? color.colorValue : 0xFFFFFF;
    }

    @Override
    public String getItemStackDisplayName(ItemStack itemStack, String unlocalizedName) {
        EnumDyeColor color = getColor(itemStack);
        String colorString = color == null ? I18n.format("metaitem.spray.creative.solvent") :
                I18n.format("metaitem.spray.creative." + color);
        return I18n.format(unlocalizedName, colorString);
    }

    @Override
    public void handleMouseEventClient(@NotNull MouseEvent event, @NotNull EntityPlayerSP playerClient,
                                       @NotNull ItemStack stack) {
        // Middle click pressed down
        if (event.getButton() == 2 && event.isButtonstate()) {
            event.setCanceled(true);

            double reach = playerClient.getEntityAttribute(EntityPlayer.REACH_DISTANCE).getAttributeValue();
            if (!playerClient.capabilities.isCreativeMode) {
                reach -= 0.5d;
            }

            RayTraceResult rayTrace = playerClient.rayTrace(reach, 1.0f);
            if (rayTrace != null && rayTrace.typeOfHit == RayTraceResult.Type.BLOCK) {
                ColoredBlockContainer colorContainer = ColoredBlockContainer.getInstance(playerClient.world,
                        rayTrace.getBlockPos(), rayTrace.sideHit, playerClient);
                EnumDyeColor hitColor = colorContainer.getColor();
                if (hitColor != null && hitColor != getColor(stack)) {
                    setColor(stack, hitColor);
                    sendToServer(buf -> buf
                            .writeByte(0)
                            .writeByte(hitColor.ordinal()));
                    return;
                }
            }

            // If the player isn't sneaking and wasn't looking at a colored block, open gui
            sendToServer(buf -> buf.writeByte(1));
        }
    }

    @Override
    public void handleMouseEventServer(@NotNull PacketItemMouseEvent packet, @NotNull EntityPlayerMP playerServer,
                                       @NotNull ItemStack stack) {
        PacketBuffer buf = packet.getBuffer();
        switch (buf.readByte()) {
            case 0 -> setColor(stack, buf.readByte());
            case 1 -> MetaItemGuiFactory.open(playerServer, EnumHand.MAIN_HAND);
        }
    }
}
