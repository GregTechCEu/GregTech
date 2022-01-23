package gregtech.api.unification.material;

import com.google.common.collect.HashBiMap;
import net.minecraft.item.EnumDyeColor;

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
    public static final MarkerMaterial Empty = new MarkerMaterial("empty");

    /**
     * Color materials
     */
    public static class Color {

        /**
         * Can be used only by direct specifying
         * Means absence of color on OrePrefix
         * Often a default value for color prefixes
         */
        public static final MarkerMaterial Colorless = new MarkerMaterial("colorless");

        public static final MarkerMaterial White = new MarkerMaterial("white");
        public static final MarkerMaterial Orange = new MarkerMaterial("orange");
        public static final MarkerMaterial Magenta = new MarkerMaterial("magenta");
        public static final MarkerMaterial LightBlue = new MarkerMaterial("light_blue");
        public static final MarkerMaterial Yellow = new MarkerMaterial("yellow");
        public static final MarkerMaterial Lime = new MarkerMaterial("lime");
        public static final MarkerMaterial Pink = new MarkerMaterial("pink");
        public static final MarkerMaterial Gray = new MarkerMaterial("gray");
        public static final MarkerMaterial LightGray = new MarkerMaterial("light_gray");
        public static final MarkerMaterial Cyan = new MarkerMaterial("cyan");
        public static final MarkerMaterial Purple = new MarkerMaterial("purple");
        public static final MarkerMaterial Blue = new MarkerMaterial("blue");
        public static final MarkerMaterial Brown = new MarkerMaterial("brown");
        public static final MarkerMaterial Green = new MarkerMaterial("green");
        public static final MarkerMaterial Red = new MarkerMaterial("red");
        public static final MarkerMaterial Black = new MarkerMaterial("black");

        /**
         * Arrays containing all possible color values (without Colorless!)
         */
        public static final MarkerMaterial[] VALUES = new MarkerMaterial[]{
                White, Orange, Magenta, LightBlue, Yellow, Lime, Pink, Gray, LightGray, Cyan, Purple, Blue, Brown, Green, Red, Black
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
        public static final Material Primitive = new MarkerMaterial("primitive");
        public static final Material Basic = new MarkerMaterial("basic");
        public static final Material Good = new MarkerMaterial("good");
        public static final Material Advanced = new MarkerMaterial("advanced");
        public static final Material Extreme = new MarkerMaterial("extreme");
        public static final Material Elite = new MarkerMaterial("elite");
        public static final Material Master = new MarkerMaterial("master");
        public static final Material Super = new MarkerMaterial("super");
        public static final Material Ultimate = new MarkerMaterial("ultimate");
        public static final Material Ultra = new MarkerMaterial("ultra");

        public static Material Insane = new MarkerMaterial("insane");
        public static Material Unreal = new MarkerMaterial("unreal");
        public static Material Epic = new MarkerMaterial("epic");
        public static Material Legendary = new MarkerMaterial("legendary");
        public static Material Maximum = new MarkerMaterial("maximum");
    }

    public static class Component {
        public static final Material Resistor = new MarkerMaterial("resistor");
        public static final Material Transistor = new MarkerMaterial("transistor");
        public static final Material Capacitor = new MarkerMaterial("capacitor");
        public static final Material Diode = new MarkerMaterial("diode");
        public static final Material Inductor = new MarkerMaterial("inductor");
    }

}
