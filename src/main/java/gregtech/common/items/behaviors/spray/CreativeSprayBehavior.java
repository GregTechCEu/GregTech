package gregtech.common.items.behaviors.spray;

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
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import net.minecraftforge.client.event.MouseEvent;
import net.minecraftforge.common.util.Constants;

import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.drawable.ItemDrawable;
import com.cleanroommc.modularui.factory.HandGuiData;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.value.sync.IntSyncValue;
import com.cleanroommc.modularui.value.sync.PanelSyncManager;
import com.cleanroommc.modularui.widgets.ButtonWidget;
import com.cleanroommc.modularui.widgets.SlotGroupWidget;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CreativeSprayBehavior extends AbstractSprayBehavior implements ItemUIFactory, IItemColorProvider,
                                   IItemNameProvider, IMouseEventHandler {

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
        ItemStack usedStack = guiData.getUsedItemStack();
        IntSyncValue colorSync = new IntSyncValue(() -> getColorOrdinal(usedStack),
                newColor -> setColor(usedStack, newColor));
        guiSyncManager.syncValue("color", 0, colorSync);

        return GTGuis.createPanel(usedStack, 176, 120)
                .child(SlotGroupWidget.builder()
                        .matrix("SCCCCCCC",
                                "CCCCCCCC")
                        .key('S', new ButtonWidget<>()
                                .size(18)
                                .onMousePressed(mouse -> {
                                    colorSync.setIntValue(-1);
                                    return true;
                                })
                                .overlay(new ItemDrawable(MetaItems.SPRAY_SOLVENT.getStackForm()))
                                .addTooltipLine(IKey.lang("metaitem.spray.creative.solvent")))
                        .key('C', index -> {
                            EnumDyeColor color = EnumDyeColor.values()[index];
                            return new ButtonWidget<>()
                                    .size(18)
                                    .onMousePressed(mouse -> {
                                        colorSync.setIntValue(index);
                                        return true;
                                    })
                                    .overlay(new ItemDrawable(MetaItems.SPRAY_CAN_DYES.get(color).getStackForm()))
                                    .addTooltipLine(IKey.lang("metaitem.spray.creative." + color));
                        })
                        .build());
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

    public static boolean isLocked(@NotNull ItemStack stack) {
        return GTUtility.getOrCreateNbtCompound(stack).getBoolean("Locked");
    }

    public static void setLocked(@NotNull ItemStack stack, boolean locked) {
        GTUtility.getOrCreateNbtCompound(stack).setBoolean("Locked", locked);
    }

    public static void toggleLocked(@NotNull ItemStack stack) {
        setLocked(stack, !isLocked(stack));
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
        if ((event.getButton() != -1 && event.isButtonstate()) || event.getDwheel() != 0) {
            int button = event.getButton();
            int scroll = event.getDwheel();

            if (button == 0) {
                setColor(stack, getColorOrdinal(stack) + 1);
                event.setCanceled(true);
                // TODO: sneak click rotates colors the other way
            } else if (button == 2) {
                toggleLocked(stack);
                event.setCanceled(true);
            }

            sendToServer(buf -> buf
                    .writeVarInt(button)
                    .writeVarInt(scroll));
        }
    }

    @Override
    public void handleMouseEventServer(@NotNull PacketItemMouseEvent packet, @NotNull EntityPlayerMP playerServer,
                                       @NotNull ItemStack stack) {
        PacketBuffer buf = packet.getBuffer();
        int button = buf.readVarInt();
        int scroll = buf.readVarInt();

        if (button == 0) {
            setColor(stack, getColorOrdinal(stack) + 1);
            // TODO: sneak click rotates colors the other way
        } else if (button == 2) {
            toggleLocked(stack);
            EnumDyeColor color = getColor(stack);
            // TODO: lang
            playerServer.sendStatusMessage(new TextComponentString(
                    isLocked(stack) ? String.format("Locked to %s", color == null ? "solvent" : color) : "Unlocked"),
                    true);
        }
    }
}
