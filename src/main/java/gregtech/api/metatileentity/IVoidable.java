package gregtech.api.metatileentity;

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

}
