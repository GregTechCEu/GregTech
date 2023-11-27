package gregtech.api.damagesources;

import gregtech.api.items.toolitem.IGTTool;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EntityDamageSource;

import org.jetbrains.annotations.Nullable;

public class DamageSources {

    private static final DamageSource EXPLOSION = new DamageSource("explosion").setExplosion();
    private static final DamageSource HEAT = new DamageSource("heat").setFireDamage();
    private static final DamageSource FROST = new DamageSource("frost");
    private static final DamageSource CHEMICAL = new DamageSource("chemical").setDamageBypassesArmor();
    private static final DamageSource ELECTRIC = new DamageSource("electric");
    private static final DamageSource RADIATION = new DamageSource("radiation").setDamageBypassesArmor();
    private static final DamageSource TURBINE = new DamageSource("turbine");

    public static DamageSource getExplodingDamage() {
        return EXPLOSION;
    }

    public static DamageSource getHeatDamage() {
        return HEAT;
    }

    public static DamageSource getFrostDamage() {
        return FROST;
    }

    public static DamageSource getChemicalDamage() {
        return CHEMICAL;
    }

    public static DamageSource getElectricDamage() {
        return ELECTRIC;
    }

    public static DamageSource getRadioactiveDamage() {
        return RADIATION;
    }

    public static DamageSource getTurbineDamage() {
        return TURBINE;
    }

    // accessed via ASM
    @SuppressWarnings("unused")
    public static DamageSource getPlayerDamage(@Nullable EntityPlayer source) {
        ItemStack stack = source != null ? source.getHeldItemMainhand() : ItemStack.EMPTY;
        if (!stack.isEmpty() && stack.getItem() instanceof IGTTool) {
            IGTTool tool = (IGTTool) stack.getItem();
            return new DamageSourceTool("player", source, String.format("death.attack.%s", tool.getToolId()));
        }
        return new EntityDamageSource("player", source);
    }

    // accessed via ASM
    @SuppressWarnings("unused")
    public static DamageSource getMobDamage(@Nullable EntityLivingBase source) {
        ItemStack stack = source != null ? source.getItemStackFromSlot(EntityEquipmentSlot.MAINHAND) : ItemStack.EMPTY;
        if (!stack.isEmpty() && stack.getItem() instanceof IGTTool) {
            IGTTool tool = (IGTTool) stack.getItem();
            return new DamageSourceTool("mob", source, String.format("death.attack.%s", tool.getToolId()));
        }
        return new EntityDamageSource("mob", source);
    }
}
