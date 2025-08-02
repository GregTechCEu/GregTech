package gregtech.common.items.behaviors.spray;

import gregtech.api.items.gui.ItemUIFactory;
import gregtech.api.items.metaitem.stats.IItemColorProvider;
import gregtech.api.items.metaitem.stats.IItemNameProvider;
import gregtech.api.items.metaitem.stats.IMouseEventHandler;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.mui.GTGuis;
import gregtech.api.mui.factory.MetaItemGuiFactory;
import gregtech.api.util.GTUtility;
import gregtech.api.util.Mods;
import gregtech.common.items.MetaItems;
import gregtech.core.network.packets.PacketItemMouseEvent;

import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.client.event.MouseEvent;
import net.minecraftforge.common.util.Constants;

import appeng.tile.networking.TileCableBus;
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
        if (event.getButton() != -1 && event.isButtonstate()) {
            int button = event.getButton();
            boolean sneaking = playerClient.isSneaking();

            if (button == 0) { // Left click
                int color;
                if (sneaking) {
                    color = getColorOrdinal(stack) - 1;
                    if (color == -2) color = 15;
                } else {
                    color = getColorOrdinal(stack) + 1;
                }

                setColor(stack, color);
                event.setCanceled(true);

                final int finalColor = color; // grr java
                sendToServer(buf -> buf
                        .writeByte(0)
                        .writeByte(finalColor));
            } else if (button == 2) { // Middle click
                if (sneaking) {
                    toggleLocked(stack);
                } else if (!isLocked(stack)) {
                    double reach = playerClient.getEntityAttribute(EntityPlayer.REACH_DISTANCE).getAttributeValue();

                    if (!playerClient.capabilities.isCreativeMode) {
                        reach -= 0.5d;
                    }

                    RayTraceResult rayTrace = playerClient.rayTrace(reach, 1.0f);
                    if (rayTrace != null && rayTrace.typeOfHit == RayTraceResult.Type.BLOCK) {
                        EnumDyeColor hitColor = getBlockColor(playerClient.world, rayTrace.getBlockPos());
                        if (hitColor != null) {
                            setColor(stack, hitColor);
                            sendToServer(buf -> buf
                                    .writeByte(0)
                                    .writeByte(hitColor.ordinal()));
                        } else {
                            // If the player isn't sneaking and also not looking at a colored block, open gui
                            sendToServer(buf -> buf.writeByte(1));
                        }
                    }
                }

                event.setCanceled(true);
            }
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

    public static @Nullable EnumDyeColor getBlockColor(@NotNull World world, @NotNull BlockPos pos) {
        if (world.isAirBlock(pos)) return null;

        IBlockState state = world.getBlockState(pos);
        for (IProperty<?> prop : state.getPropertyKeys()) {
            if (prop.getValueClass() == EnumDyeColor.class) {
                // noinspection unchecked
                return state.getValue((IProperty<EnumDyeColor>) prop);
            }
        }

        TileEntity te = world.getTileEntity(pos);
        if (te != null) {
            if (te instanceof IGregTechTileEntity gtte) {
                MetaTileEntity mte = gtte.getMetaTileEntity();
                // Unfortunately MTEs store their color as an ARGB int instead of just an EnumDyeColor. Thus, this has
                // to be done because changing MTEs to store/be limited to EnumDyeColor is API breaking and would limit
                // the colors an MTE could be to 16.
                int mteColor = mte.getPaintingColor();
                for (EnumDyeColor dyeColor : EnumDyeColor.values()) {
                    if (mteColor == dyeColor.colorValue) {
                        return dyeColor;
                    }
                }
            }

            if (Mods.AppliedEnergistics2.isModLoaded()) {
                if (te instanceof TileCableBus cable) {
                    return cable.getColor().dye;
                }
            }
        }

        return null;
    }
}
