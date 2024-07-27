package gregtech.api.capability.data.query;

public abstract class DataQueryObject {

    private static int ID = 0;

    private final int id;

    private boolean shouldTriggerWalker = false;

    public DataQueryObject() {
        this.id = ID++;
    }

    public void setShouldTriggerWalker(boolean shouldTriggerWalker) {
        this.shouldTriggerWalker = shouldTriggerWalker;
    }

    public boolean shouldTriggerWalker() {
        return shouldTriggerWalker;
    }

    public abstract DataQueryFormat getFormat();

    @Override
    public int hashCode() {
        return id;
    }
}
