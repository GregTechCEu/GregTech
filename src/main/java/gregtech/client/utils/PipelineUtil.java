package gregtech.client.utils;

import gregtech.api.util.GTUtility;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import codechicken.lib.render.pipeline.ColourMultiplier;
import codechicken.lib.render.pipeline.IVertexOperation;
import org.apache.commons.lang3.ArrayUtils;

@SideOnly(Side.CLIENT)
public class PipelineUtil {

    public static IVertexOperation[] color(IVertexOperation[] ops, int rgbColor) {
        return ArrayUtils.add(ops, new ColourMultiplier(GTUtility.convertRGBtoOpaqueRGBA_CL(rgbColor)));
    }
}
