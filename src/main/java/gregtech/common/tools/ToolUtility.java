package gregtech.common.tools;

import codechicken.lib.raytracer.RayTracer;
import gregtech.api.unification.OreDictUnifier;
import gregtech.api.unification.material.Materials;
import gregtech.api.unification.material.properties.PropertyKey;
import gregtech.api.unification.ore.OrePrefix;
import gregtech.api.unification.stack.MaterialStack;
import gregtech.api.util.GTUtility;
import gregtech.api.util.TaskScheduler;
import gregtech.common.blocks.BlockOre;
import net.minecraft.block.Block;
import net.minecraft.block.BlockCrops;
import net.minecraft.block.state.IBlockState;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.init.Enchantments;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.common.IShearable;
import net.minecraftforge.common.util.FakePlayer;

import java.util.List;
import java.util.Random;

public class ToolUtility {

    public static EnumFacing getSideHit(World world, BlockPos blockPos, EntityPlayer harvester) {
        RayTraceResult result = RayTracer.retraceBlock(world, harvester, blockPos);
        return result == null ? harvester.getHorizontalFacing() : result.sideHit;
    }

    public static boolean applyTimberAxe(ItemStack itemStack, World world, BlockPos blockPos, EntityPlayer player) {
        if (player instanceof FakePlayer) {
            return false;
        }
        IBlockState blockState = world.getBlockState(blockPos);
        if (TreeChopTask.isLogBlock(blockState) == 1) {
            if (!world.isRemote) {
                EntityPlayerMP playerMP = (EntityPlayerMP) player;
                TreeChopTask treeChopTask = new TreeChopTask(blockPos, world, playerMP, itemStack);
                TaskScheduler.scheduleTask(world, treeChopTask);
            }
            return true;
        }
        return false;
    }

    public static boolean applyShearBehavior(ItemStack itemStack, BlockPos pos, EntityPlayer player) {
        Block block = player.world.getBlockState(pos).getBlock();
        if (block instanceof IShearable) {
            IShearable target = (IShearable) block;
            if (target.isShearable(itemStack, player.world, pos)) {
                int fortuneLevel = EnchantmentHelper.getEnchantmentLevel(Enchantments.FORTUNE, itemStack);
                List<ItemStack> drops = target.onSheared(itemStack, player.world, pos, fortuneLevel);
                dropListOfItems(player.world, pos, drops);
                player.world.setBlockState(pos, Blocks.AIR.getDefaultState(), 11);
                return true;
            }
        }
        return false;
    }

    public static boolean applyHarvestBehavior(BlockPos pos, EntityPlayer player) {
        IBlockState blockState = player.world.getBlockState(pos);
        Block block = blockState.getBlock();
        if (block instanceof BlockCrops) {
            BlockCrops blockCrops = (BlockCrops) block;
            if (blockCrops.isMaxAge(blockState)) {
                @SuppressWarnings("deprecation")
                List<ItemStack> drops = blockCrops.getDrops(player.world, pos, blockState, 0);
                dropListOfItems(player.world, pos, drops);
                player.world.setBlockState(pos, blockCrops.withAge(0));
                return true;
            }
        }
        return false;
    }

    private static void dropListOfItems(World world, BlockPos pos, List<ItemStack> drops) {
        Random rand = new Random();
        for (ItemStack stack : drops) {
            float f = 0.7F;
            double offX = (rand.nextFloat() * f) + (1.0F - f) * 0.5D;
            double offY = (rand.nextFloat() * f) + (1.0F - f) * 0.5D;
            double offZ = (rand.nextFloat() * f) + (1.0F - f) * 0.5D;
            EntityItem entityItem = new EntityItem(world, pos.getX() + offX, pos.getY() + offY, pos.getZ() + offZ, stack);
            entityItem.setDefaultPickupDelay();
            world.spawnEntity(entityItem);
        }
    }

    public static void applyHammerDrops(Random random, IBlockState blockState, List<ItemStack> drops, int fortuneLevel, EntityPlayer player) {
        ItemStack blockItem = GTUtility.toItem(blockState);
        ItemStack inputStack;
        if (blockState.getBlock() instanceof BlockOre) {
            inputStack = drops.get(0);
        } else {
            inputStack = blockItem.copy();
        }
        MaterialStack input = OreDictUnifier.getMaterial(inputStack);
        if (input != null && input.material.hasProperty(PropertyKey.ORE) && GTUtility.isOre(blockItem) && !(player instanceof FakePlayer)) {
            drops.clear();
            OrePrefix prefix = OreDictUnifier.getPrefix(blockItem);
            int multiplier = (prefix == OrePrefix.oreEndstone || prefix == OrePrefix.oreNetherrack) ? 2 : 1;
            ItemStack output = OreDictUnifier.get(OrePrefix.crushed, input.material);

            if (fortuneLevel > 0) {
                if (fortuneLevel > 3) fortuneLevel = 3;
                output.setCount((input.material.getProperty((PropertyKey.ORE)).getOreMultiplier() * multiplier) * (random.nextFloat() <= (fortuneLevel / 3.0) ? 2 : 1));
                if (output.getCount() == 0) output.setCount(1);
            } else {
                output.setCount(input.material.getProperty((PropertyKey.ORE)).getOreMultiplier() * multiplier);
            }
            drops.add(output);
        } else if (inputStack.getItem() instanceof ItemBlock) {
            ItemBlock itemBlock = (ItemBlock) inputStack.getItem();
            ItemStack newOutput = ItemStack.EMPTY;
            if (itemBlock.getBlock() == Blocks.STAINED_GLASS || itemBlock.getBlock() == Blocks.GLASS) {
                newOutput = OreDictUnifier.get(OrePrefix.dust, Materials.Glass);
            } else if (itemBlock.getBlock() == Blocks.STAINED_GLASS_PANE || itemBlock.getBlock() == Blocks.GLASS_PANE) {
                newOutput = OreDictUnifier.get(OrePrefix.dustTiny, Materials.Glass);
            }
            if (newOutput != ItemStack.EMPTY) {
                drops.clear();
                drops.add(newOutput);
            }
        }
    }
}
