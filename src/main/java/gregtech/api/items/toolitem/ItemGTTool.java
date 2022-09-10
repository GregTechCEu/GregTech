package gregtech.api.items.toolitem;

import com.google.common.collect.Multimap;
import gregtech.api.unification.material.Materials;
import gregtech.api.util.LocalizationUtils;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemTool;
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

/**
 * GT-styled ItemTool (generic tool item).
 * <p>
 * Use this class if your tool isn't specialized (e.g. {@link ItemGTSword})
 */
public class ItemGTTool extends ItemTool implements IGTTool {

    protected final String domain, id;

    protected final int tier;
    protected final IGTToolDefinition toolStats;
    protected final Set<String> toolClasses;
    protected final String oreDict;
    protected final SoundEvent sound;

    protected ItemGTTool(String domain, String id, int tier, IGTToolDefinition toolStats, SoundEvent sound, Set<String> toolClasses, String oreDict) {
        super(0F, 0F, ToolMaterial.STONE, Collections.emptySet());
        this.domain = domain;
        this.id = id;
        this.tier = tier;
        this.toolStats = toolStats;
        this.sound = sound;
        this.toolClasses = Collections.unmodifiableSet(toolClasses);
        this.oreDict = oreDict;
        setMaxStackSize(1);
        setCreativeTab(CreativeTabs.TOOLS);
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
    public String getOreDictName() {
        return oreDict;
    }

    @Override
    public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> items) {
        if (this.isInCreativeTab(tab)) {
            if (isElectric()) {
                items.add(get(Materials.Neutronium, 50000L)); // TODO: change
            } else {
                items.add(get(Materials.Neutronium));
            }
        }
    }

    @Nonnull
    @Override
    public String getItemStackDisplayName(@Nonnull ItemStack stack) {
        return LocalizationUtils.format(getTranslationKey(), getToolMaterial(stack).getLocalizedName());
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
    public int getItemEnchantability(@Nonnull ItemStack stack) {
        return getTotalEnchantability(stack);
    }

    @Override
    public boolean getIsRepairable(@Nonnull ItemStack toRepair, @Nonnull ItemStack repair) {
        return definition$getIsRepairable(toRepair, repair);
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
        return definition$onItemRightClick(world, player, hand);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(@Nonnull ItemStack stack, @Nullable World world, @Nonnull List<String> tooltip, @Nonnull ITooltipFlag flag) {
        definition$addInformation(stack, world, tooltip, flag);
    }

    @Override
    public boolean canHarvestBlock(@Nonnull IBlockState state, @Nonnull ItemStack stack) {
        Material material = state.getMaterial();
        return super.canHarvestBlock(state, stack) || (toolClasses.contains(ToolClasses.PICKAXE) && material == Material.ANVIL);
    }

    public static class Builder extends ToolBuilder<ItemGTTool> {

        @Nonnull
        public static Builder of(@Nonnull String domain, @Nonnull String id) {
            return new Builder(domain, id);
        }

        public Builder(@Nonnull String domain, @Nonnull String id) {
            super(domain, id);
        }

        @Override
        public Supplier<ItemGTTool> supply() {
            return () -> new ItemGTTool(domain, id, tier, toolStats, sound, toolClasses, oreDict);
        }
    }
}
