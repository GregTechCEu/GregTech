package gregtech.integration.forestry.bees;

import gregtech.api.GTValues;
import gregtech.api.unification.OreDictUnifier;
import gregtech.api.unification.material.Materials;
import gregtech.api.unification.ore.OrePrefix;
import gregtech.api.util.Mods;
import gregtech.common.blocks.MetaBlocks;
import gregtech.common.items.MetaItems;
import gregtech.integration.forestry.ForestryModule;
import gregtech.integration.forestry.ForestryUtil;
import gregtech.integration.forestry.mutation.MaterialMutationCondition;

import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.fml.common.Optional;

import appeng.core.Api;
import forestry.api.apiculture.BeeManager;
import forestry.api.apiculture.EnumBeeType;
import forestry.api.apiculture.IAlleleBeeSpecies;
import forestry.api.apiculture.IBee;
import forestry.api.apiculture.IBeeGenome;
import forestry.api.apiculture.IBeeMutationBuilder;
import forestry.api.core.EnumHumidity;
import forestry.api.core.EnumTemperature;
import forestry.api.genetics.IAllele;
import forestry.api.genetics.IMutationBuilder;
import forestry.apiculture.ModuleApiculture;
import forestry.apiculture.genetics.Bee;
import forestry.apiculture.genetics.BeeDefinition;
import forestry.apiculture.genetics.IBeeDefinition;
import forestry.apiculture.genetics.alleles.AlleleEffects;
import forestry.apiculture.items.EnumHoneyComb;
import forestry.core.ModuleCore;
import forestry.core.genetics.alleles.AlleleHelper;
import forestry.core.genetics.alleles.EnumAllele;
import org.apache.commons.lang3.text.WordUtils;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Locale;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static forestry.api.apiculture.EnumBeeChromosome.*;
import static gregtech.api.unification.material.Materials.*;

public enum GTBeeDefinition implements IBeeDefinition {

