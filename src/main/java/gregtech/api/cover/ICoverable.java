package gregtech.api.cover;

import codechicken.lib.raytracer.CuboidRayTraceResult;
import codechicken.lib.raytracer.IndexedCuboid6;
import codechicken.lib.raytracer.RayTracer;
import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.ColourMultiplier;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.*;
import gregtech.api.pipenet.block.BlockPipe;
import gregtech.api.pipenet.block.BlockPipe.PipeConnectionData;
import gregtech.api.util.GTUtility;
import gregtech.client.utils.RenderUtil;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.RayTraceResult.Type;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;
import java.util.function.Consumer;

public interface ICoverable {

    Transformation REVERSE_HORIZONTAL_ROTATION = new Rotation(Math.PI, new Vector3(0.0, 1.0, 0.0)).at(Vector3.center);
    Transformation REVERSE_VERTICAL_ROTATION = new Rotation(Math.PI, new Vector3(1.0, 0.0, 0.0)).at(Vector3.center);

    World getWorld();

    BlockPos getPos();

    long getOffsetTimer();

    void markDirty();

    boolean isValid();

    <T> T getCapability(Capability<T> capability, EnumFacing side);

    boolean placeCoverOnSide(EnumFacing side, ItemStack itemStack, CoverDefinition definition, EntityPlayer player);

    boolean removeCover(EnumFacing side);

    boolean canPlaceCoverOnSide(EnumFacing side);

    CoverBehavior getCoverAtSide(EnumFacing side);

    void writeCoverData(CoverBehavior behavior, int id, Consumer<PacketBuffer> writer);

    int getInputRedstoneSignal(EnumFacing side, boolean ignoreCover);

    ItemStack getStackForm();

    double getCoverPlateThickness();

    int getPaintingColorForRendering();

    boolean shouldRenderBackSide();

    void notifyBlockUpdate();

    void scheduleRenderUpdate();

    default boolean hasAnyCover() {
        for(EnumFacing facing : EnumFacing.VALUES)
            if(getCoverAtSide(facing) != null)
                return true;
        return false;
    }

    @SideOnly(Side.CLIENT)
    default void renderCovers(CCRenderState renderState, Matrix4 translation, BlockRenderLayer layer) {
        renderState.lightMatrix.locate(getWorld(), getPos());
        double coverPlateThickness = getCoverPlateThickness();
        IVertexOperation[] platePipeline = new IVertexOperation[]{renderState.lightMatrix, new ColourMultiplier(GTUtility.convertRGBtoOpaqueRGBA_CL(getPaintingColorForRendering()))};
        IVertexOperation[] coverPipeline = new IVertexOperation[]{renderState.lightMatrix};

        for (EnumFacing sideFacing : EnumFacing.values()) {
            CoverBehavior coverBehavior = getCoverAtSide(sideFacing);
            if (coverBehavior == null) continue;
            Cuboid6 plateBox = getCoverPlateBox(sideFacing, coverPlateThickness);

            if (coverBehavior.canRenderInLayer(layer) && coverPlateThickness > 0) {
                renderState.preRenderWorld(getWorld(), getPos());
                coverBehavior.renderCoverPlate(renderState, translation, platePipeline, plateBox, layer);
            }

            if (coverBehavior.canRenderInLayer(layer)) {
                coverBehavior.renderCover(renderState, RenderUtil.adjustTrans(translation, sideFacing, 2), coverPipeline, plateBox, layer);
                if (coverPlateThickness == 0.0 && shouldRenderBackSide() && coverBehavior.canRenderBackside()) {
                    //machine is full block, but still not opaque - render cover on the back side too
                    Matrix4 backTranslation = translation.copy();
                    if (sideFacing.getAxis().isVertical()) {
                        REVERSE_VERTICAL_ROTATION.apply(backTranslation);
                    } else {
                        REVERSE_HORIZONTAL_ROTATION.apply(backTranslation);
                    }
                    backTranslation.translate(-sideFacing.getXOffset(), -sideFacing.getYOffset(), -sideFacing.getZOffset());
                    coverBehavior.renderCover(renderState, backTranslation, coverPipeline, plateBox, layer); // may need to translate the layer here as well
                }
            }
        }
    }

