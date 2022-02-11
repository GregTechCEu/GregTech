package gregtech.api.items.toolitem;

import com.google.common.collect.Multimap;
import gregtech.api.util.LocalizationUtils;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemTool;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.Set;

/**
 * GT-styled ItemTool (generic tool item).
 *
 * Use this class if your tool isn't specialized (e.g. {@link GTSwordItem})
 */
public class GTToolItem extends ItemTool implements GTToolDefinition {

    protected final String domain, id;

    protected final int tier;
    protected final IToolStats toolStats;
    protected final Set<String> toolClasses;
    protected final SoundEvent sound;
    protected final Set<Block> effectiveBlocks;

    protected GTToolItem(String domain, String id, int tier, IToolStats toolStats, SoundEvent sound, Set<String> toolClasses, Set<Block> effectiveBlocks) {
        super(0F, 0F, ToolMaterial.STONE, effectiveBlocks);
        this.domain = domain;
        this.id = id;
        this.tier = tier;
        this.toolStats = toolStats;
        this.sound = sound;
        this.toolClasses = Collections.unmodifiableSet(toolClasses);
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
    public IToolStats getToolStats() {
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

    public static class Builder extends ToolBuilder<GTToolItem> {

        public static Builder of(String domain, String id) {
            return new Builder(domain, id);
        }

        public Builder(String domain, String id) {
            super(domain, id);
        }

        @Override
        public GTToolItem build() {
            return new GTToolItem(domain, id, tier, toolStats, sound, toolClasses, effectiveBlocks);
        }

    }

}
