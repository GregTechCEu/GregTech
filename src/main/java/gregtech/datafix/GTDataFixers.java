package gregtech.datafix;

import gregtech.api.GTValues;
import gregtech.datafix.fixes.Fix0PostMTERegistriesBlocksTEs;
import gregtech.datafix.fixes.Fix0PostMTERegistriesItems;
import gregtech.datafix.walker.WalkChunkSection;
import gregtech.datafix.walker.WalkItemStackLike;

import net.minecraft.util.datafix.FixTypes;
import net.minecraft.util.datafix.IDataWalker;
import net.minecraft.util.datafix.IFixableData;
import net.minecraftforge.common.util.CompoundDataFixer;
import net.minecraftforge.common.util.ModFixs;
import net.minecraftforge.fml.common.FMLCommonHandler;

public final class GTDataFixers {

    /**
     * Version of data before multiple MTE registries were possible
     */
    public static final int V0_PRE_MTE = 0;

    /**
     * Version of data after multiple MTE registries were possible
     */
    public static final int V1_POST_MTE = 1;

    /**
     * Current data version for GT
     */
    public static final int DATA_VERSION = V1_POST_MTE;

    private GTDataFixers() {}

    public static void init() {
        CompoundDataFixer forgeFixer = FMLCommonHandler.instance().getDataFixer();
        IDataWalker itemStackWalker = new WalkItemStackLike();
        forgeFixer.registerVanillaWalker(FixTypes.BLOCK_ENTITY, itemStackWalker);
        forgeFixer.registerVanillaWalker(FixTypes.ENTITY, itemStackWalker);
        forgeFixer.registerVanillaWalker(FixTypes.PLAYER, itemStackWalker);
        forgeFixer.registerVanillaWalker(FixTypes.CHUNK, new WalkChunkSection());

        ModFixs gtFixer = forgeFixer.init(GTValues.MODID, DATA_VERSION);
        IFixableData itemFixer = new Fix0PostMTERegistriesItems();
        gtFixer.registerFix(FixTypes.BLOCK_ENTITY, itemFixer);
        gtFixer.registerFix(FixTypes.ENTITY, itemFixer);
        gtFixer.registerFix(FixTypes.PLAYER, itemFixer);
        gtFixer.registerFix(FixTypes.CHUNK, new Fix0PostMTERegistriesBlocksTEs());
    }
}
