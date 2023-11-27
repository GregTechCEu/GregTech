package gregtech.api.items.toolitem;

import gregtech.api.GregTechAPI;
import gregtech.api.util.LocalizationUtils;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemAxe;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import com.google.common.collect.Multimap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

public class ItemGTAxe extends ItemAxe implements IGTTool {

    protected final String domain, id;

    protected final int tier;
    protected final IGTToolDefinition toolStats;
    protected final Set<String> toolClasses;
    protected final String oreDict;
    protected final List<String> secondaryOreDicts;
    protected final SoundEvent sound;
    protected final boolean playSoundOnBlockDestroy;
    protected final Supplier<ItemStack> markerItem;

    protected ItemGTAxe(String domain, String id, int tier, IGTToolDefinition toolStats, SoundEvent sound,
                        boolean playSoundOnBlockDestroy, Set<String> toolClasses, String oreDict,
                        List<String> secondaryOreDicts, Supplier<ItemStack> markerItem) {
        super(ToolMaterial.STONE, 0F, 0F);
        this.domain = domain;
        this.id = id;
        this.tier = tier;
        this.toolStats = toolStats;
        this.sound = sound;
        this.playSoundOnBlockDestroy = playSoundOnBlockDestroy;
        this.toolClasses = Collections.unmodifiableSet(toolClasses);
        this.oreDict = oreDict;
        this.secondaryOreDicts = secondaryOreDicts;
        this.markerItem = markerItem;
        setMaxStackSize(1);
        setCreativeTab(GregTechAPI.TAB_GREGTECH_TOOLS);
        setTranslationKey("gt.tool." + id + ".name");
        setRegistryName(domain, id);
    }

    @Override
    public String getDomain() {
        return domain;
    }

    @Override
    public String getToolId() {
        return id;
    }

    @Override
    public boolean isElectric() {
        return tier > -1;
    }

    @Override
    public int getElectricTier() {
        return tier;
    }

    @Override
    public IGTToolDefinition getToolStats() {
        return toolStats;
    }

    @Nullable
    @Override
    public SoundEvent getSound() {
        return sound;
    }

    @Override
    public boolean playSoundOnBlockDestroy() {
        return playSoundOnBlockDestroy;
    }

    @Override
    public String getOreDictName() {
        return oreDict;
    }

    @NotNull
    @Override
    public List<String> getSecondaryOreDicts() {
        return this.secondaryOreDicts;
    }

    @Nullable
    @Override
    public Supplier<ItemStack> getMarkerItem() {
        return markerItem;
    }

    @Override
    public void getSubItems(@NotNull CreativeTabs tab, @NotNull NonNullList<ItemStack> items) {
        if (this.isInCreativeTab(tab)) definition$getSubItems(items);
    }

    @NotNull
    @Override
    public String getItemStackDisplayName(@NotNull ItemStack stack) {
        return LocalizationUtils.format(getTranslationKey(), getToolMaterial(stack).getLocalizedName());
    }

    @Override
    public float getDestroySpeed(@NotNull ItemStack stack, @NotNull IBlockState state) {
        return definition$getDestroySpeed(stack, state);
    }

    @Override
    public boolean hitEntity(@NotNull ItemStack stack, @NotNull EntityLivingBase target,
                             @NotNull EntityLivingBase attacker) {
        return definition$hitEntity(stack, target, attacker);
    }

    @Override
    public boolean onBlockStartBreak(@NotNull ItemStack itemstack, @NotNull BlockPos pos,
                                     @NotNull EntityPlayer player) {
        return definition$onBlockStartBreak(itemstack, pos, player);
    }

    @Override
    public boolean onBlockDestroyed(@NotNull ItemStack stack, @NotNull World worldIn, @NotNull IBlockState state,
                                    @NotNull BlockPos pos, @NotNull EntityLivingBase entityLiving) {
        return definition$onBlockDestroyed(stack, worldIn, state, pos, entityLiving);
    }

    @Override
    public int getItemEnchantability(@NotNull ItemStack stack) {
        return getTotalEnchantability(stack);
    }

    @Override
    public boolean getIsRepairable(@NotNull ItemStack toRepair, @NotNull ItemStack repair) {
        return definition$getIsRepairable(toRepair, repair);
    }

    @NotNull
    @Override
    public Multimap<String, AttributeModifier> getAttributeModifiers(@NotNull EntityEquipmentSlot slot,
                                                                     @NotNull ItemStack stack) {
        return definition$getAttributeModifiers(slot, stack);
    }

    @Override
    public int getHarvestLevel(@NotNull ItemStack stack, @NotNull String toolClass, @Nullable EntityPlayer player,
                               @Nullable IBlockState blockState) {
        return definition$getHarvestLevel(stack, toolClass, player, blockState);
    }

    @NotNull
    @Override
    public Set<String> getToolClasses(@NotNull ItemStack stack) {
        return this.toolClasses;
    }

