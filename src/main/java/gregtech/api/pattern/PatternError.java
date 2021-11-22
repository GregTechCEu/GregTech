package gregtech.api.pattern;

import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.MetaTileEntityHolder;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class PatternError {

    protected BlockWorldState worldState;

    public void setWorldState(BlockWorldState worldState) {
        this.worldState = worldState;
    }

    public World getWorld() {
        return worldState.getWorld();
    }

    public BlockPos getPos() {
        return worldState.getPos();
    }

    public List<List<ItemStack>> getCandidates() {
        TraceabilityPredicate predicate = worldState.predicate;
        List<List<ItemStack>> candidates = new LinkedList<>();
        for (TraceabilityPredicate.SimplePredicate common : predicate.common) {
            candidates.add(getCandidates(common));
        }
        for (TraceabilityPredicate.SimplePredicate limited : predicate.limited) {
            candidates.add(getCandidates(limited));
        }
        return candidates;
    }

    protected List<ItemStack> getCandidates(TraceabilityPredicate.SimplePredicate simplePredicate) {
        return Arrays.stream(simplePredicate.candidates.get()).filter(info -> info.getBlockState().getBlock() != Blocks.AIR).map(info->{
            IBlockState blockState = info.getBlockState();
            MetaTileEntity metaTileEntity = info.getTileEntity() instanceof MetaTileEntityHolder ? ((MetaTileEntityHolder) info.getTileEntity()).getMetaTileEntity() : null;
            if (metaTileEntity != null) {
                return metaTileEntity.getStackForm();
            } else {
                return new ItemStack(Item.getItemFromBlock(blockState.getBlock()), 1, blockState.getBlock().damageDropped(blockState));
            }
        }).collect(Collectors.toList());
    }

    @SideOnly(Side.CLIENT)
    public String getErrorInfo() {
        List<List<ItemStack>> candidates = getCandidates();
        StringBuilder builder = new StringBuilder();
        for (List<ItemStack> candidate : candidates) {
            if (!candidate.isEmpty()) {
                builder.append(candidate.get(0).getDisplayName());
                builder.append(", ");
            }
        }
        builder.append("...");
        return I18n.format("gregtech.multiblock.pattern.error", builder.toString(), worldState.pos);
    }
}
