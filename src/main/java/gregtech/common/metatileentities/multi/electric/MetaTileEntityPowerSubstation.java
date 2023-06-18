package gregtech.common.metatileentities.multi.electric;

import gregtech.api.GTValues;
import gregtech.api.capability.impl.EnergyContainerList;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.IBatteryDataProvider;
import gregtech.api.metatileentity.multiblock.IBatteryDataProvider.IBatteryData;
import gregtech.api.metatileentity.multiblock.IMultiblockPart;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.api.metatileentity.multiblock.MultiblockWithDisplayBase;
import gregtech.api.pattern.*;
import gregtech.client.renderer.ICubeRenderer;
import gregtech.client.renderer.texture.Textures;
import gregtech.common.ConfigHolder;
import gregtech.common.blocks.BlockBatteryPart;
import gregtech.common.blocks.BlockGlassCasing;
import gregtech.common.blocks.BlockMetalCasing;
import gregtech.common.blocks.BlockMetalCasing.MetalCasingType;
import gregtech.common.blocks.MetaBlocks;
import gregtech.common.metatileentities.MetaTileEntities;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.VisibleForTesting;

import java.math.BigInteger;
import java.util.*;

import static gregtech.api.util.RelativeDirection.*;

public class MetaTileEntityPowerSubstation extends MultiblockWithDisplayBase {

    // Structure Constants
    public static final int MAX_BATTERY_LAYERS = 18;
    private static final int MIN_CASINGS = 14;

    // Passive Drain Constants
    // 1% capacity per 24 hours
    public static final long PASSIVE_DRAIN_DIVISOR = 20 * 60 * 60 * 24 * 100;
    // no more than 100kEU/t per storage block
    public static final long PASSIVE_DRAIN_MAX_PER_STORAGE = 100_000L;

    // NBT Keys
    private static final String NBT_ENERGY_BANK = "EnergyBank";

    // Match Context Headers
    private static final String PMC_BATTERY_HEADER = "PSSBattery_";

    private PowerStationEnergyBank energyBank;
    private EnergyContainerList inputHatches;
    private EnergyContainerList outputHatches;
    private long passiveDrain;

