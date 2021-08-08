package gregtech.api.util.world;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.datafix.DataFixer;
import net.minecraft.world.World;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.storage.IChunkLoader;
import net.minecraft.world.gen.structure.template.TemplateManager;
import net.minecraft.world.storage.IPlayerFileData;
import net.minecraft.world.storage.ISaveHandler;
import net.minecraft.world.storage.WorldInfo;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;

public class DummySaveHandler implements ISaveHandler, IPlayerFileData, IChunkLoader {

    @Nullable
    @Override
    public WorldInfo loadWorldInfo() {
        return null;
    }

    @Override
    public void checkSessionLock() {
    }

    @Nonnull
    @Override
    public IChunkLoader getChunkLoader(@Nonnull WorldProvider provider) {
        return this;
    }

    @Nonnull
    @Override
    public IPlayerFileData getPlayerNBTManager() {
        return this;
    }


    @Nonnull
    @Override
    public TemplateManager getStructureTemplateManager() {
        return new TemplateManager("", new DataFixer(0));
    }

    @Override
    public void saveWorldInfoWithPlayer(@Nonnull WorldInfo worldInformation, @Nonnull NBTTagCompound tagCompound) {
    }

    @Override
    public void saveWorldInfo(@Nonnull WorldInfo worldInformation) {
    }

    @Nonnull
    @Override
    public File getWorldDirectory() {
        return null;
    }

    @Nonnull
    @Override
    public File getMapFileFromName(@Nonnull String mapName) {
        return null;
    }


    @Nullable
    @Override
    public Chunk loadChunk(@Nonnull World worldIn, int x, int z) {
        return null;
    }

    @Override
    public void saveChunk(@Nonnull World worldIn, @Nonnull Chunk chunkIn) {
    }

    @Override
    public void saveExtraChunkData(@Nonnull World worldIn, @Nonnull Chunk chunkIn) {
    }

    @Override
    public void chunkTick() {
    }

    @Override
    public void flush() {
    }

    @Override
    public boolean isChunkGeneratedAt(int x, int z) {
        return false;
    }

    @Override
    public void writePlayerData(@Nonnull EntityPlayer player) {
    }

    @Nullable
    @Override
    public NBTTagCompound readPlayerData(@Nonnull EntityPlayer player) {
        return null;
    }

    @Nonnull
    @Override
    public String[] getAvailablePlayerDat() {
        return new String[0];
    }
}