    default void addCoverCollisionBoundingBox(List<? super IndexedCuboid6> collisionList) {
        double plateThickness = getCoverPlateThickness();
        if (plateThickness > 0.0) {
            for (EnumFacing side : EnumFacing.VALUES) {
                if (getCoverAtSide(side) != null) {
                    Cuboid6 coverBox = getCoverPlateBox(side, plateThickness);
                    CoverSideData coverSideData = new CoverSideData(side);
                    collisionList.add(new IndexedCuboid6(coverSideData, coverBox));
                }
            }
        }
    }

    static boolean doesCoverCollide(EnumFacing side, List<IndexedCuboid6> collisionBox, double plateThickness) {
        if (side == null) {
            return false;
        }
        if (plateThickness > 0.0) {
            Cuboid6 coverPlateBox = getCoverPlateBox(side, plateThickness);
            for (Cuboid6 collisionCuboid : collisionBox) {
                if (collisionCuboid.intersects(coverPlateBox)) {
                    //collision box intersects with machine bounding box -
                    //cover cannot be placed on this side
                    return true;
                }
            }
            return false;
        }
        return false;
    }

    static EnumFacing rayTraceCoverableSide(ICoverable coverable, EntityPlayer player) {
        RayTraceResult result = RayTracer.retraceBlock(coverable.getWorld(), player, coverable.getPos());
        if (result == null || result.typeOfHit != Type.BLOCK) {
            return null;
        }
        return traceCoverSide(result);
    }

    class PrimaryBoxData {
        public final boolean usePlacementGrid;

        public PrimaryBoxData(boolean usePlacementGrid) {
            this.usePlacementGrid = usePlacementGrid;
        }
    }

    class CoverSideData {
        public final EnumFacing side;

        public CoverSideData(EnumFacing side) {
            this.side = side;
        }
    }

    static EnumFacing traceCoverSide(RayTraceResult result) {
        if (result instanceof CuboidRayTraceResult) {
            CuboidRayTraceResult rayTraceResult = (CuboidRayTraceResult) result;
            if (rayTraceResult.cuboid6.data == null) {
                return determineGridSideHit(result);
            } else if (rayTraceResult.cuboid6.data instanceof CoverSideData) {
                return ((CoverSideData) rayTraceResult.cuboid6.data).side;
            } else if (rayTraceResult.cuboid6.data instanceof BlockPipe.PipeConnectionData) {
                return ((PipeConnectionData) rayTraceResult.cuboid6.data).side;
            } else if (rayTraceResult.cuboid6.data instanceof PrimaryBoxData) {
                PrimaryBoxData primaryBoxData = (PrimaryBoxData) rayTraceResult.cuboid6.data;
                return primaryBoxData.usePlacementGrid ? determineGridSideHit(result) : result.sideHit;
            } //unknown hit type, fall through
        }
        //normal collision ray trace, return side hit
        return determineGridSideHit(result);
    }

    static EnumFacing determineGridSideHit(RayTraceResult result) {
        return GTUtility.determineWrenchingSide(result.sideHit,
                (float) (result.hitVec.x - result.getBlockPos().getX()),
                (float) (result.hitVec.y - result.getBlockPos().getY()),
                (float) (result.hitVec.z - result.getBlockPos().getZ()));
    }

    static Cuboid6 getCoverPlateBox(EnumFacing side, double plateThickness) {
        switch (side) {
            case UP:
                return new Cuboid6(0.0, 1.0 - plateThickness, 0.0, 1.0, 1.0, 1.0);
            case DOWN:
                return new Cuboid6(0.0, 0.0, 0.0, 1.0, plateThickness, 1.0);
            case NORTH:
                return new Cuboid6(0.0, 0.0, 0.0, 1.0, 1.0, plateThickness);
            case SOUTH:
                return new Cuboid6(0.0, 0.0, 1.0 - plateThickness, 1.0, 1.0, 1.0);
            case WEST:
                return new Cuboid6(0.0, 0.0, 0.0, plateThickness, 1.0, 1.0);
            case EAST:
                return new Cuboid6(1.0 - plateThickness, 0.0, 0.0, 1.0, 1.0, 1.0);
            default:
                throw new UnsupportedOperationException();
        }
    }

    static boolean canPlaceCover(CoverDefinition coverDef, ICoverable coverable) {
        for (EnumFacing facing : EnumFacing.VALUES) {
            CoverBehavior cover = coverDef.createCoverBehavior(coverable, facing);
            if (coverable.canPlaceCoverOnSide(facing) && cover.canAttach())
                return true;
        }
        return false;
    }

    default boolean canRenderMachineGrid() {
        return true;
    }
}
