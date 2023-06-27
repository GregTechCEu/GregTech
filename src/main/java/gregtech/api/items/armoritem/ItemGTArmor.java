package gregtech.api.items.armoritem;

import gregtech.api.GregTechAPI;
import gregtech.api.capability.impl.CombinedCapabilityProvider;
import gregtech.api.damagesources.DamageSources;
import gregtech.api.util.input.IKeyPressedListener;
import gregtech.api.util.input.KeyBind;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnumEnchantmentType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;
import net.minecraftforge.common.IRarity;
import net.minecraftforge.common.ISpecialArmor;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class ItemGTArmor extends ItemArmor implements IGTArmor, ISpecialArmor, IKeyPressedListener {

    private final IGTArmorDefinition armorDefinition;

    public ItemGTArmor(String domain, String id, IGTArmorDefinition armorDefinition) {
        super(ArmorMaterial.DIAMOND, 0, armorDefinition.getEquippedSlot());
        this.armorDefinition = armorDefinition;
        setMaxStackSize(1);
        setCreativeTab(GregTechAPI.TAB_GREGTECH_TOOLS);
        setTranslationKey("gt.armor." + id);
        setRegistryName(domain, id);
    }

    public @NotNull IGTArmorDefinition getDefinition() {
        return armorDefinition;
    }

    @Override
    public @NotNull List<IArmorBehavior> getBehaviors() {
        return getDefinition().getBehaviors();
    }

    @Override
    public @NotNull EntityEquipmentSlot getEquipmentSlot() {
        return getDefinition().getEquippedSlot();
    }

    @Override
    public ArmorProperties getProperties(EntityLivingBase player, @NotNull ItemStack armor, DamageSource source, double damage, int slot) {
        // todo this is gonna be a huge mess to unravel
        return new ArmorProperties(0, 0, 0);
    }

    @Override
    public int getArmorDisplay(EntityPlayer player, @NotNull ItemStack armor, int slot) {
        return (int) Math.round(20.0F * getDamageModifier() * getDefinition().getDamageAbsorption(getEquipmentSlot(), null));
    }

    @Override
    public void damageArmor(EntityLivingBase entity, @NotNull ItemStack stack, @Nullable DamageSource source, int damage, int slot) {
        if (getDefinition().canBreakWithDamage()) {
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
        for (IArmorBehavior behavior : getDefinition().getBehaviors()) {
            if (behavior.onArmorTick(world, player, stack)) {
                numEffects++;
            }
        }
        if (numEffects > 0 && !world.isRemote) {
            damageArmor(player, stack, DamageSources.getArmorDurabilityDamage(), numEffects, getEquipmentSlot().getIndex());
        }
    }

    public void onArmorUnequip(@NotNull World world, @NotNull EntityPlayer player, @NotNull ItemStack stack) {
        for (IArmorBehavior behavior : getDefinition().getBehaviors()) {
            if (player instanceof EntityPlayerMP playerMP) {
                for (KeyBind keyBind : behavior.getListenedKeys()) {
                    keyBind.removeListener(playerMP, this);
                }
            }
            behavior.onArmorUnequip(world, player, stack);
        }
    }

    public void onArmorEquip(@NotNull World world, @NotNull EntityPlayer player, @NotNull ItemStack stack) {
        for (IArmorBehavior behavior : getDefinition().getBehaviors()) {
            if (player instanceof EntityPlayerMP playerMP) {
                for (KeyBind keyBind : behavior.getListenedKeys()) {
                    keyBind.registerListener(playerMP, this);
                }
            }
            behavior.onArmorEquip(world, player, stack);
        }
    }

    @Override
    public void onKeyPressed(EntityPlayerMP player, KeyBind keyPressed) {
        for (IArmorBehavior behavior : getDefinition().getBehaviors()) {
            if (behavior.getListenedKeys().contains(keyPressed)) {
                behavior.onKeyPressed(player.getItemStackFromSlot(getEquipmentSlot()), player, keyPressed);
            }
        }
    }

    // todo make sure this works with how we end up doing the builder class
    @Override
    public @Nullable String getArmorTexture(@NotNull ItemStack stack, @NotNull Entity entity, @NotNull EntityEquipmentSlot slot, @NotNull String type) {
        return getDefinition().getArmorTexture();
    }

    @Override
    public @NotNull IRarity getForgeRarity(@NotNull ItemStack stack) {
        return getDefinition().getRarity();
    }

    @Override
    public int getItemEnchantability() {
        return getDefinition().getEnchantability();
    }

    @Override
    public boolean isEnchantable(@NotNull ItemStack stack) {
        return getDefinition().isEnchantable();
    }

    @Override
    public boolean canApplyAtEnchantingTable(@NotNull ItemStack stack, @NotNull Enchantment enchantment) {
        if (enchantment.type == null) return false;

        if (!getDefinition().canBreakWithDamage() && enchantment.type == EnumEnchantmentType.BREAKABLE) {
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
        for (IArmorBehavior behavior : getDefinition().getBehaviors()) {
            behavior.addInformation(stack, world, tooltip, flag);
        }
    }

    @Override
    public final @Nullable ICapabilityProvider initCapabilities(@NotNull ItemStack stack, @Nullable NBTTagCompound tag) {
        List<ICapabilityProvider> providers = getCapabilityProviders(stack, tag);
        if (providers.isEmpty()) return null;
        if (providers.size() == 1) return providers.get(0);
        return new CombinedCapabilityProvider(providers);
    }

    protected @NotNull List<ICapabilityProvider> getCapabilityProviders(@NotNull ItemStack stack, @Nullable NBTTagCompound tag) {
        List<ICapabilityProvider> providers = new ArrayList<>();
        for (IArmorBehavior behavior : getDefinition().getBehaviors()) {
            ICapabilityProvider provider = behavior.createProvider(stack, tag);
            if (provider != null) {
                providers.add(provider);
            }
        }
        return providers;
    }

    protected float getDamageModifier() {
        return switch (getEquipmentSlot()) {
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

    public static class Builder extends ArmorBuilder<ItemGTArmor, Builder> {

        public static @NotNull Builder of(@NotNull String domain, @NotNull String id, @NotNull EntityEquipmentSlot slot) {
            return new Builder(domain, id, slot);
        }

        private Builder(@NotNull String domain, @NotNull String id, @NotNull EntityEquipmentSlot slot) {
            super(domain, id, slot);
        }

        @Override
        public Supplier<ItemGTArmor> supply(IGTArmorDefinition definition) {
            return () -> new ItemGTArmor(domain, id, definition);
        }

        @Override
        public Builder cast(ArmorBuilder<ItemGTArmor, Builder> builder) {
            return (Builder) builder;
        }
    }
}
