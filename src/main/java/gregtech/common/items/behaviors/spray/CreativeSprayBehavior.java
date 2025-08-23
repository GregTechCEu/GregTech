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

import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.client.event.MouseEvent;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import codechicken.lib.raytracer.RayTracer;
import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.drawable.ItemDrawable;
import com.cleanroommc.modularui.factory.HandGuiData;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.value.BoolValue;
import com.cleanroommc.modularui.value.IntValue;
import com.cleanroommc.modularui.value.sync.BooleanSyncValue;
import com.cleanroommc.modularui.value.sync.IntSyncValue;
import com.cleanroommc.modularui.value.sync.PanelSyncManager;
import com.cleanroommc.modularui.value.sync.SyncHandlers;
import com.cleanroommc.modularui.widgets.SlotGroupWidget;
import com.cleanroommc.modularui.widgets.ToggleButton;
import com.cleanroommc.modularui.widgets.layout.Flow;
import com.cleanroommc.modularui.widgets.textfield.TextFieldWidget;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static gregtech.api.cover.CoverWithUI.UI_TITLE_COLOR;
import static gregtech.api.util.ColorUtil.*;

public class CreativeSprayBehavior extends AbstractSprayBehavior implements ItemUIFactory, IItemColorProvider,
                                   IItemNameProvider, IMouseEventHandler {

    private static final String NBT_KEY_COLOR = "color";
    private static final String NBT_KEY_USESARGB = "usesARGB";
    private static final String NBT_KEY_ARGB_COLOR = "argbColor";

    @Override
    public ModularPanel buildUI(HandGuiData guiData, PanelSyncManager guiSyncManager) {
        ItemStack usedStack = guiData.getUsedItemStack();
        IntSyncValue colorSync = SyncHandlers.intNumber(() -> getColorOrdinal(usedStack),
                newColor -> setColorOrdinal(usedStack, newColor));
        guiSyncManager.syncValue("color", 0, colorSync);
        BooleanSyncValue arbgSync = SyncHandlers.bool(() -> usesARGB(usedStack), bool -> useARGB(usedStack, bool));
        IntSyncValue argb = SyncHandlers.intNumber(() -> getColorInt(usedStack),
                newColor -> setColor(usedStack, newColor));
        guiSyncManager.syncValue("argb", 0, argb);

        // noinspection SpellCheckingInspection
        return GTGuis.createPanel(usedStack, 176, 120)
                .child(Flow.row()
                        .pos(4, 4)
                        .coverChildrenWidth()
                        .height(16)
                        .child(new ItemDrawable(usedStack).asWidget().size(16).marginRight(4))
                        .child(IKey.lang("metaitem.spray.creative.name_base")
                                .color(UI_TITLE_COLOR)
                                .asWidget().heightRel(1.0f)))
                .child(SlotGroupWidget.builder()
                        .matrix("SCCCCCCCC",
                                "CCCCCCCC")
                        .key('S', new ToggleButton()
                                .size(18)
                                .value(new BoolValue.Dynamic(
                                        () -> colorSync.getIntValue() == -1 && !arbgSync.getBoolValue(),
                                        $ -> {
                                            if (!arbgSync.getBoolValue()) colorSync.setIntValue(-1);
                                        }))
                                .overlay(new ItemDrawable(MetaItems.SPRAY_SOLVENT.getStackForm())
                                        .asIcon()
                                        .margin(2))
                                .addTooltipLine(IKey.lang("metaitem.spray.creative.solvent")))
                        .key('C', index -> {
                            EnumDyeColor color = EnumDyeColor.values()[index];
                            return new ToggleButton()
                                    .size(18)
                                    .value(new BoolValue.Dynamic(
                                            () -> colorSync.getIntValue() == index && !arbgSync.getBoolValue(),
                                            $ -> {
                                                if (!arbgSync.getBoolValue()) colorSync.setIntValue(index);
                                            }))
                                    .overlay(new ItemDrawable(MetaItems.SPRAY_CAN_DYES.get(color).getStackForm())
                                            .asIcon()
                                            .margin(2))
                                    .addTooltipLine(IKey.lang("metaitem.spray.creative." + color));
                        })
                        .build()
                        .left(4)
                        .top(24))
                .child(Flow.row()
                        .left(4)
                        .top(24 + 18 * 2 + 8)
                        .coverChildren()
                        .child(new ToggleButton()
                                .size(18)
                                .value(arbgSync))
                        .child(new TextFieldWidget()
                                .width(30)
                                .setNumbers(0, 255)
                                .value(new IntValue.Dynamic(
                                        () -> ARGBHelper.RED.getFromInt(argb.getIntValue()),
                                        newR -> argb.setIntValue(
                                                ARGBHelper.RED.setInInt(argb.getIntValue(), newR)))))
                        .child(new TextFieldWidget()
                                .width(30)
                                .setNumbers(0, 255)
                                .value(new IntValue.Dynamic(
                                        () -> ARGBHelper.GREEN.getFromInt(argb.getIntValue()),
                                        newG -> argb.setIntValue(
                                                ARGBHelper.GREEN.setInInt(argb.getIntValue(), newG)))))
                        .child(new TextFieldWidget()
                                .width(30)
                                .setNumbers(0, 255)
                                .value(new IntValue.Dynamic(
                                        () -> ARGBHelper.BLUE.getFromInt(argb.getIntValue()),
                                        newB -> argb.setIntValue(
                                                ARGBHelper.BLUE.setInInt(argb.getIntValue(), newB))))));
    }

    @Override
    public @Nullable EnumDyeColor getColor(@NotNull ItemStack sprayCan) {
        NBTTagCompound tag = GTUtility.getOrCreateNbtCompound(sprayCan);
        if (tag.hasKey(NBT_KEY_COLOR, Constants.NBT.TAG_INT)) {
            int color = tag.getInteger(NBT_KEY_COLOR);
            if (color < 0 || color > 15) return null;
            return EnumDyeColor.values()[color];
        }

        return null;
    }

    @Override
    public int getColorInt(@NotNull ItemStack sprayCan) {
        NBTTagCompound tag = GTUtility.getOrCreateNbtCompound(sprayCan);
        return tag.getBoolean(NBT_KEY_USESARGB) ? tag.getInteger(NBT_KEY_ARGB_COLOR) : super.getColorInt(sprayCan);
    }

    public static void setColor(@NotNull ItemStack sprayCan, @Nullable EnumDyeColor color) {
        GTUtility.getOrCreateNbtCompound(sprayCan).setInteger(NBT_KEY_COLOR, color == null ? -1 : color.ordinal());
    }

    public static void setColorOrdinal(@NotNull ItemStack sprayCan, int ordinal) {
        GTUtility.getOrCreateNbtCompound(sprayCan).setInteger(NBT_KEY_COLOR,
                ordinal >= 0 && ordinal <= 15 ? ordinal : -1);
    }

    public static void setColor(@NotNull ItemStack sprayCan, int argbColor) {
        GTUtility.getOrCreateNbtCompound(sprayCan).setInteger(NBT_KEY_ARGB_COLOR, argbColor);
    }

    public static boolean usesARGB(@NotNull ItemStack sprayCan) {
        return GTUtility.getOrCreateNbtCompound(sprayCan).getBoolean(NBT_KEY_USESARGB);
    }

    public static void useARGB(@NotNull ItemStack sprayCan, boolean bool) {
        GTUtility.getOrCreateNbtCompound(sprayCan).setBoolean(NBT_KEY_USESARGB, bool);
    }

    @Override
    public int getItemStackColor(ItemStack sprayCan, int tintIndex) {
        if (tintIndex != 1) return 0xFFFFFF;

        if (usesARGB(sprayCan)) {
            return getColorInt(sprayCan);
        } else {
            EnumDyeColor color = getColor(sprayCan);
            return color != null ? color.colorValue : 0xFFFFFF;
        }
    }

    @Override
    public String getItemStackDisplayName(ItemStack sprayCan, String unlocalizedName) {
        EnumDyeColor color = getColor(sprayCan);
        String colorString = color == null ? I18n.format("metaitem.spray.creative.solvent") :
                I18n.format("metaitem.spray.creative." + color);
        return I18n.format(unlocalizedName, colorString);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void handleMouseEventClient(@NotNull MouseEvent event, @NotNull EntityPlayerSP playerClient,
                                       @NotNull EnumHand hand, @NotNull ItemStack sprayCan) {
        // Middle click pressed down
        if (event.getButton() == 2 && event.isButtonstate()) {
            event.setCanceled(true);

            RayTraceResult rayTrace = RayTracer.retrace(playerClient);
            if (rayTrace != null && rayTrace.typeOfHit == RayTraceResult.Type.BLOCK) {
                World world = playerClient.world;
                BlockPos pos = rayTrace.getBlockPos();
                EnumFacing facing = rayTrace.sideHit;
                ColoredBlockContainer container = ColoredBlockContainer.getContainer(world, pos, facing,
                        playerClient);

                if (usesARGB(sprayCan) && container.supportsARGB() &&
                        !container.colorMatches(world, pos, facing, playerClient, getColorInt(sprayCan))) {
                    int color = container.getColorInt(world, pos, facing, playerClient);
                    if (color != -1) {
                        setColor(sprayCan, color);
                        sendToServer(hand, buf -> buf
                                .writeByte(1)
                                .writeInt(color));
                        return;
                    }
                } else if (!container.colorMatches(world, pos, facing, playerClient, getColor(sprayCan))) {
                    EnumDyeColor color = container.getColor(world, pos, facing, playerClient);
                    if (color != null) {
                        setColor(sprayCan, color);
                        sendToServer(hand, buf -> buf
                                .writeByte(2)
                                .writeByte(color.ordinal()));
                        return;
                    }
                }
            }

            // If the player isn't sneaking and wasn't looking at a colored block, open gui
            sendToServer(hand, buf -> buf.writeByte(0));
        }
    }

    @Override
    public void handleMouseEventServer(@NotNull PacketBuffer buf, @NotNull EntityPlayerMP playerServer,
                                       @NotNull EnumHand hand, @NotNull ItemStack sprayCan) {
        switch (buf.readByte()) {
            case 0 -> MetaItemGuiFactory.open(playerServer, EnumHand.MAIN_HAND);
            case 1 -> setColor(sprayCan, EnumDyeColor.values()[buf.readByte()]);
            case 2 -> setColor(sprayCan, buf.readInt());
        }
    }
}
