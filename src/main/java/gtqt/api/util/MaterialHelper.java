package gtqt.api.util;

import gregtech.api.unification.material.Material;

import java.util.List;
import java.util.Arrays;

import static gregtech.api.unification.material.Materials.*;

public class MaterialHelper {
    public static List<Material> Plate = Arrays.asList(WroughtIron, Steel, Aluminium, StainlessSteel, Titanium, TungstenSteel, RhodiumPlatedPalladium, NaquadahAlloy, Darmstadtium, Neutronium);
    public static List<Material> Pipe = Arrays.asList(Bronze, Bronze, Steel, StainlessSteel, Titanium, TungstenSteel, NiobiumTitanium, Iridium, Naquadah, Europium, Duranium, Neutronium);
    public static List<Material> Wire = Arrays.asList(Lead, Tin, Copper, Gold, Aluminium, Tungsten, NiobiumTitanium, VanadiumGallium, YttriumBariumCuprate, NaquadahAlloy, Trinium);
    public static List<Material> Cable = Arrays.asList(RedAlloy, Tin, Copper, Gold, Aluminium, Platinum, NiobiumTitanium, VanadiumGallium, YttriumBariumCuprate, Europium);
    public static List<Material> Plastic = Arrays.asList(Glue, Glue, Polyethylene, Polyethylene, Polytetrafluoroethylene, Polytetrafluoroethylene, Polytetrafluoroethylene, Polybenzimidazole, Polybenzimidazole, Polybenzimidazole);
}

