package gregtech.api.capability;

public interface IMultiblockController {

    boolean isStructureFormed(String name);

    default boolean isStructureFormed() {
        return isStructureFormed("main");
    }

    default boolean isStructureObstructed() {
        return false;
    }
}
