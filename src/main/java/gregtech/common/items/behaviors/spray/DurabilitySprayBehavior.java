package gregtech.common.items.behaviors.spray;

import gregtech.api.items.metaitem.stats.IItemDurabilityManager;
import gregtech.api.util.GTUtility;
import gregtech.api.util.GradientUtil;

import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.Constants;

import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.util.List;

public class DurabilitySprayBehavior extends AbstractSprayBehavior implements IItemDurabilityManager {

    public static final String NBT_KEY = "GT.UsesLeft";

    @Nullable
    private final EnumDyeColor color;
    @NotNull
    private final Pair<Color, Color> durabilityBarColors;
    @NotNull
    private final ItemStack replacementStack;
    public final int maxUses;

    public DurabilitySprayBehavior(@NotNull ItemStack replacementStack, int maxUses, @Nullable EnumDyeColor color) {
        this.color = color;
        this.replacementStack = replacementStack;
        this.maxUses = maxUses;
        this.durabilityBarColors = GradientUtil.getGradient(color == null ? 0x969696 : color.colorValue, 10);
    }

    public DurabilitySprayBehavior(int maxUses, @Nullable EnumDyeColor color) {
        this(ItemStack.EMPTY, maxUses, color);
    }

    @Override
    public boolean canSpray(@NotNull ItemStack stack) {
        return getUsesLeft(stack) > 0;
    }

    @Override
    public void onSpray(@NotNull EntityPlayer player, @NotNull ItemStack sprayCan) {
        if (player.capabilities.isCreativeMode) return;

        if (damageCan(sprayCan)) {
            if (replacementStack.isEmpty()) {
                // If replacement stack is empty, just shrink resulting stack
                sprayCan.shrink(1);
            } else {
                // Update held item to replacement stack
                sprayCan.setItemDamage(replacementStack.getItemDamage());
                // Clear NBT from old can
                sprayCan.setTagCompound(new NBTTagCompound());
                // Play sound manually since we aren't using player.setHeldItem(...)
                player.playSound(SoundEvents.ITEM_ARMOR_EQUIP_GENERIC, 1.0f, 1.0f);
            }
        }
    }

    protected boolean damageCan(@NotNull ItemStack sprayCan) {
        int usesLeft = getUsesLeft(sprayCan) - 1;
        setUsesLeft(sprayCan, usesLeft);
        return usesLeft == 0;
    }

    @Override
    public @Nullable EnumDyeColor getColor(@NotNull ItemStack stack) {
        return this.color;
    }

    @Override
    public @NotNull ColorMode getColorMode(@NotNull ItemStack sprayCan) {
        return ColorMode.DYE_ONLY;
    }

    protected int getUsesLeft(@NotNull ItemStack stack) {
        if (stack.isItemEqual(replacementStack)) return 0;

        NBTTagCompound tagCompound = GTUtility.getOrCreateNbtCompound(stack);
        if (!tagCompound.hasKey(NBT_KEY, Constants.NBT.TAG_INT)) {
            tagCompound.setInteger(NBT_KEY, maxUses);
            return maxUses;
        }

        return tagCompound.getInteger(NBT_KEY);
    }

    protected static void setUsesLeft(@NotNull ItemStack itemStack, int usesLeft) {
        GTUtility.getOrCreateNbtCompound(itemStack).setInteger(NBT_KEY, usesLeft);
    }

    @Override
    public void addInformation(ItemStack itemStack, List<String> lines) {
        int remainingUses = getUsesLeft(itemStack);
        EnumDyeColor color = getColor(itemStack);

        if (color != null) {
            lines.add(I18n.format("behaviour.paintspray." + color.getTranslationKey() + ".tooltip"));
        } else {
            lines.add(I18n.format("behaviour.paintspray.solvent.tooltip"));
        }

        lines.add(I18n.format("behaviour.paintspray.uses", remainingUses));

        if (color != null) {
            lines.add(I18n.format("behaviour.paintspray.offhand"));
        }
    }

    @Override
    public double getDurabilityForDisplay(ItemStack itemStack) {
        return GTUtility.calculateDurabilityFromRemaining(getUsesLeft(itemStack), maxUses);
    }

    @Nullable
    @Override
    public Pair<Color, Color> getDurabilityColorsForDisplay(ItemStack itemStack) {
        return durabilityBarColors;
    }

    @Override
    public boolean doDamagedStateColors(ItemStack itemStack) {
        return false;
    }
}
