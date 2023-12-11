package gregtech.api.unification.material;

import gregtech.api.GTValues;

import net.minecraft.item.EnumDyeColor;

import com.google.common.collect.HashBiMap;

public class MarkerMaterials {

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static void register() {
        Color.Colorless.toString();
        Tier.ULV.toString();
        Empty.toString();
    }

    /**
     * Marker materials without category
     */
    public static final MarkerMaterial Empty = MarkerMaterial.create("empty");

    /**
     * Color materials
     */
    public static class Color {

        /**
         * Can be used only by direct specifying
         * Means absence of color on OrePrefix
         * Often a default value for color prefixes
         */
        public static final MarkerMaterial Colorless = MarkerMaterial.create("colorless");

        public static final MarkerMaterial White = MarkerMaterial.create("white");
        public static final MarkerMaterial Orange = MarkerMaterial.create("orange");
        public static final MarkerMaterial Magenta = MarkerMaterial.create("magenta");
        public static final MarkerMaterial LightBlue = MarkerMaterial.create("light_blue");
        public static final MarkerMaterial Yellow = MarkerMaterial.create("yellow");
        public static final MarkerMaterial Lime = MarkerMaterial.create("lime");
        public static final MarkerMaterial Pink = MarkerMaterial.create("pink");
        public static final MarkerMaterial Gray = MarkerMaterial.create("gray");
        public static final MarkerMaterial LightGray = MarkerMaterial.create("light_gray");
        public static final MarkerMaterial Cyan = MarkerMaterial.create("cyan");
        public static final MarkerMaterial Purple = MarkerMaterial.create("purple");
        public static final MarkerMaterial Blue = MarkerMaterial.create("blue");
        public static final MarkerMaterial Brown = MarkerMaterial.create("brown");
        public static final MarkerMaterial Green = MarkerMaterial.create("green");
        public static final MarkerMaterial Red = MarkerMaterial.create("red");
        public static final MarkerMaterial Black = MarkerMaterial.create("black");

        /**
         * Arrays containing all possible color values (without Colorless!)
         */
        public static final MarkerMaterial[] VALUES = {
                White, Orange, Magenta, LightBlue, Yellow, Lime, Pink, Gray, LightGray, Cyan, Purple, Blue, Brown,
                Green, Red, Black
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

        public static final Material ULV = MarkerMaterial.create(GTValues.VN[GTValues.ULV].toLowerCase());
        public static final Material LV = MarkerMaterial.create(GTValues.VN[GTValues.LV].toLowerCase());
        public static final Material MV = MarkerMaterial.create(GTValues.VN[GTValues.MV].toLowerCase());
        public static final Material HV = MarkerMaterial.create(GTValues.VN[GTValues.HV].toLowerCase());
        public static final Material EV = MarkerMaterial.create(GTValues.VN[GTValues.EV].toLowerCase());
        public static final Material IV = MarkerMaterial.create(GTValues.VN[GTValues.IV].toLowerCase());
        public static final Material LuV = MarkerMaterial.create(GTValues.VN[GTValues.LuV].toLowerCase());
        public static final Material ZPM = MarkerMaterial.create(GTValues.VN[GTValues.ZPM].toLowerCase());
        public static final Material UV = MarkerMaterial.create(GTValues.VN[GTValues.UV].toLowerCase());
        public static final Material UHV = MarkerMaterial.create(GTValues.VN[GTValues.UHV].toLowerCase());

        public static final Material UEV = MarkerMaterial.create(GTValues.VN[GTValues.UEV].toLowerCase());
        public static final Material UIV = MarkerMaterial.create(GTValues.VN[GTValues.UIV].toLowerCase());
        public static final Material UXV = MarkerMaterial.create(GTValues.VN[GTValues.UXV].toLowerCase());
        public static final Material OpV = MarkerMaterial.create(GTValues.VN[GTValues.OpV].toLowerCase());
        public static final Material MAX = MarkerMaterial.create(GTValues.VN[GTValues.MAX].toLowerCase());
    }

    public static class Component {

        public static final Material Resistor = MarkerMaterial.create("resistor");
        public static final Material Transistor = MarkerMaterial.create("transistor");
        public static final Material Capacitor = MarkerMaterial.create("capacitor");
        public static final Material Diode = MarkerMaterial.create("diode");
        public static final Material Inductor = MarkerMaterial.create("inductor");
    }
}
