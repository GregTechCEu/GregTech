package gregtech.integration.tinkers.effect.modifier;

import gregtech.api.GTValues;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import slimeknights.tconstruct.library.modifiers.ModifierNBT;
import slimeknights.tconstruct.library.modifiers.ModifierTrait;
import slimeknights.tconstruct.library.tinkering.Category;
import slimeknights.tconstruct.library.tools.ToolCore;
import slimeknights.tconstruct.library.utils.TinkerUtil;

public class ModifierExplosive extends ModifierTrait {

    public ModifierExplosive() {
        super("gt_explosive", 0x8E2D04, 3, 16);
    }

    @Override
    public void onHit(ItemStack tool, EntityLivingBase player, EntityLivingBase target, float damage, boolean isCritical) {
        // 25% chance on critical hit
        if (isCritical && GTValues.RNG.nextFloat() <= 0.25f) {
            World world = target.getEntityWorld();
            if (!world.isRemote) {
                NBTTagCompound tag = TinkerUtil.getModifierTag(tool, identifier);
                ModifierNBT.IntegerNBT data = ModifierNBT.readInteger(tag);
                world.createExplosion(player, target.posX + 0.5, target.posY + 0.5, target.posZ + 0.5, data.current, false);
            }
        }
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
