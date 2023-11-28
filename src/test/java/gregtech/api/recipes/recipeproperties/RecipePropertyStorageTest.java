package gregtech.api.recipes.recipeproperties;

import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.hamcrest.CoreMatchers.is;

public class RecipePropertyStorageTest {

    private static final String propInt1Key = "propInt1";

    private static final DefaultProperty<Integer> propInt1 = new DefaultProperty<>(propInt1Key, Integer.class);
    private static final DefaultProperty<Integer> propInt2 = new DefaultProperty<>("propInt2", Integer.class);
    private static final DefaultProperty<Integer> propInt1_2 = new DefaultProperty<>("propInt1", Integer.class);
    private static final DefaultProperty<Integer> wrongCast = new DefaultProperty<>("wrongCast", Integer.class);

    private RecipePropertyStorage storage;

    @BeforeEach
    public void initTestStub() {
        this.storage = new RecipePropertyStorage();
    }

    @Test
    public void storing_unique_recipe_properties_succeeds() {
        MatcherAssert.assertThat(storage.store(propInt1, 1), is(true));
        MatcherAssert.assertThat(storage.store(propInt2, 1), is(true));
    }

    @Test
    public void storing_same_property_twice_fails() {
        MatcherAssert.assertThat(storage.store(propInt1, 1), is(true));
        MatcherAssert.assertThat(storage.store(propInt1, 1), is(false));
    }

    @Test
    public void storing_unique_properties_with_same_key_fails() {
        MatcherAssert.assertThat(storage.store(propInt1, 1), is(true));
        MatcherAssert.assertThat(storage.store(propInt1_2, 1), is(false));
    }

    @Test
    public void storing_property_with_wrong_cast_fails() {
        MatcherAssert.assertThat(storage.store(wrongCast, "This is not int"), is(false));
    }

    @Test
    public void storing_property_without_value_fails() {
        MatcherAssert.assertThat(storage.store(propInt1, null), is(false));
    }

    @Test
    public void get_size_returns_correct_value() {
        storage.store(propInt1, 1); // succeeds

        MatcherAssert.assertThat(storage.getSize(), is(1));

        storage.store(propInt2, 2); // succeeds

        MatcherAssert.assertThat(storage.getSize(), is(2));

        storage.store(propInt1, 1); // fails

        MatcherAssert.assertThat(storage.getSize(), is(2));
    }

    @Test
    public void get_recipe_properties_returns_correct_value() {
        storage.store(propInt1, 1); // succeeds
        storage.store(propInt2, 2); // succeeds

        Map<RecipeProperty<?>, Object> map = new HashMap<>();
        map.put(propInt1, 1);
        map.put(propInt2, 2);
        Set<Map.Entry<RecipeProperty<?>, Object>> expectedProperties = map.entrySet();

        Set<Map.Entry<RecipeProperty<?>, Object>> actualProperties = storage.getRecipeProperties();

        MatcherAssert.assertThat(actualProperties.size(), is(2));
        MatcherAssert.assertThat(
                actualProperties.containsAll(expectedProperties) && expectedProperties.containsAll(actualProperties),
                is(true));
    }

    @Test
    public void get_recipe_property_value_returns_correct_value_if_exists() {
        final int expectedValue = 1;
        storage.store(propInt1, expectedValue); // succeeds

        int actual = storage.getRecipePropertyValue(propInt1, 0);

        MatcherAssert.assertThat(actual, is(expectedValue));
    }

    @Test
    public void get_recipe_property_value_returns_default_value_if_does_not_exists() {
        final int expectedValue = 0;
        storage.store(propInt1, 1); // succeeds

        int actual = storage.getRecipePropertyValue(propInt2, expectedValue);

        MatcherAssert.assertThat(actual, is(expectedValue));
    }

    @Test
    // CT way
    public void get_recipe_property_keys() {
        storage.store(propInt1, 1); // succeeds
        storage.store(propInt2, 2); // succeeds

        Set<String> expectedKeys = new HashSet<>();
        expectedKeys.add(propInt1.getKey());
        expectedKeys.add(propInt2.getKey());

        Set<String> actualKeys = storage.getRecipePropertyKeys();

        MatcherAssert.assertThat(expectedKeys.containsAll(actualKeys) && actualKeys.containsAll(expectedKeys),
                is(true));
    }

    @Test
    // CT way
    public void get_raw_recipe_property_value_via_string_key() {
        final int expectedValue = 1;

        storage.store(propInt1, expectedValue); // succeeds

        Object actualValue = storage.getRawRecipePropertyValue(propInt1.getKey());

        MatcherAssert.assertThat(actualValue, is(expectedValue));
    }
}
