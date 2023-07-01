package gregtech.integration.tinkers.effect;

import gregtech.api.unification.material.Materials;
import gregtech.api.unification.ore.OrePrefix;
import gregtech.api.unification.stack.UnificationEntry;
import gregtech.api.util.GTPotion;
import gregtech.integration.tinkers.effect.modifier.ModifierSalty;
import gregtech.integration.tinkers.effect.potion.PotionUnhealing;
import gregtech.integration.tinkers.effect.trait.TraitRadioactive;
import net.minecraft.potion.Potion;
import net.minecraftforge.registries.IForgeRegistry;
import slimeknights.tconstruct.library.modifiers.ModifierTrait;
import slimeknights.tconstruct.library.traits.ITrait;

public class GTTinkerEffects {

    // Traits
    public static final ITrait TRAIT_RADIOACTIVE = new TraitRadioactive();

    // Modifiers
    public static final ModifierTrait MODIFIER_SALTY = new ModifierSalty();

    // Potions
    public static final GTPotion POTION_UNHEALING = new PotionUnhealing();

    public static void registerTraits() {
    }

    public static void registerModifiers() {
        MODIFIER_SALTY.addItem(new UnificationEntry(OrePrefix.dust, Materials.Salt).toString());
        MODIFIER_SALTY.addItem(new UnificationEntry(OrePrefix.block, Materials.Salt).toString(), 1, 9);
        MODIFIER_SALTY.addItem(new UnificationEntry(OrePrefix.gem, Materials.Salt).toString());
        MODIFIER_SALTY.addItem(new UnificationEntry(OrePrefix.dust, Materials.RockSalt).toString());
        MODIFIER_SALTY.addItem(new UnificationEntry(OrePrefix.block, Materials.RockSalt).toString(), 1, 9);
        MODIFIER_SALTY.addItem(new UnificationEntry(OrePrefix.gem, Materials.RockSalt).toString());
    }

    public static void registerEffects(IForgeRegistry<Potion> registry) {
        registry.register(POTION_UNHEALING);
    }
}
