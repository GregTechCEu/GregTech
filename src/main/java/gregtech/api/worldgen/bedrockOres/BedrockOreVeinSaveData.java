package gregtech.api.worldgen.bedrockOres;

import gregtech.api.GTValues;

import gregtech.api.worldgen.bedrockFluids.BedrockFluidVeinHandler;
import gregtech.api.worldgen.bedrockFluids.ChunkPosDimension;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.world.storage.WorldSavedData;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;

import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class BedrockOreVeinSaveData extends WorldSavedData {

    private static BedrockOreVeinSaveData INSTANCE;
    public static final String dataName = GTValues.MODID + ".bedrockOreVeinData";

    public BedrockOreVeinSaveData(String s) {
        super(s);
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        NBTTagList veinList = nbt.getTagList("veinInfo", 10);
        BedrockOreVeinHandler.veinCache.clear();
        for (int i = 0; i < veinList.tagCount(); i++) {
            NBTTagCompound tag = veinList.getCompoundTagAt(i);
            ChunkPosDimension coords = ChunkPosDimension.readFromNBT(tag);
            if (coords != null) {
                BedrockOreVeinHandler.OreVeinWorldEntry info = BedrockOreVeinHandler.OreVeinWorldEntry
                        .readFromNBT(tag.getCompoundTag("info"));
                BedrockOreVeinHandler.veinCache.put(coords, info);
            }
        }

        if (nbt.hasKey("version")) {
            BedrockOreVeinHandler.saveDataVersion = nbt.getInteger("version");
        } else if (veinList.isEmpty()) {
            // there are no veins, so there is no data to be changed or lost by bumping the version
            BedrockOreVeinHandler.saveDataVersion = 1;
        } else {
            // version number was added to the save data with version 2
            BedrockOreVeinHandler.saveDataVersion = 1;
        }
    }

    @Override
    public @NotNull NBTTagCompound writeToNBT(@NotNull NBTTagCompound nbt) {
        NBTTagList oilList = new NBTTagList();
        for (Map.Entry<ChunkPosDimension, BedrockOreVeinHandler.OreVeinWorldEntry> e : BedrockOreVeinHandler.veinCache
                .entrySet()) {
            if (e.getKey() != null && e.getValue() != null) {
                NBTTagCompound tag = e.getKey().writeToNBT();
                tag.setTag("info", e.getValue().writeToNBT());
                oilList.appendTag(tag);
            }
        }

        nbt.setTag("veinInfo", oilList);
        nbt.setInteger("version", BedrockOreVeinHandler.saveDataVersion);

        return nbt;
    }

    public static void setDirty() {
        if (FMLCommonHandler.instance().getEffectiveSide() == Side.SERVER && INSTANCE != null)
            INSTANCE.markDirty();
    }

    public static void setInstance(BedrockOreVeinSaveData in) {
        if (FMLCommonHandler.instance().getEffectiveSide() == Side.SERVER)
            INSTANCE = in;
    }
}
