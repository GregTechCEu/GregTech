package gregtech.common;

import gregtech.api.damagesources.DamageSources;
import gregtech.api.unification.material.Materials;
import gregtech.common.items.MetaItems;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import static net.minecraft.inventory.EntityEquipmentSlot.*;

public final class DimensionBreathabilityHandler {

    private static FluidStack oxyStack;
    private static final Map<Integer, BreathabilityInfo> dimensionBreathabilityMap = new HashMap<>();
    private static BreathabilityInfo defaultDimensionBreathability;
    private static final Map<BreathabilityItemMapKey, BreathabilityInfo> itemBreathabilityMap = new HashMap<>() {

        {
            this.put(MetaItems.SIMPLE_GAS_MASK.getStackForm(), new BreathabilityInfo(true, false, 10));
            this.put(MetaItems.GAS_MASK.getStackForm(), new BreathabilityInfo(true, true, 0));
            this.put(MetaItems.NANO_HELMET.getStackForm(), new BreathabilityInfo(true, true, 0, 5));
            this.put(MetaItems.NANO_CHESTPLATE.getStackForm(), new BreathabilityInfo(5));
            this.put(MetaItems.NANO_CHESTPLATE_ADVANCED.getStackForm(), new BreathabilityInfo(10));
            this.put(MetaItems.NANO_LEGGINGS.getStackForm(), new BreathabilityInfo(5));
            this.put(MetaItems.NANO_BOOTS.getStackForm(), new BreathabilityInfo(5));
            this.put(MetaItems.QUANTUM_HELMET.getStackForm(), new BreathabilityInfo(true, true, 0, 25));
            this.put(MetaItems.QUANTUM_CHESTPLATE.getStackForm(), new BreathabilityInfo(25));
            this.put(MetaItems.QUANTUM_CHESTPLATE_ADVANCED.getStackForm(), new BreathabilityInfo(35));
            this.put(MetaItems.QUANTUM_LEGGINGS.getStackForm(), new BreathabilityInfo(25));
            this.put(MetaItems.QUANTUM_BOOTS.getStackForm(), new BreathabilityInfo(25));
        }

        private void put(ItemStack stack, BreathabilityInfo info) {
            this.put(new BreathabilityItemMapKey(stack), info);
        }
    };

    private static boolean hasSuffocated = false;

    private DimensionBreathabilityHandler() {}

    public static void loadConfig() {
        oxyStack = Materials.Oxygen.getFluid(1);

        dimensionBreathabilityMap.clear();
        defaultDimensionBreathability = new BreathabilityInfo(false, false, false);
        String[] configData = ConfigHolder.misc.dimensionAirHazards;
        for (String dim : configData) {
            try {
                String[] d = dim.concat(" ").split(":");
                if (d.length != 2) throw new Exception();
                BreathabilityInfo info = new BreathabilityInfo(d[1].contains("s"), d[1].contains("t"),
                        d[1].contains("r"));

                if (Objects.equals(d[0], "default")) defaultDimensionBreathability = info;
                else dimensionBreathabilityMap.put(Integer.parseInt(d[0]), info);

            } catch (Exception e) {
                throw new IllegalArgumentException("Unparsable dim breathability data: " + dim);
            }
        }
    }

    public static void checkPlayer(EntityPlayer player) {
        BreathabilityInfo dimInfo = dimensionBreathabilityMap.get(player.dimension);
        if (dimInfo == null) {
            dimInfo = defaultDimensionBreathability;
        }
        if (ConfigHolder.misc.enableDimSuffocation && dimInfo.suffocation) suffocationCheck(player);
        if (ConfigHolder.misc.enableDimToxicity && dimInfo.toxic) toxicityCheck(player, dimInfo.toxicityRating);
        if (ConfigHolder.misc.enableDimRadiation && dimInfo.radiation)
            radiationCheck(player, dimInfo.radiationRating);
        hasSuffocated = false;
    }

    private static void suffocationCheck(EntityPlayer player) {
        BreathabilityInfo itemInfo = itemBreathabilityMap.get(getItemKey(player, HEAD));
        if (itemInfo != null && itemInfo.suffocation && drainOxy(player)) return;
        suffocate(player);
    }

    private static void suffocate(EntityPlayer player) {
        if (!hasSuffocated) {
            hasSuffocated = true;
            player.attackEntityFrom(DamageSources.getSuffocationDamage(), 2);
        }
    }

    private static void toxicityCheck(EntityPlayer player, int dimRating) {
        BreathabilityInfo itemInfo = itemBreathabilityMap.get(getItemKey(player, HEAD));
        if (itemInfo != null && itemInfo.toxic) {
            // if sealed, no need for toxicity check
            if (itemInfo.isSealed) {
                if (drainOxy(player)) return;
                else if (ConfigHolder.misc.enableDimSuffocation) suffocate(player);
            } else if (dimRating > itemInfo.toxicityRating) {
                toxificate(player, dimRating - itemInfo.toxicityRating);
                return;
            }
        }
        toxificate(player, 100);
    }

