package gtqt.api.util;

import gregtech.api.unification.material.Material;

import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;

import static gregtech.api.unification.material.Materials.*;

public class MaterialHelper {
    public static List<Material> Plate = new ArrayList<>(Arrays.asList(WroughtIron, Steel, Aluminium, StainlessSteel, Titanium, TungstenSteel, RhodiumPlatedPalladium, NaquadahAlloy, Darmstadtium, Neutronium));
    public static List<Material> Pipe = new ArrayList<>(Arrays.asList(Bronze, Bronze, Steel, StainlessSteel, Titanium, TungstenSteel, NiobiumTitanium, Iridium, Naquadah, Duranium));
    public static List<Material> Wire = new ArrayList<>(Arrays.asList(Lead, Tin, Copper, Gold, Aluminium, Tungsten, NiobiumTitanium, VanadiumGallium, YttriumBariumCuprate, NaquadahAlloy));
    public static List<Material> Cable = new ArrayList<>(Arrays.asList(RedAlloy, Tin, Copper, Gold, Aluminium, Platinum, NiobiumTitanium, VanadiumGallium, YttriumBariumCuprate, Europium));
    public static List<Material> Plastic = new ArrayList<>(Arrays.asList(Glue, Glue, Polyethylene, Polyethylene, Polytetrafluoroethylene, Polytetrafluoroethylene, Polytetrafluoroethylene, Polybenzimidazole, Polybenzimidazole, Polybenzimidazole));
}

