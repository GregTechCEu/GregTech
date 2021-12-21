package gregtech.common.items.behaviors;

import gregtech.api.items.metaitem.MetaItem;
import gregtech.api.items.metaitem.stats.IItemDurabilityManager;
import gregtech.api.items.metaitem.stats.IItemMaxStackSizeProvider;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.material.properties.PropertyKey;
import gregtech.api.unification.material.properties.ToolProperty;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public class TurbineRotorBehavior2 extends AbstractMaterialPartBehavior implements IItemMaxStackSizeProvider {

    private static final int TOOL_DURABILITY_MULTIPLIER = 100;

    @Override
    public int getPartMaxDurability(ItemStack itemStack) {
        Material material = getPartMaterial(itemStack);
        ToolProperty property = material.getProperty(PropertyKey.TOOL);
        return property != null ? property.getToolDurability() * TOOL_DURABILITY_MULTIPLIER : 1;
    }

    public int getRotorEfficiency(ItemStack stack) {
        Material material = getPartMaterial(stack);
        ToolProperty property = material.getProperty(PropertyKey.TOOL);
        return property == null ? -1 : (int) (10000 + property.getToolSpeed() * 1000);
    }

    public void applyRotorDamage(ItemStack itemStack, int damageApplied) {
        int rotorDurability = getPartMaxDurability(itemStack);
        int resultDamage = getPartDamage(itemStack) + damageApplied;
        if (resultDamage >= rotorDurability) {
            itemStack.shrink(1);
        } else {
            setPartDamage(itemStack, resultDamage);
        }
    }

    public int getRotorPower(ItemStack stack) {
        Material material = getPartMaterial(stack);
        ToolProperty property = material.getProperty(PropertyKey.TOOL);
        return property == null ? -1 : (int) (10000 + property.getToolSpeed() * 100);
    }

    @Override
    public int getMaxStackSize(ItemStack itemStack, int defaultValue) {
        return 1;
    }

    @Override
    public void addInformation(ItemStack stack, List<String> lines) {
        super.addInformation(stack, lines);
        lines.add(I18n.format("metaitem.tool.tooltip.rotor.efficiency", getRotorEfficiency(stack) / 100));
        lines.add(I18n.format("metaitem.tool.tooltip.rotor.power", getRotorPower(stack) / 100));
    }

    @Nullable
    public static TurbineRotorBehavior2 getInstanceFor(@Nonnull ItemStack itemStack) {
        if (!(itemStack.getItem() instanceof MetaItem))
            return null;

        MetaItem<?>.MetaValueItem valueItem = ((MetaItem<?>) itemStack.getItem()).getItem(itemStack);
        if (valueItem == null)
            return null;

        IItemDurabilityManager durabilityManager = valueItem.getDurabilityManager();
        if (!(durabilityManager instanceof TurbineRotorBehavior2))
            return null;

        return (TurbineRotorBehavior2) durabilityManager;
    }

}
