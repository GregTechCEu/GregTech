package gregtech.common.items.behaviors;

import gregtech.api.GTValues;
import gregtech.api.items.metaitem.stats.IItemBehaviour;
import gregtech.api.items.metaitem.stats.IItemDurabilityManager;
import gregtech.api.items.metaitem.stats.ISubItemHandler;
import gregtech.api.unification.material.Materials;
import gregtech.api.util.GTUtility;
import gregtech.api.util.GradientUtil;

import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.block.Block;
import net.minecraft.block.BlockTNT;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraftforge.fluids.capability.IFluidTankProperties;

import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.util.List;

public class LighterBehaviour implements IItemBehaviour, IItemDurabilityManager, ISubItemHandler {

    private static final String LIGHTER_OPEN = "lighterOpen";
    private static final String USES_LEFT = "usesLeft";
    private static final Pair<Color, Color> DURABILITY_BAR_COLORS = GradientUtil.getGradient(0xF07F1D, 10);

    private final ResourceLocation overrideLocation;
    private final boolean usesFluid;
    private final boolean hasMultipleUses;
    private final boolean canOpen;

    private Item destroyItem = Items.AIR;

    private int maxUses = 0;

    public LighterBehaviour(boolean usesFluid, boolean hasMultipleUses, boolean canOpen) {
        this(null, usesFluid, hasMultipleUses, canOpen);
    }

    public LighterBehaviour(boolean usesFluid, boolean hasMultipleUses, boolean canOpen, Item destroyItem,
                            int maxUses) {
        this(null, usesFluid, hasMultipleUses, canOpen);
        this.maxUses = maxUses;
        this.destroyItem = destroyItem;
    }

    public LighterBehaviour(@Nullable ResourceLocation overrideLocation, boolean usesFluid, boolean hasMultipleUses,
                            boolean canOpen) {
        this.overrideLocation = overrideLocation;
        this.usesFluid = usesFluid;
        this.hasMultipleUses = hasMultipleUses;
        this.canOpen = canOpen;
    }

    @Override
    public boolean onLeftClickEntity(ItemStack stack, EntityPlayer player, Entity entity) {
        if (entity instanceof EntityCreeper) {
            NBTTagCompound compound = GTUtility.getOrCreateNbtCompound(stack);
            // If this item does not have opening mechanics, or if it does and is currently open
            if ((!canOpen || compound.getBoolean(LIGHTER_OPEN)) && consumeFuel(player, stack)) {
                player.getEntityWorld().playSound(null, player.getPosition(), SoundEvents.ITEM_FLINTANDSTEEL_USE,
                        SoundCategory.PLAYERS, 1.0F, GTValues.RNG.nextFloat() * 0.4F + 0.8F);
                ((EntityCreeper) entity).ignite();
                return true;
            }
        }
        return false;
    }

    @Override
    public EnumActionResult onItemUseFirst(@NotNull EntityPlayer player, @NotNull World world, BlockPos pos,
                                           EnumFacing side, float hitX, float hitY, float hitZ, EnumHand hand) {
        ItemStack stack = player.getHeldItem(hand);
        NBTTagCompound compound = GTUtility.getOrCreateNbtCompound(stack);

        if (canOpen && player.isSneaking()) {
            compound.setBoolean(LIGHTER_OPEN, !compound.getBoolean(LIGHTER_OPEN));
            stack.setTagCompound(compound);
            return EnumActionResult.PASS;
        }

        if (!player.canPlayerEdit(pos, side, player.getHeldItem(hand))) return EnumActionResult.FAIL;
        // If this item does not have opening mechanics, or if it does and is currently open
        if ((!canOpen || compound.getBoolean(LIGHTER_OPEN)) && consumeFuel(player, stack)) {
            player.getEntityWorld().playSound(null, player.getPosition(), SoundEvents.ITEM_FLINTANDSTEEL_USE,
                    SoundCategory.PLAYERS, 1.0F, GTValues.RNG.nextFloat() * 0.4F + 0.8F);
            IBlockState blockState = world.getBlockState(pos);
            Block block = blockState.getBlock();
            if (block instanceof BlockTNT) {
                ((BlockTNT) block).explode(world, pos, blockState.withProperty(BlockTNT.EXPLODE, true), player);
                world.setBlockState(pos, Blocks.AIR.getDefaultState(), 11);
                return EnumActionResult.SUCCESS;
            }

            BlockPos offset = pos.offset(side);
            world.setBlockState(offset, Blocks.FIRE.getDefaultState(), 11);
            if (!world.isRemote) {
                CriteriaTriggers.PLACED_BLOCK.trigger((EntityPlayerMP) player, offset, stack);
            }
            return EnumActionResult.SUCCESS;
        }

        return EnumActionResult.FAIL;
    }

