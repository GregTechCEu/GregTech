package gregtech.api.items.armoritem;

import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.util.DamageSource;
import net.minecraftforge.common.IRarity;
import org.jetbrains.annotations.Nullable;

import java.util.List;

// TODO A lot of these do not need to be in the "definition" class, but in the Item class
public interface IGTArmorDefinition {

    EntityEquipmentSlot getEquippedSlot();

    List<IArmorBehavior> getBehaviors();

    /* ArmorProperties */
    double getDamageAbsorption(EntityEquipmentSlot slot, @Nullable DamageSource damageSource);

    List<DamageSource> handledUnblockableSources();

    String getArmorTexture();

    // meaning, does this item ever break or does it just use power or something
    boolean canBreakWithDamage();

    boolean isEnchantable();

    int getEnchantability();

    IRarity getRarity();
}
