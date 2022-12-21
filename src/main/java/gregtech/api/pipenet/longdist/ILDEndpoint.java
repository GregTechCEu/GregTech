package gregtech.api.pipenet.longdist;

import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public interface ILDEndpoint {

    void setInput();

    void setOutput();

    void setUnknown();

    boolean isInput();

    boolean isOutput();

    ILDEndpoint getLink();

    void invalidateLink();

    LongDistancePipeType getPipeType();

    BlockPos getPos();

    static ILDEndpoint tryGet(World world, BlockPos pos) {
        TileEntity te = world.getTileEntity(pos);
        if (te instanceof IGregTechTileEntity) {
            MetaTileEntity mte = ((IGregTechTileEntity) te).getMetaTileEntity();
            if (mte instanceof ILDEndpoint) {
                return (ILDEndpoint) mte;
            }
        }
        return null;
    }
}
