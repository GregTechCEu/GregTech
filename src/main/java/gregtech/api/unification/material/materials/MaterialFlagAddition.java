package gregtech.api.unification.material.materials;

import gregtech.api.unification.material.properties.OreProperty;
import gregtech.api.unification.material.properties.PropertyKey;
import gregtech.loaders.recipe.handlers.oreproc.BathRecipeHandler;

import java.util.ArrayList;
import java.util.Arrays;

import static gregtech.api.unification.material.Materials.*;

public class MaterialFlagAddition {
    // Bath Stacks
    private static final ArrayList<Object> RED_VITRIOL = new ArrayList<>(Arrays.asList(SulfuricAcid, 500, RedVitriol, 500, Hydrogen, 1000));
    private static final ArrayList<Object> BLUE_VITRIOL = new ArrayList<>(Arrays.asList(SulfuricAcid, 500, BlueVitriol, 500, Hydrogen, 1000));
    private static final ArrayList<Object> GREEN_VITRIOL = new ArrayList<>(Arrays.asList(SulfuricAcid, 500, GreenVitriol, 500, Hydrogen, 1000));
    private static final ArrayList<Object> PINK_VITRIOL = new ArrayList<>(Arrays.asList(SulfuricAcid, 500, PinkVitriol, 500, Hydrogen, 1000));
    private static final ArrayList<Object> CYAN_VITRIOL = new ArrayList<>(Arrays.asList(SulfuricAcid, 500, CyanVitriol, 500, Hydrogen, 1000));
    private static final ArrayList<Object> WHITE_VITRIOL = new ArrayList<>(Arrays.asList(SulfuricAcid, 500, WhiteVitriol, 500, Hydrogen, 1000));
    private static final ArrayList<Object> GRAY_VITRIOL = new ArrayList<>(Arrays.asList(SulfuricAcid, 500, GrayVitriol, 500, Hydrogen, 1000));
    private static final ArrayList<Object> CLAY_VITRIOL = new ArrayList<>(Arrays.asList(SulfuricAcid, 1500, ClayVitriol, 500, Hydrogen, 1000));
    private static final ArrayList<Object> SODIUM_GOLD_CYANIDE = new ArrayList<>(Arrays.asList(SodiumCyanide, 2000, SodiumGoldCyanide, 1000));
    private static final ArrayList<Object> INDIUM_CONCENTRATE = new ArrayList<>(Arrays.asList(SulfuricAcid, 2000, IndiumConcentrate, 500));
    private static final ArrayList<Object> PGS_CHLOROPLATINIC_ACID = new ArrayList<>(Arrays.asList(AquaRegia, 2000, PlatinumGroupSlurry, 1000, ChloroplatinicAcid, 1000));


