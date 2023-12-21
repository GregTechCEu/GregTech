package gregtech.integration.forestry.tools;

import gregtech.api.GTValues;
import gregtech.api.items.toolitem.IGTTool;
import gregtech.api.items.toolitem.behavior.IToolBehavior;

import gregtech.common.blocks.wood.BlockRubberLeaves;

import net.minecraft.block.Block;
import net.minecraft.block.BlockLeaves;
import net.minecraft.block.BlockNewLeaf;
import net.minecraft.block.BlockOldLeaf;
import net.minecraft.block.BlockPlanks;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.event.world.BlockEvent;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class BranchCutterBehavior implements IToolBehavior {

    @Override
    public void convertBlockDrops(@NotNull ItemStack stack, @NotNull EntityPlayer player,
                                  @NotNull List<ItemStack> drops, BlockEvent.@NotNull HarvestDropsEvent event) {
        IBlockState state = event.getState();
        IGTTool tool = (IGTTool) stack.getItem();
        if (state.getMaterial() == Material.LEAVES) {
            int level = tool.getTotalHarvestLevel(stack);
            event.setDropChance(MathHelper.clamp((level + 1) * 0.2F, 0.0F, 1.0F));
        }

        Block block = state.getBlock();
        if (block instanceof BlockOldLeaf && state.getValue(BlockOldLeaf.VARIANT) == BlockPlanks.EnumType.OAK) {
            drops.clear();
            if (GTValues.RNG.nextInt(9) <= event.getFortuneLevel() * 2) {
                drops.add(new ItemStack(Items.APPLE));
            } else {
                drops.add(new ItemStack(Blocks.SAPLING, 1, BlockPlanks.EnumType.OAK.getMetadata()));
            }
            return;
        }

        if (block instanceof BlockNewLeaf && state.getValue(BlockNewLeaf.VARIANT) == BlockPlanks.EnumType.DARK_OAK) {
            drops.clear();
            if (GTValues.RNG.nextInt(9) <= event.getFortuneLevel() * 2) {
                drops.add(new ItemStack(Items.APPLE));
            } else {
                drops.add(new ItemStack(Blocks.SAPLING, 1, BlockPlanks.EnumType.DARK_OAK.getMetadata()));
            }
            return;
        }

        if (block instanceof BlockLeaves) {
            drops.clear();
            drops.add(new ItemStack(Blocks.SAPLING, 1, block.getMetaFromState(state)));
            return;
        }
        
        if (block == Blocks.VINE) {

        }


        if (aBlock == Blocks.leaves) {
            aDrops.clear();
            if ((aMetaData & 3) == 0 && RNGSUS.nextInt(9) <= aFortune * 2) aDrops.add(IL.Food_Apple_Red.get(1)); else aDrops.add(ST.make(Blocks.sapling, 1, aMetaData & 3));
        } else if (aBlock == Blocks.leaves2) {
            aDrops.clear();
            aDrops.add(ST.make(Blocks.sapling, 1, (aMetaData & 3) + 4));
        } else if (aBlock == Blocks.vine) {
            aDrops.clear();
            aDrops.add(ST.make(Blocks.vine, 1, 0));
        } else if (aBlock instanceof BlockBaseLeaves) {
            aDrops.clear();
            aDrops.add(ST.make(aBlock.getItemDropped(aMetaData, RNGSUS, aFortune), 1, aBlock.damageDropped(aMetaData)));
        } else if (IL.IC2_Leaves_Rubber.equal(aBlock)) {
            aDrops.clear();
            aDrops.add(IL.IC2_Sapling_Rubber.get(1));
        } else if (IL.AETHER_Skyroot_Leaves_Gold.equal(aBlock)) {
            aDrops.clear();
            aDrops.add(IL.AETHER_Skyroot_Sapling_Gold.get(1));
        } else if (IL.AETHER_Skyroot_Leaves_Green.equal(aBlock)) {
            aDrops.clear();
            aDrops.add(IL.AETHER_Skyroot_Sapling_Green.get(1));
        } else if (IL.AETHER_Skyroot_Leaves_Blue.equal(aBlock)) {
            aDrops.clear();
            aDrops.add(IL.AETHER_Skyroot_Sapling_Blue.get(1));
        } else if (IL.AETHER_Skyroot_Leaves_Dark.equal(aBlock)) {
            aDrops.clear();
            aDrops.add(IL.AETHER_Skyroot_Sapling_Dark.get(1));
        } else if (IL.AETHER_Skyroot_Leaves_Purple.equal(aBlock)) {
            aDrops.clear();
            aDrops.add(IL.AETHER_Skyroot_Sapling_Purple.get(1));
        } else if (IL.AETHER_Skyroot_Leaves_Apple.equal(aBlock)) {
            if (RNGSUS.nextInt(9) <= aFortune * 2) aDrops.add(IL.AETHER_Apple.get(1)); else aDrops.add(IL.AETHER_Skyroot_Sapling_Purple.get(1));
        }
        return 0;
    }
}