    // Organic
    CLAY(GTBranchDefinition.GT_ORGANIC, "Lutum", true, 0xC8C8DA, 0x0000FF,
            beeSpecies -> {
                if (Mods.ExtraBees.isModLoaded()) {
                    beeSpecies.addProduct(getExtraBeesComb(22), 0.30f); // CLAY
                } else {
                    beeSpecies.addProduct(getForestryComb(EnumHoneyComb.HONEY), 0.30f);
                }
                beeSpecies.addProduct(new ItemStack(Items.CLAY_BALL), 0.15f);
                beeSpecies.setHumidity(EnumHumidity.DAMP);
                beeSpecies.setTemperature(EnumTemperature.NORMAL);
                if (Mods.BiomesOPlenty.isModLoaded()) {
                    beeSpecies.addSpecialty(Mods.BiomesOPlenty.getItem("mudball", 0), 0.05f);
                }
            },
            template -> {
                AlleleHelper.getInstance().set(template, FLOWERING, EnumAllele.Flowering.SLOWER);
                AlleleHelper.getInstance().set(template, HUMIDITY_TOLERANCE, EnumAllele.Tolerance.NONE);
                AlleleHelper.getInstance().set(template, FLOWER_PROVIDER, EnumAllele.Flowers.VANILLA);
            },
            dis -> {
                IBeeMutationBuilder mutation = dis.registerMutation(BeeDefinition.INDUSTRIOUS, BeeDefinition.DILIGENT,
                        10);
                mutation.requireResource(Blocks.HARDENED_CLAY.getDefaultState());
            }),
    SLIMEBALL(GTBranchDefinition.GT_ORGANIC, "Bituminipila", true, 0x4E9E55, 0x00FF15,
            beeSpecies -> {
                beeSpecies.addProduct(getForestryComb(EnumHoneyComb.MOSSY), 0.30f);
                beeSpecies.addProduct(new ItemStack(Items.SLIME_BALL), 0.15f);
                beeSpecies.addSpecialty(getGTComb(GTCombType.STICKY), 0.05f);
                beeSpecies.setHumidity(EnumHumidity.DAMP);
                beeSpecies.setTemperature(EnumTemperature.NORMAL);
                if (Mods.TinkersConstruct.isModLoaded()) {
                    beeSpecies.addProduct(Mods.TinkersConstruct.getItem("edible", 1), 0.10f);
                    beeSpecies.addSpecialty(Mods.TinkersConstruct.getItem("slime_congealed", 2), 0.01f);
                } else {
                    beeSpecies.addSpecialty(new ItemStack(Blocks.SLIME_BLOCK), 0.01f);
                }
            },
            template -> {
                AlleleHelper.getInstance().set(template, FLOWER_PROVIDER, EnumAllele.Flowers.MUSHROOMS);
                AlleleHelper.getInstance().set(template, FLOWERING, EnumAllele.Flowering.SLOWER);
                AlleleHelper.getInstance().set(template, TEMPERATURE_TOLERANCE, EnumAllele.Tolerance.BOTH_1);
                AlleleHelper.getInstance().set(template, HUMIDITY_TOLERANCE, EnumAllele.Tolerance.BOTH_1);
                if (Mods.ExtraBees.isModLoaded()) {
                    AlleleHelper.getInstance().set(template, FLOWER_PROVIDER,
                            ForestryUtil.getFlowers(Mods.ExtraBees, "water"));
                }
            },
            dis -> {
                IBeeMutationBuilder mutation = dis.registerMutation(BeeDefinition.MARSHY, CLAY, 7);
                mutation.requireResource(Blocks.SLIME_BLOCK.getDefaultState());
            }),
    PEAT(GTBranchDefinition.GT_ORGANIC, "Limus", true, 0x906237, 0x58300B,
            beeSpecies -> {
                beeSpecies.addProduct(getForestryComb(EnumHoneyComb.HONEY), 0.15f);
                beeSpecies.addProduct(getGTComb(GTCombType.COAL), 0.15f);
                beeSpecies.addSpecialty(ModuleCore.getItems().peat.getItemStack(), 0.30f);
                beeSpecies.addSpecialty(ModuleCore.getItems().mulch.getItemStack(), 0.05f);
                beeSpecies.setHumidity(EnumHumidity.NORMAL);
                beeSpecies.setTemperature(EnumTemperature.NORMAL);
            },
            template -> {
                AlleleHelper.getInstance().set(template, SPEED, EnumAllele.Speed.SLOWER);
                AlleleHelper.getInstance().set(template, LIFESPAN, EnumAllele.Lifespan.SHORTER);
                AlleleHelper.getInstance().set(template, FLOWER_PROVIDER, EnumAllele.Flowers.WHEAT);
                AlleleHelper.getInstance().set(template, FLOWERING, EnumAllele.Flowering.FASTER);
                AlleleHelper.getInstance().set(template, HUMIDITY_TOLERANCE, EnumAllele.Tolerance.NONE);
            },
            dis -> dis.registerMutation(BeeDefinition.RURAL, CLAY, 10)),
    STICKYRESIN(GTBranchDefinition.GT_ORGANIC, "Lenturesinae", true, 0x2E8F5B, 0xDCC289,
            beeSpecies -> {
                beeSpecies.addProduct(getForestryComb(EnumHoneyComb.HONEY), 0.30f);
                beeSpecies.addSpecialty(getGTComb(GTCombType.STICKY), 0.15f);
                beeSpecies.addSpecialty(MetaItems.STICKY_RESIN.getStackForm(), 0.15f);
                beeSpecies.setHumidity(EnumHumidity.NORMAL);
                beeSpecies.setTemperature(EnumTemperature.NORMAL);
            },
            template -> {
                AlleleHelper.getInstance().set(template, FLOWERING, EnumAllele.Flowering.SLOWER);
                AlleleHelper.getInstance().set(template, HUMIDITY_TOLERANCE, EnumAllele.Tolerance.NONE);
            },
            dis -> {
                IBeeMutationBuilder mutation = dis.registerMutation(SLIMEBALL, PEAT, 15);
                mutation.requireResource("logRubber");
            }),
    COAL(GTBranchDefinition.GT_ORGANIC, "Carbo", true, 0x666666, 0x525252,
            beeSpecies -> {
                beeSpecies.addProduct(getGTComb(GTCombType.COAL), 0.30f);
                beeSpecies.addSpecialty(getGTComb(GTCombType.COKE), 0.15f);
                beeSpecies.setHumidity(EnumHumidity.NORMAL);
                beeSpecies.setTemperature(EnumTemperature.NORMAL);
            },
            template -> {
                AlleleHelper.getInstance().set(template, FLOWER_PROVIDER, EnumAllele.Flowers.CACTI);
                AlleleHelper.getInstance().set(template, SPEED, EnumAllele.Speed.SLOWEST);
                AlleleHelper.getInstance().set(template, LIFESPAN, EnumAllele.Lifespan.LONGER);
                AlleleHelper.getInstance().set(template, TEMPERATURE_TOLERANCE, EnumAllele.Tolerance.DOWN_2);
                AlleleHelper.getInstance().set(template, HUMIDITY_TOLERANCE, EnumAllele.Tolerance.DOWN_1);
                AlleleHelper.getInstance().set(template, NEVER_SLEEPS, true);
                AlleleHelper.getInstance().set(template, EFFECT, AlleleEffects.effectCreeper);
            },
            dis -> {
                IBeeMutationBuilder mutation = dis.registerMutation(BeeDefinition.INDUSTRIOUS, PEAT, 9);
                mutation.addMutationCondition(new MaterialMutationCondition(Coal));
            }),
    OIL(GTBranchDefinition.GT_ORGANIC, "Oleum", true, 0x4C4C4C, 0x333333,
            beeSpecies -> {
                beeSpecies.addProduct(getForestryComb(EnumHoneyComb.HONEY), 0.30f);
                beeSpecies.addSpecialty(getGTComb(GTCombType.OIL), 0.75f);
                beeSpecies.setHumidity(EnumHumidity.DAMP);
                beeSpecies.setTemperature(EnumTemperature.NORMAL);
                beeSpecies.setHasEffect();
            },
            template -> {
                AlleleHelper.getInstance().set(template, FLOWERING, EnumAllele.Flowering.SLOWER);
                AlleleHelper.getInstance().set(template, NEVER_SLEEPS, true);
                AlleleHelper.getInstance().set(template, LIFESPAN, EnumAllele.Lifespan.NORMAL);
                AlleleHelper.getInstance().set(template, SPEED, EnumAllele.Speed.SLOWER);
                AlleleHelper.getInstance().set(template, TEMPERATURE_TOLERANCE, EnumAllele.Tolerance.NONE);
                AlleleHelper.getInstance().set(template, HUMIDITY_TOLERANCE, EnumAllele.Tolerance.NONE);
                if (Mods.ExtraBees.isModLoaded()) {
                    AlleleHelper.getInstance().set(template, FLOWER_PROVIDER,
                            ForestryUtil.getFlowers(Mods.ExtraBees, "water"));
                }
            },
            dis -> dis.registerMutation(COAL, STICKYRESIN, 4)),
    ASH(GTBranchDefinition.GT_ORGANIC, "Cinis", true, 0x1E1A18, 0xC6C6C6,
            beeSpecies -> {
                if (Mods.ExtraBees.isModLoaded()) {
                    beeSpecies.addProduct(getExtraBeesComb(9), 0.30f); // SEED
                } else {
                    beeSpecies.addProduct(getForestryComb(EnumHoneyComb.HONEY), 0.30f);
                }
                beeSpecies.addSpecialty(getGTComb(GTCombType.ASH), 0.15f);
                beeSpecies.setHumidity(EnumHumidity.ARID);
                beeSpecies.setTemperature(EnumTemperature.HOT);
            },
            template -> {
                AlleleHelper.getInstance().set(template, SPEED, EnumAllele.Speed.NORMAL);
                AlleleHelper.getInstance().set(template, TERRITORY, EnumAllele.Territory.LARGE);
                AlleleHelper.getInstance().set(template, LIFESPAN, EnumAllele.Lifespan.SHORTER);
                AlleleHelper.getInstance().set(template, FLOWER_PROVIDER, EnumAllele.Flowers.WHEAT);
                AlleleHelper.getInstance().set(template, FLOWERING, EnumAllele.Flowering.FASTER);
            },
            dis -> {
                IBeeMutationBuilder mutation = dis.registerMutation(COAL, CLAY, 10);
                mutation.restrictTemperature(EnumTemperature.HELLISH);
            }),
    APATITE(GTBranchDefinition.GT_ORGANIC, "Stercorat", true, 0x7FCEF5, 0x654525,
            beeSpecies -> {
                if (Mods.ExtraBees.isModLoaded()) {
                    beeSpecies.addProduct(getExtraBeesComb(9), 0.15f); // SEED
                } else {
                    beeSpecies.addProduct(getForestryComb(EnumHoneyComb.HONEY), 0.15f);
                }
                beeSpecies.addSpecialty(getGTComb(GTCombType.APATITE), 0.15f);
                beeSpecies.setHumidity(EnumHumidity.NORMAL);
                beeSpecies.setTemperature(EnumTemperature.WARM);
            },
            template -> {
                AlleleHelper.getInstance().set(template, SPEED, EnumAllele.Speed.FASTEST);
                AlleleHelper.getInstance().set(template, LIFESPAN, EnumAllele.Lifespan.LONGER);
                AlleleHelper.getInstance().set(template, FLOWER_PROVIDER, EnumAllele.Flowers.WHEAT);
                AlleleHelper.getInstance().set(template, FLOWERING, EnumAllele.Flowering.FASTER);
                if (Mods.ExtraBees.isModLoaded()) {
                    AlleleHelper.getInstance().set(template, FLOWER_PROVIDER,
                            ForestryUtil.getFlowers(Mods.ExtraBees, "rock"));
                }
            },
            dis -> {
                IBeeMutationBuilder mutation = dis.registerMutation(ASH, COAL, 10);
                mutation.addMutationCondition(new MaterialMutationCondition(Apatite));
            }),
    BIOMASS(GTBranchDefinition.GT_ORGANIC, "Taeda", true, 0x21E118, 0x17AF0E,
            beeSpecies -> {
                beeSpecies.addProduct(getForestryComb(EnumHoneyComb.MOSSY), 0.30f);
                beeSpecies.addSpecialty(getGTComb(GTCombType.BIOMASS), 0.15f);
                beeSpecies.setHumidity(EnumHumidity.NORMAL);
                beeSpecies.setTemperature(EnumTemperature.NORMAL);
            },
            template -> {
                AlleleHelper.getInstance().set(template, SPEED, EnumAllele.Speed.FASTEST);
                AlleleHelper.getInstance().set(template, LIFESPAN, EnumAllele.Lifespan.LONGEST);
                AlleleHelper.getInstance().set(template, FLOWER_PROVIDER, EnumAllele.Flowers.WHEAT);
                AlleleHelper.getInstance().set(template, FLOWERING, EnumAllele.Flowering.FASTER);
            },
            dis -> {
                IBeeMutationBuilder mutation = dis.registerMutation(BeeDefinition.INDUSTRIOUS, BeeDefinition.RURAL, 10);
                mutation.restrictBiomeType(BiomeDictionary.Type.FOREST);
            }),
    FERTILIZER(GTBranchDefinition.GT_ORGANIC, "Stercorat", true, 0x7FCEF5, 0x654525,
            beeSpecies -> {
                if (Mods.ExtraBees.isModLoaded()) {
                    beeSpecies.addProduct(getExtraBeesComb(9), 0.15f); // SEED
                } else {
                    beeSpecies.addProduct(getForestryComb(EnumHoneyComb.MOSSY), 0.15f);
                }
                beeSpecies.addSpecialty(OreDictUnifier.get(OrePrefix.dustTiny, Materials.Ash), 0.2f);
                beeSpecies.addSpecialty(OreDictUnifier.get(OrePrefix.dustTiny, Materials.DarkAsh), 0.2f);
                beeSpecies.addSpecialty(MetaItems.FERTILIZER.getStackForm(), 0.3f);
                beeSpecies.addSpecialty(Mods.Forestry.getItem("fertilizer_compound", 0), 0.3f);
                beeSpecies.setHumidity(EnumHumidity.DAMP);
                beeSpecies.setTemperature(EnumTemperature.WARM);
            },
            template -> {
                AlleleHelper.getInstance().set(template, SPEED, EnumAllele.Speed.FASTEST);
                AlleleHelper.getInstance().set(template, LIFESPAN, EnumAllele.Lifespan.LONGER);
                AlleleHelper.getInstance().set(template, FLOWER_PROVIDER, EnumAllele.Flowers.WHEAT);
                AlleleHelper.getInstance().set(template, FLOWERING, EnumAllele.Flowering.FASTER);
            },
            dis -> dis.registerMutation(ASH, APATITE, 8)),
    PHOSPHORUS(GTBranchDefinition.GT_ORGANIC, "Phosphorus", false, 0xFFC826, 0xC1C1F6,
            beeSpecies -> {
                beeSpecies.addSpecialty(getGTComb(GTCombType.PHOSPHORUS), 0.35f);
                beeSpecies.setTemperature(EnumTemperature.HOT);
                beeSpecies.setNocturnal();
                beeSpecies.setHasEffect();
            },
            template -> AlleleHelper.getInstance().set(template, LIFESPAN, EnumAllele.Lifespan.SHORTEST),
            dis -> {
                IBeeMutationBuilder mutation = dis.registerMutation(APATITE, ASH, 12);
                mutation.restrictTemperature(EnumTemperature.HOT);
                mutation.addMutationCondition(new MaterialMutationCondition(TricalciumPhosphate));
            }),
    SANDWICH(GTBranchDefinition.GT_ORGANIC, "Sandwico", true, 0x32CD32, 0xDAA520,
            beeSpecies -> {
                beeSpecies.addProduct(Mods.GregTechFoodOption.getItem("gtfo_meta_item", 81), 0.05f); // Cucumber Slice
                beeSpecies.addProduct(Mods.GregTechFoodOption.getItem("gtfo_meta_item", 80), 0.05f); // Onion Slice
                beeSpecies.addProduct(Mods.GregTechFoodOption.getItem("gtfo_meta_item", 79), 0.05f); // Tomato Slice
                beeSpecies.addSpecialty(new ItemStack(Items.COOKED_PORKCHOP), 0.05f);
                beeSpecies.addSpecialty(new ItemStack(Items.COOKED_BEEF), 0.15f);
                beeSpecies.addSpecialty(Mods.GregTechFoodOption.getItem("gtfo_meta_item", 97), 0.05f); // Cheddar Slice
                beeSpecies.setHumidity(EnumHumidity.NORMAL);
                beeSpecies.setTemperature(EnumTemperature.NORMAL);
            },
            template -> {
                AlleleHelper.getInstance().set(template, SPEED, EnumAllele.Speed.SLOW);
                AlleleHelper.getInstance().set(template, HUMIDITY_TOLERANCE, EnumAllele.Tolerance.BOTH_2);
                AlleleHelper.getInstance().set(template, EFFECT, AlleleEffects.effectFertile);
                AlleleHelper.getInstance().set(template, TERRITORY, EnumAllele.Territory.LARGE);
                AlleleHelper.getInstance().set(template, LIFESPAN, EnumAllele.Lifespan.SHORTER);
                AlleleHelper.getInstance().set(template, FLOWER_PROVIDER, EnumAllele.Flowers.WHEAT);
                AlleleHelper.getInstance().set(template, FLOWERING, EnumAllele.Flowering.FASTER);
            },
            dis -> {
                if (Mods.MagicBees.isModLoaded()) {
                    dis.registerMutation(BeeDefinition.AGRARIAN, ForestryUtil.getSpecies(Mods.MagicBees, "Batty"), 10);
                } else {
                    dis.registerMutation(BeeDefinition.AGRARIAN, BeeDefinition.IMPERIAL, 10);
                }
            },
            Mods.GregTechFoodOption::isModLoaded),

