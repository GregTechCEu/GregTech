package gregtech.integration.tinkers.effect.modifier;

import gregtech.integration.tinkers.effect.GTTinkerEffects;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import slimeknights.tconstruct.library.modifiers.ModifierNBT;
import slimeknights.tconstruct.library.modifiers.ModifierTrait;
import slimeknights.tconstruct.library.tinkering.Category;
import slimeknights.tconstruct.library.tools.ToolCore;
import slimeknights.tconstruct.library.utils.TinkerUtil;

public class ModifierSalty extends ModifierTrait {

    public ModifierSalty() {
        super("gt_salty", 0xFAFAFA, 4, 16);
    }

    @Override
    public void onHit(ItemStack tool, EntityLivingBase player, EntityLivingBase target, float damage, boolean isCritical) {
        NBTTagCompound tag = TinkerUtil.getModifierTag(tool, identifier);
        ModifierNBT.IntegerNBT data = ModifierNBT.readInteger(tag);
        GTTinkerEffects.POTION_UNHEALING.apply(target, data.current * 2);
    }

    @Override
    public String getTooltip(NBTTagCompound modifierTag, boolean detailed) {
        return getLeveledTooltip(modifierTag, detailed);
    }

    @Override
    public boolean canApplyCustom(ItemStack stack) {
        // Can apply to any melee weapon or any projectile (arrow, bolt, shuriken, etc)
        if (stack.getItem() instanceof ToolCore tool) {
            return (tool.hasCategory(Category.WEAPON) || tool.hasCategory(Category.PROJECTILE))
                    && !tool.hasCategory(Category.LAUNCHER);
        }
        return false;
    }
}