    @Override
    public boolean canDisableShield(@NotNull ItemStack stack, @NotNull ItemStack shield,
                                    @NotNull EntityLivingBase entity, @NotNull EntityLivingBase attacker) {
        return definition$canDisableShield(stack, shield, entity, attacker);
    }

    @Override
    public boolean doesSneakBypassUse(@NotNull ItemStack stack, @NotNull IBlockAccess world, @NotNull BlockPos pos,
                                      @NotNull EntityPlayer player) {
        return definition$doesSneakBypassUse(stack, world, pos, player);
    }

    @Override
    public boolean shouldCauseBlockBreakReset(@NotNull ItemStack oldStack, @NotNull ItemStack newStack) {
        return definition$shouldCauseBlockBreakReset(oldStack, newStack);
    }

    @Override
    public boolean hasContainerItem(@NotNull ItemStack stack) {
        return definition$hasContainerItem(stack);
    }

    @NotNull
    @Override
    public ItemStack getContainerItem(@NotNull ItemStack stack) {
        return definition$getContainerItem(stack);
    }

    @Override
    public boolean onEntitySwing(@NotNull EntityLivingBase entityLiving, @NotNull ItemStack stack) {
        return definition$onEntitySwing(entityLiving, stack);
    }

    @Override
    public boolean canDestroyBlockInCreative(@NotNull World world, @NotNull BlockPos pos, @NotNull ItemStack stack,
                                             @NotNull EntityPlayer player) {
        return definition$canDestroyBlockInCreative(world, pos, stack, player);
    }

    @Override
    public boolean shouldCauseReequipAnimation(@NotNull ItemStack oldStack, @NotNull ItemStack newStack,
                                               boolean slotChanged) {
        return definition$shouldCauseReequipAnimation(oldStack, newStack, slotChanged);
    }

    @Override
    public boolean isDamaged(@NotNull ItemStack stack) {
        return definition$isDamaged(stack);
    }

    @Override
    public int getDamage(@NotNull ItemStack stack) {
        return definition$getDamage(stack);
    }

    @Override
    public int getMaxDamage(@NotNull ItemStack stack) {
        return definition$getMaxDamage(stack);
    }

    @Override
    public void setDamage(@NotNull ItemStack stack, int damage) {
        definition$setDamage(stack, damage);
    }

    @Override
    public boolean showDurabilityBar(@NotNull ItemStack stack) {
        return false;
    }

    @Override
    public double getDurabilityForDisplay(@NotNull ItemStack stack) {
        return definition$getDurabilityForDisplay(stack);
    }

    @Nullable
    @Override
    public ICapabilityProvider initCapabilities(@NotNull ItemStack stack, @Nullable NBTTagCompound nbt) {
        return definition$initCapabilities(stack, nbt);
    }

    @NotNull
    @Override
    public EnumActionResult onItemUseFirst(@NotNull EntityPlayer player, @NotNull World world, @NotNull BlockPos pos,
                                           @NotNull EnumFacing side, float hitX, float hitY, float hitZ,
                                           @NotNull EnumHand hand) {
        return definition$onItemUseFirst(player, world, pos, side, hitX, hitY, hitZ, hand);
    }

    @NotNull
    @Override
    public EnumActionResult onItemUse(@NotNull EntityPlayer player, @NotNull World world, @NotNull BlockPos pos,
                                      @NotNull EnumHand hand, @NotNull EnumFacing facing, float hitX, float hitY,
                                      float hitZ) {
        return definition$onItemUse(player, world, pos, hand, facing, hitX, hitY, hitZ);
    }

    @NotNull
    @Override
    public ActionResult<ItemStack> onItemRightClick(@NotNull World world, @NotNull EntityPlayer player,
                                                    @NotNull EnumHand hand) {
        return definition$onItemRightClick(world, player, hand);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(@NotNull ItemStack stack, @Nullable World world, @NotNull List<String> tooltip,
                               @NotNull ITooltipFlag flag) {
        definition$addInformation(stack, world, tooltip, flag);
    }

    @Override
    public boolean canApplyAtEnchantingTable(@NotNull ItemStack stack, @NotNull Enchantment enchantment) {
        return definition$canApplyAtEnchantingTable(stack, enchantment);
    }

    @Override
    public boolean canHarvestBlock(@NotNull IBlockState state, @NotNull ItemStack stack) {
        return ToolHelper.isToolEffective(state, getToolClasses(stack), getTotalHarvestLevel(stack));
    }

    public static class Builder extends ToolBuilder<ItemGTAxe> {

        @NotNull
        public static ItemGTAxe.Builder of(@NotNull String domain, @NotNull String id) {
            return new ItemGTAxe.Builder(domain, id);
        }

        public Builder(@NotNull String domain, @NotNull String id) {
            super(domain, id);
        }

        @Override
        public Supplier<ItemGTAxe> supply() {
            return () -> new ItemGTAxe(domain, id, tier, toolStats, sound, playSoundOnBlockDestroy, toolClasses,
                    oreDict, secondaryOreDicts, markerItem);
        }
    }
}