    // Gems
    REDSTONE(GTBranchDefinition.GT_GEM, "Rubrumlapis", true, 0x7D0F0F, 0xD11919,
            beeSpecies -> {
                beeSpecies.addProduct(getGTComb(GTCombType.STONE), 0.30f);
                beeSpecies.addSpecialty(getGTComb(GTCombType.REDSTONE), 0.15f);
                beeSpecies.addSpecialty(getGTComb(GTCombType.RAREEARTH), 0.15f);
                beeSpecies.setHumidity(EnumHumidity.NORMAL);
                beeSpecies.setTemperature(EnumTemperature.NORMAL);
            },
            template -> AlleleHelper.getInstance().set(template, SPEED, EnumAllele.Speed.SLOWER),
            dis -> {
                IBeeMutationBuilder mutation = dis.registerMutation(BeeDefinition.INDUSTRIOUS, BeeDefinition.DEMONIC,
                        10);
                mutation.addMutationCondition(new MaterialMutationCondition(Redstone));
            }),
    LAPIS(GTBranchDefinition.GT_GEM, "Lapidi", true, 0x1947D1, 0x476CDA,
            beeSpecies -> {
                beeSpecies.addProduct(getGTComb(GTCombType.STONE), 0.30f);
                beeSpecies.addSpecialty(getGTComb(GTCombType.LAPIS), 0.15f);
                beeSpecies.setHumidity(EnumHumidity.NORMAL);
                beeSpecies.setTemperature(EnumTemperature.NORMAL);
            },
            template -> AlleleHelper.getInstance().set(template, SPEED, EnumAllele.Speed.SLOWER),
            dis -> {
                IBeeMutationBuilder mutation = dis.registerMutation(BeeDefinition.DEMONIC, BeeDefinition.IMPERIAL, 10);
                mutation.addMutationCondition(new MaterialMutationCondition(Lapis));
            }),
    CERTUS(GTBranchDefinition.GT_GEM, "Quarzeus", true, 0x57CFFB, 0xBBEEFF,
            beeSpecies -> {
                beeSpecies.addProduct(getGTComb(GTCombType.STONE), 0.30f);
                beeSpecies.addSpecialty(getGTComb(GTCombType.CERTUS), 0.15f);
                beeSpecies.setHumidity(EnumHumidity.NORMAL);
                beeSpecies.setTemperature(EnumTemperature.NORMAL);
            },
            template -> AlleleHelper.getInstance().set(template, SPEED, EnumAllele.Speed.SLOWER),
            dis -> {
                IBeeMutationBuilder mutation = dis.registerMutation(BeeDefinition.HERMITIC, LAPIS, 10);
                mutation.addMutationCondition(new MaterialMutationCondition(CertusQuartz));
            }),
    FLUIX(GTBranchDefinition.GT_GEM, "", true, 0xA375FF, 0xB591FF,
            beeSpecies -> {
                beeSpecies.addProduct(getGTComb(GTCombType.STONE), 0.30f);
                beeSpecies.addProduct(getGTComb(GTCombType.FLUIX), 0.15f);
                beeSpecies.setHumidity(EnumHumidity.NORMAL);
                beeSpecies.setTemperature(EnumTemperature.NORMAL);
            },
            template -> AlleleHelper.getInstance().set(template, SPEED, EnumAllele.Speed.SLOWER),
            dis -> {
                IBeeMutationBuilder mutation = dis.registerMutation(REDSTONE, LAPIS, 7);
                Api.INSTANCE.definitions().blocks().fluixBlock().maybeBlock()
                        .ifPresent(block -> mutation.requireResource(block.getDefaultState()));
            },
            Mods.AppliedEnergistics2::isModLoaded),
    DIAMOND(GTBranchDefinition.GT_GEM, "Adamas", false, 0xCCFFFF, 0xA3CCCC,
            beeSpecies -> {
                beeSpecies.addProduct(getGTComb(GTCombType.STONE), 0.30f);
                beeSpecies.addSpecialty(getGTComb(GTCombType.DIAMOND), 0.15f);
                beeSpecies.setHumidity(EnumHumidity.NORMAL);
                beeSpecies.setTemperature(EnumTemperature.HOT);
                beeSpecies.setHasEffect();
            },
            template -> AlleleHelper.getInstance().set(template, SPEED, EnumAllele.Speed.SLOWER),
            dis -> {
                IBeeMutationBuilder mutation = dis.registerMutation(CERTUS, COAL, 3);
                mutation.addMutationCondition(new MaterialMutationCondition(Diamond));
            }),
    RUBY(GTBranchDefinition.GT_GEM, "Rubinus", false, 0xE6005C, 0xCC0052,
            beeSpecies -> {
                beeSpecies.addProduct(getGTComb(GTCombType.STONE), 0.30f);
                beeSpecies.addProduct(getGTComb(GTCombType.RUBY), 0.15f);
                beeSpecies.addProduct(getGTComb(GTCombType.REDSTONE), 0.05f);
                beeSpecies.setHumidity(EnumHumidity.DAMP);
                beeSpecies.setTemperature(EnumTemperature.HOT);
            },
            template -> AlleleHelper.getInstance().set(template, SPEED, EnumAllele.Speed.SLOWER),
            dis -> {
                IBeeMutationBuilder mutation = dis.registerMutation(REDSTONE, DIAMOND, 5);
                mutation.addMutationCondition(new MaterialMutationCondition(Ruby));
            }),
    SAPPHIRE(GTBranchDefinition.GT_GEM, "Sapphirus", true, 0x0033CC, 0x00248F,
            beeSpecies -> {
                beeSpecies.addProduct(getGTComb(GTCombType.STONE), 0.30f);
                beeSpecies.addSpecialty(getGTComb(GTCombType.SAPPHIRE), 0.15f);
                beeSpecies.setHumidity(EnumHumidity.NORMAL);
                beeSpecies.setTemperature(EnumTemperature.NORMAL);
            },
            template -> AlleleHelper.getInstance().set(template, SPEED, EnumAllele.Speed.SLOWER),
            dis -> {
                IBeeMutationBuilder mutation = dis.registerMutation(CERTUS, LAPIS, 5);
                mutation.addMutationCondition(new MaterialMutationCondition(Sapphire));
            }),
    OLIVINE(GTBranchDefinition.GT_GEM, "Olivinum", true, 0x248F24, 0xCCFFCC,
            beeSpecies -> {
                beeSpecies.addProduct(getGTComb(GTCombType.STONE), 0.30f);
                beeSpecies.addSpecialty(getGTComb(GTCombType.OLIVINE), 0.15f);
                beeSpecies.addSpecialty(getGTComb(GTCombType.MAGNESIUM), 0.05f);
                beeSpecies.setHumidity(EnumHumidity.NORMAL);
                beeSpecies.setTemperature(EnumTemperature.NORMAL);
            },
            template -> AlleleHelper.getInstance().set(template, SPEED, EnumAllele.Speed.SLOWER),
            dis -> dis.registerMutation(CERTUS, BeeDefinition.ENDED, 5)),
    EMERALD(GTBranchDefinition.GT_GEM, "Smaragdus", false, 0x248F24, 0x2EB82E,
            beeSpecies -> {
                beeSpecies.addProduct(getGTComb(GTCombType.STONE), 0.30f);
                beeSpecies.addSpecialty(getGTComb(GTCombType.EMERALD), 0.15f);
                beeSpecies.addSpecialty(getGTComb(GTCombType.ALUMINIUM), 0.05f);
                beeSpecies.setHumidity(EnumHumidity.NORMAL);
                beeSpecies.setTemperature(EnumTemperature.COLD);
                beeSpecies.setHasEffect();
            },
            template -> AlleleHelper.getInstance().set(template, SPEED, EnumAllele.Speed.SLOWER),
            dis -> {
                IBeeMutationBuilder mutation = dis.registerMutation(OLIVINE, DIAMOND, 4);
                mutation.addMutationCondition(new MaterialMutationCondition(Emerald));
            }),
    SPARKLING(GTBranchDefinition.GT_GEM, "Vesperstella", true, 0x7A007A, 0xFFFFFF,
            beeSpecies -> {
                beeSpecies.addProduct(Mods.MagicBees.getItem("resource", 3), 0.20f);
                beeSpecies.addSpecialty(getGTComb(GTCombType.SPARKLING), 0.125f);
                beeSpecies.setHumidity(EnumHumidity.NORMAL);
                beeSpecies.setTemperature(EnumTemperature.NORMAL);
            },
            template -> {
                AlleleHelper.getInstance().set(template, TEMPERATURE_TOLERANCE, EnumAllele.Tolerance.DOWN_2);
                AlleleHelper.getInstance().set(template, NEVER_SLEEPS, true);
                AlleleHelper.getInstance().set(template, CAVE_DWELLING, true);
                AlleleHelper.getInstance().set(template, FLOWER_PROVIDER, EnumAllele.Flowers.NETHER);
                AlleleHelper.getInstance().set(template, LIFESPAN, EnumAllele.Lifespan.SHORT);
                AlleleHelper.getInstance().set(template, EFFECT, AlleleEffects.effectAggressive);
                AlleleHelper.getInstance().set(template, FLOWERING, EnumAllele.Flowering.AVERAGE);
            },
            dis -> {
                IBeeMutationBuilder mutation = dis.registerMutation(
                        ForestryUtil.getSpecies(Mods.MagicBees, "Withering"),
                        ForestryUtil.getSpecies(Mods.MagicBees, "Draconic"), 1);
                mutation.addMutationCondition(new MaterialMutationCondition(NetherStar));
                mutation.restrictBiomeType(BiomeDictionary.Type.END);
            },
            Mods.MagicBees::isModLoaded),

