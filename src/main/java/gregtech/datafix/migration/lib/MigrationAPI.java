package gregtech.datafix.migration.lib;

import org.jetbrains.annotations.NotNull;

public final class MigrationAPI {

    private final MTERegistriesMigrator registriesMigrator = new MTERegistriesMigrator();

    /**
     * @return the data migrator for the Multiple MTE Registries functionality
     */
    public @NotNull MTERegistriesMigrator registriesMigrator() {
        return registriesMigrator;
    }
}
