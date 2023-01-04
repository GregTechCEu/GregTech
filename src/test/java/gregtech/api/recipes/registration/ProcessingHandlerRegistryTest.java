package gregtech.api.recipes.registration;

import gregtech.Bootstrap;
import gregtech.api.GTValues;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.material.Materials;
import gregtech.api.unification.material.properties.BlastProperty;
import gregtech.api.unification.material.properties.PropertyKey;
import gregtech.api.unification.ore.OrePrefix;
import net.minecraft.util.ResourceLocation;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javax.annotation.Nonnull;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.core.IsNot.not;

public class ProcessingHandlerRegistryTest {

    @BeforeAll
    public static void setup() {
        Bootstrap.perform();
    }

    @AfterEach
    public void cleanup() {
        // clear and re-register everything for the next test(s)
        ProcessingHandlerRegistry.clear();
        ProcessingHandlerRegistry.register(this.getClass());
    }

    @Test
    public void testRegistration() {
        Assertions.assertDoesNotThrow(() -> ProcessingHandlerRegistry.register(this.getClass()));

        // Ensure all 4 methods got registered
        MatcherAssert.assertThat(ProcessingHandlerRegistry.size(), is(4));

        final ResourceLocation location = new ResourceLocation(GTValues.MODID, "processing_handler_registry_test.material_handler");
        ProcessingHandlerRegistry.blacklist(location, Materials.Aluminium);

        // Ensure the blacklist got registered
        MatcherAssert.assertThat(ProcessingHandlerRegistry.isBlacklisted(location, Materials.Aluminium), is(true));

        ProcessingHandlerRegistry.runHandlers();
    }

    @Test
    public void testRemoval() {
        // Starts with 4
        MatcherAssert.assertThat(ProcessingHandlerRegistry.size(), is(4));

        final ResourceLocation location = new ResourceLocation(GTValues.MODID, "processing_handler_registry_test.material_handler");

        // Remove a registered handler
        MatcherAssert.assertThat(ProcessingHandlerRegistry.remove(location), is(true));

        // Size should decrease to 3
        MatcherAssert.assertThat(ProcessingHandlerRegistry.size(), is(3));
    }

    @SuppressWarnings("unused")
    @ProcessingHandler
    public static void materialHandler(@Nonnull Material material) {
        // test that blacklisting Aluminium worked
        MatcherAssert.assertThat(material, not(Materials.Aluminium));
    }

    @SuppressWarnings("unused")
    @ProcessingHandler(type = ProcessingHandler.Type.PROPERTY)
    public static void propertyHandler(@Nonnull BlastProperty property, @Nonnull Material material) {
        MatcherAssert.assertThat(material.hasProperty(PropertyKey.BLAST), is(true));
        MatcherAssert.assertThat(property, is(material.getProperty(PropertyKey.BLAST)));
    }

    @SuppressWarnings("unused")
    @ProcessingHandler(type = ProcessingHandler.Type.ORE_PREFIX, prefixes = {"ingot"})
    public static void prefixHandler(@Nonnull OrePrefix prefix, @Nonnull Material material) {
        MatcherAssert.assertThat(prefix, is(OrePrefix.ingot));
    }

    @SuppressWarnings("unused")
    @ProcessingHandler(type = ProcessingHandler.Type.PROPERTY_PREFIX, prefixes = {"ingot"})
    public static void propertyPrefixHandler(@Nonnull BlastProperty property, @Nonnull OrePrefix prefix, @Nonnull Material material) {
        MatcherAssert.assertThat(material.hasProperty(PropertyKey.BLAST), is(true));
        MatcherAssert.assertThat(property, is(material.getProperty(PropertyKey.BLAST)));
        MatcherAssert.assertThat(prefix, is(OrePrefix.ingot));
    }
}
