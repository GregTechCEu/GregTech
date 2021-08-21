package gregtech.common.tools;

import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;

import java.util.Collections;
import java.util.Set;

public class ToolScrewdriver extends ToolBase {

    private static final Set<String> DRIVER_TOOL_CLASSES = Collections.singleton("screwdriver");

    @Override
    public float getNormalDamageBonus(EntityLivingBase entity, ItemStack stack, EntityLivingBase attacker) {
        String name = entity.getClass().getName();
        name = name.substring(name.lastIndexOf('.') + 1);
        return name.toLowerCase().contains("spider") ? 3.0F : 1.0F;
    }

    @Override
    public int getToolDamagePerBlockBreak(ItemStack stack) {
        return 2;
    }

    @Override
    public int getToolDamagePerContainerCraft(ItemStack stack) {
        return 4;
    }

    @Override
    public float getBaseDamage(ItemStack stack) {
        return 1.5F;
    }

    @Override
    public boolean canMineBlock(IBlockState block, ItemStack stack) {
        String tool = block.getBlock().getHarvestTool(block);
        return (tool != null && DRIVER_TOOL_CLASSES.contains(tool)) ||
                block.getMaterial() == Material.CIRCUITS;
    }

    @Override
    public Set<String> getToolClasses(ItemStack stack) {
        return DRIVER_TOOL_CLASSES;
    }
}
