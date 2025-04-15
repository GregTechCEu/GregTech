package gtqt.common.items.covers;

import gregtech.api.cover.CoverDefinition;
import gregtech.api.items.behavior.CoverItemBehavior;
import gregtech.api.items.metaitem.MetaItem;

import gtqt.common.items.GTQTMetaItems;

import net.minecraft.util.ResourceLocation;

import static gregtech.api.util.GTUtility.gregtechId;

public class GTQTCoverBehavior {

    public static void init() {

        registerBehavior(gregtechId("programmable_circuit_cover"), GTQTMetaItems.COVER_PROGRAMMABLE_CIRCUIT,
                CoverProgrammableHatch::new);

    }

    @SuppressWarnings("rawtypes")
    public static void registerBehavior(ResourceLocation coverId,
                                        MetaItem.MetaValueItem placerItem,
                                        CoverDefinition.CoverCreator behaviorCreator) {
        CoverDefinition coverDefinition = gregtech.common.covers.CoverBehaviors.registerCover(coverId, placerItem.getStackForm(), behaviorCreator);
        placerItem.addComponents(new CoverItemBehavior(coverDefinition));
    }
}

