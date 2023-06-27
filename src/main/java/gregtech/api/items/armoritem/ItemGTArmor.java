package gregtech.api.items.armoritem;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnumEnchantmentType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;
import net.minecraftforge.common.IRarity;
import net.minecraftforge.common.ISpecialArmor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ItemGTArmor extends ItemArmor implements ISpecialArmor {

    protected final EntityEquipmentSlot equipmentSlot;
    protected final IGTArmorDefinition armorDefinition;

    public ItemGTArmor(IGTArmorDefinition armorDefinition, EntityEquipmentSlot equipmentSlot) {
        super(ArmorMaterial.DIAMOND, 0, equipmentSlot);
        this.armorDefinition = armorDefinition;
        this.equipmentSlot = equipmentSlot;
    }

    @Override
    public ArmorProperties getProperties(EntityLivingBase player, @NotNull ItemStack armor, DamageSource source, double damage, int slot) {
        // todo this is gonna be a huge mess to unravel
        return new ArmorProperties(0, 0, 0);
    }

    @Override
    public int getArmorDisplay(EntityPlayer player, @NotNull ItemStack armor, int slot) {
        return (int) Math.round(20.0F * getDamageModifier() * armorDefinition.getDamageAbsorption(equipmentSlot, null));
    }

    @Override
    public void damageArmor(EntityLivingBase entity, @NotNull ItemStack stack, @Nullable DamageSource source, int damage, int slot) {
        if (armorDefinition.canBreakWithDamage()) {
            // todo need to do actual damage and save to NBT here, as "default" armor can break
            // todo then override in ItemGTElectricArmor can do it as energy instead (or in addition to)
        }
    }

    @Override
    public boolean handleUnblockableDamage(EntityLivingBase entity, @NotNull ItemStack armor, DamageSource source, double damage, int slot) {
        return armorDefinition.handledUnblockableSources().contains(source);
    }

    @Override
    public void onArmorTick(@NotNull World world, @NotNull EntityPlayer player, @NotNull ItemStack stack) {
        int numEffects = 0;
        for (IArmorBehavior behavior : armorDefinition.getBehaviors()) {
            if (behavior.onArmorTick(world, player, stack)) {
                numEffects++;
            }
        }
        if (numEffects > 0 && !world.isRemote) {
            // todo passing a null DamageSource into here may cause problems
            damageArmor(player, stack, null, numEffects, equipmentSlot.getIndex());
        }
    }

    public void onArmorUnequip(@NotNull World world, @NotNull EntityPlayer player, @NotNull ItemStack stack) {
        for (IArmorBehavior behavior : armorDefinition.getBehaviors()) {
            behavior.onArmorUnequip(world, player, stack);
        }
    }

    public void onArmorEquip(@NotNull World world, @NotNull EntityPlayer player, @NotNull ItemStack stack) {
        for (IArmorBehavior behavior : armorDefinition.getBehaviors()) {
            behavior.onArmorEquip(world, player, stack);
        }
    }

    // todo make sure this works with how we end up doing the builder class
    @Override
    public @Nullable String getArmorTexture(@NotNull ItemStack stack, @NotNull Entity entity, @NotNull EntityEquipmentSlot slot, @NotNull String type) {
        return armorDefinition.getArmorTexture();
    }

    @Override
    public @NotNull IRarity getForgeRarity(@NotNull ItemStack stack) {
        return armorDefinition.getRarity();
    }

    @Override
    public int getItemEnchantability() {
        return armorDefinition.getEnchantability();
    }

    @Override
    public boolean isEnchantable(@NotNull ItemStack stack) {
        return armorDefinition.isEnchantable();
    }

    @Override
    public boolean canApplyAtEnchantingTable(@NotNull ItemStack stack, @NotNull Enchantment enchantment) {
        if (enchantment.type == null) return false;

        if (!armorDefinition.canBreakWithDamage() && enchantment.type == EnumEnchantmentType.BREAKABLE) {
            return false;
        }

        // todo this should be good enough, but the below might be needed
        return super.canApplyAtEnchantingTable(stack, enchantment);

        //return switch (equipmentSlot) {
        //    case HEAD -> enchantment.type.canEnchantItem(Items.DIAMOND_HELMET);
        //    case CHEST -> enchantment.type.canEnchantItem(Items.DIAMOND_CHESTPLATE);
        //    case LEGS -> enchantment.type.canEnchantItem(Items.DIAMOND_LEGGINGS);
        //    case FEET -> enchantment.type.canEnchantItem(Items.DIAMOND_BOOTS);
        //    default -> enchantment.isAllowedOnBooks();
        //};
    }

    @Override
    public void addInformation(@NotNull ItemStack stack, @Nullable World world, @NotNull List<String> tooltip, @NotNull ITooltipFlag flag) {
        // todo armor toughness tooltip
        for (IArmorBehavior behavior : armorDefinition.getBehaviors()) {
            behavior.addInformation(stack, world, tooltip, flag);
        }
    }

    protected float getDamageModifier() {
        return switch (equipmentSlot) {
            case HEAD, FEET -> 0.15f;
            case CHEST      -> 0.4f;
            case LEGS       -> 0.3f;
            default         -> 0.0f;
        };
    }

    private static EntityEquipmentSlot getSlotByIndex(int index) {
        return switch (index) {
            case 0  -> EntityEquipmentSlot.FEET;
            case 1  -> EntityEquipmentSlot.LEGS;
            case 2  -> EntityEquipmentSlot.CHEST;
            default -> EntityEquipmentSlot.HEAD;
        };
    }
}
