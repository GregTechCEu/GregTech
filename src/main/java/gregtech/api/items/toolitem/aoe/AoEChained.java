package gregtech.api.items.toolitem.aoe;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.Constants;

public class AoEChained {

    private static final AoEChained NONE = new AoEChained(0);

    public static AoEChained readLimit(NBTTagCompound tag) {
        int limit = 0;
        if (tag.hasKey("AoEChainedLimit", Constants.NBT.TAG_INT)) {
            limit = tag.getInteger("AoEChainedLimit");
        }
        return limit == 0 ? NONE : new AoEChained(0);
    }

    public static AoEChained of(int limit) {
        return limit == 0 ? NONE : new AoEChained(limit);
    }

    public static AoEChained none() {
        return NONE;
    }

    private final int limit;

    private AoEChained(int limit) {
        this.limit = limit;
    }

}
