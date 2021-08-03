package gregtech.api.unification.material.materials;

import gregtech.api.unification.material.properties.OreProperty;
import gregtech.api.unification.material.properties.PropertyKey;

import static gregtech.api.unification.material.Materials.*;

public class MaterialFlagAddition {

    public static void register() {
        OreProperty prop = Aluminium.getProperty(PropertyKey.ORE);
        prop.setOreByProducts(Bauxite);

        prop = Antimony.getProperty(PropertyKey.ORE);
        prop.setOreByProducts(Zinc, Iron, Zinc);
        prop.setSeparatedInto(Iron);
        prop.setWashedIn(SodiumPersulfate);

        prop = Beryllium.getProperty(PropertyKey.ORE);
        prop.setOreByProducts(Emerald);

        prop = Chrome.getProperty(PropertyKey.ORE);
        prop.setOreByProducts(Iron, Magnesium);
        prop.setSeparatedInto(Iron);

        prop = Cobalt.getProperty(PropertyKey.ORE);
        prop.setOreByProducts(Cobaltite);
        prop.setWashedIn(SodiumPersulfate);

        prop = Copper.getProperty(PropertyKey.ORE);
        prop.setOreByProducts(Cobalt, Gold, Nickel, Gold);
        prop.setWashedIn(Mercury);

        prop = Gold.getProperty(PropertyKey.ORE);
        prop.setOreByProducts(Copper, Nickel, Gold);
        prop.setWashedIn(Mercury);

        prop = Iridium.getProperty(PropertyKey.ORE);
        prop.setOreByProducts(Platinum, Osmium, Platinum);
        prop.setWashedIn(Mercury);

        prop = Iron.getProperty(PropertyKey.ORE);
        prop.setOreByProducts(Nickel, Tin, Nickel);
        prop.setWashedIn(SodiumPersulfate);

        prop = Lead.getProperty(PropertyKey.ORE);
        prop.setOreByProducts(Silver, Sulfur, Silver);
        prop.setWashedIn(Mercury);

        prop = Lithium.getProperty(PropertyKey.ORE);
        prop.setOreByProducts(Lithium);

        //prop = Magnesium.getProperty(PropertyKey.ORE);
        //prop.setOreByProducts(Olivine);

        //prop = Manganese.getProperty(PropertyKey.ORE);
        //prop.setOreByProducts(Chrome, Iron);
        //prop.setSeparatedInto(Iron);

        prop = Neodymium.getProperty(PropertyKey.ORE);
        prop.setOreByProducts(Monazite, RareEarth);

        prop = Nickel.getProperty(PropertyKey.ORE);
        prop.setOreByProducts(Cobalt, Platinum, Iron, Platinum);
        prop.setSeparatedInto(Iron);
        prop.setWashedIn(Mercury);

        prop = Osmium.getProperty(PropertyKey.ORE);
        prop.setOreByProducts(Iridium, Osmium);
        prop.setWashedIn(Mercury);

        prop = Platinum.getProperty(PropertyKey.ORE);
        prop.setOreByProducts(Nickel, Iridium);
        prop.setWashedIn(Mercury);

        //prop = Plutonium239.getProperty(PropertyKey.ORE);
        //prop.setOreByProducts(Uranium238, Lead);

        //prop = Silicon.getProperty(PropertyKey.ORE);
        //prop.setOreByProducts(SiliconDioxide);

        prop = Silver.getProperty(PropertyKey.ORE);
        prop.setOreByProducts(Lead, Sulfur, Silver);
        prop.setWashedIn(Mercury);

        prop = Sulfur.getProperty(PropertyKey.ORE);
        prop.setOreByProducts(Sulfur);

        prop = Thorium.getProperty(PropertyKey.ORE);
        prop.setOreByProducts(Uranium238, Lead);

        prop = Tin.getProperty(PropertyKey.ORE);
        prop.setOreByProducts(Iron, Zinc);
        prop.setSeparatedInto(Iron);
        prop.setWashedIn(SodiumPersulfate);

        //prop = Titanium.getProperty(PropertyKey.ORE);
        //prop.setOreByProducts(Almandine);

        //prop = Tungsten.getProperty(PropertyKey.ORE);
        //prop.setOreByProducts(Manganese, Molybdenum);

        prop = Uranium238.getProperty(PropertyKey.ORE);
        prop.setOreByProducts(Lead, Uranium235, Thorium);

        prop = Zinc.getProperty(PropertyKey.ORE);
        prop.setOreByProducts(Tin, Gallium);
        prop.setWashedIn(SodiumPersulfate);

        prop = Naquadah.getProperty(PropertyKey.ORE);
        prop.setOreByProducts(NaquadahEnriched);

        prop = NaquadahEnriched.getProperty(PropertyKey.ORE);
        prop.setOreByProducts(Naquadah, Naquadria);

        prop = Almandine.getProperty(PropertyKey.ORE);
        prop.setOreByProducts(GarnetRed, Aluminium);

        //prop = Andradite.getProperty(PropertyKey.ORE);
        //prop.setOreByProducts(GarnetYellow, Iron);
        //prop.setSeparatedInto(Iron);

        prop = Asbestos.getProperty(PropertyKey.ORE);
        prop.setOreByProducts(Asbestos, Silicon, Magnesium);

        prop = BlueTopaz.getProperty(PropertyKey.ORE);
        prop.setOreByProducts(Topaz);

        prop = BrownLimonite.getProperty(PropertyKey.ORE);
        prop.setOreByProducts(Malachite, YellowLimonite);
        prop.setSeparatedInto(Iron);
        prop.setDirectSmeltResult(Iron);

        prop = Calcite.getProperty(PropertyKey.ORE);
        prop.setOreByProducts(Andradite, Malachite);

        prop = Cassiterite.getProperty(PropertyKey.ORE);
        prop.setOreByProducts(Tin, Bismuth);
        prop.setDirectSmeltResult(Tin);

        prop = CassiteriteSand.getProperty(PropertyKey.ORE);
        prop.setOreByProducts(Tin);
        prop.setDirectSmeltResult(Tin);

        prop = Chalcopyrite.getProperty(PropertyKey.ORE);
        prop.setOreByProducts(Pyrite, Cobalt, Cadmium, Gold);
        prop.setWashedIn(Mercury);
        prop.setDirectSmeltResult(Copper);

        prop = Chromite.getProperty(PropertyKey.ORE);
        prop.setOreByProducts(Iron, Magnesium);
        prop.setSeparatedInto(Iron);
        prop.setDirectSmeltResult(Chrome);

        prop = Cinnabar.getProperty(PropertyKey.ORE);
        prop.setOreByProducts(Redstone, Sulfur, Glowstone);

        prop = Coal.getProperty(PropertyKey.ORE);
        prop.setOreByProducts(Lignite, Thorium);

        prop = Cobaltite.getProperty(PropertyKey.ORE);
        prop.setOreByProducts(Cobalt, Cobaltite);
        prop.setWashedIn(SodiumPersulfate);
        prop.setDirectSmeltResult(Cobalt);

        prop = Cooperite.getProperty(PropertyKey.ORE);
        prop.setOreByProducts(Palladium, Nickel, Iridium, Cooperite);
        prop.setWashedIn(Mercury);
        prop.setDirectSmeltResult(Platinum);

        prop = Diamond.getProperty(PropertyKey.ORE);
        prop.setOreByProducts(Graphite);

        prop = Emerald.getProperty(PropertyKey.ORE);
        prop.setOreByProducts(Beryllium, Aluminium);

        prop = Galena.getProperty(PropertyKey.ORE);
        prop.setOreByProducts(Sulfur, Silver, Lead, Silver);
        prop.setWashedIn(Mercury);
        prop.setDirectSmeltResult(Lead);

        prop = Garnierite.getProperty(PropertyKey.ORE);
        prop.setOreByProducts(Nickel);
        prop.setWashedIn(SodiumPersulfate);
        prop.setDirectSmeltResult(Nickel);

        prop = GreenSapphire.getProperty(PropertyKey.ORE);
        prop.setOreByProducts(Aluminium, Sapphire);

        prop = Grossular.getProperty(PropertyKey.ORE);
        prop.setOreByProducts(GarnetYellow, Calcium);

        prop = Ilmenite.getProperty(PropertyKey.ORE);
        prop.setOreByProducts(Iron, Rutile);
        prop.setSeparatedInto(Iron);

        prop = Bauxite.getProperty(PropertyKey.ORE);
        prop.setOreByProducts(Grossular, Rutile, Gallium);

        prop = Lazurite.getProperty(PropertyKey.ORE);
        prop.setOreByProducts(Sodalite, Lapis);

        prop = Magnesite.getProperty(PropertyKey.ORE);
        prop.setOreByProducts(Magnesium);
        prop.setDirectSmeltResult(Magnesium);

        prop = Magnetite.getProperty(PropertyKey.ORE);
        prop.setOreByProducts(Iron, Gold);
        prop.setSeparatedInto(Gold);
        prop.setWashedIn(Mercury);
        prop.setDirectSmeltResult(Iron);

        prop = Molybdenite.getProperty(PropertyKey.ORE);
        prop.setOreByProducts(Molybdenum);
        prop.setDirectSmeltResult(Molybdenum);

        prop = Phosphate.getProperty(PropertyKey.ORE);
        prop.setOreByProducts(Phosphorus);

        prop = Pyrite.getProperty(PropertyKey.ORE);
        prop.setOreByProducts(Sulfur, TricalciumPhosphate, Iron);
        prop.setSeparatedInto(Iron);
        prop.setDirectSmeltResult(Iron);

        prop = Pyrolusite.getProperty(PropertyKey.ORE);
        prop.setOreByProducts(Manganese);
        prop.setDirectSmeltResult(Manganese);

        prop = Pyrope.getProperty(PropertyKey.ORE);
        prop.setOreByProducts(GarnetRed, Magnesium);

        prop = RockSalt.getProperty(PropertyKey.ORE);
        prop.setOreByProducts(Salt, Borax);

        prop = Ruby.getProperty(PropertyKey.ORE);
        prop.setOreByProducts(Chrome, GarnetRed);

        prop = Salt.getProperty(PropertyKey.ORE);
        prop.setOreByProducts(RockSalt, Borax);

        prop = Saltpeter.getProperty(PropertyKey.ORE);
        prop.setOreByProducts(Saltpeter);

        prop = Sapphire.getProperty(PropertyKey.ORE);
        prop.setOreByProducts(Aluminium, GreenSapphire);

        prop = Scheelite.getProperty(PropertyKey.ORE);
        prop.setOreByProducts(Manganese, Molybdenum, Calcium);

        prop = Sodalite.getProperty(PropertyKey.ORE);
        prop.setOreByProducts(Lazurite, Lapis);

        prop = Tantalite.getProperty(PropertyKey.ORE);
        prop.setOreByProducts(Manganese, Niobium, Tantalum);

        prop = Spessartine.getProperty(PropertyKey.ORE);
        prop.setOreByProducts(GarnetRed, Manganese);

        prop = Sphalerite.getProperty(PropertyKey.ORE);
        prop.setOreByProducts(GarnetYellow, Cadmium, Gallium, Zinc);
        prop.setWashedIn(SodiumPersulfate);
        prop.setDirectSmeltResult(Zinc);

        prop = Stibnite.getProperty(PropertyKey.ORE);
        prop.setOreByProducts(Antimony);
        prop.setDirectSmeltResult(Antimony);

        prop = Tanzanite.getProperty(PropertyKey.ORE);
        prop.setOreByProducts(Opal);

        prop = Tetrahedrite.getProperty(PropertyKey.ORE);
        prop.setOreByProducts(Antimony, Zinc, Tetrahedrite);
        prop.setWashedIn(SodiumPersulfate);
        prop.setDirectSmeltResult(Copper);

        prop = Topaz.getProperty(PropertyKey.ORE);
        prop.setOreByProducts(BlueTopaz);

        prop = Tungstate.getProperty(PropertyKey.ORE);
        prop.setOreByProducts(Manganese, Silver, Lithium, Silver);
        prop.setWashedIn(Mercury);

        prop = Uraninite.getProperty(PropertyKey.ORE);
        prop.setOreByProducts(Uranium238, Thorium, Uranium235);

        //prop = Uvarovite.getProperty(PropertyKey.ORE);
        //prop.setOreByProducts(GarnetYellow, Chrome);

        prop = YellowLimonite.getProperty(PropertyKey.ORE);
        prop.setOreByProducts(Nickel, BrownLimonite, Cobalt, Nickel);
        prop.setSeparatedInto(Iron);
        prop.setWashedIn(SodiumPersulfate);
        prop.setDirectSmeltResult(Iron);

        prop = NetherQuartz.getProperty(PropertyKey.ORE);
        prop.setOreByProducts(Netherrack);

        prop = CertusQuartz.getProperty(PropertyKey.ORE);
        prop.setOreByProducts(Quartzite, Barite);

        prop = Quartzite.getProperty(PropertyKey.ORE);
        prop.setOreByProducts(CertusQuartz, Barite);

        prop = Graphite.getProperty(PropertyKey.ORE);
        prop.setOreByProducts(Carbon);

        prop = Tenorite.getProperty(PropertyKey.ORE);
        prop.setOreByProducts(Iron, Manganese, Malachite);
        prop.setDirectSmeltResult(Copper);

        prop = Cuprite.getProperty(PropertyKey.ORE);
        prop.setOreByProducts(Iron, Antimony, Malachite);
        prop.setDirectSmeltResult(Copper);

        prop = Bornite.getProperty(PropertyKey.ORE);
        prop.setOreByProducts(Pyrite, Cobalt, Cadmium, Gold);
        prop.setWashedIn(Mercury);
        prop.setDirectSmeltResult(Copper);

        prop = Chalcocite.getProperty(PropertyKey.ORE);
        prop.setOreByProducts(Sulfur, Lead, Silver);
        prop.setDirectSmeltResult(Copper);

        prop = Enargite.getProperty(PropertyKey.ORE);
        prop.setOreByProducts(Pyrite, Zinc, Quartzite);

        prop = Tennantite.getProperty(PropertyKey.ORE);
        prop.setOreByProducts(Iron, Antimony, Zinc);

        prop = Bastnasite.getProperty(PropertyKey.ORE);
        prop.setOreByProducts(Neodymium, RareEarth);
        prop.setSeparatedInto(Neodymium);

        prop = Pentlandite.getProperty(PropertyKey.ORE);
        prop.setOreByProducts(Iron, Sulfur, Cobalt);
        prop.setSeparatedInto(Iron);
        prop.setWashedIn(SodiumPersulfate);
        prop.setDirectSmeltResult(Nickel);

        prop = Spodumene.getProperty(PropertyKey.ORE);
        prop.setOreByProducts(Aluminium, Lithium);

        prop = Lepidolite.getProperty(PropertyKey.ORE);
        prop.setOreByProducts(Lithium, Caesium, Boron);

        prop = Glauconite.getProperty(PropertyKey.ORE);
        prop.setOreByProducts(Sodium, Aluminium, Iron);
        prop.setSeparatedInto(Iron);

        //prop = GlauconiteSand.getProperty(PropertyKey.ORE);
        //prop.setOreByProducts(Sodium, Aluminium, Iron);
        //prop.setSeparatedInto(Iron);

        prop = Malachite.getProperty(PropertyKey.ORE);
        prop.setOreByProducts(Copper, BrownLimonite, Calcite, Copper);
        prop.setWashedIn(SodiumPersulfate);
        prop.setDirectSmeltResult(Copper);

        prop = Lignite.getProperty(PropertyKey.ORE);
        prop.setOreByProducts(Coal);

        prop = Olivine.getProperty(PropertyKey.ORE);
        prop.setOreByProducts(Pyrope, Magnesium, Manganese);

        prop = Opal.getProperty(PropertyKey.ORE);
        prop.setOreByProducts(Tanzanite);

        prop = Amethyst.getProperty(PropertyKey.ORE);
        prop.setOreByProducts(Amethyst);

        prop = Lapis.getProperty(PropertyKey.ORE);
        prop.setOreByProducts(Lazurite, Sodalite, Pyrite);

        //prop = Niter.getProperty(PropertyKey.ORE);
        //prop.setOreByProducts(Saltpeter);

        prop = Apatite.getProperty(PropertyKey.ORE);
        prop.setOreByProducts(TricalciumPhosphate);

        prop = TricalciumPhosphate.getProperty(PropertyKey.ORE);
        prop.setOreByProducts(Apatite, Phosphate);

        prop = GarnetRed.getProperty(PropertyKey.ORE);
        prop.setOreByProducts(Spessartine, Pyrope, Almandine);

        prop = GarnetYellow.getProperty(PropertyKey.ORE);
        prop.setOreByProducts(Andradite, Grossular, Uvarovite);

        //prop = Marble.getProperty(PropertyKey.ORE);
        //prop.setOreByProducts(Calcite);

        //prop = GraniteBlack.getProperty(PropertyKey.ORE);
        //prop.setOreByProducts(Biotite);

        //prop = GraniteRed.getProperty(PropertyKey.ORE);
        //prop.setOreByProducts(PotassiumFeldspar);

        //prop = Chrysotile.getProperty(PropertyKey.ORE);
        //prop.setOreByProducts(Asbestos, Silicon, Magnesium);

        prop = VanadiumMagnetite.getProperty(PropertyKey.ORE);
        prop.setOreByProducts(Magnetite, Vanadium);
        prop.setSeparatedInto(Gold);

        //prop = QuartzSand.getProperty(PropertyKey.ORE);
        //prop.setOreByProducts(CertusQuartz, Quartzite, Barite);

        //prop = Pollucite.getProperty(PropertyKey.ORE);
        //prop.setOreByProducts(Caesium, Aluminium, Rubidium);

        //prop = Vermiculite.getProperty(PropertyKey.ORE);
        //prop.setOreByProducts(Iron, Aluminium, Magnesium);
        //prop.setSeparatedInto(Iron);

        prop = Bentonite.getProperty(PropertyKey.ORE);
        prop.setOreByProducts(Aluminium, Calcium, Magnesium);

        prop = FullersEarth.getProperty(PropertyKey.ORE);
        prop.setOreByProducts(Aluminium, Silicon, Magnesium);

        prop = Pitchblende.getProperty(PropertyKey.ORE);
        prop.setOreByProducts(Thorium, Uranium238, Lead);

        prop = Monazite.getProperty(PropertyKey.ORE);
        prop.setOreByProducts(Thorium, Neodymium, RareEarth);
        prop.setSeparatedInto(Neodymium);

        prop = Vinteum.getProperty(PropertyKey.ORE);
        prop.setOreByProducts(Vinteum);

        prop = Redstone.getProperty(PropertyKey.ORE);
        prop.setOreByProducts(Cinnabar, RareEarth, Glowstone);

        prop = Diatomite.getProperty(PropertyKey.ORE);
        prop.setOreByProducts(BandedIron, Sapphire);

        //prop = Basalt.getProperty(PropertyKey.ORE);
        //prop.setOreByProducts(Olivine, DarkAsh);

        prop = GraniticMineralSand.getProperty(PropertyKey.ORE);
        prop.setOreByProducts(GraniteBlack, Magnetite);
        prop.setSeparatedInto(Gold);
        prop.setDirectSmeltResult(Iron);

        //prop = Redrock.getProperty(PropertyKey.ORE);
        //prop.setOreByProducts(Clay);

        //prop = GarnetSand.getProperty(PropertyKey.ORE);
        //prop.setOreByProducts(GarnetRed, GarnetYellow);

        prop = BasalticMineralSand.getProperty(PropertyKey.ORE);
        prop.setOreByProducts(Basalt, Magnetite);
        prop.setSeparatedInto(Gold);
        prop.setDirectSmeltResult(Iron);
    }
}
