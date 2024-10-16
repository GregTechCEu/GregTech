package gregtech.datafix.migration.api;

public abstract class AbstractMTEMigrator implements MTEMigrator {

    private final int fixVersion;

    protected AbstractMTEMigrator(int fixVersion) {
        this.fixVersion = fixVersion;
    }

    @Override
    public int fixVersion() {
        return fixVersion;
    }
}