    public MetaTileEntityPowerSubstation(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId);
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity tileEntity) {
        return new MetaTileEntityPowerSubstation(metaTileEntityId);
    }

    @Override
    protected void formStructure(PatternMatchContext context) {
        super.formStructure(context);
        this.inputHatches = new EnergyContainerList(getAbilities(MultiblockAbility.INPUT_ENERGY));
        this.outputHatches = new EnergyContainerList(getAbilities(MultiblockAbility.OUTPUT_ENERGY));

        List<IBatteryData> parts = new ArrayList<>();
        for (Map.Entry<String, Object> battery : context.entrySet()) {
            if (battery.getKey().startsWith(PMC_BATTERY_HEADER)) {
                BatteryMatchWrapper wrapper = (BatteryMatchWrapper) battery.getValue();
                for (int i = 0; i < wrapper.amount; i++) {
                    parts.add(wrapper.partType);
                }
            }
        }
        if (this.energyBank == null) {
            this.energyBank = new PowerStationEnergyBank(parts);
        } else {
            this.energyBank = energyBank.rebuild(parts);
        }
        this.passiveDrain = this.energyBank.getPassiveDrainPerTick();
    }

    @Override
    protected void updateFormedValid() {
        if (!getWorld().isRemote) {
            // Bank from Energy Input Hatches
            long energyBanked = energyBank.fill(inputHatches.getEnergyStored());
            inputHatches.changeEnergy(-energyBanked);

            // Passive drain
            energyBank.drain(this.passiveDrain);

            // Debank to Dynamo Hatches
            long energyDebanked = energyBank.drain(outputHatches.getEnergyCapacity() - outputHatches.getEnergyStored());
            outputHatches.changeEnergy(energyDebanked);
        }
    }

    @NotNull
    @Override
    protected BlockPattern createStructurePattern() {
        return FactoryBlockPattern.start(RIGHT, FRONT, UP)
                .aisle("XXSXX", "XXXXX", "XXXXX", "XXXXX", "XXXXX")
                .aisle("XXXXX", "XCCCX", "XCCCX", "XCCCX", "XXXXX")
                .aisle("GGGGG", "GBBBG", "GBBBG", "GBBBG", "GGGGG").setRepeatable(1, MAX_BATTERY_LAYERS)
                .aisle("GGGGG", "GGGGG", "GGGGG", "GGGGG", "GGGGG")
                .where('S', selfPredicate())
                .where('C', states(getCasingState()))
                .where('X' ,states(getCasingState()).setMinGlobalLimited(MIN_CASINGS)
                        .or(abilities(MultiblockAbility.MAINTENANCE_HATCH))
                        .or(abilities(MultiblockAbility.INPUT_ENERGY, MultiblockAbility.SUBSTATION_INPUT_ENERGY).setMinGlobalLimited(1))
                        .or(abilities(MultiblockAbility.OUTPUT_ENERGY, MultiblockAbility.SUBSTATION_OUTPUT_ENERGY).setMinGlobalLimited(1)))
                .where('G', states(getGlassState()))
                .where('B', batteryPredicate())
                .build();
    }

    @Override
    public List<MultiblockShapeInfo> getMatchingShapes() {
        List<MultiblockShapeInfo> shapeInfo = new ArrayList<>();
        MultiblockShapeInfo.Builder builder = MultiblockShapeInfo.builder()
                .aisle("CCCCC", "CCCCC", "GGGGG", "GGGGG", "GGGGG")
                .aisle("CCCCC", "CCCCC", "GBBBG", "GBBBG", "GGGGG")
                .aisle("CCCCC", "CCCCC", "GBBBG", "GBBBG", "GGGGG")
                .aisle("CCCCC", "CCCCC", "GBBBG", "GBBBG", "GGGGG")
                .aisle("ICSCO", "NCMCT", "GGGGG", "GGGGG", "GGGGG")
                .where('S', MetaTileEntities.POWER_SUBSTATION, EnumFacing.SOUTH)
                .where('C', MetaBlocks.METAL_CASING.getState(MetalCasingType.PALLADIUM_SUBSTATION))
                .where('G', MetaBlocks.TRANSPARENT_CASING.getState(BlockGlassCasing.CasingType.LAMINATED_GLASS))
                .where('I', MetaTileEntities.ENERGY_INPUT_HATCH[GTValues.HV], EnumFacing.SOUTH)
                .where('N', MetaTileEntities.SUBSTATION_ENERGY_INPUT_HATCH[0], EnumFacing.SOUTH)
                .where('O', MetaTileEntities.ENERGY_OUTPUT_HATCH[GTValues.HV], EnumFacing.SOUTH)
                .where('T', MetaTileEntities.SUBSTATION_ENERGY_OUTPUT_HATCH[0], EnumFacing.SOUTH)
                .where('M', () -> ConfigHolder.machines.enableMaintenance
                        ? MetaTileEntities.MAINTENANCE_HATCH
                        : MetaBlocks.METAL_CASING.getState(MetalCasingType.PALLADIUM_SUBSTATION),
                        EnumFacing.SOUTH);

        Arrays.stream(BlockBatteryPart.BatteryPartType.values())
                .sorted(Comparator.comparingLong(BlockBatteryPart.BatteryPartType::getTier))
                .forEach(entry -> shapeInfo.add(builder.where('B', MetaBlocks.BATTERY_BLOCK.getState(entry)).build()));

        return shapeInfo;
    }

    protected IBlockState getCasingState() {
        return MetaBlocks.METAL_CASING.getState(BlockMetalCasing.MetalCasingType.PALLADIUM_SUBSTATION);
    }

    protected IBlockState getGlassState() {
        return MetaBlocks.TRANSPARENT_CASING.getState(BlockGlassCasing.CasingType.LAMINATED_GLASS);
    }

    protected TraceabilityPredicate batteryPredicate() {
        return new TraceabilityPredicate(state -> {
            Block block = state.getBlockState().getBlock();
            if (!(block instanceof IBatteryDataProvider)) {
                return false;
            }
            IBatteryData battery = ((IBatteryDataProvider) block).getData(state.getBlockState());

            // Allow unfilled batteries in the structure, but do not add them to match context.
            // This lets you use empty batteries as "filler slots" for convenience if desired.
            if (battery.getTier() != -1 && battery.getCapacity() > 0) {
                String key = String.format("%s%s", PMC_BATTERY_HEADER, battery.getName());
                BatteryMatchWrapper wrapper = state.getMatchContext().get(key);
                if (wrapper == null) wrapper = new BatteryMatchWrapper(battery);
                state.getMatchContext().set(key, wrapper.increment());
            }
            return true;
        });
    }

    @Override
    public ICubeRenderer getBaseTexture(IMultiblockPart sourcePart) {
        return Textures.PALLADIUM_SUBSTATION_CASING;
    }

    @NotNull
    @Override
    protected ICubeRenderer getFrontOverlay() {
        return super.getFrontOverlay();
    }

    @Override
    protected void addDisplayText(List<ITextComponent> textList) {
        super.addDisplayText(textList);
        if (isStructureFormed()) {
            if (energyBank != null) {
                BigInteger energyStored = energyBank.getStored();
                BigInteger energyCapacity = energyBank.getCapacity();
                textList.add(new TextComponentTranslation("gregtech.multiblock.energy_stored", energyStored, energyCapacity));
            }
        }
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound data) {
        super.writeToNBT(data);
        if (energyBank != null) {
            data.setTag(NBT_ENERGY_BANK, energyBank.writeToNBT(new NBTTagCompound()));
        }
        return data;
    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
        super.readFromNBT(data);
        if (data.hasKey(NBT_ENERGY_BANK)) {
            energyBank = new PowerStationEnergyBank(data.getCompoundTag(NBT_ENERGY_BANK));
        }
    }

    public static class PowerStationEnergyBank {

        private static final String NBT_SIZE = "Size";
        private static final String NBT_STORED = "Stored";
        private static final String NBT_MAX = "Max";

        private final long[] storage;
        private final long[] maximums;
        private final BigInteger capacity;
        private int index;

        public PowerStationEnergyBank(List<IBatteryData> batteries) {
            storage = new long[batteries.size()];
            maximums = new long[batteries.size()];
            for (int i = 0; i < batteries.size(); i++) {
                maximums[i] = batteries.get(i).getCapacity();
            }
            capacity = summarize(maximums);
        }

        public PowerStationEnergyBank(NBTTagCompound storageTag) {
            int size = storageTag.getInteger(NBT_SIZE);
            storage = new long[size];
            maximums = new long[size];
            for (int i = 0; i < size; i++) {
                NBTTagCompound subtag = storageTag.getCompoundTag(String.valueOf(i));
                if (subtag.hasKey(NBT_STORED)) {
                    storage[i] = subtag.getLong(NBT_STORED);
                }
                maximums[i] = subtag.getLong(NBT_MAX);
            }
            capacity = summarize(maximums);
        }

        private NBTTagCompound writeToNBT(NBTTagCompound compound) {
            compound.setInteger(NBT_SIZE, storage.length);
            for (int i = 0; i < storage.length; i++) {
                NBTTagCompound subtag = new NBTTagCompound();
                if (storage[i] > 0) {
                    subtag.setLong(NBT_STORED, storage[i]);
                }
                subtag.setLong(NBT_MAX, maximums[i]);
                compound.setTag(String.valueOf(i), subtag);
            }
            return compound;
        }

        /**
         * Rebuild the power storage with a new list of batteries.
         * Will use existing stored power and try to map it onto new batteries.
         * If there was more power before the rebuild operation, it will be lost.
         */
        public PowerStationEnergyBank rebuild(@NotNull List<IBatteryData> batteries) {
            if (batteries.size() == 0) {
                throw new IllegalArgumentException("Cannot rebuild Power Substation power bank with no batteries!");
            }
            PowerStationEnergyBank newStorage = new PowerStationEnergyBank(batteries);
            for (long stored : storage) {
                newStorage.fill(stored);
            }
            return newStorage;
        }

        /** @return Amount filled into storage */
        public long fill(long amount) {
            if (amount < 0) throw new IllegalArgumentException("Amount cannot be negative!");

            // ensure index
            if (index != storage.length - 1 && storage[index] == maximums[index]) {
                index++;
            }

            long maxFill = Math.min(maximums[index] - storage[index], amount);

            // storage is completely full
            if (maxFill == 0 && index == storage.length - 1) {
                return 0;
            }

            // fill this "battery" as much as possible
            storage[index] += maxFill;
            amount -= maxFill;

            // try to fill other "batteries" if necessary
            if (amount > 0 && index != storage.length - 1) {
                return maxFill + fill(amount);
            }

            // other fill not necessary, either because the storage is now completely full,
            // or we were able to consume all the energy in this "battery"
            return maxFill;
        }

        /** @return Amount drained from storage */
        public long drain(long amount) {
            if (amount < 0) throw new IllegalArgumentException("Amount cannot be negative!");

            // ensure index
            if (index != 0 && storage[index] == 0) {
                index--;
            }

            long maxDrain = Math.min(storage[index], amount);

            // storage is completely empty
            if (maxDrain == 0 && index == 0) {
                return 0;
            }

            // drain this "battery" as much as possible
            storage[index] -= maxDrain;
            amount -= maxDrain;

            // try to drain other "batteries" if necessary
            if (amount > 0 && index != 0) {
                index--;
                return maxDrain + drain(amount);
            }

            // other drain not necessary, either because the storage is now completely empty,
            // or we were able to drain all the energy from this "battery"
            return maxDrain;
        }

        public BigInteger getCapacity() {
            return capacity;
        }

        public BigInteger getStored() {
            return summarize(storage);
        }

        private static BigInteger summarize(long[] values) {
            BigInteger retVal = BigInteger.ZERO;
            long currentSum = 0;
            for (long value : values) {
                if (currentSum != 0 && value > Long.MAX_VALUE - currentSum) {
                    // will overflow if added
                    retVal = retVal.add(BigInteger.valueOf(currentSum));
                    currentSum = 0;
                }
                currentSum += value;
            }
            if (currentSum != 0) {
                retVal = retVal.add(BigInteger.valueOf(currentSum));
            }
            return retVal;
        }

        @VisibleForTesting
        public long getPassiveDrainPerTick() {
            long[] maximumsExcl = new long[maximums.length];
            int index = 0;
            int numExcl = 0;
            for (long maximum : maximums) {
                if (maximum / PASSIVE_DRAIN_DIVISOR >= PASSIVE_DRAIN_MAX_PER_STORAGE) {
                    numExcl++;
                } else {
                    maximumsExcl[index++] = maximum;
                }
            }
            maximumsExcl = Arrays.copyOf(maximumsExcl, index);
            BigInteger capacityExcl = summarize(maximumsExcl);

            return capacityExcl.divide(BigInteger.valueOf(PASSIVE_DRAIN_DIVISOR))
                    .add(BigInteger.valueOf(PASSIVE_DRAIN_MAX_PER_STORAGE * numExcl))
                    .longValue();
        }
    }

    private static class BatteryMatchWrapper {

        private final IBatteryData partType;
        private int amount;

        public BatteryMatchWrapper(IBatteryData partType) {
            this.partType = partType;
        }

        public BatteryMatchWrapper increment() {
            amount++;
            return this;
        }
    }
}
