package gregtech.api.capability;

public interface IMultiblockController {

    boolean isStructureFormed(String name);

    default boolean isStructureFormed() {
        return isStructureFormed("MAIN");
    }

    default boolean isStructureObstructed() {
        return false;
    }
}
