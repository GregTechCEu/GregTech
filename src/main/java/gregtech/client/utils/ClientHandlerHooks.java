package gregtech.client.utils;

import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.util.GTUtility;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.List;

public class ClientHandlerHooks {

    /**
     * Creates a MetaTileEntity from each tag and sets them in the world
     *
     * @param world        typically client world
     * @param tagCompounds list of TileEntity tag data
     */
    public static void handleTags(World world, List<NBTTagCompound> tagCompounds) {
        for (NBTTagCompound tagCompound : tagCompounds) {
            handleTag(world, tagCompound);
        }
    }

    /**
     * Creates a MetaTileEntity from the tag and sets it in the world
     *
     * @param world       typically client world
     * @param tagCompound TileEntity tag data
     */
    public static void handleTag(World world, NBTTagCompound tagCompound) {
        MetaTileEntity mte = GTUtility.getMetaTileEntity(tagCompound);
        if (mte != null) {
            BlockPos pos = new BlockPos(
                    tagCompound.getInteger("x"),
                    tagCompound.getInteger("y"),
                    tagCompound.getInteger("z"));
            placeTile(world, mte, pos);
        }
    }

    /**
     * Sets a copy of the MetaTileEntity to the world IF a TileEntity doesn't already exist at the position
     *
     * @param world client world
     * @param mte   MetaTileEntity to set in world
     * @param pos   Position to place the MetaTileEntity
     */
    private static void placeTile(World world, MetaTileEntity mte, BlockPos pos) {
        // set te in world directly
        // check if world contains a TE at this pos?
        // is null checking good enough?
        if (!GTUtility.hasTileEntity(world, pos)) {
            world.setBlockState(pos, mte.getBlock().getDefaultState());
            world.setTileEntity(pos, mte.copy());
        }
    }
}
