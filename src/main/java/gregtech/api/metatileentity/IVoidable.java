package gregtech.api.metatileentity;

import net.minecraft.util.IStringSerializable;

import javax.annotation.Nonnull;

public interface IVoidable {

    boolean canVoidRecipeItemOutputs();

    boolean canVoidRecipeFluidOutputs();

    // -1 is taken into account as a skip case. I would have passed Integer.MAX_VALUE, but that would have been bad for some sublisting stuff
    default int getItemOutputLimit() {
        return -1;
    }

    default int getFluidOutputLimit() {
        return -1;
    }

    enum VoidingMode implements IStringSerializable {
        VOID_NONE("gregtech.gui.multiblock_no_voiding"),
        VOID_ITEMS("gregtech.gui.multiblock_item_voiding"),
        VOID_FLUIDS("gregtech.gui.multiblock_fluid_voiding"),
        VOID_BOTH("gregtech.gui.multiblock_item_fluid_voiding");

        public static final VoidingMode[] VALUES = values();

        public final String localeName;

        VoidingMode(String name) {
            this.localeName = name;
        }

        @Nonnull
        @Override
        public String getName() {
            return localeName;
        }
    }

}
