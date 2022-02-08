package gregtech.api.items.toolitem;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import gregtech.api.GregTechAPI;
import gregtech.api.unification.OreDictUnifier;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.material.Materials;
import gregtech.api.unification.material.properties.DustProperty;
import gregtech.api.unification.material.properties.PropertyKey;
import gregtech.api.unification.material.properties.ToolProperty;
import gregtech.api.unification.stack.MaterialStack;
import gregtech.api.util.GTLog;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.Set;

import static gregtech.api.items.armor.IArmorLogic.*;

/**
 * Backing of every variation of a GT Tool
 */
public interface GTToolDefinition {

    String getDomain();

    String getId();

    IToolStats getToolStats();

    Set<String> getToolClasses();

    @Nullable
    SoundEvent getSound();

    Set<Block> getEffectiveBlocks();

    default Item get() {
        return (Item) this;
    }

    default NBTTagCompound getToolTag(ItemStack stack) {
        return stack.getOrCreateSubCompound("GT.Tools");
    }

    default Material getToolMaterial(ItemStack stack) {
        String string = getToolTag(stack).getString("Material");
        Material material = GregTechAPI.MaterialRegistry.get(string);
        if (material == null) {
            GTLog.logger.error("Attempt to get {} as a tool material, but material does not exist. Using Neutronium instead.", string);
            material = Materials.Neutronium;
        }
        return material;
    }

    default ToolProperty getToolProperty(ItemStack stack) {
        Material material = getToolMaterial(stack);
        ToolProperty property = material.getProperty(PropertyKey.TOOL);
        if (property == null) {
            GTLog.logger.error("Tool property for {} does not exist. Using Neutronium's tool property instead.", material.getId());
            property = Materials.Neutronium.getProperty(PropertyKey.TOOL);
        }
        return property;
    }

    default DustProperty getDustProperty(ItemStack stack) {
        Material material = getToolMaterial(stack);
        DustProperty property = material.getProperty(PropertyKey.DUST);
        if (property == null) {
            GTLog.logger.error("Dust property for {} does not exist. Using Neutronium's dust property instead.", material.getId());
            property = Materials.Neutronium.getProperty(PropertyKey.DUST);
        }
        return property;
    }

    default float getMaterialToolSpeed(ItemStack stack) {
        return getToolProperty(stack).getToolSpeed();
    }

    default float getMaterialAttackDamage(ItemStack stack) {
        return getToolProperty(stack).getToolAttackDamage();
    }

    default int getMaterialDurability(ItemStack stack) {
        return getToolProperty(stack).getToolDurability();
    }

    default int getMaterialEnchantability(ItemStack stack) {
        return getToolProperty(stack).getToolEnchantability();
    }

    default int getMaterialHarvestLevel(ItemStack stack) {
        return getDustProperty(stack).getHarvestLevel();
    }

    default Object2IntMap<Enchantment> getMaterialEnchantments(ItemStack stack) {
        return getToolProperty(stack).getEnchantments();
    }

    default float getTotalToolSpeed(ItemStack stack) {
        NBTTagCompound toolTag = getToolTag(stack);
        if (toolTag.hasKey("ToolSpeed", Constants.NBT.TAG_FLOAT)) {
            return toolTag.getFloat("ToolSpeed");
        }
        float toolSpeed = getMaterialToolSpeed(stack) + getToolStats().getBaseEfficiency(stack);
        toolTag.setFloat("ToolSpeed", toolSpeed);
        return toolSpeed;
    }

    default float getTotalAttackDamage(ItemStack stack) {
        NBTTagCompound toolTag = getToolTag(stack);
        if (toolTag.hasKey("AttackDamage", Constants.NBT.TAG_FLOAT)) {
            return toolTag.getFloat("AttackDamage");
        }
        float attackDamage = getMaterialAttackDamage(stack) + getToolStats().getBaseDamage(stack);
        toolTag.setFloat("AttackDamage", attackDamage);
        return attackDamage;
    }