    // Metals
    COPPER(GTBranchDefinition.GT_METAL, "Cuprum", true, 0xFF6600, 0xE65C00,
            beeSpecies -> {
                beeSpecies.addProduct(getGTComb(GTCombType.SLAG), 0.30f);
                beeSpecies.addProduct(getGTComb(GTCombType.COPPER), 0.15f);
                beeSpecies.addSpecialty(getGTComb(GTCombType.GOLD), 0.05f);
                beeSpecies.setHumidity(EnumHumidity.NORMAL);
                beeSpecies.setTemperature(EnumTemperature.NORMAL);
            },
            template -> AlleleHelper.getInstance().set(template, SPEED, EnumAllele.Speed.SLOWER),
            dis -> {
                IBeeMutationBuilder mutation = dis.registerMutation(BeeDefinition.MAJESTIC, CLAY, 13);
                mutation.addMutationCondition(new MaterialMutationCondition(Copper));
            }),
    TIN(GTBranchDefinition.GT_METAL, "Stannum", true, 0xD4D4D4, 0xDDDDDD,
            beeSpecies -> {
                beeSpecies.addProduct(getGTComb(GTCombType.SLAG), 0.30f);
                beeSpecies.addProduct(getGTComb(GTCombType.TIN), 0.15f);
                beeSpecies.addSpecialty(getGTComb(GTCombType.ZINC), 0.05f);
                beeSpecies.setHumidity(EnumHumidity.NORMAL);
                beeSpecies.setTemperature(EnumTemperature.NORMAL);
            },
            template -> AlleleHelper.getInstance().set(template, SPEED, EnumAllele.Speed.SLOWER),
            dis -> {
                IBeeMutationBuilder mutation = dis.registerMutation(CLAY, BeeDefinition.DILIGENT, 13);
                mutation.addMutationCondition(new MaterialMutationCondition(Tin));
            }),
    LEAD(GTBranchDefinition.GT_METAL, "Plumbum", true, 0x666699, 0xA3A3CC,
            beeSpecies -> {
                beeSpecies.addProduct(getGTComb(GTCombType.SLAG), 0.30f);
                beeSpecies.addProduct(getGTComb(GTCombType.LEAD), 0.15f);
                beeSpecies.addSpecialty(getGTComb(GTCombType.SULFUR), 0.05f);
                beeSpecies.setHumidity(EnumHumidity.DAMP);
                beeSpecies.setTemperature(EnumTemperature.WARM);
            },
            template -> AlleleHelper.getInstance().set(template, SPEED, EnumAllele.Speed.SLOWER),
            dis -> {
                IBeeMutationBuilder mutation = dis.registerMutation(COAL, COPPER, 13);
                mutation.addMutationCondition(new MaterialMutationCondition(Lead));
            }),
    IRON(GTBranchDefinition.GT_METAL, "Ferrum", true, 0xDA9147, 0xDE9C59,
            beeSpecies -> {
                beeSpecies.addProduct(getGTComb(GTCombType.SLAG), 0.30f);
                beeSpecies.addProduct(getGTComb(GTCombType.IRON), 0.15f);
                beeSpecies.addSpecialty(getGTComb(GTCombType.TIN), 0.05f);
                beeSpecies.setHumidity(EnumHumidity.NORMAL);
                beeSpecies.setTemperature(EnumTemperature.NORMAL);
            },
            template -> AlleleHelper.getInstance().set(template, SPEED, EnumAllele.Speed.SLOWER),
            dis -> {
                IBeeMutationBuilder mutation = dis.registerMutation(TIN, COPPER, 13);
                mutation.addMutationCondition(new MaterialMutationCondition(Iron));
            }),
    STEEL(GTBranchDefinition.GT_METAL, "Chalybe", true, 0x808080, 0x999999,
            beeSpecies -> {
                beeSpecies.addProduct(getGTComb(GTCombType.SLAG), 0.30f);
                beeSpecies.addProduct(getGTComb(GTCombType.STEEL), 0.15f);
                beeSpecies.addSpecialty(getGTComb(GTCombType.IRON), 0.05f);
                beeSpecies.setHumidity(EnumHumidity.NORMAL);
                beeSpecies.setTemperature(EnumTemperature.WARM);
            },
            template -> AlleleHelper.getInstance().set(template, SPEED, EnumAllele.Speed.SLOWER),
            dis -> {
                IBeeMutationBuilder mutation = dis.registerMutation(IRON, COAL, 10);
                mutation.addMutationCondition(new MaterialMutationCondition(Steel));
                mutation.restrictTemperature(EnumTemperature.HOT);
            }),
    NICKEL(GTBranchDefinition.GT_METAL, "Nichelium", true, 0x8585AD, 0x8585AD,
            beeSpecies -> {
                beeSpecies.addProduct(getGTComb(GTCombType.SLAG), 0.30f);
                beeSpecies.addProduct(getGTComb(GTCombType.NICKEL), 0.15f);
                beeSpecies.addSpecialty(getGTComb(GTCombType.PLATINUM), 0.02f);
                beeSpecies.setHumidity(EnumHumidity.NORMAL);
                beeSpecies.setTemperature(EnumTemperature.NORMAL);
            },
            template -> AlleleHelper.getInstance().set(template, SPEED, EnumAllele.Speed.SLOWER),
            dis -> {
                IBeeMutationBuilder mutation = dis.registerMutation(IRON, COPPER, 13);
                mutation.addMutationCondition(new MaterialMutationCondition(Nickel));
            }),
    ZINC(GTBranchDefinition.GT_METAL, "Cadmiae", true, 0xF0DEF0, 0xF2E1F2,
            beeSpecies -> {
                beeSpecies.addProduct(getGTComb(GTCombType.SLAG), 0.30f);
                beeSpecies.addProduct(getGTComb(GTCombType.ZINC), 0.15f);
                beeSpecies.addSpecialty(getGTComb(GTCombType.GALLIUM), 0.05f);
                beeSpecies.setHumidity(EnumHumidity.NORMAL);
                beeSpecies.setTemperature(EnumTemperature.NORMAL);
            },
            template -> AlleleHelper.getInstance().set(template, SPEED, EnumAllele.Speed.SLOWER),
            dis -> {
                IBeeMutationBuilder mutation = dis.registerMutation(IRON, TIN, 13);
                mutation.addMutationCondition(new MaterialMutationCondition(Zinc));
            }),
    SILVER(GTBranchDefinition.GT_METAL, "Argenti", true, 0xC2C2D6, 0xCECEDE,
            beeSpecies -> {
                beeSpecies.addProduct(getGTComb(GTCombType.SLAG), 0.30f);
                beeSpecies.addProduct(getGTComb(GTCombType.SILVER), 0.15f);
                beeSpecies.addSpecialty(getGTComb(GTCombType.SULFUR), 0.05f);
                beeSpecies.setHumidity(EnumHumidity.NORMAL);
                beeSpecies.setTemperature(EnumTemperature.COLD);
            },
            template -> AlleleHelper.getInstance().set(template, SPEED, EnumAllele.Speed.SLOWER),
            dis -> {
                IBeeMutationBuilder mutation = dis.registerMutation(LEAD, TIN, 10);
                mutation.addMutationCondition(new MaterialMutationCondition(Silver));
            }),
    GOLD(GTBranchDefinition.GT_METAL, "Aurum", true, 0xEBC633, 0xEDCC47,
            beeSpecies -> {
                beeSpecies.addProduct(getGTComb(GTCombType.SLAG), 0.30f);
                beeSpecies.addProduct(getGTComb(GTCombType.GOLD), 0.15f);
                beeSpecies.addSpecialty(getGTComb(GTCombType.NICKEL), 0.05f);
                beeSpecies.setHumidity(EnumHumidity.NORMAL);
                beeSpecies.setTemperature(EnumTemperature.WARM);
            },
            template -> AlleleHelper.getInstance().set(template, SPEED, EnumAllele.Speed.SLOWER),
            dis -> {
                IBeeMutationBuilder mutation = dis.registerMutation(LEAD, COPPER, 13);
                mutation.addMutationCondition(new MaterialMutationCondition(Gold));
                mutation.restrictTemperature(EnumTemperature.HOT);
            }),
    ARSENIC(GTBranchDefinition.GT_METAL, "Arsenicum", true, 0x736C52, 0x292412,
            beeSpecies -> {
                beeSpecies.addProduct(getGTComb(GTCombType.SLAG), 0.30f);
                beeSpecies.addProduct(getGTComb(GTCombType.ARSENIC), 0.15f);
                beeSpecies.setHumidity(EnumHumidity.NORMAL);
                beeSpecies.setTemperature(EnumTemperature.WARM);
            },
            template -> AlleleHelper.getInstance().set(template, SPEED, EnumAllele.Speed.SLOWER),
            dis -> {
                IBeeMutationBuilder mutation = dis.registerMutation(ZINC, SILVER, 10);
                mutation.addMutationCondition(new MaterialMutationCondition(Arsenic));
            }),
    SILICON(GTBranchDefinition.GT_ORGANIC, "Silex", false, 0xADA2A7, 0x736675,
            beeSpecies -> {
                beeSpecies.addProduct(getForestryComb(EnumHoneyComb.HONEY), 0.10f);
                beeSpecies.addSpecialty(OreDictUnifier.get(OrePrefix.dust, Materials.Silicon), 0.30f);
            },
            template -> {
                AlleleHelper.getInstance().set(template, SPEED, EnumAllele.Speed.SLOW);
                AlleleHelper.getInstance().set(template, TERRITORY, EnumAllele.Territory.LARGER);
                AlleleHelper.getInstance().set(template, TOLERATES_RAIN, true);
            },
            dis -> {
                if (Mods.MagicBees.isModLoaded() && Mods.AppliedEnergistics2.isModLoaded()) {
                    // MB Skystone bee is only registered if AE2 is also active
                    dis.registerMutation(IRON, ForestryUtil.getSpecies(Mods.MagicBees, "AESkystone"), 17);
                } else {
                    dis.registerMutation(IRON, BeeDefinition.IMPERIAL, 17);
                }
            }),

