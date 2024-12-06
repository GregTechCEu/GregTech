package gregtech.common.items.tool;

import gregtech.api.items.toolitem.behavior.IToolBehavior;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;

import com.cleanroommc.modularui.factory.ItemGuiFactory;
import org.jetbrains.annotations.NotNull;

public class OpenGUIBehavior implements IToolBehavior {

    public static final OpenGUIBehavior INSTANCE = new OpenGUIBehavior();

    protected OpenGUIBehavior() {}

    @Override
    public @NotNull ActionResult<ItemStack> onItemRightClick(@NotNull World world, @NotNull EntityPlayer player,
                                                             @NotNull EnumHand hand) {
        if (!world.isRemote) {
            ItemGuiFactory.open((EntityPlayerMP) player, hand);
        }
        return ActionResult.newResult(EnumActionResult.SUCCESS, player.getHeldItem(hand));
    }
}
