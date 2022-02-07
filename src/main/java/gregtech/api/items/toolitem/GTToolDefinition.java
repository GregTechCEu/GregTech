package gregtech.api.items.toolitem;

import gregtech.api.GregTechAPI;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.material.Materials;
import gregtech.api.unification.material.properties.PropertyKey;
import gregtech.api.unification.material.properties.ToolProperty;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Backing of every variation of a GT Tool
 */
public interface GTToolDefinition {

    IToolStats getToolStats();

    SoundEvent getSound();

    default NBTTagCompound getToolTag(ItemStack stack) {
        return stack.getOrCreateSubCompound("GT.Tools");
    }

    default Material getToolMaterial(ItemStack stack) {
        String string = getToolTag(stack).getString("Material");
        Material material = GregTechAPI.MaterialRegistry.get(string);
        return material != null ? material : Materials.Darmstadtium;
    }

    default ToolProperty getToolProperty(ItemStack stack) {
        return getToolMaterial(stack).getProperty(PropertyKey.TOOL);
    }

    default float getMaterialToolSpeed(ItemStack stack) {
        return getToolProperty(stack).getToolSpeed();
    }

    default float getMaterialAttackDamage(ItemStack stack) {
        return getToolProperty(stack).getToolAttackDamage();
    }

    default int getMaterialDurability(ItemStack stack) {
        return getToolProperty(stack).getToolDurability();
    }

    default int getMaterialEnchantability(ItemStack stack) {
        return getToolProperty(stack).getToolEnchantability();
    }

    @SideOnly(Side.CLIENT)
    default int getColorIndex(ItemStack stack, int tintIndex) {
        return tintIndex % 2 == 1 ? getToolMaterial(stack).getMaterialRGB() : 0xFFFFFF;
    }

}