    // Rare Metals
    ALUMINIUM(GTBranchDefinition.GT_RAREMETAL, "Alumen", true, 0xB8B8FF, 0xD6D6FF,
            beeSpecies -> {
                beeSpecies.addProduct(getGTComb(GTCombType.SLAG), 0.30f);
                beeSpecies.addProduct(getGTComb(GTCombType.ALUMINIUM), 0.15f);
                beeSpecies.addSpecialty(getGTComb(GTCombType.BAUXITE), 0.05f);
                beeSpecies.setHumidity(EnumHumidity.ARID);
                beeSpecies.setTemperature(EnumTemperature.HOT);
            },
            template -> AlleleHelper.getInstance().set(template, SPEED, EnumAllele.Speed.SLOWER),
            dis -> {
                IBeeMutationBuilder mutation = dis.registerMutation(NICKEL, ZINC, 9);
                mutation.addMutationCondition(new MaterialMutationCondition(Aluminium));
            }),
    TITANIUM(GTBranchDefinition.GT_RAREMETAL, "Titanus", true, 0xCC99FF, 0xDBB8FF,
            beeSpecies -> {
                beeSpecies.addProduct(getGTComb(GTCombType.SLAG), 0.30f);
                beeSpecies.addProduct(getGTComb(GTCombType.TITANIUM), 0.15f);
                beeSpecies.addSpecialty(getGTComb(GTCombType.ALMANDINE), 0.05f);
                beeSpecies.setHumidity(EnumHumidity.ARID);
                beeSpecies.setTemperature(EnumTemperature.HOT);
            },
            template -> AlleleHelper.getInstance().set(template, SPEED, EnumAllele.Speed.SLOWER),
            dis -> {
                IBeeMutationBuilder mutation = dis.registerMutation(REDSTONE, ALUMINIUM, 5);
                mutation.addMutationCondition(new MaterialMutationCondition(Titanium));
            }),
    // todo glowstone?
    CHROME(GTBranchDefinition.GT_RAREMETAL, "Chroma", true, 0xEBA1EB, 0xF2C3F2,
            beeSpecies -> {
                beeSpecies.addProduct(getGTComb(GTCombType.SLAG), 0.30f);
                beeSpecies.addProduct(getGTComb(GTCombType.CHROME), 0.15f);
                beeSpecies.addSpecialty(getGTComb(GTCombType.MAGNESIUM), 0.05f);
                beeSpecies.setHumidity(EnumHumidity.ARID);
                beeSpecies.setTemperature(EnumTemperature.HOT);
            },
            template -> AlleleHelper.getInstance().set(template, SPEED, EnumAllele.Speed.SLOWER),
            dis -> {
                IBeeMutationBuilder mutation = dis.registerMutation(TITANIUM, RUBY, 5);
                mutation.addMutationCondition(new MaterialMutationCondition(Chrome));
            }),
    MANGANESE(GTBranchDefinition.GT_RAREMETAL, "Manganum", true, 0xD5D5D5, 0xAAAAAA,
            beeSpecies -> {
                beeSpecies.addProduct(getGTComb(GTCombType.SLAG), 0.30f);
                beeSpecies.addProduct(getGTComb(GTCombType.MANGANESE), 0.15f);
                beeSpecies.addSpecialty(getGTComb(GTCombType.IRON), 0.05f);
                beeSpecies.setHumidity(EnumHumidity.ARID);
                beeSpecies.setTemperature(EnumTemperature.HOT);
            },
            template -> AlleleHelper.getInstance().set(template, SPEED, EnumAllele.Speed.SLOWER),
            dis -> {
                IBeeMutationBuilder mutation = dis.registerMutation(TITANIUM, ALUMINIUM, 5);
                mutation.addMutationCondition(new MaterialMutationCondition(Manganese));
            }),
    TUNGSTEN(GTBranchDefinition.GT_RAREMETAL, "Wolframium", false, 0x5C5C8A, 0x7D7DA1,
            beeSpecies -> {
                beeSpecies.addProduct(getGTComb(GTCombType.SLAG), 0.30f);
                beeSpecies.addProduct(getGTComb(GTCombType.TUNGSTEN), 0.15f);
                beeSpecies.addSpecialty(getGTComb(GTCombType.MOLYBDENUM), 0.05f);
                beeSpecies.setHumidity(EnumHumidity.ARID);
                beeSpecies.setTemperature(EnumTemperature.HOT);
            },
            template -> AlleleHelper.getInstance().set(template, SPEED, EnumAllele.Speed.SLOWER),
            dis -> {
                IBeeMutationBuilder mutation = dis.registerMutation(BeeDefinition.HEROIC, MANGANESE, 5);
                mutation.addMutationCondition(new MaterialMutationCondition(Tungsten));
            }),
    PLATINUM(GTBranchDefinition.GT_RAREMETAL, "Platina", false, 0xE6E6E6, 0xFFFFCC,
            beeSpecies -> {
                beeSpecies.addProduct(getGTComb(GTCombType.SLAG), 0.30f);
                beeSpecies.addProduct(getGTComb(GTCombType.PLATINUM), 0.15f);
                beeSpecies.addSpecialty(getGTComb(GTCombType.IRIDIUM), 0.05f);
                beeSpecies.setHumidity(EnumHumidity.ARID);
                beeSpecies.setTemperature(EnumTemperature.HOT);
            },
            template -> AlleleHelper.getInstance().set(template, SPEED, EnumAllele.Speed.SLOWER),
            dis -> {
                IBeeMutationBuilder mutation = dis.registerMutation(DIAMOND, CHROME, 5);
                mutation.addMutationCondition(new MaterialMutationCondition(Platinum));
            }),
    IRIDIUM(GTBranchDefinition.GT_RAREMETAL, "Iris", false, 0xDADADA, 0xD1D1E0,
            beeSpecies -> {
                beeSpecies.addProduct(getGTComb(GTCombType.SLAG), 0.30f);
                beeSpecies.addSpecialty(getGTComb(GTCombType.IRIDIUM), 0.15f);
                beeSpecies.addSpecialty(getGTComb(GTCombType.OSMIUM), 0.05f);
                beeSpecies.setHumidity(EnumHumidity.ARID);
                beeSpecies.setTemperature(EnumTemperature.HELLISH);
                beeSpecies.setHasEffect();
            },
            template -> AlleleHelper.getInstance().set(template, SPEED, EnumAllele.Speed.SLOWER),
            dis -> {
                IBeeMutationBuilder mutation = dis.registerMutation(TUNGSTEN, PLATINUM, 5);
                mutation.addMutationCondition(new MaterialMutationCondition(Iridium));
            }),
    OSMIUM(GTBranchDefinition.GT_RAREMETAL, "Osmia", false, 0x2B2BDA, 0x8B8B8B,
            beeSpecies -> {
                beeSpecies.addProduct(getGTComb(GTCombType.SLAG), 0.30f);
                beeSpecies.addSpecialty(getGTComb(GTCombType.OSMIUM), 0.15f);
                beeSpecies.addSpecialty(getGTComb(GTCombType.IRIDIUM), 0.05f);
                beeSpecies.setHumidity(EnumHumidity.ARID);
                beeSpecies.setTemperature(EnumTemperature.COLD);
                beeSpecies.setHasEffect();
            },
            template -> AlleleHelper.getInstance().set(template, SPEED, EnumAllele.Speed.SLOWER),
            dis -> {
                IBeeMutationBuilder mutation = dis.registerMutation(TUNGSTEN, PLATINUM, 5);
                mutation.addMutationCondition(new MaterialMutationCondition(Osmium));
            }),
    SALTY(GTBranchDefinition.GT_RAREMETAL, "Sal", true, 0xF0C8C8, 0xFAFAFA,
            beeSpecies -> {
                beeSpecies.addProduct(getGTComb(GTCombType.SLAG), 0.30f);
                beeSpecies.addSpecialty(getGTComb(GTCombType.SALT), 0.15f);
                beeSpecies.addSpecialty(getGTComb(GTCombType.LITHIUM), 0.05f);
                beeSpecies.setHumidity(EnumHumidity.NORMAL);
                beeSpecies.setTemperature(EnumTemperature.WARM);
            },
            template -> AlleleHelper.getInstance().set(template, SPEED, EnumAllele.Speed.SLOWER),
            dis -> {
                IBeeMutationBuilder mutation = dis.registerMutation(CLAY, ALUMINIUM, 5);
                mutation.addMutationCondition(new MaterialMutationCondition(Salt));
            }),
    LITHIUM(GTBranchDefinition.GT_RAREMETAL, "Lithos", false, 0xF0328C, 0xE1DCFF,
            beeSpecies -> {
                beeSpecies.addProduct(getGTComb(GTCombType.SLAG), 0.30f);
                beeSpecies.addSpecialty(getGTComb(GTCombType.LITHIUM), 0.15f);
                beeSpecies.addSpecialty(getGTComb(GTCombType.SALT), 0.05f);
                beeSpecies.setHumidity(EnumHumidity.NORMAL);
                beeSpecies.setTemperature(EnumTemperature.COLD);
            },
            template -> AlleleHelper.getInstance().set(template, SPEED, EnumAllele.Speed.SLOWER),
            dis -> {
                IBeeMutationBuilder mutation = dis.registerMutation(SALTY, ALUMINIUM, 5);
                mutation.addMutationCondition(new MaterialMutationCondition(Lithium));
            }),
    ELECTROTINE(GTBranchDefinition.GT_RAREMETAL, "Electrum", false, 0x1E90FF, 0x3CB4C8,
            beeSpecies -> {
                beeSpecies.addProduct(getGTComb(GTCombType.SLAG), 0.30f);
                beeSpecies.addSpecialty(getGTComb(GTCombType.ELECTROTINE), 0.15f);
                beeSpecies.addSpecialty(getGTComb(GTCombType.REDSTONE), 0.05f);
                beeSpecies.setHumidity(EnumHumidity.NORMAL);
                beeSpecies.setTemperature(EnumTemperature.HOT);
            },
            template -> AlleleHelper.getInstance().set(template, SPEED, EnumAllele.Speed.SLOWER),
            dis -> {
                IBeeMutationBuilder mutation = dis.registerMutation(REDSTONE, GOLD, 5);
                mutation.addMutationCondition(new MaterialMutationCondition(Electrotine));
            }),
    SULFUR(GTBranchDefinition.GT_RAREMETAL, "Sulphur", false, 0x1E90FF, 0x3CB4C8,
            beeSpecies -> {
                beeSpecies.addProduct(getGTComb(GTCombType.SULFUR), 0.70f);
                beeSpecies.setHumidity(EnumHumidity.NORMAL);
                beeSpecies.setTemperature(EnumTemperature.HOT);
            },
            template -> AlleleHelper.getInstance().set(template, SPEED, EnumAllele.Speed.NORMAL),
            dis -> dis.registerMutation(ASH, PEAT, 15)),
    INDIUM(GTBranchDefinition.GT_RAREMETAL, "Indicium", false, 0xFFA9FF, 0x8F5D99,
            beeSpecies -> {
                beeSpecies.addProduct(getGTComb(GTCombType.INDIUM), 0.05f);
                beeSpecies.setHumidity(EnumHumidity.NORMAL);
                beeSpecies.setTemperature(EnumTemperature.HOT);
                beeSpecies.setHasEffect();
            },
            template -> AlleleHelper.getInstance().set(template, SPEED, EnumAllele.Speed.SLOWEST),
            dis -> {
                IBeeMutationBuilder mutation = dis.registerMutation(LEAD, OSMIUM, 1);
                mutation.addMutationCondition(new MaterialMutationCondition(Indium));
                mutation.restrictBiomeType(BiomeDictionary.Type.END);
            }),

