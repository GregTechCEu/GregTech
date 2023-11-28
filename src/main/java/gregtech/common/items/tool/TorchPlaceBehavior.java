package gregtech.common.items.tool;

import gregtech.api.items.toolitem.ToolHelper;
import gregtech.api.items.toolitem.behavior.IToolBehavior;
import gregtech.api.unification.OreDictUnifier;

import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static gregtech.api.items.toolitem.ToolHelper.TORCH_PLACING_KEY;

public class TorchPlaceBehavior implements IToolBehavior {

    public static final TorchPlaceBehavior INSTANCE = new TorchPlaceBehavior();

    protected TorchPlaceBehavior() {/**/}

    @NotNull
    @Override
    public EnumActionResult onItemUse(@NotNull EntityPlayer player, @NotNull World world, @NotNull BlockPos pos,
                                      @NotNull EnumHand hand, @NotNull EnumFacing facing, float hitX, float hitY,
                                      float hitZ) {
        ItemStack stack = player.getHeldItem(hand);
        NBTTagCompound behaviourTag = ToolHelper.getBehaviorsTag(stack);
        if (behaviourTag.getBoolean(ToolHelper.TORCH_PLACING_KEY)) {
            int cachedTorchSlot;
            ItemStack slotStack;
            if (behaviourTag.getBoolean(ToolHelper.TORCH_PLACING_CACHE_SLOT_KEY)) {
                cachedTorchSlot = behaviourTag.getInteger(ToolHelper.TORCH_PLACING_CACHE_SLOT_KEY);
                if (cachedTorchSlot < 0) {
                    slotStack = player.inventory.offHandInventory.get(0);
                } else {
                    slotStack = player.inventory.mainInventory.get(cachedTorchSlot);
                }
                if (checkAndPlaceTorch(slotStack, player, world, pos, hand, facing, hitX, hitY, hitZ)) {
                    return EnumActionResult.SUCCESS;
                }
            }
            for (int i = 0; i < player.inventory.offHandInventory.size(); i++) {
                slotStack = player.inventory.offHandInventory.get(i);
                if (checkAndPlaceTorch(slotStack, player, world, pos, hand, facing, hitX, hitY, hitZ)) {
                    behaviourTag.setInteger(ToolHelper.TORCH_PLACING_CACHE_SLOT_KEY, -(i + 1));
                    return EnumActionResult.SUCCESS;
                }
            }
            for (int i = 0; i < player.inventory.mainInventory.size(); i++) {
                slotStack = player.inventory.mainInventory.get(i);
                if (checkAndPlaceTorch(slotStack, player, world, pos, hand, facing, hitX, hitY, hitZ)) {
                    behaviourTag.setInteger(ToolHelper.TORCH_PLACING_CACHE_SLOT_KEY, i);
                    return EnumActionResult.SUCCESS;
                }
            }
        }
        return EnumActionResult.PASS;
    }

    private static boolean checkAndPlaceTorch(ItemStack slotStack, EntityPlayer player, World world, BlockPos pos,
                                              EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        if (!slotStack.isEmpty()) {
            Item slotItem = slotStack.getItem();
            if (slotItem instanceof ItemBlock) {
                ItemBlock slotItemBlock = (ItemBlock) slotItem;
                Block slotBlock = slotItemBlock.getBlock();
                if (slotBlock == Blocks.TORCH ||
                        OreDictUnifier.hasOreDictionary(slotStack, "torch") ||
                        OreDictUnifier.hasOreDictionary(slotStack, "blockTorch")) {
                    IBlockState state = world.getBlockState(pos);
                    Block block = state.getBlock();
                    if (!block.isReplaceable(world, pos)) {
                        pos = pos.offset(facing);
                    }
                    if (player.canPlayerEdit(pos, facing, slotStack) &&
                            world.mayPlace(slotBlock, pos, false, facing, player)) {
                        int i = slotItemBlock.getMetadata(slotStack.getMetadata());
                        IBlockState slotState = slotBlock.getStateForPlacement(world, pos, facing, hitX, hitY, hitZ, i,
                                player, hand);
                        if (slotItemBlock.placeBlockAt(slotStack, player, world, pos, facing, hitX, hitY, hitZ,
                                slotState)) {
                            slotState = world.getBlockState(pos);
                            SoundType soundtype = slotState.getBlock().getSoundType(slotState, world, pos, player);
                            world.playSound(player, pos, soundtype.getPlaceSound(), SoundCategory.BLOCKS,
                                    (soundtype.getVolume() + 1.0F) / 2.0F, soundtype.getPitch() * 0.8F);
                            if (!player.isCreative()) slotStack.shrink(1);
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    @Override
    public void addBehaviorNBT(@NotNull ItemStack stack, @NotNull NBTTagCompound tag) {
        tag.setBoolean(TORCH_PLACING_KEY, true);
    }

    @Override
    public void addInformation(@NotNull ItemStack stack, @Nullable World world, @NotNull List<String> tooltip,
                               @NotNull ITooltipFlag flag) {
        tooltip.add(I18n.format("item.gt.tool.behavior.torch_place"));
    }
}
