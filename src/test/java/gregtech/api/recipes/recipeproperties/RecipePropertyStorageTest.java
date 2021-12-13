package gregtech.api.recipes.recipeproperties;

import org.junit.Before;
import org.junit.Test;

import java.util.*;

import static org.junit.Assert.*;

public class RecipePropertyStorageTest {
    private static final String propInt1Key = "propInt1";

    private static final DefaultProperty<Integer> propInt1 = new DefaultProperty<>(propInt1Key, Integer.class);
    private static final DefaultProperty<Integer> propInt2 = new DefaultProperty<>("propInt2", Integer.class);
    private static final DefaultProperty<Integer> propInt1_2 = new DefaultProperty<>("propInt1", Integer.class);
    private static final DefaultProperty<Integer> wrongCast = new DefaultProperty<>("wrongCast", Integer.class);

    private RecipePropertyStorage storage;

    @Before
    public void initTestStub() {
        this.storage = new RecipePropertyStorage();
    }

    @Test
    public void storing_unique_recipe_properties_succeeds() {
        assertTrue(storage.store(propInt1, 1));
        assertTrue(storage.store(propInt2, 1));
    }

    @Test
    public void storing_same_property_twice_fails() {
        assertTrue(storage.store(propInt1, 1));
        assertFalse(storage.store(propInt1, 1));
    }

    @Test
    public void storing_unique_properties_with_same_key_fails() {
        assertTrue(storage.store(propInt1, 1));
        assertFalse(storage.store(propInt1_2, 1));
    }

    @Test
    public void storing_property_with_wrong_cast_fails() {
        assertFalse(storage.store(wrongCast, "This is not int"));
    }


    @Test
    public void storing_property_without_value_fails() {
        assertFalse(storage.store(propInt1, null));
    }

    @Test
    public void get_size_returns_correct_value() {
        storage.store(propInt1, 1); //succeeds

        assertEquals(1, storage.getSize());

        storage.store(propInt2, 2); //succeeds

        assertEquals(2, storage.getSize());

        storage.store(propInt1, 1); //fails

        assertEquals(2, storage.getSize());
    }

    @Test
    public void get_recipe_properties_returns_correct_value() {
        storage.store(propInt1, 1); //succeeds
        storage.store(propInt2, 2); //succeeds

        HashMap<RecipeProperty<?>, Object> map = new HashMap<>();
        map.put(propInt1, 1);
        map.put(propInt2, 2);
        Set<Map.Entry<RecipeProperty<?>, Object>> expectedProperties = map.entrySet();

        Set<Map.Entry<RecipeProperty<?>, Object>> actualProperties = storage.getRecipeProperties();

        assertEquals(2, actualProperties.size());
        assertTrue(actualProperties.containsAll(expectedProperties) && expectedProperties.containsAll(actualProperties));
    }

    @Test
    public void get_recipe_property_value_returns_correct_value_if_exists() {
        final int expectedValue = 1;
        storage.store(propInt1, expectedValue); //succeeds

        int actual = storage.getRecipePropertyValue(propInt1, 0);

        assertEquals(expectedValue, actual);
    }

    @Test
    public void get_recipe_property_value_returns_default_value_if_does_not_exists() {
        final int expectedValue = 0;
        storage.store(propInt1, 1); //succeeds

        int actual = storage.getRecipePropertyValue(propInt2, expectedValue);

        assertEquals(expectedValue, actual);
    }

    @Test
    //CT way
    public void get_recipe_property_keys() {
        storage.store(propInt1, 1); //succeeds
        storage.store(propInt2, 2); //succeeds

        Set<String> expectedKeys = new HashSet<>();
        expectedKeys.add(propInt1.getKey());
        expectedKeys.add(propInt2.getKey());

        Set<String> actualKeys = storage.getRecipePropertyKeys();

        assertTrue(expectedKeys.containsAll(actualKeys) && actualKeys.containsAll(expectedKeys));
    }

    @Test
    //CT way
    public void get_raw_recipe_property_value_via_string_key() {
        final int expectedValue = 1;

        storage.store(propInt1, expectedValue); //succeeds

        Object actualValue = storage.getRawRecipePropertyValue(propInt1.getKey());

        assertEquals(expectedValue, actualValue);
    }
}