    // Industrial
    ENERGY(GTBranchDefinition.GT_INDUSTRIAL, "Industria", false, 0xC11F1F, 0xEBB9B9,
            beeSpecies -> {
                if (Mods.ExtraBees.isModLoaded()) {
                    beeSpecies.addProduct(getExtraBeesComb(14), 0.30f); // STATIC
                } else {
                    beeSpecies.addProduct(getForestryComb(EnumHoneyComb.SIMMERING), 0.30f);
                }
                beeSpecies.addSpecialty(getGTComb(GTCombType.ENERGY), 0.15f);
                beeSpecies.setHumidity(EnumHumidity.NORMAL);
                beeSpecies.setTemperature(EnumTemperature.WARM);
                beeSpecies.setHasEffect();
            },
            template -> {
                AlleleHelper.getInstance().set(template, SPEED, EnumAllele.Speed.SLOWER);
                AlleleHelper.getInstance().set(template, LIFESPAN, EnumAllele.Lifespan.LONGER);
                AlleleHelper.getInstance().set(template, EFFECT, AlleleEffects.effectIgnition);
                AlleleHelper.getInstance().set(template, TEMPERATURE_TOLERANCE, EnumAllele.Tolerance.DOWN_2);
                AlleleHelper.getInstance().set(template, NEVER_SLEEPS, true);
                AlleleHelper.getInstance().set(template, FLOWER_PROVIDER, EnumAllele.Flowers.NETHER);
                AlleleHelper.getInstance().set(template, FLOWERING, EnumAllele.Flowering.AVERAGE);
            },
            dis -> {
                IBeeMutationBuilder mutation;
                if (Mods.ExtraBees.isModLoaded()) {
                    mutation = dis.registerMutation(BeeDefinition.DEMONIC,
                            ForestryUtil.getSpecies(Mods.ExtraBees, "volcanic"), 10);
                } else {
                    mutation = dis.registerMutation(BeeDefinition.DEMONIC, BeeDefinition.FIENDISH, 10);
                }
                mutation.addMutationCondition(new MaterialMutationCondition(Redstone));
            }),
    LAPOTRON(GTBranchDefinition.GT_INDUSTRIAL, "Azureus", false, 0xFFEBC4, 0xE36400,
            beeSpecies -> {
                beeSpecies.addProduct(getGTComb(GTCombType.LAPIS), 0.20f);
                beeSpecies.addSpecialty(getGTComb(GTCombType.ENERGY), 0.15f);
                beeSpecies.addSpecialty(getGTComb(GTCombType.LAPOTRON), 0.10f);
                beeSpecies.setHumidity(EnumHumidity.DAMP);
                beeSpecies.setTemperature(EnumTemperature.ICY);
                beeSpecies.setHasEffect();
            },
            template -> {
                AlleleHelper.getInstance().set(template, SPEED, EnumAllele.Speed.SLOWER);
                AlleleHelper.getInstance().set(template, LIFESPAN, EnumAllele.Lifespan.LONGER);
                AlleleHelper.getInstance().set(template, EFFECT, AlleleEffects.effectIgnition);
                AlleleHelper.getInstance().set(template, TEMPERATURE_TOLERANCE, EnumAllele.Tolerance.UP_1);
                AlleleHelper.getInstance().set(template, NEVER_SLEEPS, true);
                AlleleHelper.getInstance().set(template, FLOWER_PROVIDER, EnumAllele.Flowers.SNOW);
                AlleleHelper.getInstance().set(template, FLOWERING, EnumAllele.Flowering.AVERAGE);
            },
            dis -> {
                IBeeMutationBuilder mutation = dis.registerMutation(LAPIS, ENERGY, 6);
                mutation.addMutationCondition(new MaterialMutationCondition(Lapis));
                mutation.restrictTemperature(EnumTemperature.ICY);
            }),
    EXPLOSIVE(GTBranchDefinition.GT_INDUSTRIAL, "Explosionis", false, 0x7E270F, 0x747474,
            beeSpecies -> {
                beeSpecies.addProduct(new ItemStack(MetaBlocks.ITNT), 0.2f);
                beeSpecies.setHumidity(EnumHumidity.ARID);
                beeSpecies.setTemperature(EnumTemperature.HELLISH);
                beeSpecies.setHasEffect();
            },
            template -> {
                AlleleHelper.getInstance().set(template, SPEED, EnumAllele.Speed.SLOWEST);
                AlleleHelper.getInstance().set(template, LIFESPAN, EnumAllele.Lifespan.LONGEST);
                AlleleHelper.getInstance().set(template, EFFECT, AlleleEffects.effectSnowing);
                AlleleHelper.getInstance().set(template, TEMPERATURE_TOLERANCE, EnumAllele.Tolerance.NONE);
                AlleleHelper.getInstance().set(template, NEVER_SLEEPS, true);
                AlleleHelper.getInstance().set(template, FLOWER_PROVIDER, EnumAllele.Flowers.SNOW);
                AlleleHelper.getInstance().set(template, FLOWERING, EnumAllele.Flowering.AVERAGE);
            },
            dis -> {
                IBeeMutationBuilder mutation = dis.registerMutation(BeeDefinition.AUSTERE, COAL, 4);
                mutation.requireResource(Blocks.TNT.getDefaultState());
            }),

    // Alloys
    REDALLOY(GTBranchDefinition.GT_ALLOY, "Rubrum", false, 0xE60000, 0xB80000,
            beeSpecies -> {
                beeSpecies.addProduct(getForestryComb(EnumHoneyComb.PARCHED), 0.30f);
                beeSpecies.addSpecialty(getGTComb(GTCombType.REDALLOY), 0.15f);
                beeSpecies.setHumidity(EnumHumidity.NORMAL);
                beeSpecies.setTemperature(EnumTemperature.NORMAL);
            },
            template -> {
                AlleleHelper.getInstance().set(template, SPEED, EnumAllele.Speed.SLOWER);
                AlleleHelper.getInstance().set(template, LIFESPAN, EnumAllele.Lifespan.SHORTER);
            },
            dis -> {
                IBeeMutationBuilder mutation = dis.registerMutation(COPPER, REDSTONE, 10);
                mutation.addMutationCondition(new MaterialMutationCondition(RedAlloy));
            }),
    STAINLESSSTEEL(GTBranchDefinition.GT_ALLOY, "Nonferrugo", false, 0xC8C8DC, 0x778899,
            beeSpecies -> {
                beeSpecies.addProduct(getGTComb(GTCombType.SLAG), 0.30f);
                beeSpecies.addProduct(getGTComb(GTCombType.STEEL), 0.10f);
                beeSpecies.addSpecialty(getGTComb(GTCombType.STAINLESSSTEEL), 0.15f);
                beeSpecies.addSpecialty(getGTComb(GTCombType.CHROME), 0.05f);
                beeSpecies.setHumidity(EnumHumidity.NORMAL);
                beeSpecies.setTemperature(EnumTemperature.HOT);
            },
            template -> {
                AlleleHelper.getInstance().set(template, SPEED, EnumAllele.Speed.FAST);
                AlleleHelper.getInstance().set(template, LIFESPAN, EnumAllele.Lifespan.SHORTEST);
                AlleleHelper.getInstance().set(template, EFFECT, AlleleEffects.effectIgnition);
            },
            dis -> {
                IBeeMutationBuilder mutation = dis.registerMutation(CHROME, STEEL, 9);
                mutation.addMutationCondition(new MaterialMutationCondition(StainlessSteel));
            }),

