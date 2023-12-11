package gregtech.common.items.tool;

import gregtech.api.items.toolitem.ToolHelper;
import gregtech.api.items.toolitem.behavior.IToolBehavior;

import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class DisableShieldBehavior implements IToolBehavior {

    public static final DisableShieldBehavior INSTANCE = new DisableShieldBehavior();

    protected DisableShieldBehavior() {/**/}

    @Override
    public boolean canDisableShield(ItemStack stack, ItemStack shield, EntityLivingBase entity,
                                    EntityLivingBase attacker) {
        return true;
    }

    @Override
    public void addBehaviorNBT(@NotNull ItemStack stack, @NotNull NBTTagCompound tag) {
        tag.setBoolean(ToolHelper.DISABLE_SHIELDS_KEY, true);
    }

    @Override
    public void addInformation(@NotNull ItemStack stack, @Nullable World world, @NotNull List<String> tooltip,
                               @NotNull ITooltipFlag flag) {
        tooltip.add(I18n.format("item.gt.tool.behavior.shield_disable"));
    }
}