    @Override
    public void addInformation(ItemStack itemStack, @NotNull List<String> lines) {
        lines.add(I18n.format(usesFluid ? "behaviour.lighter.fluid.tooltip" : "behaviour.lighter.tooltip"));
        if (hasMultipleUses && !usesFluid) {
            lines.add(I18n.format("behaviour.lighter.uses", getUsesLeft(itemStack)));
        }
    }

    public boolean consumeFuel(EntityPlayer entity, ItemStack stack) {
        if (entity != null && entity.isCreative())
            return true;

        int usesLeft = getUsesLeft(stack);

        // if there is enough fuel, consume 1
        if (usesLeft - 1 >= 0) {
            setUsesLeft(entity, stack, usesLeft - 1);
            return true;
        }
        return false;
    }

    private int getUsesLeft(@NotNull ItemStack stack) {
        if (usesFluid) {
            IFluidHandlerItem fluidHandlerItem = stack
                    .getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null);
            if (fluidHandlerItem == null)
                return 0;

            FluidStack drained = fluidHandlerItem.drain(Integer.MAX_VALUE, false);
            return drained == null ? 0 : drained.amount;
        }
        if (hasMultipleUses) {
            NBTTagCompound compound = GTUtility.getOrCreateNbtCompound(stack);
            if (compound.hasKey(USES_LEFT)) {
                return compound.getInteger(USES_LEFT);
            }
            compound.setInteger(USES_LEFT, maxUses);
            stack.setTagCompound(compound);
            return compound.getInteger(USES_LEFT);
        }
        return stack.getCount();
    }

    private void setUsesLeft(EntityPlayer entity, @NotNull ItemStack stack, int usesLeft) {
        if (usesFluid) {
            IFluidHandlerItem fluidHandlerItem = stack
                    .getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null);
            if (fluidHandlerItem != null) {
                FluidStack drained = fluidHandlerItem.drain(Integer.MAX_VALUE, false);
                if (drained != null) {
                    fluidHandlerItem.drain(drained.amount - usesLeft, true);
                }
            }
        } else if (hasMultipleUses) {
            if (usesLeft == 0) {
                stack.setCount(0);
                entity.addItemStackToInventory(new ItemStack(destroyItem));
            } else {
                GTUtility.getOrCreateNbtCompound(stack).setInteger(USES_LEFT, usesLeft);
            }
        } else {
            stack.setCount(usesLeft);
        }
    }

    @Override
    public void addPropertyOverride(@NotNull Item item) {
        if (overrideLocation != null) {
            item.addPropertyOverride(overrideLocation,
                    (stack, world, entity) -> GTUtility.getOrCreateNbtCompound(stack).getBoolean(LIGHTER_OPEN) ? 1.0F :
                            0.0F);
        }
    }

    @Override
    public double getDurabilityForDisplay(ItemStack itemStack) {
        if (usesFluid) {
            // Lighters
            IFluidHandlerItem fluidHandlerItem = itemStack
                    .getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null);
            if (fluidHandlerItem == null) return 0.0;
            IFluidTankProperties properties = fluidHandlerItem.getTankProperties()[0];
            FluidStack fluidStack = properties.getContents();
            return fluidStack == null ? 0.0 : (double) fluidStack.amount / (double) properties.getCapacity();
        }
        if (hasMultipleUses) {
            // Matchbox
            return (double) getUsesLeft(itemStack) / (double) maxUses;
        }
        // no durability for Match
        return 0.0;
    }

    @Nullable
    @Override
    public Pair<Color, Color> getDurabilityColorsForDisplay(ItemStack itemStack) {
        if (hasMultipleUses && usesFluid) {
            return DURABILITY_BAR_COLORS;
        }
        // use default colors for Matchbox
        return null;
    }

    @Override
    public boolean doDamagedStateColors(ItemStack itemStack) {
        return hasMultipleUses && !usesFluid; // don't show for Matchbox
    }

    @Override
    public boolean showEmptyBar(ItemStack itemStack) {
        return hasMultipleUses; // don't show for Match
    }

    @Override
    public boolean showFullBar(ItemStack itemStack) {
        return hasMultipleUses; // don't show for Match
    }

    @Override
    public String getItemSubType(ItemStack itemStack) {
        return "";
    }

    @Override
    public void getSubItems(ItemStack itemStack, CreativeTabs creativeTab, NonNullList<ItemStack> subItems) {
        if (usesFluid) {
            // Show Lighters as filled in JEI
            ItemStack copy = itemStack.copy();
            IFluidHandlerItem fluidHandlerItem = copy
                    .getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null);
            if (fluidHandlerItem != null) {
                fluidHandlerItem.fill(Materials.Butane.getFluid(Integer.MAX_VALUE), true);
                subItems.add(copy);
                return;
            }
        }
        subItems.add(itemStack);
    }
}
