package gregtech.api.unification.material.materials;

import gregtech.api.unification.material.properties.FluidProperty;
import gregtech.api.unification.material.properties.OreProperty;
import gregtech.api.unification.material.properties.PropertyKey;

import static gregtech.api.unification.material.Materials.*;

public class MaterialFlagAddition {

    public static void register() {
        OreProperty oreProp = Aluminium.getProperty(PropertyKey.ORE);
        oreProp.setOreByProducts(Bauxite);

        oreProp = Beryllium.getProperty(PropertyKey.ORE);
        oreProp.setOreByProducts(Emerald);

        oreProp = Cobalt.getProperty(PropertyKey.ORE);
        oreProp.setOreByProducts(Cobaltite);
        oreProp.setWashedIn(SodiumPersulfate);

        oreProp = Copper.getProperty(PropertyKey.ORE);
        oreProp.setOreByProducts(Cobalt, Gold, Nickel);
        oreProp.setWashedIn(Mercury);

        oreProp = Gold.getProperty(PropertyKey.ORE);
        oreProp.setOreByProducts(Copper, Nickel);
        oreProp.setWashedIn(Mercury);

        oreProp = Iron.getProperty(PropertyKey.ORE);
        oreProp.setOreByProducts(Nickel, Tin);
        oreProp.setWashedIn(SodiumPersulfate);

        oreProp = Lead.getProperty(PropertyKey.ORE);
        oreProp.setOreByProducts(Silver, Sulfur);
        oreProp.setWashedIn(Mercury);

        oreProp = Lithium.getProperty(PropertyKey.ORE);
        oreProp.setOreByProducts(Lithium);

        //oreProp = Magnesium.getProperty(PropertyKey.ORE);
        //oreProp.setOreByProducts(Olivine);

        //oreProp = Manganese.getProperty(PropertyKey.ORE);
        //oreProp.setOreByProducts(Chrome, Iron);
        //oreProp.setSeparatedInto(Iron);

        oreProp = Neodymium.getProperty(PropertyKey.ORE);
        oreProp.setOreByProducts(Monazite, RareEarth);

        oreProp = Nickel.getProperty(PropertyKey.ORE);
        oreProp.setOreByProducts(Cobalt, Platinum, Iron);
        oreProp.setSeparatedInto(Iron);
        oreProp.setWashedIn(Mercury);

        oreProp = Platinum.getProperty(PropertyKey.ORE);
        oreProp.setOreByProducts(Nickel, Iridium);
        oreProp.setWashedIn(Mercury);

        //oreProp = Plutonium239.getProperty(PropertyKey.ORE);
        //oreProp.setOreByProducts(Uranium238, Lead);

        //oreProp = Silicon.getProperty(PropertyKey.ORE);
        //oreProp.setOreByProducts(SiliconDioxide);

        oreProp = Silver.getProperty(PropertyKey.ORE);
        oreProp.setOreByProducts(Lead, Sulfur);
        oreProp.setWashedIn(Mercury);

        oreProp = Sulfur.getProperty(PropertyKey.ORE);
        oreProp.setOreByProducts(Sulfur);

        oreProp = Thorium.getProperty(PropertyKey.ORE);
        oreProp.setOreByProducts(Uranium238, Lead);

        oreProp = Tin.getProperty(PropertyKey.ORE);
        oreProp.setOreByProducts(Iron, Zinc);
        oreProp.setSeparatedInto(Iron);
        oreProp.setWashedIn(SodiumPersulfate);

        //oreProp = Titanium.getProperty(PropertyKey.ORE);
        //oreProp.setOreByProducts(Almandine);

        //oreProp = Tungsten.getProperty(PropertyKey.ORE);
        //oreProp.setOreByProducts(Manganese, Molybdenum);

        oreProp = Naquadah.getProperty(PropertyKey.ORE);
        oreProp.setOreByProducts(NaquadahEnriched);
        oreProp.setSeparatedInto(NaquadahEnriched, Trinium);

        oreProp = Almandine.getProperty(PropertyKey.ORE);
        oreProp.setOreByProducts(GarnetRed, Aluminium);

        oreProp = Asbestos.getProperty(PropertyKey.ORE);
        oreProp.setOreByProducts(Asbestos, Silicon, Magnesium);

        oreProp = BlueTopaz.getProperty(PropertyKey.ORE);
        oreProp.setOreByProducts(Topaz);

        oreProp = BrownLimonite.getProperty(PropertyKey.ORE);
        oreProp.setOreByProducts(Malachite, YellowLimonite);
        oreProp.setSeparatedInto(Iron);
        oreProp.setDirectSmeltResult(Iron);

        oreProp = Calcite.getProperty(PropertyKey.ORE);
        oreProp.setOreByProducts(Andradite, Malachite);

        oreProp = Cassiterite.getProperty(PropertyKey.ORE);
        oreProp.setOreByProducts(Tin, Bismuth);
        oreProp.setDirectSmeltResult(Tin);

        oreProp = CassiteriteSand.getProperty(PropertyKey.ORE);
        oreProp.setOreByProducts(Tin);
        oreProp.setDirectSmeltResult(Tin);

        oreProp = Chalcopyrite.getProperty(PropertyKey.ORE);
        oreProp.setOreByProducts(Pyrite, Cobalt, Cadmium, Gold);
        oreProp.setWashedIn(Mercury);
        oreProp.setDirectSmeltResult(Copper);

        oreProp = Chromite.getProperty(PropertyKey.ORE);
        oreProp.setOreByProducts(Iron, Magnesium);
        oreProp.setSeparatedInto(Iron);

        oreProp = Cinnabar.getProperty(PropertyKey.ORE);
        oreProp.setOreByProducts(Redstone, Sulfur, Glowstone);

        oreProp = Coal.getProperty(PropertyKey.ORE);
        oreProp.setOreByProducts(Coal, Thorium);

        oreProp = Cobaltite.getProperty(PropertyKey.ORE);
        oreProp.setOreByProducts(Cobalt);
        oreProp.setWashedIn(SodiumPersulfate);
        oreProp.setDirectSmeltResult(Cobalt);

        oreProp = Cooperite.getProperty(PropertyKey.ORE);
        oreProp.setOreByProducts(Palladium, Nickel, Iridium);
        oreProp.setWashedIn(Mercury);
        oreProp.setDirectSmeltResult(Platinum);

        oreProp = Diamond.getProperty(PropertyKey.ORE);
        oreProp.setOreByProducts(Graphite);

        oreProp = Emerald.getProperty(PropertyKey.ORE);
        oreProp.setOreByProducts(Beryllium, Aluminium);

        oreProp = Galena.getProperty(PropertyKey.ORE);
        oreProp.setOreByProducts(Sulfur, Silver, Lead);
        oreProp.setWashedIn(Mercury);
        oreProp.setDirectSmeltResult(Lead);

        oreProp = Garnierite.getProperty(PropertyKey.ORE);
        oreProp.setOreByProducts(Nickel);
        oreProp.setWashedIn(SodiumPersulfate);
        oreProp.setDirectSmeltResult(Nickel);

        oreProp = GreenSapphire.getProperty(PropertyKey.ORE);
        oreProp.setOreByProducts(Aluminium, Sapphire);

        oreProp = Grossular.getProperty(PropertyKey.ORE);
        oreProp.setOreByProducts(GarnetYellow, Calcium);

        oreProp = Ilmenite.getProperty(PropertyKey.ORE);
        oreProp.setOreByProducts(Iron, Rutile);
        oreProp.setSeparatedInto(Iron);

        oreProp = Bauxite.getProperty(PropertyKey.ORE);
        oreProp.setOreByProducts(Grossular, Rutile, Gallium);

        oreProp = Lazurite.getProperty(PropertyKey.ORE);
        oreProp.setOreByProducts(Sodalite, Lapis);

        oreProp = Magnesite.getProperty(PropertyKey.ORE);
        oreProp.setOreByProducts(Magnesium);
        oreProp.setDirectSmeltResult(Magnesium);

        oreProp = Magnetite.getProperty(PropertyKey.ORE);
        oreProp.setOreByProducts(Iron, Gold);
        oreProp.setSeparatedInto(Gold);
        oreProp.setWashedIn(Mercury);
        oreProp.setDirectSmeltResult(Iron);

        oreProp = Molybdenite.getProperty(PropertyKey.ORE);
        oreProp.setOreByProducts(Molybdenum);
        oreProp.setDirectSmeltResult(Molybdenum);

        oreProp = Pyrite.getProperty(PropertyKey.ORE);
        oreProp.setOreByProducts(Sulfur, TricalciumPhosphate, Iron);
        oreProp.setSeparatedInto(Iron);
        oreProp.setDirectSmeltResult(Iron);

        oreProp = Pyrolusite.getProperty(PropertyKey.ORE);
        oreProp.setOreByProducts(Manganese, Tantalite, Niobium);
        oreProp.setDirectSmeltResult(Manganese);

        oreProp = Pyrope.getProperty(PropertyKey.ORE);
        oreProp.setOreByProducts(GarnetRed, Magnesium);

        oreProp = Realgar.getProperty(PropertyKey.ORE); // todo
        oreProp.setOreByProducts(Sulfur);

        oreProp = RockSalt.getProperty(PropertyKey.ORE);
        oreProp.setOreByProducts(Salt, Borax);

        oreProp = Ruby.getProperty(PropertyKey.ORE);
        oreProp.setOreByProducts(Chrome, GarnetRed);

        oreProp = Salt.getProperty(PropertyKey.ORE);
        oreProp.setOreByProducts(RockSalt, Borax);

        oreProp = Saltpeter.getProperty(PropertyKey.ORE);
        oreProp.setOreByProducts(Saltpeter, Potassium, Salt);

        oreProp = Sapphire.getProperty(PropertyKey.ORE);
        oreProp.setOreByProducts(Aluminium, GreenSapphire);

        oreProp = Scheelite.getProperty(PropertyKey.ORE);
        oreProp.setOreByProducts(Manganese, Molybdenum, Calcium);

        oreProp = Sodalite.getProperty(PropertyKey.ORE);
        oreProp.setOreByProducts(Lazurite, Lapis);

        oreProp = Tantalite.getProperty(PropertyKey.ORE);
        oreProp.setOreByProducts(Manganese, Niobium, Tantalum);

        oreProp = Spessartine.getProperty(PropertyKey.ORE);
        oreProp.setOreByProducts(GarnetRed, Manganese);

        oreProp = Sphalerite.getProperty(PropertyKey.ORE);
        oreProp.setOreByProducts(GarnetYellow, Cadmium, Gallium, Zinc);
        oreProp.setWashedIn(SodiumPersulfate);
        oreProp.setDirectSmeltResult(Zinc);

        oreProp = Stibnite.getProperty(PropertyKey.ORE);
        oreProp.setOreByProducts(Antimony);
        oreProp.setDirectSmeltResult(Antimony);

        oreProp = Tetrahedrite.getProperty(PropertyKey.ORE);
        oreProp.setOreByProducts(Antimony, Zinc);
        oreProp.setWashedIn(SodiumPersulfate);
        oreProp.setDirectSmeltResult(Copper);

        oreProp = Topaz.getProperty(PropertyKey.ORE);
        oreProp.setOreByProducts(BlueTopaz);

        oreProp = Tungstate.getProperty(PropertyKey.ORE);
        oreProp.setOreByProducts(Manganese, Silver, Lithium);
        oreProp.setWashedIn(Mercury);

        oreProp = Uraninite.getProperty(PropertyKey.ORE);
        oreProp.setOreByProducts(Uranium238, Thorium, Uranium235);

        oreProp = YellowLimonite.getProperty(PropertyKey.ORE);
        oreProp.setOreByProducts(Nickel, BrownLimonite, Cobalt);
        oreProp.setSeparatedInto(Iron);
        oreProp.setWashedIn(SodiumPersulfate);
        oreProp.setDirectSmeltResult(Iron);

        oreProp = NetherQuartz.getProperty(PropertyKey.ORE);
        oreProp.setOreByProducts(Netherrack);

        oreProp = CertusQuartz.getProperty(PropertyKey.ORE);
        oreProp.setOreByProducts(Quartzite, Barite);

        oreProp = Quartzite.getProperty(PropertyKey.ORE);
        oreProp.setOreByProducts(CertusQuartz, Barite);

        oreProp = Graphite.getProperty(PropertyKey.ORE);
        oreProp.setOreByProducts(Carbon);

        oreProp = Bornite.getProperty(PropertyKey.ORE);
        oreProp.setOreByProducts(Pyrite, Cobalt, Cadmium, Gold);
        oreProp.setWashedIn(Mercury);
        oreProp.setDirectSmeltResult(Copper);

        oreProp = Chalcocite.getProperty(PropertyKey.ORE);
        oreProp.setOreByProducts(Sulfur, Lead, Silver);
        oreProp.setDirectSmeltResult(Copper);

        oreProp = Bastnasite.getProperty(PropertyKey.ORE);
        oreProp.setOreByProducts(Neodymium, RareEarth);
        oreProp.setSeparatedInto(Neodymium);

        oreProp = Pentlandite.getProperty(PropertyKey.ORE);
        oreProp.setOreByProducts(Iron, Sulfur, Cobalt);
        oreProp.setSeparatedInto(Iron);
        oreProp.setWashedIn(SodiumPersulfate);
        oreProp.setDirectSmeltResult(Nickel);

        oreProp = Spodumene.getProperty(PropertyKey.ORE);
        oreProp.setOreByProducts(Aluminium, Lithium);

        oreProp = Lepidolite.getProperty(PropertyKey.ORE);
        oreProp.setOreByProducts(Lithium, Caesium, Boron);

        oreProp = GlauconiteSand.getProperty(PropertyKey.ORE);
        oreProp.setOreByProducts(Sodium, Aluminium, Iron);
        oreProp.setSeparatedInto(Iron);

        oreProp = Malachite.getProperty(PropertyKey.ORE);
        oreProp.setOreByProducts(Copper, BrownLimonite, Calcite);
        oreProp.setWashedIn(SodiumPersulfate);
        oreProp.setDirectSmeltResult(Copper);

        oreProp = Olivine.getProperty(PropertyKey.ORE);
        oreProp.setOreByProducts(Pyrope, Magnesium, Manganese);

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
        oreProp.setOreByProducts(Magnetite, Vanadium);
        oreProp.setSeparatedInto(Gold);

        oreProp = Pollucite.getProperty(PropertyKey.ORE);
        oreProp.setOreByProducts(Caesium, Aluminium, Rubidium);

        oreProp = Bentonite.getProperty(PropertyKey.ORE);
        oreProp.setOreByProducts(Aluminium, Calcium, Magnesium);

        oreProp = FullersEarth.getProperty(PropertyKey.ORE);
        oreProp.setOreByProducts(Aluminium, Silicon, Magnesium);

        oreProp = Pitchblende.getProperty(PropertyKey.ORE);
        oreProp.setOreByProducts(Thorium, Uranium238, Lead);

        oreProp = Monazite.getProperty(PropertyKey.ORE);
        oreProp.setOreByProducts(Thorium, Neodymium, RareEarth);
        oreProp.setSeparatedInto(Neodymium);

        oreProp = Redstone.getProperty(PropertyKey.ORE);
        oreProp.setOreByProducts(Cinnabar, RareEarth, Glowstone);

        oreProp = Diatomite.getProperty(PropertyKey.ORE);
        oreProp.setOreByProducts(BandedIron, Sapphire);

        oreProp = GraniticMineralSand.getProperty(PropertyKey.ORE);
        oreProp.setOreByProducts(GraniteBlack, Magnetite);
        oreProp.setSeparatedInto(Gold);
        oreProp.setDirectSmeltResult(Iron);

        oreProp = GarnetSand.getProperty(PropertyKey.ORE);
        oreProp.setOreByProducts(GarnetRed, GarnetYellow);

        oreProp = BasalticMineralSand.getProperty(PropertyKey.ORE);
        oreProp.setOreByProducts(Basalt, Magnetite);
        oreProp.setSeparatedInto(Gold);
        oreProp.setDirectSmeltResult(Iron);

        oreProp = BandedIron.getProperty(PropertyKey.ORE);
        oreProp.setSeparatedInto(Iron);
        oreProp.setDirectSmeltResult(Iron);
        oreProp.setOreByProducts(Magnetite, Calcium, Magnesium);

        oreProp = Wulfenite.getProperty(PropertyKey.ORE);
        oreProp.setSeparatedInto(Trinium);
        oreProp.setOreByProducts(Iron, Manganese, Manganese, Lead);

        oreProp = Soapstone.getProperty(PropertyKey.ORE);
        oreProp.setOreByProducts(SiliconDioxide, Magnesium, Calcite, Talc);

        oreProp = Kyanite.getProperty(PropertyKey.ORE);
        oreProp.setOreByProducts(Talc, Aluminium, Silicon);

        oreProp = Gypsum.getProperty(PropertyKey.ORE);
        oreProp.setOreByProducts(Sulfur, Calcium, Salt);

        oreProp = Talc.getProperty(PropertyKey.ORE);
        oreProp.setOreByProducts(Clay, Clay, Carbon);

        oreProp = Powellite.getProperty(PropertyKey.ORE);
        oreProp.setOreByProducts(Iron, Potassium, Molybdenite);

        oreProp = Trona.getProperty(PropertyKey.ORE);
        oreProp.setOreByProducts(Sodium, SodaAsh, SodaAsh);

        oreProp = Mica.getProperty(PropertyKey.ORE);
        oreProp.setOreByProducts(Potassium, Aluminium);

        oreProp = Zeolite.getProperty(PropertyKey.ORE);
        oreProp.setOreByProducts(Calcium, Silicon, Aluminium);

        oreProp = Electrotine.getProperty(PropertyKey.ORE);
        oreProp.setOreByProducts(Redstone, Electrum, Diamond);

        oreProp = Pyrochlore.getProperty(PropertyKey.ORE);
        oreProp.setOreByProducts(Apatite, Calcium, Niobium);

        FluidProperty fluidProp = LiquidAir.getProperty(PropertyKey.FLUID);
        fluidProp.setFluidTemperature(77);

        fluidProp = LiquidNetherAir.getProperty(PropertyKey.FLUID);
        fluidProp.setFluidTemperature(67);

        fluidProp = LiquidEnderAir.getProperty(PropertyKey.FLUID);
        fluidProp.setFluidTemperature(57);
    }
}
