package gregtech.api.worldgen.bedrockFluids;

import gregtech.api.GTValues;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.world.storage.WorldSavedData;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;

import javax.annotation.Nonnull;
import java.util.Map;

public class BedrockFluidVeinSaveData extends WorldSavedData {

    private static BedrockFluidVeinSaveData INSTANCE;
    public static final String dataName = GTValues.MODID + ".bedrockFluidVeinData";

    public BedrockFluidVeinSaveData(String s) {
        super(s);
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        NBTTagList veinList = nbt.getTagList("veinInfo", 10);
        BedrockFluidVeinHandler.veinCache.clear();
        for (int i = 0; i < veinList.tagCount(); i++) {
            NBTTagCompound tag = veinList.getCompoundTagAt(i);
            ChunkPosDimension coords = ChunkPosDimension.readFromNBT(tag);
            if (coords != null) {
                BedrockFluidVeinHandler.FluidVeinWorldEntry info = BedrockFluidVeinHandler.FluidVeinWorldEntry.readFromNBT(tag.getCompoundTag("info"));
                BedrockFluidVeinHandler.veinCache.put(coords, info);
            }
        }
    }

    @Override
    public @Nonnull
    NBTTagCompound writeToNBT(@Nonnull NBTTagCompound nbt) {
        NBTTagList oilList = new NBTTagList();
        for (Map.Entry<ChunkPosDimension, BedrockFluidVeinHandler.FluidVeinWorldEntry> e : BedrockFluidVeinHandler.veinCache.entrySet()) {
            if (e.getKey() != null && e.getValue() != null) {
                NBTTagCompound tag = e.getKey().writeToNBT();
                tag.setTag("info", e.getValue().writeToNBT());
                oilList.appendTag(tag);
            }
        }
        nbt.setTag("veinInfo", oilList);

        return nbt;
    }


    public static void setDirty() {
        if (FMLCommonHandler.instance().getEffectiveSide() == Side.SERVER && INSTANCE != null)
            INSTANCE.markDirty();
    }

    public static void setInstance(BedrockFluidVeinSaveData in) {
        if (FMLCommonHandler.instance().getEffectiveSide() == Side.SERVER)
            INSTANCE = in;
    }
}
