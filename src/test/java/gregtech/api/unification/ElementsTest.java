package gregtech.api.unification;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ElementsTest {

    @Test
    public void test() {
        assertEquals(Elements.Au, Elements.get("Gold"));
        assertEquals(Elements.Au, Elements.get("gold"));
        assertEquals(Elements.Au, Elements.get("Au"));
        assertNull(Elements.get("_gold"));
        assertNull(Elements.get("gold_"));
        assertEquals(Elements.Au, Elements.get("Au"));
        assertNull(Elements.get("au"));
        assertNull(Elements.get("aU"));
        assertEquals(Elements.U, Elements.get("Uranium"));
        assertEquals(Elements.U235, Elements.get("Uranium-235"));
        assertEquals(Elements.U238, Elements.get("Uranium-238"));
        assertEquals(Elements.U238, Elements.get("U-238"));
        assertNull(Elements.get("uranium_238"));
        assertNull(Elements.get("uranium 238"));
        assertNull(Elements.get("uranium238"));
        assertNull(Elements.get("u-238"));
        assertEquals(Elements.Nq1, Elements.get("NaquadahEnriched"));
        assertEquals(Elements.Nq1, Elements.get("naquadahEnriched"));
        assertNull(Elements.get("Naquadahenriched"));
        assertNull(Elements.get("naquadahenriched"));
        assertEquals(Elements.Nq1, Elements.get("naquadah_enriched"));
        assertNull(Elements.get("NAQUADAH_ENRICHED"));
        assertEquals(Elements.Nq1, Elements.get("Nq+"));
        assertEquals(Elements.Nq2, Elements.get("*Nq*"));

        testElementName("gold", false);
        testElementName("gold_12", false);
        testElementName("Gold12", false);
        testElementName("Gold-12", true);
        testElementName("GOLD-12", true);
    }

    private static void testElementName(String name, boolean works) {
        if (works) {
            Elements.add(-1, -1, -1, null, name, "Any", false);
        } else {
            assertThrows(IllegalArgumentException.class, () -> Elements.add(-1, -1, -1, null, name, "Any", false));
        }
    }
}
