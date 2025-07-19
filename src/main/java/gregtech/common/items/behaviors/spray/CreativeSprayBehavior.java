package gregtech.common.items.behaviors.spray;

import gregtech.api.items.gui.ItemUIFactory;
import gregtech.api.mui.GTGuis;
import gregtech.api.mui.factory.MetaItemGuiFactory;
import gregtech.api.util.GTUtility;
import gregtech.common.items.MetaItems;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;

import com.cleanroommc.modularui.drawable.ItemDrawable;
import com.cleanroommc.modularui.factory.HandGuiData;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.value.sync.IntSyncValue;
import com.cleanroommc.modularui.value.sync.PanelSyncManager;
import com.cleanroommc.modularui.widgets.ButtonWidget;
import com.cleanroommc.modularui.widgets.SlotGroupWidget;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CreativeSprayBehavior extends AbstractSprayBehavior implements ItemUIFactory {

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
                                .overlay(new ItemDrawable(MetaItems.SPRAY_SOLVENT.getStackForm()))
                                .onMousePressed(mouse -> {
                                    colorSync.setIntValue(-1);
                                    return true;
                                }))
                        .key('C', index -> new ButtonWidget<>()
                                .size(18)
                                .overlay(new ItemDrawable(
                                        MetaItems.SPRAY_CAN_DYES.get(EnumDyeColor.values()[index]).getStackForm()))
                                .onMousePressed(mouse -> {
                                    colorSync.setIntValue(index);
                                    return true;
                                }))
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

    public void setColor(@NotNull ItemStack stack, @Nullable EnumDyeColor color) {
        GTUtility.getOrCreateNbtCompound(stack).setInteger("color", color == null ? -1 : color.ordinal());
    }

    public void setColor(@NotNull ItemStack stack, int color) {
        if (color >= 0 && color <= 15) {
            setColor(stack, EnumDyeColor.values()[color]);
        } else {
            setColor(stack, null);
        }
    }
}