    // Radioactive
    URANIUM(GTBranchDefinition.GT_RADIOACTIVE, "Ouranos", true, 0x19AF19, 0x169E16,
            beeSpecies -> {
                beeSpecies.addProduct(getGTComb(GTCombType.SLAG), 0.30f);
                beeSpecies.addSpecialty(getGTComb(GTCombType.URANIUM), 0.15f);
                beeSpecies.setHumidity(EnumHumidity.NORMAL);
                beeSpecies.setTemperature(EnumTemperature.COLD);
                beeSpecies.setNocturnal();
            },
            template -> {
                AlleleHelper.getInstance().set(template, SPEED, EnumAllele.Speed.SLOWEST);
                AlleleHelper.getInstance().set(template, LIFESPAN, EnumAllele.Lifespan.LONGEST);
            },
            dis -> {
                IBeeMutationBuilder mutation = dis.registerMutation(BeeDefinition.AVENGING, PLATINUM, 2);
                mutation.addMutationCondition(new MaterialMutationCondition(Uranium));
            }),
    PLUTONIUM(GTBranchDefinition.GT_RADIOACTIVE, "Plutos", true, 0x570000, 0x240000,
            beeSpecies -> {
                beeSpecies.addProduct(getGTComb(GTCombType.SLAG), 0.30f);
                beeSpecies.addProduct(getGTComb(GTCombType.LEAD), 0.15f);
                beeSpecies.addSpecialty(getGTComb(GTCombType.PLUTONIUM), 0.15f);
                beeSpecies.setHumidity(EnumHumidity.NORMAL);
                beeSpecies.setTemperature(EnumTemperature.ICY);
                beeSpecies.setNocturnal();
            },
            template -> {
                AlleleHelper.getInstance().set(template, SPEED, EnumAllele.Speed.SLOWEST);
                AlleleHelper.getInstance().set(template, LIFESPAN, EnumAllele.Lifespan.LONGEST);
            },
            dis -> {
                IBeeMutationBuilder mutation = dis.registerMutation(URANIUM, EMERALD, 2);
                mutation.addMutationCondition(new MaterialMutationCondition(Plutonium239));
            }),
    NAQUADAH(GTBranchDefinition.GT_RADIOACTIVE, "Nasquis", false, 0x003300, 0x002400,
            beeSpecies -> {
                beeSpecies.addProduct(getGTComb(GTCombType.SLAG), 0.30f);
                beeSpecies.addSpecialty(getGTComb(GTCombType.NAQUADAH), 0.15f);
                beeSpecies.setHumidity(EnumHumidity.ARID);
                beeSpecies.setTemperature(EnumTemperature.ICY);
                beeSpecies.setNocturnal();
                beeSpecies.setHasEffect();
            },
            template -> {
                AlleleHelper.getInstance().set(template, SPEED, EnumAllele.Speed.SLOWEST);
                AlleleHelper.getInstance().set(template, LIFESPAN, EnumAllele.Lifespan.LONGEST);
            },
            dis -> {
                IBeeMutationBuilder mutation = dis.registerMutation(PLUTONIUM, IRIDIUM, 1);
                mutation.addMutationCondition(new MaterialMutationCondition(Naquadah));
            }),
    NAQUADRIA(GTBranchDefinition.GT_RADIOACTIVE, "Nasquidrius", false, 0x000000, 0x002400,
            beeSpecies -> {
                beeSpecies.addProduct(getGTComb(GTCombType.SLAG), 0.30f);
                beeSpecies.addSpecialty(getGTComb(GTCombType.NAQUADAH), 0.20f);
                beeSpecies.addSpecialty(getGTComb(GTCombType.NAQUADRIA), 0.15f);
                beeSpecies.setHumidity(EnumHumidity.ARID);
                beeSpecies.setTemperature(EnumTemperature.ICY);
                beeSpecies.setNocturnal();
                beeSpecies.setHasEffect();
            },
            template -> {
                AlleleHelper.getInstance().set(template, SPEED, EnumAllele.Speed.SLOWEST);
                AlleleHelper.getInstance().set(template, LIFESPAN, EnumAllele.Lifespan.LONGEST);
            },
            dis -> {
                IBeeMutationBuilder mutation = dis.registerMutation(PLUTONIUM, IRIDIUM, 1);
                mutation.addMutationCondition(new MaterialMutationCondition(Naquadria));
            }),
    TRINIUM(GTBranchDefinition.GT_RADIOACTIVE, "Trinium", false, 0xB0E0E6, 0xC8C8D2,
            beeSpecies -> {
                beeSpecies.addProduct(getGTComb(GTCombType.TRINIUM), 0.75f);
                beeSpecies.addSpecialty(getGTComb(GTCombType.NAQUADAH), 0.10f);
                beeSpecies.setHumidity(EnumHumidity.NORMAL);
                beeSpecies.setTemperature(EnumTemperature.COLD);
                beeSpecies.setNocturnal();
                beeSpecies.setHasEffect();
            },
            template -> AlleleHelper.getInstance().set(template, SPEED, GTAlleleBeeSpecies.speedBlinding),
            dis -> {
                IBeeMutationBuilder mutation = dis.registerMutation(IRIDIUM, NAQUADAH, 4);
                mutation.addMutationCondition(new MaterialMutationCondition(Trinium));
            }),
    THORIUM(GTBranchDefinition.GT_RADIOACTIVE, "Thorax", false, 0x005000, 0x001E00,
            beeSpecies -> {
                beeSpecies.addProduct(getGTComb(GTCombType.THORIUM), 0.75f);
                beeSpecies.setHumidity(EnumHumidity.NORMAL);
                beeSpecies.setTemperature(EnumTemperature.COLD);
                beeSpecies.setNocturnal();
            },
            template -> {
                AlleleHelper.getInstance().set(template, SPEED, EnumAllele.Speed.SLOWEST);
                AlleleHelper.getInstance().set(template, LIFESPAN, EnumAllele.Lifespan.LONGEST);
            },
            dis -> {
                IMutationBuilder mutation = dis.registerMutation(COAL, URANIUM, 2).setIsSecret();
                mutation.addMutationCondition(new MaterialMutationCondition(Thorium));
            }),
    LUTETIUM(GTBranchDefinition.GT_RADIOACTIVE, "Lutetia", false, 0x00AAFF, 0x0059FF,
            beeSpecies -> {
                beeSpecies.addProduct(getGTComb(GTCombType.LUTETIUM), 0.15f);
                beeSpecies.setHumidity(EnumHumidity.NORMAL);
                beeSpecies.setTemperature(EnumTemperature.NORMAL);
                beeSpecies.setNocturnal();
                beeSpecies.setHasEffect();
            },
            template -> {
                AlleleHelper.getInstance().set(template, SPEED, EnumAllele.Speed.SLOWEST);
                AlleleHelper.getInstance().set(template, LIFESPAN, EnumAllele.Lifespan.LONGEST);
            },
            dis -> {
                IMutationBuilder mutation;
                if (Mods.ExtraBees.isModLoaded()) {
                    mutation = dis.registerMutation(THORIUM, ForestryUtil.getSpecies(Mods.ExtraBees, "rotten"), 1);
                } else {
                    mutation = dis.registerMutation(THORIUM, BeeDefinition.IMPERIAL, 1);
                }
                mutation.setIsSecret();
                mutation.addMutationCondition(new MaterialMutationCondition(Lutetium));
            }),
    AMERICIUM(GTBranchDefinition.GT_RADIOACTIVE, "Libertas", false, 0x287869, 0x0C453A,
            beeSpecies -> {
                beeSpecies.addProduct(getGTComb(GTCombType.AMERICIUM), 0.05f);
                beeSpecies.setHumidity(EnumHumidity.NORMAL);
                beeSpecies.setTemperature(EnumTemperature.NORMAL);
                beeSpecies.setNocturnal();
                beeSpecies.setHasEffect();
            },
            template -> {
                AlleleHelper.getInstance().set(template, SPEED, EnumAllele.Speed.SLOWEST);
                AlleleHelper.getInstance().set(template, LIFESPAN, EnumAllele.Lifespan.LONGEST);
            },
            dis -> {
                IMutationBuilder mutation = dis.registerMutation(LUTETIUM, CHROME, 1).setIsSecret();
                mutation.addMutationCondition(new MaterialMutationCondition(Americium));
            }),
    NEUTRONIUM(GTBranchDefinition.GT_RADIOACTIVE, "Media", false, 0xFFF0F0, 0xFAFAFA,
            beeSpecies -> {
                beeSpecies.addProduct(getGTComb(GTCombType.NEUTRONIUM), 0.0001f);
                beeSpecies.setHumidity(EnumHumidity.DAMP);
                beeSpecies.setTemperature(EnumTemperature.HELLISH);
                beeSpecies.setHasEffect();
            },
            template -> {
                AlleleHelper.getInstance().set(template, SPEED, EnumAllele.Speed.SLOWEST);
                AlleleHelper.getInstance().set(template, LIFESPAN, EnumAllele.Lifespan.LONGEST);
                AlleleHelper.getInstance().set(template, NEVER_SLEEPS, true);
            },
            dis -> {
                IMutationBuilder mutation = dis.registerMutation(NAQUADRIA, AMERICIUM, 1).setIsSecret();
                mutation.addMutationCondition(new MaterialMutationCondition(Neutronium));
            }),

