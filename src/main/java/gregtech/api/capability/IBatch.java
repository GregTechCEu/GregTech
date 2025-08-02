package gregtech.api.capability;

public interface IBatch {

    boolean isBatchAllowed();

    boolean isBatchEnable();

    void setBatchEnable(boolean isBatchAllowed);
}
