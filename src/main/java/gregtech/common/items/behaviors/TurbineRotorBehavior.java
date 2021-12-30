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

public class TurbineRotorBehavior extends AbstractMaterialPartBehavior implements IItemMaxStackSizeProvider {

    //TODO rework rotor stats once material stats are also reworked
    @Override
    public int getPartMaxDurability(ItemStack itemStack) {
        Material material = getPartMaterial(itemStack);
        ToolProperty property = material.getProperty(PropertyKey.TOOL);
        return property == null ? -1 : 800 * (int) Math.pow(property.getToolDurability(), 0.65);
    }

    public int getRotorEfficiency(ItemStack stack) {
        Material material = getPartMaterial(stack);
        ToolProperty property = material.getProperty(PropertyKey.TOOL);
        return property == null ? -1 : ((int) ((60 + property.getToolSpeed() * 8)) / 5 * 5);
    }

    public int getRotorDurabilityPercent(ItemStack itemStack) {
        return 100 - 100 * getPartDamage(itemStack) / getPartMaxDurability(itemStack);
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
        return property == null ? -1 : (int) (40 + property.getToolAttackDamage() * 30);
    }

    @Override
    public int getMaxStackSize(ItemStack itemStack, int defaultValue) {
        return 1;
    }

    @Override
    public void addInformation(ItemStack stack, List<String> lines) {
        super.addInformation(stack, lines);
        lines.add(I18n.format("metaitem.tool.tooltip.rotor.efficiency", getRotorEfficiency(stack)));
        lines.add(I18n.format("metaitem.tool.tooltip.rotor.power", getRotorPower(stack)));
    }

    @Nullable
    public static TurbineRotorBehavior getInstanceFor(@Nonnull ItemStack itemStack) {
        if (!(itemStack.getItem() instanceof MetaItem))
            return null;

        MetaItem<?>.MetaValueItem valueItem = ((MetaItem<?>) itemStack.getItem()).getItem(itemStack);
        if (valueItem == null)
            return null;

        IItemDurabilityManager durabilityManager = valueItem.getDurabilityManager();
        if (!(durabilityManager instanceof TurbineRotorBehavior))
            return null;

        return (TurbineRotorBehavior) durabilityManager;
    }
}
