package gregtech.datafix.migration.lib;

import org.jetbrains.annotations.NotNull;

public class MigrationAPI {

    private final MTERegistriesMigrator registriesMigrator = new MTERegistriesMigrator();

    public @NotNull MTERegistriesMigrator registriesMigrator() {
        return registriesMigrator;
    }
}
