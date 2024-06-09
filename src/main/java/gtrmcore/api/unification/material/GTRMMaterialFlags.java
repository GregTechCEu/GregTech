package gtrmcore.api.unification.material;

import gregtech.api.unification.material.Materials;

import static gregtech.api.unification.material.info.MaterialFlags.*;

public class GTRMMaterialFlags {

    public static void init() {
        // Invar
        Materials.Invar.addFlags(GENERATE_SMALL_GEAR);

        // Graphene
        Materials.Graphene.addFlags(GENERATE_FINE_WIRE);

        // Kanthal
        Materials.Kanthal.addFlags(GENERATE_FINE_WIRE);

        // Paper
        Materials.Paper.addFlags(GENERATE_RING);

        // WroughtIron
        Materials.WroughtIron.addFlags(GENERATE_SMALL_GEAR);

        // RedAlloy
        Materials.RedAlloy.addFlags(GENERATE_ROD);
    }
}
