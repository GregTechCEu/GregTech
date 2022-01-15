package gregtech.api.items.armor;

import com.google.common.base.Preconditions;
import com.google.common.collect.Multimap;
import gregtech.api.items.metaitem.MetaItem;
import gregtech.api.items.metaitem.stats.IEnchantabilityHelper;
import gregtech.api.items.metaitem.stats.IItemComponent;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;
import net.minecraftforge.common.ISpecialArmor;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ArmorMetaItem<T extends ArmorMetaItem<?>.ArmorMetaValueItem> extends MetaItem<T> implements IArmorItem, ISpecialArmor, IEnchantabilityHelper {

    public ArmorMetaItem() {
        super((short) 0);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected T constructMetaValueItem(short metaValue, String unlocalizedName) {
        return (T) new ArmorMetaValueItem(metaValue, unlocalizedName);
    }

    @Nonnull
    private IArmorLogic getArmorLogic(ItemStack itemStack) {
        T metaValueItem = getItem(itemStack);
        return metaValueItem == null ? new DummyArmorLogic() : metaValueItem.getArmorLogic();
    }

    @Nonnull
    @Override
    public Multimap<String, AttributeModifier> getAttributeModifiers(@Nonnull EntityEquipmentSlot slot, @Nonnull ItemStack stack) {
        Multimap<String, AttributeModifier> multimap = super.getAttributeModifiers(slot, stack);
        IArmorLogic armorLogic = getArmorLogic(stack);
        multimap.putAll(armorLogic.getAttributeModifiers(slot, stack));
        return multimap;
    }

    @Override
    public ArmorProperties getProperties(EntityLivingBase player, @Nonnull ItemStack armor, DamageSource source, double damage, int slot) {
        IArmorLogic armorLogic = getArmorLogic(armor);
        if (armorLogic instanceof ISpecialArmorLogic) {
            return ((ISpecialArmorLogic) armorLogic).getProperties(player, armor, source, damage, getSlotByIndex(slot));
        }
        return new ArmorProperties(0, 0, Integer.MAX_VALUE);
    }

    @Override
    public int getArmorDisplay(EntityPlayer player, @Nonnull ItemStack armor, int slot) {
        IArmorLogic armorLogic = getArmorLogic(armor);
        if (armorLogic instanceof ISpecialArmorLogic) {
            return ((ISpecialArmorLogic) armorLogic).getArmorDisplay(player, armor, slot);
        }
        return 0;
    }

    @Override
    public void damageArmor(EntityLivingBase entity, @Nonnull ItemStack stack, DamageSource source, int damage, int slot) {
        IArmorLogic armorLogic = getArmorLogic(stack);
        armorLogic.damageArmor(entity, stack, source, damage, getSlotByIndex(slot));
    }

    @Override
    public boolean handleUnblockableDamage(EntityLivingBase entity, @Nonnull ItemStack armor, DamageSource source, double damage, int slot) {
        IArmorLogic armorLogic = getArmorLogic(armor);
        if (armorLogic instanceof ISpecialArmorLogic) {
            return ((ISpecialArmorLogic) armorLogic).handleUnblockableDamage(entity, armor, source, damage, getSlotByIndex(slot));
        }
        return false;
    }

    @Override
    public void onArmorTick(@Nonnull World world, @Nonnull EntityPlayer player, @Nonnull ItemStack itemStack) {
        IArmorLogic armorLogic = getArmorLogic(itemStack);
        armorLogic.onArmorTick(world, player, itemStack);
    }

    @Override
    public boolean isValidArmor(@Nonnull ItemStack stack, @Nonnull EntityEquipmentSlot armorType, @Nonnull Entity entity) {
        IArmorLogic armorLogic = getArmorLogic(stack);
        return super.isValidArmor(stack, armorType, entity) &&
                armorLogic.isValidArmor(stack, entity, armorType);
    }

    @Nullable
    @Override
    public EntityEquipmentSlot getEquipmentSlot(@Nonnull ItemStack stack) {
        IArmorLogic armorLogic = getArmorLogic(stack);
        return armorLogic.getEquipmentSlot(stack);
    }

    @Nullable
    @Override
    public String getArmorTexture(@Nonnull ItemStack stack, @Nonnull Entity entity, @Nonnull EntityEquipmentSlot slot, @Nonnull String type) {
        IArmorLogic armorLogic = getArmorLogic(stack);
        return armorLogic.getArmorTexture(stack, entity, slot, type);
    }

    @Nullable
    @Override
    @SideOnly(Side.CLIENT)
    public ModelBiped getArmorModel(@Nonnull EntityLivingBase entityLiving, @Nonnull ItemStack itemStack, @Nonnull EntityEquipmentSlot armorSlot, @Nonnull ModelBiped _default) {
        IArmorLogic armorLogic = getArmorLogic(itemStack);
        return armorLogic.getArmorModel(entityLiving, itemStack, armorSlot, _default);
    }

    @Override
    public int getArmorLayersAmount(ItemStack itemStack) {
        IArmorLogic armorLogic = getArmorLogic(itemStack);
        return armorLogic.getArmorLayersAmount(itemStack);
    }

    @Override
    public int getArmorLayerColor(ItemStack itemStack, int layerIndex) {
        IArmorLogic armorLogic = getArmorLogic(itemStack);
        return armorLogic.getArmorLayerColor(itemStack, layerIndex);
    }

    @Override
    public void renderHelmetOverlay(@Nonnull ItemStack stack, @Nonnull EntityPlayer player, @Nonnull ScaledResolution resolution, float partialTicks) {
        IArmorLogic armorLogic = getArmorLogic(stack);
        armorLogic.renderHelmetOverlay(stack, player, resolution, partialTicks);
    }

    private static EntityEquipmentSlot getSlotByIndex(int index) {
        switch (index) {
            case 0:
                return EntityEquipmentSlot.FEET;
            case 1:
                return EntityEquipmentSlot.LEGS;
            case 2:
                return EntityEquipmentSlot.CHEST;
            default:
                return EntityEquipmentSlot.HEAD;
        }
    }

    public class ArmorMetaValueItem extends MetaValueItem {

        private IArmorLogic armorLogic = new DummyArmorLogic();

        protected ArmorMetaValueItem(int metaValue, String unlocalizedName) {
            super(metaValue, unlocalizedName);
            setMaxStackSize(1);
        }

        @Nonnull
        public IArmorLogic getArmorLogic() {
            return armorLogic;
        }

        public ArmorMetaValueItem setArmorLogic(IArmorLogic armorLogic) {
            Preconditions.checkNotNull(armorLogic, "Cannot set ArmorLogic to null");
            this.armorLogic = armorLogic;
            this.armorLogic.addToolComponents(this);
            return this;
        }


        @Override
        public ArmorMetaValueItem addComponents(IItemComponent... stats) {
            super.addComponents(stats);
            return this;
        }

        @Override
        public ArmorMetaValueItem setModelAmount(int modelAmount) {
            return (ArmorMetaValueItem) super.setModelAmount(modelAmount);
        }

        @Override
        public ArmorMetaValueItem setRarity(EnumRarity rarity) {
            return (ArmorMetaValueItem) super.setRarity(rarity);
        }
    }

    @Override
    public boolean isEnchantable(@Nonnull ItemStack stack) {
        return true;
    }

    @Override
    public int getItemEnchantability(@Nonnull ItemStack stack) {
        return 50;
    }

    @Override
    public boolean canApplyAtEnchantingTable(@Nonnull ItemStack stack, @Nonnull Enchantment enchantment) {
        EntityEquipmentSlot slot = this.getEquipmentSlot(stack);
        if(slot == null || enchantment.type == null) {
            return false;
        }

        switch (slot) {
            case HEAD:
                return enchantment.type.canEnchantItem(Items.DIAMOND_HELMET);
            case CHEST:
                return enchantment.type.canEnchantItem(Items.DIAMOND_CHESTPLATE);
            case LEGS:
                return enchantment.type.canEnchantItem(Items.DIAMOND_LEGGINGS);
            case FEET:
                return enchantment.type.canEnchantItem(Items.DIAMOND_BOOTS);
            default:
                return enchantment.isAllowedOnBooks();
        }
    }

}