    public static void register() {
        OreProperty oreProp = Aluminium.getProperty(PropertyKey.ORE);
        oreProp.setOreByProducts(Bauxite, Bauxite, Ilmenite, Rutile);
        oreProp.setBathIOStacks(CLAY_VITRIOL);

        oreProp = Beryllium.getProperty(PropertyKey.ORE);
        oreProp.setOreByProducts(Emerald, Emerald, Thorium);

        oreProp = Cobalt.getProperty(PropertyKey.ORE);
        oreProp.setOreByProducts(Cobaltite);
        oreProp.setBathIOStacks(RED_VITRIOL);

        oreProp = Copper.getProperty(PropertyKey.ORE);
        oreProp.setOreByProducts(Cobalt, Gold, Nickel, Gold);
        oreProp.setBathIOStacks(BLUE_VITRIOL);

        oreProp = Gold.getProperty(PropertyKey.ORE);
        oreProp.setOreByProducts(Copper, Nickel, Silver);
        oreProp.setBathIOStacks(SODIUM_GOLD_CYANIDE);

        oreProp = Iron.getProperty(PropertyKey.ORE);
        oreProp.setOreByProducts(Nickel, Tin, Tin, Gold);
        oreProp.setBathIOStacks(GREEN_VITRIOL);

        oreProp = Lead.getProperty(PropertyKey.ORE);
        oreProp.setOreByProducts(Silver, Sulfur);

        oreProp = Lithium.getProperty(PropertyKey.ORE);
        oreProp.setOreByProducts(Lithium);

        oreProp = Molybdenum.getProperty(PropertyKey.ORE);
        oreProp.setOreByProducts(Molybdenum);

        oreProp = Magnesium.getProperty(PropertyKey.ORE);
        oreProp.setOreByProducts(Olivine);
        oreProp.setBathIOStacks(PINK_VITRIOL);

        oreProp = Manganese.getProperty(PropertyKey.ORE);
        oreProp.setOreByProducts(Chrome, Iron);
        oreProp.setBathIOStacks(GRAY_VITRIOL);

        oreProp = Neodymium.getProperty(PropertyKey.ORE);
        oreProp.setOreByProducts(RareEarth);

        oreProp = Nickel.getProperty(PropertyKey.ORE);
        oreProp.setOreByProducts(Cobalt, Iron, Platinum);
        oreProp.setBathIOStacks(CYAN_VITRIOL);

        oreProp = Palladium.getProperty(PropertyKey.ORE);
        oreProp.setOreByProducts(Palladium);
        oreProp.setBathIOStacks(PGS_CHLOROPLATINIC_ACID);

        oreProp = Platinum.getProperty(PropertyKey.ORE);
        oreProp.setOreByProducts(Nickel, Nickel, Cobalt, Platinum);
        oreProp.setBathIOStacks(PGS_CHLOROPLATINIC_ACID);

        oreProp = Plutonium239.getProperty(PropertyKey.ORE);
        oreProp.setOreByProducts(Uraninite, Lead, Uraninite);

        //oreProp = Silicon.getProperty(PropertyKey.ORE);
        //oreProp.setOreByProducts(SiliconDioxide);

        oreProp = Silver.getProperty(PropertyKey.ORE);
        oreProp.setOreByProducts(Lead, Sulfur, Sulfur, Gold);

        oreProp = Sulfur.getProperty(PropertyKey.ORE);
        oreProp.setOreByProducts(Pyrite, Cinnabar, Sphalerite);

        oreProp = Thorium.getProperty(PropertyKey.ORE);
        oreProp.setOreByProducts(Uraninite, Lead);

        oreProp = Tin.getProperty(PropertyKey.ORE);
        oreProp.setOreByProducts(Iron, Zinc, Zinc, Cooperite);

        //oreProp = Titanium.getProperty(PropertyKey.ORE);
        //oreProp.setOreByProducts(Almandine);

        //oreProp = Tungsten.getProperty(PropertyKey.ORE);
        //oreProp.setOreByProducts(Manganese, Molybdenum);
        //oreProp.setVitriol(ManganeseSulfate);

        oreProp = Naquadah.getProperty(PropertyKey.ORE);
        oreProp.setOreByProducts(Sulfur, Barite, NaquadahEnriched);

        oreProp = Zinc.getProperty(PropertyKey.ORE);
        oreProp.setOreByProducts(Zinc, Zinc, Gallium);
        oreProp.setBathIOStacks(WHITE_VITRIOL);

        oreProp = CertusQuartz.getProperty(PropertyKey.ORE);
        oreProp.setOreByProducts(NetherQuartz, Barite);

        oreProp = Almandine.getProperty(PropertyKey.ORE);
        oreProp.setOreByProducts(GarnetRed, Aluminium);
        oreProp.setBathIOStacks(CLAY_VITRIOL);

        oreProp = Asbestos.getProperty(PropertyKey.ORE);
        oreProp.setOreByProducts(Diatomite, Silicon, Magnesium);
        oreProp.setBathIOStacks(PINK_VITRIOL);

        oreProp = BlueTopaz.getProperty(PropertyKey.ORE);
        oreProp.setOreByProducts(Topaz);
        oreProp.setBathIOStacks(CLAY_VITRIOL);

        oreProp = BrownLimonite.getProperty(PropertyKey.ORE);
        oreProp.setOreByProducts(Malachite, YellowLimonite);
        oreProp.setDirectSmeltResult(Iron);
        oreProp.setBathIOStacks(GREEN_VITRIOL);

        oreProp = Calcite.getProperty(PropertyKey.ORE);
        oreProp.setOreByProducts(Calcium, Calcium, Sodalite, Malachite);

        oreProp = Cassiterite.getProperty(PropertyKey.ORE);
        oreProp.setOreByProducts(Tin, Bismuth);
        oreProp.setDirectSmeltResult(Tin);

        oreProp = CassiteriteSand.getProperty(PropertyKey.ORE);
        oreProp.setOreByProducts(Tin);
        oreProp.setDirectSmeltResult(Tin);

        oreProp = Chalcopyrite.getProperty(PropertyKey.ORE);
        oreProp.setOreByProducts(Pyrite, Cobalt, Cadmium, Gold);
        oreProp.setDirectSmeltResult(Copper);
        oreProp.setBathIOStacks(BLUE_VITRIOL);

        oreProp = Chromite.getProperty(PropertyKey.ORE);
        oreProp.setOreByProducts(Iron, Magnesium, Chrome);
        oreProp.setBathIOStacks(GREEN_VITRIOL);

        oreProp = Cinnabar.getProperty(PropertyKey.ORE);
        oreProp.setOreByProducts(Redstone, Sulfur, Glowstone);

        oreProp = Coal.getProperty(PropertyKey.ORE);
        oreProp.setOreByProducts(Coal, Coal, Thorium);

        oreProp = Cobaltite.getProperty(PropertyKey.ORE);
        oreProp.setOreByProducts(Sulfur, Cobalt);
        oreProp.setDirectSmeltResult(Cobalt);
        oreProp.setBathIOStacks(RED_VITRIOL);

        oreProp = Cooperite.getProperty(PropertyKey.ORE);
        oreProp.setOreByProducts(Nickel, Nickel, Cobalt, Palladium);
        oreProp.setBathIOStacks(CYAN_VITRIOL);

        oreProp = Diamond.getProperty(PropertyKey.ORE);
        oreProp.setOreByProducts(Graphite);

        oreProp = Emerald.getProperty(PropertyKey.ORE);
        oreProp.setOreByProducts(Beryllium, Aluminium);

        oreProp = Galena.getProperty(PropertyKey.ORE);
        oreProp.setOreByProducts(Sulfur, Silver);
        oreProp.setDirectSmeltResult(Lead);
        oreProp.setBathIOStacks(INDIUM_CONCENTRATE);

        oreProp = Garnierite.getProperty(PropertyKey.ORE);
        oreProp.setOreByProducts(Iron, Nickel);
        oreProp.setDirectSmeltResult(Nickel);
        oreProp.setBathIOStacks(CYAN_VITRIOL);

        oreProp = GreenSapphire.getProperty(PropertyKey.ORE);
        oreProp.setOreByProducts(Aluminium, Sapphire);

        oreProp = Grossular.getProperty(PropertyKey.ORE);
        oreProp.setOreByProducts(GarnetYellow, Calcium);

        oreProp = Ilmenite.getProperty(PropertyKey.ORE);
        oreProp.setOreByProducts(Iron, Rutile);
        oreProp.setBathIOStacks(GREEN_VITRIOL);

        oreProp = Bauxite.getProperty(PropertyKey.ORE);
        oreProp.setOreByProducts(Grossular, Rutile, Gallium);
        oreProp.setBathIOStacks(CLAY_VITRIOL);

        oreProp = Lazurite.getProperty(PropertyKey.ORE);
        oreProp.setOreByProducts(Sodalite, Lapis);

        oreProp = Magnesite.getProperty(PropertyKey.ORE);
        oreProp.setOreByProducts(Magnesium, Magnesium, Cobaltite);
        oreProp.setDirectSmeltResult(Magnesium);
        oreProp.setBathIOStacks(PINK_VITRIOL);

        oreProp = Magnetite.getProperty(PropertyKey.ORE);
        oreProp.setOreByProducts(Iron, Iron, Gold);
        oreProp.setDirectSmeltResult(Iron);
        oreProp.setBathIOStacks(GREEN_VITRIOL);

        oreProp = Molybdenite.getProperty(PropertyKey.ORE);
        oreProp.setOreByProducts(Molybdenum, Sulfur, Quartzite);
        oreProp.setDirectSmeltResult(Molybdenum);

        oreProp = Pyrite.getProperty(PropertyKey.ORE);
        oreProp.setOreByProducts(Sulfur, TricalciumPhosphate, Iron);
        oreProp.setDirectSmeltResult(Iron);
        oreProp.setBathIOStacks(GREEN_VITRIOL);

        oreProp = Pyrolusite.getProperty(PropertyKey.ORE);
        oreProp.setOreByProducts(Manganese, Tantalite, Niobium, Silver);
        oreProp.setDirectSmeltResult(Manganese);
        oreProp.setBathIOStacks(GRAY_VITRIOL);

        oreProp = Pyrope.getProperty(PropertyKey.ORE);
        oreProp.setOreByProducts(GarnetRed, Magnesium);
        oreProp.setBathIOStacks(PINK_VITRIOL);

        oreProp = Realgar.getProperty(PropertyKey.ORE);
        oreProp.setOreByProducts(Sulfur, Antimony, Barite);

        oreProp = RockSalt.getProperty(PropertyKey.ORE);
        oreProp.setOreByProducts(Salt, Borax);

        oreProp = Ruby.getProperty(PropertyKey.ORE);
        oreProp.setOreByProducts(Chrome, GarnetRed, Chrome);

        oreProp = Salt.getProperty(PropertyKey.ORE);
        oreProp.setOreByProducts(RockSalt, Borax);

        oreProp = Saltpeter.getProperty(PropertyKey.ORE);
        oreProp.setOreByProducts(Saltpeter, Potassium, Salt);

        oreProp = Sapphire.getProperty(PropertyKey.ORE);
        oreProp.setOreByProducts(Aluminium, GreenSapphire);

        oreProp = Scheelite.getProperty(PropertyKey.ORE);
        oreProp.setOreByProducts(Manganese, Molybdenum, Calcium);
        oreProp.setBathIOStacks(GRAY_VITRIOL);

        oreProp = Sodalite.getProperty(PropertyKey.ORE);
        oreProp.setOreByProducts(Lazurite, Lapis);

        oreProp = Tantalite.getProperty(PropertyKey.ORE);
        oreProp.setOreByProducts(Manganese, Niobium, Tantalum);
        oreProp.setBathIOStacks(GRAY_VITRIOL);

        oreProp = Spessartine.getProperty(PropertyKey.ORE);
        oreProp.setOreByProducts(GarnetRed, Manganese);
        oreProp.setBathIOStacks(GRAY_VITRIOL);

        oreProp = Sphalerite.getProperty(PropertyKey.ORE);
        oreProp.setOreByProducts(GarnetYellow, Gallium, Cadmium, Zinc);
        oreProp.setDirectSmeltResult(Zinc);
        oreProp.setBathIOStacks(INDIUM_CONCENTRATE);

        oreProp = Stibnite.getProperty(PropertyKey.ORE);
        oreProp.setOreByProducts(Antimony);

        oreProp = Tetrahedrite.getProperty(PropertyKey.ORE);
        oreProp.setOreByProducts(Zinc, Antimony, Cadmium, Copper);
        oreProp.setDirectSmeltResult(Copper);
        oreProp.setBathIOStacks(BLUE_VITRIOL);

        oreProp = Topaz.getProperty(PropertyKey.ORE);
        oreProp.setOreByProducts(BlueTopaz);
        oreProp.setBathIOStacks(CLAY_VITRIOL);

        oreProp = Tungstate.getProperty(PropertyKey.ORE);
        oreProp.setOreByProducts(Manganese, Silver, Lithium);
        oreProp.setBathIOStacks(GRAY_VITRIOL);

        oreProp = Uraninite.getProperty(PropertyKey.ORE);
        oreProp.setOreByProducts(Uraninite, Thorium, Silver);

        oreProp = YellowLimonite.getProperty(PropertyKey.ORE);
        oreProp.setOreByProducts(Nickel, BrownLimonite, Cobalt);
        oreProp.setDirectSmeltResult(Iron);
        oreProp.setBathIOStacks(GREEN_VITRIOL);

        oreProp = NetherQuartz.getProperty(PropertyKey.ORE);
        oreProp.setOreByProducts(Netherrack, Barite);

        oreProp = Quartzite.getProperty(PropertyKey.ORE);
        oreProp.setOreByProducts(CertusQuartz, Barite, Hematite);

        oreProp = Graphite.getProperty(PropertyKey.ORE);
        oreProp.setOreByProducts(Carbon);

        oreProp = Bornite.getProperty(PropertyKey.ORE);
        oreProp.setOreByProducts(Pyrite, Cobalt, Cadmium, Gold);
        oreProp.setDirectSmeltResult(Copper);
        oreProp.setBathIOStacks(BLUE_VITRIOL);

        oreProp = Chalcocite.getProperty(PropertyKey.ORE);
        oreProp.setOreByProducts(Sulfur, Lead, Silver);
        oreProp.setDirectSmeltResult(Copper);
        oreProp.setBathIOStacks(BLUE_VITRIOL);

        oreProp = Bastnasite.getProperty(PropertyKey.ORE);
        oreProp.setOreByProducts(Neodymium, RareEarth);

        oreProp = Pentlandite.getProperty(PropertyKey.ORE);
        oreProp.setOreByProducts(Iron, Sulfur, Cobalt);
        oreProp.setDirectSmeltResult(Nickel);
        oreProp.setBathIOStacks(CYAN_VITRIOL);

        oreProp = Spodumene.getProperty(PropertyKey.ORE);
        oreProp.setOreByProducts(Aluminium, Lithium);

        oreProp = Lepidolite.getProperty(PropertyKey.ORE);
        oreProp.setOreByProducts(Lithium, Caesium, Boron);

        oreProp = GlauconiteSand.getProperty(PropertyKey.ORE);
        oreProp.setOreByProducts(Sodium, Aluminium, Iron);

        oreProp = Malachite.getProperty(PropertyKey.ORE);
        oreProp.setOreByProducts(Copper, BrownLimonite, Calcite);
        oreProp.setDirectSmeltResult(Copper);
        oreProp.setBathIOStacks(BLUE_VITRIOL);

        oreProp = Olivine.getProperty(PropertyKey.ORE);
        oreProp.setOreByProducts(Pyrope, Magnesium, Manganese);
        oreProp.setBathIOStacks(PINK_VITRIOL);

        oreProp = Opal.getProperty(PropertyKey.ORE);
        oreProp.setOreByProducts(Opal);

        oreProp = Amethyst.getProperty(PropertyKey.ORE);
        oreProp.setOreByProducts(Amethyst);

        oreProp = Lapis.getProperty(PropertyKey.ORE);
        oreProp.setOreByProducts(Lazurite, Sodalite, Pyrite);

        oreProp = Apatite.getProperty(PropertyKey.ORE);
        oreProp.setOreByProducts(TricalciumPhosphate, Phosphate, Pyrochlore);

        oreProp = TricalciumPhosphate.getProperty(PropertyKey.ORE);
        oreProp.setOreByProducts(Apatite, Phosphate, Pyrochlore);

        oreProp = GarnetRed.getProperty(PropertyKey.ORE);
        oreProp.setOreByProducts(Spessartine, Pyrope, Almandine);

        oreProp = GarnetYellow.getProperty(PropertyKey.ORE);
        oreProp.setOreByProducts(Andradite, Grossular, Uvarovite);

        oreProp = VanadiumMagnetite.getProperty(PropertyKey.ORE);
        oreProp.setOreByProducts(Magnetite, Magnetite, Vanadium);
        oreProp.setBathIOStacks(GREEN_VITRIOL);

        oreProp = Pollucite.getProperty(PropertyKey.ORE);
        oreProp.setOreByProducts(Caesium, Aluminium, Potassium);

        oreProp = Bentonite.getProperty(PropertyKey.ORE);
        oreProp.setOreByProducts(Aluminium, Calcium, Magnesium);

        oreProp = FullersEarth.getProperty(PropertyKey.ORE);
        oreProp.setOreByProducts(Aluminium, Silicon, Magnesium);

        oreProp = Pitchblende.getProperty(PropertyKey.ORE);
        oreProp.setOreByProducts(Thorium, Uraninite, Lead);

        oreProp = Monazite.getProperty(PropertyKey.ORE);
        oreProp.setOreByProducts(Thorium, Neodymium, RareEarth);

        oreProp = Redstone.getProperty(PropertyKey.ORE);
        oreProp.setOreByProducts(Cinnabar, RareEarth, Glowstone);

        oreProp = Diatomite.getProperty(PropertyKey.ORE);
        oreProp.setOreByProducts(Hematite, Sapphire);

        oreProp = GraniticMineralSand.getProperty(PropertyKey.ORE);
        oreProp.setOreByProducts(GraniteBlack, Magnetite);
        oreProp.setDirectSmeltResult(Iron);

        oreProp = GarnetSand.getProperty(PropertyKey.ORE);
        oreProp.setOreByProducts(GarnetRed, GarnetYellow);

        oreProp = BasalticMineralSand.getProperty(PropertyKey.ORE);
        oreProp.setOreByProducts(Basalt, Magnetite);
        oreProp.setDirectSmeltResult(Iron);

        oreProp = Hematite.getProperty(PropertyKey.ORE);
        oreProp.setOreByProducts(Magnetite, Calcium, Magnesium);
        oreProp.setDirectSmeltResult(Iron);
        oreProp.setBathIOStacks(GREEN_VITRIOL);

        oreProp = Wulfenite.getProperty(PropertyKey.ORE);
        oreProp.setOreByProducts(Iron, Manganese, Manganese, Lead);

        oreProp = Soapstone.getProperty(PropertyKey.ORE);
        oreProp.setOreByProducts(SiliconDioxide, Magnesium, Calcite, Talc);
        oreProp.setBathIOStacks(PINK_VITRIOL);

        oreProp = Kyanite.getProperty(PropertyKey.ORE);
        oreProp.setOreByProducts(Talc, Aluminium, Silicon);
        oreProp.setBathIOStacks(PINK_VITRIOL);

        oreProp = Gypsum.getProperty(PropertyKey.ORE);
        oreProp.setOreByProducts(Sulfur, Calcium, Salt);

        oreProp = Talc.getProperty(PropertyKey.ORE);
        oreProp.setOreByProducts(Clay, Carbon, Clay);
        oreProp.setBathIOStacks(PINK_VITRIOL);

        oreProp = Powellite.getProperty(PropertyKey.ORE);
        oreProp.setOreByProducts(Iron, Potassium, Molybdenite);

        oreProp = Trona.getProperty(PropertyKey.ORE);
        oreProp.setOreByProducts(Sodium, SodaAsh, SodaAsh);

        oreProp = Mica.getProperty(PropertyKey.ORE);
        oreProp.setOreByProducts(Potassium, Aluminium);

        oreProp = Zeolite.getProperty(PropertyKey.ORE);
        oreProp.setOreByProducts(Calcium, Silicon, Aluminium);

        oreProp = Electrotine.getProperty(PropertyKey.ORE);
        oreProp.setOreByProducts(Copper);
        oreProp.setBathIOStacks(BLUE_VITRIOL);

        oreProp = Pyrochlore.getProperty(PropertyKey.ORE);
        oreProp.setOreByProducts(Apatite, Calcium, Niobium);
    }
}
