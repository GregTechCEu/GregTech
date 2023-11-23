package gregtech.integration.tinkers.material;

import gregtech.api.unification.material.Materials;

public class TinkersMaterialAdditions {

    /**
     * IDEAS:
     * - Unburnable
     *     - Like Netherrite, cannot be destroyed in lava if a tool has this
     *     - Auto-apply to any fluid above MC lava temp (1300K)
     *
     * - Salty
     *     - Modifier applied with Salt
     *     - Gives a potion effect to the entity hit preventing them from healing
     *
     * - Explosive
     *     - Modifier applied with Gelled Toluene
     *     - Has a chance to cause a small explosion on entity hit (ideally not damage player)
     *
     * - Rework radioactive trait to have levels, give different levels depending on material and material type
     */

    public static void init() {

        // Cancellations, to avoid autogen nastiness
        TinkersMaterialProcessing.getBuilder(Materials.Diamond).cancel();

        // Modifications
        TinkersMaterialProcessing.getBuilder(Materials.Flint);
        TinkersMaterialProcessing.getBuilder(Materials.Iron);
        TinkersMaterialProcessing.getBuilder(Materials.Bronze);
        TinkersMaterialProcessing.getBuilder(Materials.WroughtIron);
        TinkersMaterialProcessing.getBuilder(Materials.Invar);
        TinkersMaterialProcessing.getBuilder(Materials.DamascusSteel);

        TinkersMaterialProcessing.getBuilder(Materials.Steel);
        TinkersMaterialProcessing.getBuilder(Materials.CobaltBrass);

        TinkersMaterialProcessing.getBuilder(Materials.Aluminium);
        TinkersMaterialProcessing.getBuilder(Materials.VanadiumSteel);
        TinkersMaterialProcessing.getBuilder(Materials.SterlingSilver);
        TinkersMaterialProcessing.getBuilder(Materials.RoseGold);

        TinkersMaterialProcessing.getBuilder(Materials.StainlessSteel);
        TinkersMaterialProcessing.getBuilder(Materials.BlueSteel);
        TinkersMaterialProcessing.getBuilder(Materials.RedSteel);
        // MaterialProcessing.getBuilder(Materials.NeodymiumMagnetic);

        TinkersMaterialProcessing.getBuilder(Materials.Titanium);
        TinkersMaterialProcessing.getBuilder(Materials.Ultimet);

        TinkersMaterialProcessing.getBuilder(Materials.TungstenSteel);
        TinkersMaterialProcessing.getBuilder(Materials.HSSE);
        TinkersMaterialProcessing.getBuilder(Materials.TungstenCarbide);

        TinkersMaterialProcessing.getBuilder(Materials.NaquadahAlloy);
        TinkersMaterialProcessing.getBuilder(Materials.Duranium);
        TinkersMaterialProcessing.getBuilder(Materials.Neutronium);

        // Additions

        TinkersMaterialStats.createPolymerTemplate(Materials.Polyethylene)
                .setBowString(1.2F)
                .setFletching(0.85F, 1.4F)
                .build();

        TinkersMaterialStats.createPolymerTemplate(Materials.PolyvinylChloride)
                .setBowString(1.3F)
                .setFletching(0.90F, 1.8F)
                .build();

        TinkersMaterialStats.createPolymerTemplate(Materials.Polytetrafluoroethylene)
                .setBowString(1.5F)
                .setFletching(0.95F, 2.1F)
                .build();

        TinkersMaterialStats.createPolymerTemplate(Materials.Polycaprolactam)
                .setBowString(1.7F)
                .build();

        TinkersMaterialStats.createPolymerTemplate(Materials.Polybenzimidazole)
                .setBowString(2.0F)
                .setFletching(1.0F, 2.5F)
                .build();
    }
}
