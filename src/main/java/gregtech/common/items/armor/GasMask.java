package gregtech.common.items.armor;

import net.minecraft.entity.Entity;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;

public class GasMask extends NightvisionGoggles {

    public GasMask(int energyPerUse, long capacity, int voltageTier, EntityEquipmentSlot slot) {
        super(energyPerUse, capacity, voltageTier, slot);
    }

    @Override
    public String getArmorTexture(ItemStack stack, Entity entity, EntityEquipmentSlot slot, String type) {
        return "gregtech:textures/armor/gas_mask.png";
    }
}
