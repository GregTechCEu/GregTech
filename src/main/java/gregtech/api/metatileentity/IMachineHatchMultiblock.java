package gregtech.api.metatileentity;

public interface IMachineHatchMultiblock {

    /**
     * @return a String array of blacklisted RecipeMaps for the {@link gregtech.common.metatileentities.electric.multiblockpart.MetaTileEntityMachineHatch}
     */
    default String[] getBlacklist() {
        return new String[0];
    }

    default int getMachineLimit() {
        return 64;
    }

    void notifyMachineChanged();
}
