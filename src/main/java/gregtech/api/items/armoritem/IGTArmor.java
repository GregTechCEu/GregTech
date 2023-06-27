package gregtech.api.items.armoritem;

import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface IGTArmor {

    @NotNull IGTArmorDefinition getDefinition();

    @NotNull List<IArmorBehavior> getBehaviors();

    @NotNull EntityEquipmentSlot getEquipmentSlot();

    default @NotNull Item get() {
        return (Item) this;
    }
}
