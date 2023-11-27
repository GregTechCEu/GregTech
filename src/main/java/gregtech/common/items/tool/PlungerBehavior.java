package gregtech.common.items.tool;

import gregtech.api.capability.impl.FluidHandlerProxy;
import gregtech.api.capability.impl.VoidFluidHandlerItemStack;
import gregtech.api.items.toolitem.ToolHelper;
import gregtech.api.items.toolitem.behavior.IToolBehavior;

import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.IFluidHandler;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class PlungerBehavior implements IToolBehavior {

    public static final PlungerBehavior INSTANCE = new PlungerBehavior();

    protected PlungerBehavior() {/**/}

    @Override
    public EnumActionResult onItemUseFirst(@NotNull EntityPlayer player, @NotNull World world, @NotNull BlockPos pos,
                                           @NotNull EnumFacing facing, float hitX, float hitY, float hitZ,
                                           @NotNull EnumHand hand) {
        IFluidHandler fluidHandler = FluidUtil.getFluidHandler(world, pos, facing);
        if (fluidHandler == null) {
            return EnumActionResult.PASS;
        }

        IFluidHandler handlerToRemoveFrom = player.isSneaking() ?
                (fluidHandler instanceof FluidHandlerProxy ? ((FluidHandlerProxy) fluidHandler).input : null) :
                (fluidHandler instanceof FluidHandlerProxy ? ((FluidHandlerProxy) fluidHandler).output : fluidHandler);

        if (handlerToRemoveFrom != null && handlerToRemoveFrom.drain(1000, true) != null) {
            ToolHelper.onActionDone(player, world, hand);
            return EnumActionResult.SUCCESS;
        }
        return EnumActionResult.PASS;
    }

    @Override
    public ICapabilityProvider createProvider(ItemStack stack, @Nullable NBTTagCompound tag) {
        return new VoidFluidHandlerItemStack(stack) {

            @Override
            public int fill(FluidStack resource, boolean doFill) {
                int result = super.fill(resource, doFill);
                if (result > 0) {
                    ToolHelper.damageItem(getContainer(), null);
                }
                return result;
            }
        };
    }

    @Override
    public void addInformation(@NotNull ItemStack stack, @Nullable World world, @NotNull List<String> tooltip,
                               @NotNull ITooltipFlag flag) {
        tooltip.add(I18n.format("item.gt.tool.behavior.plunger"));
    }
}
