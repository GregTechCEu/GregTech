package gregtech.api.pattern;

import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class PatternError {

    protected BlockPos pos;
    protected List<List<ItemStack>> candidates;

    public PatternError(BlockPos pos, List<List<ItemStack>> candidates) {
        this.pos = pos;
        this.candidates = candidates;
    }

    public PatternError(BlockPos pos, TraceabilityPredicate failingPredicate) {
        this(pos, failingPredicate.getCandidates());
    }

    @Nullable
    public BlockPos getPos() {
        return pos;
    }

    public List<List<ItemStack>> getCandidates() {
        return this.candidates;
    }

    @SideOnly(Side.CLIENT)
    public String getErrorInfo() {
        StringBuilder builder = new StringBuilder();
        for (List<ItemStack> candidate : candidates) {
            if (!candidate.isEmpty()) {
                builder.append(candidate.get(0).getDisplayName());
                builder.append(", ");
            }
        }
        builder.append("...");
        return I18n.format("gregtech.multiblock.pattern.error", builder.toString(), pos);
    }
}
