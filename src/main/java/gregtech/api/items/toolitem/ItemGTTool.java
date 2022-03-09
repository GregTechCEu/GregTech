package gregtech.api.items.toolitem;

import com.google.common.collect.Multimap;
import gregtech.api.unification.material.Materials;
import gregtech.api.util.LocalizationUtils;
import net.minecraft.block.Block;
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

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * GT-styled ItemTool (generic tool item).
 *
 * Use this class if your tool isn't specialized (e.g. {@link ItemGTSword})
 */
public class ItemGTTool extends ItemTool implements IGTTool {

    protected final String domain, id;

    protected final int tier;
    protected final IGTToolDefinition toolStats;
    protected final Set<String> toolClasses;
    protected final Set<String> oreDicts;
    protected final SoundEvent sound;
    protected final Set<Block> effectiveBlocks;

    protected ItemGTTool(String domain, String id, int tier, IGTToolDefinition toolStats, SoundEvent sound, Set<String> toolClasses, Set<String> oreDicts, Set<Block> effectiveBlocks) {
        super(0F, 0F, ToolMaterial.STONE, effectiveBlocks);
        this.domain = domain;
        this.id = id;
        this.tier = tier;
        this.toolStats = toolStats;
        this.sound = sound;
        this.toolClasses = Collections.unmodifiableSet(toolClasses);
        this.oreDicts = Collections.unmodifiableSet(oreDicts);
        this.effectiveBlocks = effectiveBlocks;
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
    public Set<Block> getEffectiveBlocks() {
        return effectiveBlocks;
    }

    @Override
    public Set<String> getOreDictNames() {
        return oreDicts;
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

    @Override
    public String getItemStackDisplayName(ItemStack stack) {
        return LocalizationUtils.format(getTranslationKey(), getToolMaterial(stack).getLocalizedName());
    }

    @Override
    public float getDestroySpeed(ItemStack stack, IBlockState state) {
        return definition$getDestroySpeed(stack, state);
    }

    @Override
    public boolean hitEntity(ItemStack stack, EntityLivingBase target, EntityLivingBase attacker) {
        return definition$hitEntity(stack, target, attacker);
    }

    @Override
    public boolean onBlockStartBreak(ItemStack itemstack, BlockPos pos, EntityPlayer player) {
        return definition$onBlockStartBreak(itemstack, pos, player);
    }

    @Override
    public boolean onBlockDestroyed(ItemStack stack, World worldIn, IBlockState state, BlockPos pos, EntityLivingBase entityLiving) {
        return definition$onBlockDestroyed(stack, worldIn, state, pos, entityLiving);
    }

    @Override
    public int getItemEnchantability(ItemStack stack) {
        return getTotalEnchantability(stack);
    }

    @Override
    public boolean getIsRepairable(ItemStack toRepair, ItemStack repair) {
        return definition$getIsRepairable(toRepair, repair);
    }

    @Override
    public Multimap<String, AttributeModifier> getAttributeModifiers(EntityEquipmentSlot slot, ItemStack stack) {
        return definition$getAttributeModifiers(slot, stack);
    }

    @Override
    public int getHarvestLevel(ItemStack stack, String toolClass, @Nullable EntityPlayer player, @Nullable IBlockState blockState) {
        return definition$getHarvestLevel(stack, toolClass, player, blockState);
    }

    @Override
    public Set<String> getToolClasses(ItemStack stack) {
        return toolClasses;
    }

    @Override
    public boolean canDisableShield(ItemStack stack, ItemStack shield, EntityLivingBase entity, EntityLivingBase attacker) {
        return definition$canDisableShield(stack, shield, entity, attacker);
    }

    @Override
    public boolean doesSneakBypassUse(ItemStack stack, IBlockAccess world, BlockPos pos, EntityPlayer player) {
        return definition$doesSneakBypassUse(stack, world, pos, player);
    }

    @Override
    public boolean hasContainerItem(ItemStack stack) {
        return definition$hasContainerItem(stack);
    }

    @Override
    public ItemStack getContainerItem(ItemStack stack) {
        return definition$getContainerItem(stack);
    }

    @Override
    public boolean isDamaged(ItemStack stack) {
        return definition$isDamaged(stack);
    }

    @Override
    public int getDamage(ItemStack stack) {
        return definition$getDamage(stack);
    }

    @Override
    public int getMaxDamage(ItemStack stack) {
        return definition$getMaxDamage(stack);
    }

    @Override
    public void setDamage(ItemStack stack, int damage) {
        definition$setDamage(stack, damage);
    }

    @Nullable
    @Override
    public ICapabilityProvider initCapabilities(ItemStack stack, @Nullable NBTTagCompound nbt) {
        return definition$initCapabilities(stack, nbt);
    }

    @Override
    public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        return definition$onItemUse(player, world, pos, hand, facing, hitX, hitY, hitZ);
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
        return definition$onItemRightClick(world, player, hand);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World world, List<String> tooltip, ITooltipFlag flag) {
        definition$addInformation(stack, world, tooltip, flag);
    }

    public static class Builder extends ToolBuilder<ItemGTTool> {

        public static Builder of(String domain, String id) {
            return new Builder(domain, id);
        }

        public Builder(String domain, String id) {
            super(domain, id);
        }

        @Override
        public ItemGTTool build() {
            return new ItemGTTool(domain, id, tier, toolStats, sound, toolClasses, oreDicts, effectiveBlocks);
        }

    }

}
