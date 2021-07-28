package gregtech.api.unification.material;

import com.google.common.collect.HashBiMap;
import gregtech.api.unification.material.type.MarkerMaterial;
import gregtech.api.unification.material.type.Material;
import net.minecraft.item.EnumDyeColor;

import static com.google.common.collect.ImmutableList.of;

public class MarkerMaterials {

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static void register() {
        Color.Colorless.toString();
        Tier.Primitive.toString();
        Empty.toString();
    }

    /**
     * Marker materials without category
     */
    public static MarkerMaterial Empty = new MarkerMaterial("empty");

    /**
     * Color materials
     */
    public static class Color {

        /**
         * Can be used only by direct specifying
         * Means absence of color on OrePrefix
         * Often a default value for color prefixes
         */
        public static MarkerMaterial Colorless = new MarkerMaterial("colorless");

        public static MarkerMaterial White = new MarkerMaterial("white");
        public static MarkerMaterial Orange = new MarkerMaterial("orange");
        public static MarkerMaterial Magenta = new MarkerMaterial("magenta");
        public static MarkerMaterial LightBlue = new MarkerMaterial("light_blue");
        public static MarkerMaterial Yellow = new MarkerMaterial("yellow");
        public static MarkerMaterial Lime = new MarkerMaterial("lime");
        public static MarkerMaterial Pink = new MarkerMaterial("pink");
        public static MarkerMaterial Gray = new MarkerMaterial("gray");
        public static MarkerMaterial Silver = new MarkerMaterial("silver");
        public static MarkerMaterial Cyan = new MarkerMaterial("cyan");
        public static MarkerMaterial Purple = new MarkerMaterial("purple");
        public static MarkerMaterial Blue = new MarkerMaterial("blue");
        public static MarkerMaterial Brown = new MarkerMaterial("brown");
        public static MarkerMaterial Green = new MarkerMaterial("green");
        public static MarkerMaterial Red = new MarkerMaterial("red");
        public static MarkerMaterial Black = new MarkerMaterial("black");

        /**
         * Arrays containing all possible color values (without Colorless!)
         */
        public static final MarkerMaterial[] VALUES = new MarkerMaterial[]{
            White, Orange, Magenta, LightBlue, Yellow, Lime, Pink, Gray, Silver, Cyan, Purple, Blue, Brown, Green, Red, Black
        };

        /**
         * Gets color by it's name
         * Name format is equal to EnumDyeColor
         */
        public static MarkerMaterial valueOf(String string) {
            for (MarkerMaterial color : VALUES) {
                if (color.toString().equals(string)) {
                    return color;
                }
            }
            return null;
        }

        /**
         * Contains associations between MC EnumDyeColor and Color MarkerMaterial
         */
        public static final HashBiMap<EnumDyeColor, MarkerMaterial> COLORS = HashBiMap.create();

        static {
            for (EnumDyeColor color : EnumDyeColor.values()) {
                COLORS.put(color, Color.valueOf(color.getName()));
            }
        }

    }

    /**
     * Circuitry, batteries and other technical things
     */
    public static class Tier {
        public static Material Primitive = new MarkerMaterial("primitive");
        public static Material Basic = new MarkerMaterial("basic");
        public static Material Good = new MarkerMaterial("good");
        public static Material Advanced = new MarkerMaterial("advanced");
        public static Material Extreme = new MarkerMaterial("extreme");
        public static Material Elite = new MarkerMaterial("elite");
        public static Material Master = new MarkerMaterial("master");
        public static Material Ultimate = new MarkerMaterial("ultimate");
        public static Material Superconductor = new Material(387, "superconductor", 0xFFFFFF, MaterialIconSet.NONE, of(), 0L, null) {
        };
        public static Material Infinite = new MarkerMaterial("infinite");

        public static Material UEVCircuit = new MarkerMaterial("uev");
        public static Material UIVCircuit = new MarkerMaterial("uiv");
        public static Material UMVCircuit = new MarkerMaterial("umv");
        public static Material UXVCircuit = new MarkerMaterial("uxv");
        public static Material MAXCircuit = new MarkerMaterial("max");
    }

    public static class Component {
        public static Material Resistor = new MarkerMaterial("resistor");
        public static Material Transistor = new MarkerMaterial("transistor");
        public static Material Capacitor = new MarkerMaterial("capacitor");
        public static Material Diode = new MarkerMaterial("diode");
    }

}
