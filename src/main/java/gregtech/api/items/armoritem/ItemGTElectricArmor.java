package gregtech.api.items.armoritem;

import gregtech.api.capability.GregtechCapabilities;
import gregtech.api.capability.IElectricItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class ItemGTElectricArmor extends ItemGTArmor {

    protected final int energyPerUse; // todo better name

    public ItemGTElectricArmor(IGTArmorDefinition armorDefinition, EntityEquipmentSlot equipmentSlot) {
        // todo
        super(armorDefinition, equipmentSlot);
        this.energyPerUse = 0;
    }

    protected IElectricItem getElectricItem(ItemStack armor) {
        return armor.getCapability(GregtechCapabilities.CAPABILITY_ELECTRIC_ITEM, null);
    }

    @Override
    public int getArmorDisplay(EntityPlayer player, @NotNull ItemStack armor, int slot) {
        IElectricItem electricItem = getElectricItem(armor);
        if (electricItem == null) return 0;
        if (electricItem.getCharge() < energyPerUse) {
            return (int) Math.round(4.0f * getDamageModifier() * armorDefinition.getDamageAbsorption(equipmentSlot, null));
        }
        return super.getArmorDisplay(player, armor, slot);
    }
}
