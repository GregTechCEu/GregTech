package gregtech.client.renderer.cclop;

import codechicken.lib.vec.uv.UV;
import codechicken.lib.vec.uv.UVTransformation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class UVMirror extends UVTransformation {
    public final double minU;
    public final double maxU;
    public final double minV;
    public final double maxV;

    public UVMirror(double minU, double maxU, double minV, double maxV) {
        this.minU = minU;
        this.maxU = maxU;
        this.minV = minV;
        this.maxV = maxV;
    }

    @Override
    public void apply(UV vec) {
        if (vec.u == minU) {
            vec.u = maxU;
        } else if (vec.u == maxU) {
            vec.u = minU;
        }
        if (vec.v == minV) {
            vec.v = maxV;
        } else if (vec.v == maxV) {
            vec.v = minV;
        }
    }

    @Override
    public UVTransformation inverse() {
        return null;
    }

}
