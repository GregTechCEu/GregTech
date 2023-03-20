package gregtech.api.items.toolitem;

import com.google.common.collect.Multimap;
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
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

public class ItemGTSword extends ItemSword implements IGTTool {

    private final String domain;
    private final String id;

    private final int tier;
    private final IGTToolDefinition toolStats;
    private final Set<String> toolClasses;
    private final SoundEvent sound;

    private final boolean playSoundOnBlockDestroy;
    private final String oredict;
    private final List<String> secondaryOreDicts;
    private final Supplier<ItemStack> markerItem;

    protected ItemGTSword(String domain, String id, int tier, IGTToolDefinition toolStats, SoundEvent sound,
                          boolean playSoundOnBlockDestroy, Set<String> toolClasses, String oreDict,
                          List<String> secondaryOreDicts, Supplier<ItemStack> markerItem) {
        super(ToolMaterial.STONE);
        this.domain = domain;
        this.id = id;
        this.tier = tier;
        this.toolStats = toolStats;
        this.sound = sound;
        this.playSoundOnBlockDestroy = playSoundOnBlockDestroy;
        this.toolClasses = Collections.unmodifiableSet(toolClasses);
        this.oredict = oreDict;
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
    public String getId() {
        return id;
    }

    @Nonnull
    @Override
    public String getItemStackDisplayName(@Nonnull ItemStack stack) {
        return LocalizationUtils.format(getTranslationKey(), getToolMaterial(stack).getLocalizedName());
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

    @Nullable
    @Override
    public String getOreDictName() {
        return oredict;
    }

    @Nonnull
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
    public void getSubItems(@Nonnull CreativeTabs tab, @Nonnull NonNullList<ItemStack> items) {
        if (this.isInCreativeTab(tab)) definition$getSubItems(items);
    }

    @Override
    public float getDestroySpeed(@Nonnull ItemStack stack, @Nonnull IBlockState state) {
        return definition$getDestroySpeed(stack, state);
    }

    @Override
    public boolean hitEntity(@Nonnull ItemStack stack, @Nonnull EntityLivingBase target, @Nonnull EntityLivingBase attacker) {
        return definition$hitEntity(stack, target, attacker);
    }

    @Override
    public boolean onBlockStartBreak(@Nonnull ItemStack itemstack, @Nonnull BlockPos pos, @Nonnull EntityPlayer player) {
        return definition$onBlockStartBreak(itemstack, pos, player);
    }

    @Override
    public boolean onBlockDestroyed(@Nonnull ItemStack stack, @Nonnull World worldIn, @Nonnull IBlockState state, @Nonnull BlockPos pos, @Nonnull EntityLivingBase entityLiving) {
        return definition$onBlockDestroyed(stack, worldIn, state, pos, entityLiving);
    }

    @Override
    public boolean canApplyAtEnchantingTable(@Nonnull ItemStack stack, @Nonnull Enchantment enchantment) {
        return definition$canApplyAtEnchantingTable(stack, enchantment);
    }

    @Override
    public int getItemEnchantability(@Nonnull ItemStack stack) {
        return getTotalEnchantability(stack);
    }

    @Override
    public boolean getIsRepairable(@Nonnull ItemStack toRepair, @Nonnull ItemStack repair) {
        return definition$getIsRepairable(toRepair, repair);
    }

    @Override
    public boolean canDestroyBlockInCreative(@Nonnull World world, @Nonnull BlockPos pos, @Nonnull ItemStack stack, @Nonnull EntityPlayer player) {
        return false;
    }

    @Nonnull
    @Override
    public Multimap<String, AttributeModifier> getAttributeModifiers(@Nonnull EntityEquipmentSlot slot, @Nonnull ItemStack stack) {
        return definition$getAttributeModifiers(slot, stack);
    }

    @Override
    public int getHarvestLevel(@Nonnull ItemStack stack, @Nonnull String toolClass, @Nullable EntityPlayer player, @Nullable IBlockState blockState) {
        return definition$getHarvestLevel(stack, toolClass, player, blockState);
    }

    @Nonnull
    @Override
    public Set<String> getToolClasses(@Nonnull ItemStack stack) {
        return this.toolClasses;
    }

    @Override
    public boolean canDisableShield(@Nonnull ItemStack stack, @Nonnull ItemStack shield, @Nonnull EntityLivingBase entity, @Nonnull EntityLivingBase attacker) {
        return definition$canDisableShield(stack, shield, entity, attacker);
    }

    @Override
    public boolean doesSneakBypassUse(@Nonnull ItemStack stack, @Nonnull IBlockAccess world, @Nonnull BlockPos pos, @Nonnull EntityPlayer player) {
        return definition$doesSneakBypassUse(stack, world, pos, player);
    }

    @Override
    public boolean shouldCauseBlockBreakReset(@Nonnull ItemStack oldStack, @Nonnull ItemStack newStack) {
        return definition$shouldCauseBlockBreakReset(oldStack, newStack);
    }

    @Override
    public boolean hasContainerItem(@Nonnull ItemStack stack) {
        return definition$hasContainerItem(stack);
    }

    @Nonnull
    @Override
    public ItemStack getContainerItem(@Nonnull ItemStack stack) {
        return definition$getContainerItem(stack);
    }

    @Override
    public boolean onEntitySwing(@Nonnull EntityLivingBase entityLiving, @Nonnull ItemStack stack) {
        return definition$onEntitySwing(entityLiving, stack);
    }

    @Override
    public boolean shouldCauseReequipAnimation(@Nonnull ItemStack oldStack, @Nonnull ItemStack newStack, boolean slotChanged) {
        return definition$shouldCauseReequipAnimation(oldStack, newStack, slotChanged);
    }

    @Override
    public boolean isDamaged(@Nonnull ItemStack stack) {
        return definition$isDamaged(stack);
    }

    @Override
    public int getDamage(@Nonnull ItemStack stack) {
        return definition$getDamage(stack);
    }

    @Override
    public int getMaxDamage(@Nonnull ItemStack stack) {
        return definition$getMaxDamage(stack);
    }

    @Override
    public void setDamage(@Nonnull ItemStack stack, int damage) {
        definition$setDamage(stack, damage);
    }

    @Override
    public boolean showDurabilityBar(@Nonnull ItemStack stack) {
        return false;
    }

    @Override
    public double getDurabilityForDisplay(@Nonnull ItemStack stack) {
        return definition$getDurabilityForDisplay(stack);
    }

    @Nullable
    @Override
    public ICapabilityProvider initCapabilities(@Nonnull ItemStack stack, @Nullable NBTTagCompound nbt) {
        return definition$initCapabilities(stack, nbt);
    }

    @Nonnull
    @Override
    public EnumActionResult onItemUse(@Nonnull EntityPlayer player, @Nonnull World world, @Nonnull BlockPos pos, @Nonnull EnumHand hand, @Nonnull EnumFacing facing, float hitX, float hitY, float hitZ) {
        return definition$onItemUse(player, world, pos, hand, facing, hitX, hitY, hitZ);
    }

    @Nonnull
    @Override
    public ActionResult<ItemStack> onItemRightClick(@Nonnull World world, @Nonnull EntityPlayer player, @Nonnull EnumHand hand) {
        // do not utilize IGTTool method to prevent a config gui from appearing
        return new ActionResult<>(EnumActionResult.PASS, player.getHeldItem(hand));
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(@Nonnull ItemStack stack, @Nullable World world, @Nonnull List<String> tooltip, @Nonnull ITooltipFlag flag) {
        definition$addInformation(stack, world, tooltip, flag);
    }

    @Override
    public boolean canHarvestBlock(@Nonnull IBlockState state, @Nonnull ItemStack stack) {
        // special case vanilla behavior
        if (state.getBlock().getHarvestTool(state) == null) {
            return ToolHelper.isToolEffective(state, getToolClasses(stack), getTotalHarvestLevel(stack));
        }

        return false;
    }

    public static class Builder extends ToolBuilder<ItemGTSword> {

        @Nonnull
        public static ItemGTSword.Builder of(@Nonnull String domain, @Nonnull String id) {
            return new ItemGTSword.Builder(domain, id);
        }

        public Builder(@Nonnull String domain, @Nonnull String id) {
            super(domain, id);
        }

        @Override
        public Supplier<ItemGTSword> supply() {
            return () -> new ItemGTSword(domain, id, tier, toolStats, sound, playSoundOnBlockDestroy, toolClasses, oreDict, secondaryOreDicts, markerItem);
        }
    }
}
