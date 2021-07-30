package gregtech.common.pipelike.cable.net;

import gregtech.api.unification.material.properties.WireProperties;
import gregtech.common.pipelike.cable.tile.TileEntityCable;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;

import java.util.HashMap;

public class RoutePath {

    public BlockPos destination;
    public HashMap<BlockPos, WireProperties> path = new HashMap<>();
    public int maxAmperage = Integer.MAX_VALUE;
    public int minVoltage = Integer.MAX_VALUE;
    public int totalLoss;

    public RoutePath cloneAndCompute(BlockPos destination) {
        RoutePath newPath = new RoutePath();
        newPath.path = new HashMap<>(path);
        newPath.destination = destination;
        for (WireProperties wireProperties : path.values()) {
            newPath.maxAmperage = Math.min(newPath.maxAmperage, wireProperties.amperage);
            newPath.minVoltage = Math.min(newPath.minVoltage, wireProperties.voltage);
            newPath.totalLoss += wireProperties.lossPerBlock;
        }
        return newPath;
    }

    public boolean burnCablesInPath(World world, long voltage, long amperage) {
        for (BlockPos blockPos : path.keySet()) {
            WireProperties wireProperties = path.get(blockPos);
            if (voltage > wireProperties.voltage || amperage > wireProperties.amperage) {
                TileEntity tileEntity = world.getTileEntity(blockPos);
                if (tileEntity instanceof TileEntityCable) {
                    world.setBlockToAir(blockPos);
                    world.setBlockState(blockPos, Blocks.FIRE.getDefaultState());

                    if (!world.isRemote) {
                        ((WorldServer) world).spawnParticle(EnumParticleTypes.SMOKE_LARGE,
                            blockPos.getX() + 0.5, blockPos.getY() + 0.5, blockPos.getZ() + 0.5,
                            5 + world.rand.nextInt(3), 0.0, 0.0, 0.0, 0.1);
                    }
                }
            }
        }
        return true;
    }

}
