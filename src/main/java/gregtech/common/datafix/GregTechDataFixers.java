package gregtech.common.datafix;

import gregtech.api.GTValues;
import gregtech.common.datafix.fixes.Fix0PostMachineBlockChange;
import gregtech.common.datafix.fixes.Fix0PostMachineBlockChangeInWorld;
import gregtech.common.datafix.walker.WalkChunkSection;
import gregtech.common.datafix.walker.WalkItemStackLike;
import net.minecraft.util.datafix.FixTypes;
import net.minecraft.util.datafix.IDataWalker;
import net.minecraftforge.common.util.CompoundDataFixer;
import net.minecraftforge.common.util.ModFixs;
import net.minecraftforge.fml.common.FMLCommonHandler;

public class GregTechDataFixers {

    public static final int V_INITIAL = -1;

    public static final int V0_POST_MACHINE_BLOCK_CHANGE = 0;

    public static final int DATA_VERSION = V0_POST_MACHINE_BLOCK_CHANGE;

    public static void init() {
        CompoundDataFixer fmlFixer = FMLCommonHandler.instance().getDataFixer();
        IDataWalker walkItemStackLike = new WalkItemStackLike();
        fmlFixer.registerVanillaWalker(FixTypes.BLOCK_ENTITY, walkItemStackLike);
        fmlFixer.registerVanillaWalker(FixTypes.ENTITY, walkItemStackLike);
        fmlFixer.registerVanillaWalker(FixTypes.PLAYER, walkItemStackLike);
        fmlFixer.registerVanillaWalker(FixTypes.CHUNK, new WalkChunkSection());

        ModFixs gtFixer = fmlFixer.init(GTValues.MODID, DATA_VERSION);

        // Register your new fixes here
        gtFixer.registerFix(GregTechFixType.ITEM_STACK_LIKE, new Fix0PostMachineBlockChange());
        gtFixer.registerFix(GregTechFixType.CHUNK_SECTION, new Fix0PostMachineBlockChangeInWorld());
    }
}
