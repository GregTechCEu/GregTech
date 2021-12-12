package gregtech.client.renderer.cclop;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Cuboid6;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Arrays;

@SideOnly(Side.CLIENT)
public class GTBlockOperation implements IVertexOperation {
    public static final int operationIndex = CCRenderState.registerOperation();
    public final BlockRenderLayer layer;
    public final boolean[] sideMask;
    public static boolean[] PASS_MASK = new boolean[EnumFacing.VALUES.length];

    static {
        Arrays.fill(PASS_MASK, true);
    }

    public GTBlockOperation(BlockRenderLayer layer, boolean[] sideMask) {
        this.layer = layer;
        this.sideMask = sideMask;
    }

    public boolean shouldSideBeRendered(EnumFacing face, Cuboid6 bounds) {
        if (!sideMask[face.getIndex()]) { // check if the side is unnecessary be rendered
            if (bounds == Cuboid6.full) {
                return false;
            }
            switch (face) {
                case DOWN:
                    if (bounds.min.y <= 0) return false;
                    break;
                case UP:
                    if (bounds.max.y >= 1) return false;
                    break;
                case NORTH:
                    if (bounds.min.z <= 0) return false;
                    break;
                case SOUTH:
                    if (bounds.max.z >= 1) return false;
                    break;
                case WEST:
                    if (bounds.min.x <= 0) return false;
                    break;
                case EAST:
                    if (bounds.max.x >= 1) return false;
                    break;
            }
        }
        return true;
    }

    @Override
    public boolean load(CCRenderState ccRenderState) {
        return false;
    }

    @Override
    public void operate(CCRenderState ccRenderState) {

    }

    @Override
    public int operationID() {
        return operationIndex;
    }
}
