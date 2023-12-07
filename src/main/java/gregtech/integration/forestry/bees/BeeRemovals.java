package gregtech.integration.forestry.bees;

import gregtech.api.GTValues;
import gregtech.integration.IntegrationModule;

import net.minecraftforge.fml.common.Loader;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

public class BeeRemovals {

    private static final List<String> MB_REMOVALS = new ArrayList<>();
    private static final List<String> EB_REMOVALS = new ArrayList<>();

    public static void init() {
        if (Loader.isModLoaded(GTValues.MODID_MB)) {
            removeMagicBees();
        }
        if (Loader.isModLoaded(GTValues.MODID_EB)) {
            removeExtraBees();
        }
    }

    // No breed tree issues with these removals
    private static void removeMagicBees() {
        MB_REMOVALS.add("FLUIX");
        MB_REMOVALS.add("CERTUS");
        MB_REMOVALS.add("SILICON");
        MB_REMOVALS.add("APATITE");
        MB_REMOVALS.add("EMERALD");
        MB_REMOVALS.add("DIAMOND");
        MB_REMOVALS.add("BRONZE");
        MB_REMOVALS.add("INVAR");
        MB_REMOVALS.add("NICKEL");
        MB_REMOVALS.add("PLATINUM");
        MB_REMOVALS.add("ELECTRUM");
        MB_REMOVALS.add("OSMIUM");
        MB_REMOVALS.add("ALUMINIUM");
        MB_REMOVALS.add("LEAD");
        MB_REMOVALS.add("SILVER");
        MB_REMOVALS.add("TIN");
        MB_REMOVALS.add("COPPER");
        MB_REMOVALS.add("GOLD");
        MB_REMOVALS.add("IRON");

        try {
            Class<?> mbBeeDefinition = Class.forName("magicbees.bees.EnumBeeSpecies");
            Field enabledField = mbBeeDefinition.getDeclaredField("enabledOverride");
            enabledField.setAccessible(true);

            for (var o : mbBeeDefinition.getEnumConstants()) {
                if (o instanceof Enum<?>bee) {
                    String name = bee.name();
                    if (MB_REMOVALS.contains(name)) {
                        try {
                            enabledField.set(bee, false);
                        } catch (IllegalAccessException e) {
                            IntegrationModule.logger.error("Failed to disable bee {}! Skipping...", name);
                        }
                    }
                }
            }

        } catch (ClassNotFoundException e) {
            IntegrationModule.logger.error("Could not find MagicBees EnumBeeSpecies! Skipping...");
        } catch (NoSuchFieldException e) {
            IntegrationModule.logger.error("Could not find MagicBees \"enabledOverride\" field! Skipping...");
        }
    }

    private static void removeExtraBees() {
        EB_REMOVALS.add("COPPER");
        EB_REMOVALS.add("TIN");
        EB_REMOVALS.add("IRON");
        EB_REMOVALS.add("LEAD");
        EB_REMOVALS.add("ZINC");
        EB_REMOVALS.add("TITANIUM");
        EB_REMOVALS.add("TUNGSTATE");
        EB_REMOVALS.add("NICKEL");
        EB_REMOVALS.add("GOLD");
        EB_REMOVALS.add("SILVER");
        EB_REMOVALS.add("PLATINUM");
        EB_REMOVALS.add("LAPIS");
        EB_REMOVALS.add("SODALITE");
        EB_REMOVALS.add("PYRITE");
        EB_REMOVALS.add("BAUXITE");
        EB_REMOVALS.add("CINNABAR");
        EB_REMOVALS.add("SPHALERITE");
        EB_REMOVALS.add("EMERALD");
        EB_REMOVALS.add("RUBY");
        EB_REMOVALS.add("SAPPHIRE");
        EB_REMOVALS.add("DIAMOND");
        EB_REMOVALS.add("NUCLEAR");
        EB_REMOVALS.add("RADIOACTIVE");
        EB_REMOVALS.add("YELLORIUM");
        EB_REMOVALS.add("CYANITE");
        EB_REMOVALS.add("BLUTONIUM");

        try {
            Class<?> ebBeeDefinition = Class.forName("binnie.extrabees.genetics.ExtraBeeDefinition");
            Field branchField = ebBeeDefinition.getDeclaredField("branch");
            Field speciesBuilderField = ebBeeDefinition.getDeclaredField("speciesBuilder");
            branchField.setAccessible(true);
            speciesBuilderField.setAccessible(true);
            Field modifiersField = Field.class.getDeclaredField("modifiers");
            modifiersField.setAccessible(true);
            modifiersField.setInt(branchField, branchField.getModifiers() & ~Modifier.FINAL);
            modifiersField.setInt(speciesBuilderField, speciesBuilderField.getModifiers() & ~Modifier.FINAL);

            for (var o : ebBeeDefinition.getEnumConstants()) {
                if (o instanceof Enum<?>bee) {
                    String name = bee.name();
                    if (EB_REMOVALS.contains(name)) {
                        branchField.set(bee, null);
                        speciesBuilderField.set(bee, null);
                    }
                }
            }
        } catch (ClassNotFoundException e) {
            IntegrationModule.logger.error("Could not find ExtraBees ExtraBeeDefinition! Skipping...");
        } catch (NoSuchFieldException e) {
            IntegrationModule.logger.error("Could not find ExtraBees \"branch\" field! Skipping...");
        } catch (IllegalAccessException e) {
            IntegrationModule.logger.error("Could not properly set ExtraBees \"branch\" field! Skipping...");
        }
    }
}