    // Noble Gases
    HELIUM(GTBranchDefinition.GT_NOBLEGAS, "Helium", false, 0xFFA9FF, 0xC8B8B4,
            beeSpecies -> {
                beeSpecies.addProduct(getGTComb(GTCombType.HELIUM), 0.35f);
                beeSpecies.setHumidity(EnumHumidity.NORMAL);
                beeSpecies.setTemperature(EnumTemperature.ICY);
                beeSpecies.setNocturnal();
                beeSpecies.setHasEffect();
            },
            template -> AlleleHelper.getInstance().set(template, LIFESPAN, EnumAllele.Lifespan.SHORTEST),
            dis -> {
                IBeeMutationBuilder mutation;
                if (Mods.MagicBees.isModLoaded()) {
                    mutation = dis.registerMutation(STAINLESSSTEEL,
                            ForestryUtil.getSpecies(Mods.MagicBees, "Watery"), 10);
                } else {
                    mutation = dis.registerMutation(STAINLESSSTEEL, BeeDefinition.INDUSTRIOUS, 10);
                }
                mutation.restrictTemperature(EnumTemperature.ICY);
            }),
    ARGON(GTBranchDefinition.GT_NOBLEGAS, "Argon", false, 0x89D9E1, 0xBDA5C2,
            beeSpecies -> {
                beeSpecies.addProduct(getGTComb(GTCombType.ARGON), 0.35f);
                beeSpecies.setHumidity(EnumHumidity.NORMAL);
                beeSpecies.setTemperature(EnumTemperature.ICY);
                beeSpecies.setNocturnal();
                beeSpecies.setHasEffect();
            },
            template -> AlleleHelper.getInstance().set(template, LIFESPAN, EnumAllele.Lifespan.SHORTEST),
            dis -> {
                IBeeMutationBuilder mutation;
                if (Mods.MagicBees.isModLoaded()) {
                    mutation = dis.registerMutation(HELIUM, ForestryUtil.getSpecies(Mods.MagicBees, "Supernatural"), 8);
                } else {
                    mutation = dis.registerMutation(HELIUM, BeeDefinition.IMPERIAL, 8);
                }
                mutation.restrictTemperature(EnumTemperature.ICY);
            }),
    NEON(GTBranchDefinition.GT_NOBLEGAS, "Novum", false, 0xFFC826, 0xFF7200,
            beeSpecies -> {
                beeSpecies.addProduct(getGTComb(GTCombType.NEON), 0.35f);
                beeSpecies.setHumidity(EnumHumidity.NORMAL);
                beeSpecies.setTemperature(EnumTemperature.ICY);
                beeSpecies.setNocturnal();
                beeSpecies.setHasEffect();
            },
            template -> AlleleHelper.getInstance().set(template, LIFESPAN, EnumAllele.Lifespan.SHORTEST),
            dis -> {
                IBeeMutationBuilder mutation = dis.registerMutation(ARGON, IRON, 6);
                mutation.restrictTemperature(EnumTemperature.ICY);
            }),
    KRYPTON(GTBranchDefinition.GT_NOBLEGAS, "Kryptos", false, 0x8A97B0, 0x160822,
            beeSpecies -> {
                beeSpecies.addProduct(getGTComb(GTCombType.KRYPTON), 0.35f);
                beeSpecies.setHumidity(EnumHumidity.NORMAL);
                beeSpecies.setTemperature(EnumTemperature.ICY);
                beeSpecies.setNocturnal();
                beeSpecies.setHasEffect();
            },
            template -> AlleleHelper.getInstance().set(template, LIFESPAN, EnumAllele.Lifespan.SHORTEST),
            dis -> {
                IBeeMutationBuilder mutation;
                if (Mods.MagicBees.isModLoaded()) {
                    mutation = dis.registerMutation(NEON, ForestryUtil.getSpecies(Mods.MagicBees, "Supernatural"), 4);
                } else {
                    mutation = dis.registerMutation(NEON, BeeDefinition.AVENGING, 4);
                }
                mutation.restrictTemperature(EnumTemperature.ICY);
            }),
    XENON(GTBranchDefinition.GT_NOBLEGAS, "Hostis", false, 0x8A97B0, 0x160822,
            beeSpecies -> {
                beeSpecies.addProduct(getGTComb(GTCombType.XENON), 0.525f);
                beeSpecies.setHumidity(EnumHumidity.NORMAL);
                beeSpecies.setTemperature(EnumTemperature.ICY);
                beeSpecies.setNocturnal();
                beeSpecies.setHasEffect();
            },
            template -> AlleleHelper.getInstance().set(template, LIFESPAN, EnumAllele.Lifespan.SHORTEST),
            dis -> {
                IBeeMutationBuilder mutation = dis.registerMutation(KRYPTON, BeeDefinition.EDENIC, 2);
                mutation.restrictTemperature(EnumTemperature.ICY);
            }),
    OXYGEN(GTBranchDefinition.GT_NOBLEGAS, "Oxygeni", false, 0xFFFFFF, 0x8F8FFF,
            beeSpecies -> {
                beeSpecies.addProduct(getGTComb(GTCombType.OXYGEN), 0.45f);
                beeSpecies.addSpecialty(getGTComb(GTCombType.HYDROGEN), 0.20f);
                beeSpecies.setHumidity(EnumHumidity.NORMAL);
                beeSpecies.setTemperature(EnumTemperature.ICY);
                beeSpecies.setNocturnal();
                beeSpecies.setHasEffect();
            },
            template -> AlleleHelper.getInstance().set(template, LIFESPAN, EnumAllele.Lifespan.SHORTEST),
            dis -> {
                IBeeMutationBuilder mutation = dis.registerMutation(HELIUM, BeeDefinition.ENDED, 15);
                mutation.restrictTemperature(EnumTemperature.ICY);
            }),
    HYDROGEN(GTBranchDefinition.GT_NOBLEGAS, "Hydrogenium", false, 0xFFFFFF, 0xFF1493,
            beeSpecies -> {
                beeSpecies.addProduct(getGTComb(GTCombType.HYDROGEN), 0.45f);
                beeSpecies.addSpecialty(getGTComb(GTCombType.NITROGEN), 0.20f);
                beeSpecies.setHumidity(EnumHumidity.NORMAL);
                beeSpecies.setTemperature(EnumTemperature.ICY);
                beeSpecies.setNocturnal();
                beeSpecies.setHasEffect();
            },
            template -> AlleleHelper.getInstance().set(template, LIFESPAN, EnumAllele.Lifespan.SHORTEST),
            dis -> {
                IBeeMutationBuilder mutation;
                if (Mods.MagicBees.isModLoaded()) {
                    mutation = dis.registerMutation(OXYGEN, ForestryUtil.getSpecies(Mods.MagicBees, "Watery"), 15);
                } else {
                    mutation = dis.registerMutation(OXYGEN, BeeDefinition.INDUSTRIOUS, 15);
                }
                mutation.restrictTemperature(EnumTemperature.ICY);
            }),
    NITROGEN(GTBranchDefinition.GT_NOBLEGAS, "Nitrogenium", false, 0xFFC832, 0xA52A2A,
            beeSpecies -> {
                beeSpecies.addProduct(getGTComb(GTCombType.NITROGEN), 0.45f);
                beeSpecies.addSpecialty(getGTComb(GTCombType.FLUORINE), 0.20f);
                beeSpecies.setHumidity(EnumHumidity.NORMAL);
                beeSpecies.setTemperature(EnumTemperature.ICY);
                beeSpecies.setNocturnal();
                beeSpecies.setHasEffect();
            },
            template -> AlleleHelper.getInstance().set(template, LIFESPAN, EnumAllele.Lifespan.SHORTEST),
            dis -> {
                IBeeMutationBuilder mutation = dis.registerMutation(OXYGEN, HYDROGEN, 15);
                mutation.restrictTemperature(EnumTemperature.ICY);
            }),
    FLUORINE(GTBranchDefinition.GT_NOBLEGAS, "Fluens", false, 0x86AFF0, 0xFF6D00,
            beeSpecies -> {
                beeSpecies.addProduct(getGTComb(GTCombType.FLUORINE), 0.45f);
                beeSpecies.addSpecialty(getGTComb(GTCombType.OXYGEN), 0.20f);
                beeSpecies.setHumidity(EnumHumidity.NORMAL);
                beeSpecies.setTemperature(EnumTemperature.ICY);
                beeSpecies.setNocturnal();
                beeSpecies.setHasEffect();
            },
            template -> AlleleHelper.getInstance().set(template, LIFESPAN, EnumAllele.Lifespan.SHORTEST),
            dis -> {
                IBeeMutationBuilder mutation = dis.registerMutation(NITROGEN, HYDROGEN, 15);
                mutation.restrictTemperature(EnumTemperature.ICY);
            });

    private final GTBranchDefinition branch;
    private final GTAlleleBeeSpecies species;
    private final Consumer<GTAlleleBeeSpecies> speciesProperties;
    private final Consumer<IAllele[]> alleles;
    private final Consumer<GTBeeDefinition> mutations;
    private IAllele[] template;
    private IBeeGenome genome;
    private final Supplier<Boolean> generationCondition;

    GTBeeDefinition(GTBranchDefinition branch,
                    String binomial,
                    boolean dominant,
                    int primary,
                    int secondary,
                    Consumer<GTAlleleBeeSpecies> speciesProperties,
                    Consumer<IAllele[]> alleles,
                    Consumer<GTBeeDefinition> mutations) {
        this(branch, binomial, dominant, primary, secondary, speciesProperties, alleles, mutations, () -> true);
    }

    GTBeeDefinition(GTBranchDefinition branch,
                    String binomial,
                    boolean dominant,
                    int primary,
                    int secondary,
                    Consumer<GTAlleleBeeSpecies> speciesProperties,
                    Consumer<IAllele[]> alleles,
                    Consumer<GTBeeDefinition> mutations,
                    Supplier<Boolean> generationCondition) {
        this.alleles = alleles;
        this.mutations = mutations;
        this.speciesProperties = speciesProperties;
        String lowercaseName = this.toString().toLowerCase(Locale.ENGLISH);
        String species = WordUtils.capitalize(lowercaseName);

        String uid = "gregtech.bee.species" + species;
        String description = "for.bees.description." + lowercaseName;
        String name = "for.bees.species." + lowercaseName;

        this.branch = branch;
        this.species = new GTAlleleBeeSpecies(GTValues.MODID, uid, name, GTValues.MOD_NAME, description, dominant,
                branch.getBranch(), binomial, primary, secondary);
        this.generationCondition = generationCondition;
    }

    public static void initBees() {
        for (GTBeeDefinition bee : values()) {
            bee.init();
        }
        for (GTBeeDefinition bee : values()) {
            bee.registerMutations();
        }
    }

    private static ItemStack getForestryComb(EnumHoneyComb type) {
        return ModuleApiculture.getItems().beeComb.get(type, 1);
    }

    @Optional.Method(modid = Mods.Names.EXTRA_BEES)
    private static ItemStack getExtraBeesComb(int meta) {
        return Mods.ExtraBees.getItem("honey_comb", meta);
    }

    @Optional.Method(modid = Mods.Names.MAGIC_BEES)
    private static ItemStack getMagicBeesComb(int meta) {
        return Mods.MagicBees.getItem("beecomb", meta);
    }

    private static ItemStack getGTComb(GTCombType type) {
        return new ItemStack(ForestryModule.COMBS, 1, type.ordinal());
    }

    private void setSpeciesProperties(GTAlleleBeeSpecies beeSpecies) {
        this.speciesProperties.accept(beeSpecies);
    }

    private void setAlleles(IAllele[] template) {
        this.alleles.accept(template);
    }

    private void registerMutations() {
        if (generationCondition.get()) {
            this.mutations.accept(this);
        }
    }

    private void init() {
        if (generationCondition.get()) {
            setSpeciesProperties(species);

            template = branch.getTemplate();
            AlleleHelper.getInstance().set(template, SPECIES, species);
            setAlleles(template);

            // noinspection ConstantConditions
            genome = BeeManager.beeRoot.templateAsGenome(template);

            BeeManager.beeRoot.registerTemplate(template);
        }
    }

    private IBeeMutationBuilder registerMutation(IBeeDefinition parent1, IBeeDefinition parent2, int chance) {
        return registerMutation(parent1.getGenome().getPrimary(), parent2.getGenome().getPrimary(), chance);
    }

    private IBeeMutationBuilder registerMutation(IAlleleBeeSpecies parent1, IBeeDefinition parent2, int chance) {
        return registerMutation(parent1, parent2.getGenome().getPrimary(), chance);
    }

    private IBeeMutationBuilder registerMutation(IBeeDefinition parent1, IAlleleBeeSpecies parent2, int chance) {
        return registerMutation(parent1.getGenome().getPrimary(), parent2, chance);
    }

    private IBeeMutationBuilder registerMutation(IAlleleBeeSpecies parent1, IAlleleBeeSpecies parent2, int chance) {
        // noinspection ConstantConditions
        return BeeManager.beeMutationFactory.createMutation(parent1, parent2, getTemplate(), chance);
    }

    @Override
    public final IAllele @NotNull [] getTemplate() {
        return Arrays.copyOf(template, template.length);
    }

    @NotNull
    @Override
    public final IBeeGenome getGenome() {
        return genome;
    }

    @NotNull
    @Override
    public final IBee getIndividual() {
        return new Bee(genome);
    }

    @NotNull
    @Override
    public final ItemStack getMemberStack(@NotNull EnumBeeType beeType) {
        // noinspection ConstantConditions
        return BeeManager.beeRoot.getMemberStack(getIndividual(), beeType);
    }
}
