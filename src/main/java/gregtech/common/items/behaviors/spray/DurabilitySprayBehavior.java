package gregtech.common.items.behaviors.spray;

import gregtech.api.items.metaitem.stats.IItemDurabilityManager;
import gregtech.api.util.GTUtility;
import gregtech.api.util.GradientUtil;

import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
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

    @Override
    public @Nullable EnumDyeColor getColor(@NotNull ItemStack stack) {
        return this.color;
    }

    @Override
    public ActionResult<ItemStack> onItemUse(EntityPlayer player, World world, BlockPos pos, EnumHand hand,
                                             EnumFacing facing, float hitX, float hitY, float hitZ) {
        ActionResult<ItemStack> result = super.onItemUse(player, world, pos, hand, facing, hitX, hitY, hitZ);

        if (result.getType() == EnumActionResult.SUCCESS) {
            useItemDurability(player, hand, player.getHeldItem(hand), replacementStack.copy());
        }

        return result;
    }

    @Override
    public EnumActionResult useFromToolbelt(@NotNull EntityPlayer player, @NotNull World world, @NotNull BlockPos pos,
                                            @NotNull EnumHand hand, @NotNull EnumFacing facing,
                                            @NotNull ItemStack sprayCan) {
        EnumActionResult result = super.useFromToolbelt(player, world, pos, hand, facing, sprayCan);

        if (result == EnumActionResult.SUCCESS) {
            useItemDurability(player, hand, sprayCan, replacementStack.copy());
        }

        return result;
    }

    public void useItemDurability(@NotNull EntityPlayer player, @NotNull EnumHand hand, @NotNull ItemStack stack,
                                  @NotNull ItemStack replacementStack) {
        int usesLeft = getUsesLeft(stack);
        if (!player.capabilities.isCreativeMode) {
            if (--usesLeft <= 0) {
                if (replacementStack.isEmpty()) {
                    // If replacement stack is empty, just shrink resulting stack
                    stack.shrink(1);
                } else {
                    // Otherwise, update held item to replacement stack
                    player.setHeldItem(hand, replacementStack);
                }
                return;
            }

            setUsesLeft(stack, usesLeft);
        }
    }

    protected int getUsesLeft(@NotNull ItemStack stack) {
        NBTTagCompound tagCompound = stack.getTagCompound();
        if (tagCompound == null || !tagCompound.hasKey(NBT_KEY, Constants.NBT.TAG_INT)) {
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
        EnumDyeColor color = getColor();

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
