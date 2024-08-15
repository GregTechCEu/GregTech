package gregtech.api.graphnet.pipenet.logic;

import gregtech.api.cover.Cover;
import gregtech.api.graphnet.logic.NetLogicEntry;
import gregtech.api.graphnet.pipenet.physical.tile.PipeTileEntity;
import gregtech.api.util.DimensionFacingPos;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.ref.WeakReference;

public final class EdgeCoverReferenceLogic extends NetLogicEntry<EdgeCoverReferenceLogic, NBTTagCompound> {

    public static final EdgeCoverReferenceLogic INSTANCE = new EdgeCoverReferenceLogic();

    @Nullable
    private WeakReference<Cover> coverSource;
    private DimensionFacingPos coverSourcePos;
    @Nullable
    private WeakReference<Cover> coverTarget;
    private DimensionFacingPos coverTargetPos;

    public EdgeCoverReferenceLogic() {
        super("EdgeCoverReference");
    }

    @Contract("_,_ -> this")
    public EdgeCoverReferenceLogic coverSource(@NotNull DimensionFacingPos pos, @NotNull Cover cover) {
        this.coverSource = new WeakReference<>(cover);
        this.coverSourcePos = pos;
        return this;
    }

    @Contract("_,_ -> this")
    public EdgeCoverReferenceLogic coverTarget(@NotNull DimensionFacingPos pos, @NotNull Cover cover) {
        this.coverTarget = new WeakReference<>(cover);
        this.coverTargetPos = pos;
        return this;
    }

    private @Nullable Cover getSource() {
        if (coverSource == null) return null;
        Cover ref = coverSource.get();
        if (ref == null) {
            World world = DimensionManager.getWorld(coverSourcePos.getDimension());
            if (world == null || !world.isBlockLoaded(coverSourcePos.getPos())) return null;

            TileEntity tile = world.getTileEntity(coverSourcePos.getPos());
            if (tile instanceof PipeTileEntity pipe) {
                Cover cover = pipe.getCoverHolder().getCoverAtSide(coverSourcePos.getFacing());
                if (cover != null) {
                    this.coverSource = new WeakReference<>(cover);
                    return cover;
                } else {
                    // the cover doesn't exist, which makes no sense since we have a reference to its location but
                    // whatever
                    this.coverSource = null;
                    return null;
                }
            } else {
                // the pipe doesn't exist, which makes no sense since the edge holding us exists but whatever
                this.coverSource = null;
                return null;
            }
        } else {
            return ref;
        }
    }

    private @Nullable Cover getTarget() {
        if (coverTarget == null) return null;
        Cover ref = coverTarget.get();
        if (ref == null) {
            World world = DimensionManager.getWorld(coverTargetPos.getDimension());
            if (world == null || !world.isBlockLoaded(coverTargetPos.getPos())) return null;

            TileEntity tile = world.getTileEntity(coverTargetPos.getPos());
            if (tile instanceof PipeTileEntity pipe) {
                Cover cover = pipe.getCoverHolder().getCoverAtSide(coverTargetPos.getFacing());
                if (cover != null) {
                    this.coverTarget = new WeakReference<>(cover);
                    return cover;
                } else {
                    // the cover doesn't exist, which makes no sense since we have a reference to its location but
                    // whatever
                    this.coverTarget = null;
                    return null;
                }
            } else {
                // the pipe doesn't exist, which makes no sense since the edge holding us exists but whatever
                this.coverTarget = null;
                return null;
            }
        } else {
            return ref;
        }
    }

    @Override
    public NBTTagCompound serializeNBT() {
        NBTTagCompound tag = new NBTTagCompound();
        tag.setLong("SourcePos", coverSourcePos.getPos().toLong());
        tag.setByte("SourceFacing", (byte) coverSourcePos.getFacing().ordinal());
        tag.setInteger("SourceDim", coverSourcePos.getDimension());
        tag.setLong("TargetPos", coverTargetPos.getPos().toLong());
        tag.setByte("TargetFacing", (byte) coverTargetPos.getFacing().ordinal());
        tag.setInteger("TargetDim", coverTargetPos.getDimension());
        return tag;
    }

    @Override
    public void deserializeNBT(NBTTagCompound nbt) {
        this.coverSourcePos = new DimensionFacingPos(BlockPos.fromLong(nbt.getLong("SourcePos")),
                EnumFacing.VALUES[nbt.getByte("SourceFacing")], nbt.getInteger("SourceDim"));
        this.coverSource = new WeakReference<>(null);
        this.coverTargetPos = new DimensionFacingPos(BlockPos.fromLong(nbt.getLong("TargetPos")),
                EnumFacing.VALUES[nbt.getByte("TargetFacing")], nbt.getInteger("TargetDim"));
        this.coverTarget = coverSource;
    }

    @Override
    public @NotNull EdgeCoverReferenceLogic getNew() {
        return new EdgeCoverReferenceLogic();
    }

    @Override
    public boolean shouldEncode() {
        return false;
    }

    @Override
    public void encode(PacketBuffer buf, boolean fullChange) {}

    @Override
    public void decode(PacketBuffer buf, boolean fullChange) {}
}
