package gregtech.api.metatileentity;

import net.minecraft.util.IStringSerializable;

import org.jetbrains.annotations.NotNull;

public interface IVoidable {

    boolean canVoidRecipeItemOutputs();

    boolean canVoidRecipeFluidOutputs();

    default int getItemOutputLimit() {
        return Integer.MAX_VALUE;
    }

    default int getFluidOutputLimit() {
        return Integer.MAX_VALUE;
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

        @NotNull
        @Override
        public String getName() {
            return localeName;
        }
    }
}