    private static void toxificate(EntityPlayer player, int mult) {
        player.attackEntityFrom(DamageSources.getToxicAtmoDamage(), 0.03f * mult);
    }

    private static void radiationCheck(EntityPlayer player, int dimRating) {
        // natural radiation protection of 30
        int ratingSum = 30;

        BreathabilityInfo itemInfo = itemBreathabilityMap.get(getItemKey(player, HEAD));
        if (itemInfo != null && itemInfo.radiation) ratingSum += itemInfo.radiationRating;
        itemInfo = itemBreathabilityMap.get(getItemKey(player, CHEST));
        if (itemInfo != null && itemInfo.radiation) ratingSum += itemInfo.radiationRating;
        itemInfo = itemBreathabilityMap.get(getItemKey(player, LEGS));
        if (itemInfo != null && itemInfo.radiation) ratingSum += itemInfo.radiationRating;
        itemInfo = itemBreathabilityMap.get(getItemKey(player, FEET));
        if (itemInfo != null && itemInfo.radiation) ratingSum += itemInfo.radiationRating;

        if (dimRating > ratingSum) radiate(player, dimRating - ratingSum);
    }

    private static void radiate(EntityPlayer player, int mult) {
        player.attackEntityFrom(DamageSources.getRadioactiveDamage(), 0.01f * mult);
    }

    private static BreathabilityItemMapKey getItemKey(EntityPlayer player, EntityEquipmentSlot slot) {
        return new BreathabilityItemMapKey(player.getItemStackFromSlot(slot));
    }

    private static boolean drainOxy(EntityPlayer player) {
        // don't drain if we are in creative
        if (player.isCreative()) return true;
        Optional<IFluidHandlerItem> tank = player.inventory.mainInventory.stream()
                .map(a -> a.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null))
                .filter(Objects::nonNull)
                .filter(a -> {
                    FluidStack drain = a.drain(oxyStack, false);
                    return drain != null && drain.amount > 0;
                }).findFirst();
        tank.ifPresent(a -> a.drain(oxyStack, true));
        return tank.isPresent();
    }

    public void addBreathabilityItem(ItemStack item, BreathabilityInfo info) {
        itemBreathabilityMap.put(new BreathabilityItemMapKey(item), info);
    }

    public void removeBreathabilityItem(ItemStack item) {
        itemBreathabilityMap.remove(new BreathabilityItemMapKey(item));
    }

    private static final class BreathabilityItemMapKey {

        public final Item item;
        public final int meta;

        BreathabilityItemMapKey(ItemStack stack) {
            this.item = stack.getItem();
            this.meta = stack.getMetadata();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            BreathabilityItemMapKey that = (BreathabilityItemMapKey) o;
            return meta == that.meta && Objects.equals(item, that.item);
        }

        @Override
        public int hashCode() {
            return Objects.hash(item, meta);
        }
    }

    public static final class BreathabilityInfo {

        public final boolean suffocation;
        public final boolean toxic;
        public final boolean radiation;

        private int toxicityRating;
        private int radiationRating;

        public final boolean isSealed;

        /**
         * Default constructor for dimensions only
         */
        public BreathabilityInfo(boolean suffocation, boolean toxic, boolean radiation) {
            this.suffocation = suffocation;
            this.toxic = toxic;
            this.radiation = radiation;
            this.radiationRating = 100;
            this.toxicityRating = 100;
            this.isSealed = false;
        }

        public BreathabilityInfo(boolean suffocation) {
            this.suffocation = suffocation;
            this.toxic = false;
            this.radiation = false;
            this.isSealed = false;
        }

        public BreathabilityInfo(boolean suffocation, boolean isSealed, int toxicityRating) {
            this.suffocation = suffocation;
            this.toxic = true;
            this.radiation = false;
            this.isSealed = isSealed;
            this.toxicityRating = toxicityRating;
        }

        public BreathabilityInfo(boolean suffocation, int radiationRating) {
            this.suffocation = suffocation;
            this.toxic = false;
            this.radiation = true;
            this.isSealed = false;
            this.radiationRating = radiationRating;
        }

        public BreathabilityInfo(boolean suffocation, boolean isSealed, int toxicityRating, int radiationRating) {
            this.suffocation = suffocation;
            this.toxic = true;
            this.radiation = true;
            this.isSealed = isSealed;
            this.toxicityRating = toxicityRating;
            this.radiationRating = radiationRating;
        }

        /**
         * For non-helmet items
         */
        public BreathabilityInfo(int radiationRating) {
            this.suffocation = false;
            this.toxic = false;
            this.radiation = true;
            this.isSealed = false;
            this.toxicityRating = 0;
            this.radiationRating = radiationRating;
        }
    }
}
