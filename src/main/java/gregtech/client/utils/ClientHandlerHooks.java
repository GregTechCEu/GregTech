package gregtech.client.utils;

import gregtech.api.GregTechAPI;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.registry.MTERegistry;

import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.server.SPacketChunkData;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;

import org.spongepowered.asm.mixin.Unique;

import java.util.List;

public class ClientHandlerHooks {

    /**
     * Initializes any MetaTileEntities right before {@link Chunk#read(PacketBuffer, int, boolean) Chunk.read()} is
     * called in
     * {@link NetHandlerPlayClient#handleChunkData(SPacketChunkData) handleChunkData()}.
     *
     * @param packetIn Chunk Data Packet
     */
    public static void handleTags(World world, List<NBTTagCompound> tagCompounds) {
        tagCompounds.forEach(tagCompound -> handleTag(world, tagCompound));
    }

    /**
     * Initializes the MetaTileEntity before it handles
     *
     * @param packetIn Tile Entity Update Packet
     */
    public static void handleTag(World world, NBTTagCompound tagCompound) {
        MetaTileEntity mte = fromTag(tagCompound);
        if (mte != null) {
            BlockPos pos = new BlockPos(tagCompound.getInteger("x"), tagCompound.getInteger("y"),
                    tagCompound.getInteger("z"));
            placeTile(world, mte, pos);
        }
    }

    /**
     * Creates the correct MetaTileEntity from the given tag and sets the tile entity to the world
     * IF a TileEntity doesn't already exist at the position
     *
     * @param tag TileEntity tag data
     */
    @Unique
    private static MetaTileEntity fromTag(NBTTagCompound tag) {
        if (tag.hasKey("MetaId")) {
            ResourceLocation metaId = new ResourceLocation(tag.getString("MetaId"));
            MTERegistry registry = GregTechAPI.mteManager.getRegistry(metaId.getNamespace());
            return registry.getObject(metaId);
        }
        return null;
    }

    private static void placeTile(World world, MetaTileEntity mte, BlockPos pos) {
        // set te in world directly
        // check if world contains a TE at this pos?
        // is null checking good enough?
        if (world.getChunk(pos).getTileEntity(pos, Chunk.EnumCreateEntityType.CHECK) == null) {
            if (world.getBlockState(pos).getBlock() != mte.getBlock())
                world.setBlockState(pos, mte.getBlock().getDefaultState());
            world.setTileEntity(pos, mte.copy());
        }
    }
}
