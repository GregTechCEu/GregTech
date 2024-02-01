package gregtech.api.items.materialitem;

import gregtech.api.items.metaitem.MetaItem;
import gregtech.api.items.metaitem.stats.IItemDurabilityManager;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.material.properties.PropertyKey;
import gregtech.api.unification.material.properties.RotorProperty;
import gregtech.api.unification.material.registry.MaterialRegistry;
import gregtech.api.unification.ore.OrePrefix;

import gregtech.api.util.GTUtility;
import gregtech.api.util.GradientUtil;

import net.minecraft.item.ItemStack;

import net.minecraft.nbt.NBTTagCompound;

import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.Color;

public class MetaTurbineItem extends MetaPrefixItem {

    private static final String NBT_ROTOR_DAMAGE = "RotorDamage";

    public MetaTurbineItem(@NotNull MaterialRegistry registry, @NotNull OrePrefix orePrefix) {
        super(registry, orePrefix);
    }

    @Override
    protected void attachComponents(@NotNull MetaItem<?>.MetaValueItem mvi) {
        super.attachComponents(mvi);
        mvi.addComponents(new DurabilityManager());
    }

    /**
     * @param stack        The Rotor stack, will be mutated if destroyed by this damage.
     * @param damageToDeal Amount of damage to deal to the Rotor.
     */
    public void damageTurbine(@NotNull ItemStack stack, int damageToDeal) {
        NBTTagCompound tag = GTUtility.getOrCreateNbtCompound(stack);
        int damage;
        if (tag.hasKey(NBT_ROTOR_DAMAGE)) {
            damage = tag.getInteger(NBT_ROTOR_DAMAGE);
        } else {
            damage = getMaxDamage(stack);
        }
        damage -= damageToDeal;
        if (damage < 0) { // allow zero, MC behavior
            stack.shrink(1);
        }
        tag.setInteger(NBT_ROTOR_DAMAGE, damage);
    }

    public int getTurbineMaxDurability(@NotNull ItemStack stack) {
        Material m = getMaterial(stack);
        if (m == null) return -1;

        RotorProperty p = m.getProperty(PropertyKey.ROTOR);
        if (p == null) return -1;

        //return p.getMaxDurability(); todo
        return 100;
    }

    public int getTurbineDurability(@NotNull ItemStack stack) {
        NBTTagCompound tag = GTUtility.getOrCreateNbtCompound(stack);
        int damage;
        if (tag.hasKey(NBT_ROTOR_DAMAGE)) {
            damage = tag.getInteger(NBT_ROTOR_DAMAGE);
        } else {
            damage = getMaxDamage(stack);
            tag.setInteger(NBT_ROTOR_DAMAGE, damage);
        }
        return damage;
    }

    private static class DurabilityManager implements IItemDurabilityManager {

        private Pair<Color, Color> durabilityColors;

        @Override
        public double getDurabilityForDisplay(@NotNull ItemStack stack) {
            if (stack.getItem() instanceof MetaTurbineItem turbine) {
                int maxDamage = turbine.getTurbineMaxDurability(stack);
                int damage = turbine.getTurbineDurability(stack);
                return (double) (maxDamage - damage) / (double) maxDamage;
            }
            return -1.0;
        }

        @Override
        public @Nullable Pair<Color, Color> getDurabilityColorsForDisplay(@NotNull ItemStack stack) {
            if (durabilityColors == null) {
                Material m = MetaPrefixItem.tryGetMaterial(stack);
                if (m == null) return null;
                durabilityColors = GradientUtil.getGradient(m.getMaterialRGB(), 10);
            }
            return durabilityColors;
        }
    }
}
