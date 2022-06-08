package gregtech.api.items.toolitem;

import com.google.common.collect.Multimap;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.Set;

public class ItemGTSword extends ItemSword implements IGTTool {

    private final String domain, id;

    private final int tier;
    private final IGTToolDefinition toolStats;
    private final Set<String> toolClasses;
    private final SoundEvent sound;
    private final Set<Block> effectiveBlocks;

    protected ItemGTSword(String domain, String id, int tier, IGTToolDefinition toolStats, SoundEvent sound, Set<String> toolClasses, Set<Block> effectiveBlocks) {
        super(ToolMaterial.STONE);
        this.domain = domain;
        this.id = id;
        this.tier = tier;
        this.toolStats = toolStats;
        this.sound = sound;
        this.toolClasses = Collections.unmodifiableSet(toolClasses);
        this.effectiveBlocks = effectiveBlocks;
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

    @Nullable
    @Override
    public String getOreDictName() {
        return null;
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
    public boolean canHarvestBlock(IBlockState blockIn, ItemStack stack) {
        return getToolClasses(stack).stream().anyMatch(s -> blockIn.getBlock().isToolEffective(s, blockIn)) || super.canHarvestBlock(blockIn, stack);
    }

}
