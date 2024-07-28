package gregtech.client.renderer.pipe.quad;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public enum UVCorner {

    UL,
    UR,
    DR,
    DL;

    public static final UVCorner[] VALUES = values();
}