    default int getTotalMaxDurability(ItemStack stack) {
        NBTTagCompound toolTag = getToolTag(stack);
        if (toolTag.hasKey("Durability", Constants.NBT.TAG_INT)) {
            return toolTag.getInteger("Durability");
        }
        int durability = getToolProperty(stack).getToolDurability();
        toolTag.setInteger("Durability", durability);
        return durability;
    }

    default int getTotalEnchantability(ItemStack stack) {
        NBTTagCompound toolTag = getToolTag(stack);
        if (toolTag.hasKey("Enchantability", Constants.NBT.TAG_INT)) {
            return toolTag.getInteger("Enchantability");
        }
        int enchantability = getMaterialEnchantability(stack);
        toolTag.setInteger("Enchantability", enchantability);
        return enchantability;
    }

    default int getTotalHarvestLevel(ItemStack stack) {
        NBTTagCompound toolTag = getToolTag(stack);
        if (toolTag.hasKey("HarvestLevel", Constants.NBT.TAG_INT)) {
            return toolTag.getInteger("HarvestLevel");
        }
        int harvestLevel = getMaterialHarvestLevel(stack) + getToolStats().getBaseQuality(stack);
        toolTag.setInteger("HarvestLevel", harvestLevel);
        return harvestLevel;
    }

    @SideOnly(Side.CLIENT)
    default int getColorIndex(ItemStack stack, int tintIndex) {
        return tintIndex % 2 == 1 ? getToolMaterial(stack).getMaterialRGB() : 0xFFFFFF;
    }

    // Item.class methods
    default float definition$getDestroySpeed(ItemStack stack, IBlockState state) {
        for (String type : get().getToolClasses(stack)) {
            if (state.getBlock().isToolEffective(type, state)) {
                return getTotalToolSpeed(stack);
            }
        }
        return getEffectiveBlocks().contains(state.getBlock()) ? getTotalToolSpeed(stack) : 1.0F;
    }

    default boolean definition$hitEntity(ItemStack stack, EntityLivingBase target, EntityLivingBase attacker) {
        stack.damageItem(getToolStats().getToolDamagePerEntityAttack(stack), attacker);
        return true;
    }

    default boolean definition$onBlockDestroyed(ItemStack stack, World worldIn, IBlockState state, BlockPos pos, EntityLivingBase entityLiving) {
        if (!worldIn.isRemote && (double) state.getBlockHardness(worldIn, pos) != 0.0D) {
            stack.damageItem(getToolStats().getToolDamagePerBlockBreak(stack), entityLiving);
        }
        return true;
    }

    default boolean definition$getIsRepairable(ItemStack toRepair, ItemStack repair) {
        MaterialStack repairMaterialStack = OreDictUnifier.getMaterial(repair);
        return repairMaterialStack.material == getToolMaterial(toRepair);
    }

    default Multimap<String, AttributeModifier> definition$getAttributeModifiers(EntityEquipmentSlot equipmentSlot, ItemStack stack) {
        Multimap<String, AttributeModifier> multimap = HashMultimap.create();
        if (equipmentSlot == EntityEquipmentSlot.MAINHAND) {
            multimap.put(SharedMonsterAttributes.ATTACK_DAMAGE.getName(), new AttributeModifier(ATTACK_DAMAGE_MODIFIER, "Tool modifier", getTotalAttackDamage(stack), 0));
            multimap.put(SharedMonsterAttributes.ATTACK_SPEED.getName(), new AttributeModifier(ATTACK_SPEED_MODIFIER, "Tool modifier", getToolStats().getAttackSpeed(stack), 0));
        }
        return multimap;
    }

    default int getHarvestLevel(ItemStack stack, String toolClass, @javax.annotation.Nullable net.minecraft.entity.player.EntityPlayer player, @javax.annotation.Nullable IBlockState blockState) {
        return get().getToolClasses(stack).contains(toolClass) ? getTotalHarvestLevel(stack) : -1;
    }

}
