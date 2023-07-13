package gregtech.common.metatileentities.multi;

import gregtech.api.GregTechAPI;
import gregtech.api.capability.ILockableHandler;
import gregtech.api.gui.Widget;
import gregtech.api.metatileentity.IDataInfoProvider;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.IFissionReactorHatch;
import gregtech.api.metatileentity.multiblock.IMultiblockPart;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.api.metatileentity.multiblock.MultiblockWithDisplayBase;
import gregtech.api.nuclear.fission.FissionReactor;
import gregtech.api.nuclear.fission.components.ControlRod;
import gregtech.api.nuclear.fission.components.CoolantChannel;
import gregtech.api.nuclear.fission.components.FuelRod;
import gregtech.api.nuclear.fission.components.ReactorComponent;
import gregtech.api.pattern.BlockPattern;
import gregtech.api.pattern.FactoryBlockPattern;
import gregtech.api.unification.OreDictUnifier;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.material.properties.FissionFuelProperty;
import gregtech.api.unification.material.properties.PropertyKey;
import gregtech.api.unification.ore.OrePrefix;
import gregtech.api.unification.stack.MaterialStack;
import gregtech.api.util.GTUtility;
import gregtech.api.util.RelativeDirection;
import gregtech.client.renderer.ICubeRenderer;
import gregtech.client.renderer.texture.Textures;
import gregtech.common.blocks.BlockFissionCasing;
import gregtech.common.blocks.MetaBlocks;
import gregtech.common.metatileentities.multi.multiblockpart.MetaTileEntityControlRodPort;
import gregtech.common.metatileentities.multi.multiblockpart.MetaTileEntityCoolantImportHatch;
import gregtech.common.metatileentities.multi.multiblockpart.MetaTileEntityFuelRodImportHatch;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.*;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidStack;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

import static gregtech.api.gui.widgets.AdvancedTextWidget.withButton;

public class MetaTileEntityFissionReactor extends MultiblockWithDisplayBase implements IDataInfoProvider {

    private FissionReactor fissionReactor;
    private int diameter;
    private int heightTop;
    private int heightBottom;
    private int height;
    private boolean locked;

    public MetaTileEntityFissionReactor(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId);
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity tileEntity) {
        return new MetaTileEntityFissionReactor(metaTileEntityId);
    }

    public boolean isBlockEdge(@Nonnull World world, @Nonnull BlockPos pos, @Nonnull EnumFacing direction) {
        return this.isBlockEdge(world, pos, direction, 1);
    }

    public boolean isBlockEdge(@Nonnull World world, @Nonnull BlockPos pos, @Nonnull EnumFacing direction, int steps) {
        return world.getBlockState(pos.offset(direction, steps)).getBlock() != MetaBlocks.FISSION_CASING;
    }

    /**
     * Uses the layer the controller is on to determine the diameter of the structure
     */
    public int findDiameter() {
        int i = 1;
        while (i <= 15) {
            if (this.isBlockEdge(this.getWorld(), this.getPos(), this.getFrontFacing().getOpposite(), i))
                break;
            i++;
        }
        return i;
    }

    /**
     * Checks for casings on top or bottom of the controller to determine the height of the reactor
     */
    public int findHeight(boolean top) {
        int i = 1;
        while (i <= 15) {
            if (this.isBlockEdge(this.getWorld(), this.getPos(), top ? EnumFacing.UP : EnumFacing.DOWN, i))
                break;
            i++;
        }
        return i - 1;
    }

    @Override
    protected void updateFormedValid() {
        // Take in coolant, take in fuel, update reactor, output steam
        if (this.locked) {
            List<ILockableHandler> coolantImports = this.getAbilities(MultiblockAbility.IMPORT_COOLANT);
            for (ILockableHandler coolantImport : coolantImports) {

            }

            this.updateReactorState();

            if (this.fissionReactor.checkForMeltdown()) {
                //TODO Meltdown consequences
            }

            if (this.fissionReactor.checkForExplosion()) {
                //TODO Explosion consequences
                if (this.fissionReactor.checkForSecondaryExplosion()) {
                    //TODO Secondary explosion consequences
                }
            }
        }
    }

    public boolean updateStructureDimensions() {
        return false;
    }

    @Nonnull
    @Override
    protected BlockPattern createStructurePattern() {

        this.heightTop = Math.max(Math.min(this.getWorld() != null ? this.findHeight(true) : 1, 7), 1);
        this.heightBottom = Math.max(Math.min(this.getWorld() != null ? this.findHeight(false) : 1, 7), 1);

        this.height = heightTop + heightBottom + 1;

        this.diameter = this.getWorld() != null ? Math.max(Math.min(this.findDiameter(), 15), 5) : 5;

        int radius = this.diameter % 2 == 0 ? (int) Math.floor(this.diameter / 2.f) : Math.round((this.diameter - 1) / 2.f);

        StringBuilder interiorBuilder = new StringBuilder();

        String[] interiorSlice = new String[this.diameter];
        String[] controllerSlice;
        String[] topSlice;
        String[] bottomSlice;

        // First loop over the matrix
        for (int i = 0; i < this.diameter; i++) {
            for (int j = 0; j < this.diameter; j++) {

                if (Math.pow(i - Math.floor(this.diameter / 2.), 2) + Math.pow(j - Math.floor(this.diameter / 2.), 2) < Math.pow(radius + 0.5f, 2)) {
                    interiorBuilder.append('A');
                } else {
                    interiorBuilder.append(' ');
                }
            }

            interiorSlice[i] = interiorBuilder.toString();
            interiorBuilder.setLength(0);
        }

        //Second loop is to detect where to put walls, the controller and I/O, two less iterations are needed because two strings always represent two walls on opposite sides
        interiorSlice[this.diameter - 1] = interiorSlice[0] = interiorSlice[0].replace('A', 'B');
        for (int i = 1; i < this.diameter - 1; i++) {
            for (int j = 0; j < this.diameter; j++) {
                if (j > 0 && j + 1 < this.diameter) {
                    if ((interiorSlice[i].charAt(j) == 'A' && interiorSlice[i].charAt(j - 1) == ' ') || (interiorSlice[i].charAt(j) == 'A' && interiorSlice[i].charAt(j + 1) == ' ')) {
                        interiorSlice[i] = interiorSlice[i].substring(0, j) + 'B' + interiorSlice[i].substring(j + 1);
                    }
                } else if (j == 0 && interiorSlice[i].charAt(0) == 'A') {
                    interiorSlice[i] = 'B' + interiorSlice[i].substring(1);
                } else if (j == this.diameter - 1 && interiorSlice[i].charAt(this.diameter - 1) == 'A') {
                    interiorSlice[i] = interiorSlice[i].substring(0, this.diameter - 1) + 'B';
                }
            }
        }

        controllerSlice = interiorSlice.clone();
        topSlice = interiorSlice.clone();
        bottomSlice = interiorSlice.clone();
        controllerSlice[0] = controllerSlice[0].substring(0, (int) Math.floor(this.diameter / 2.)) + 'S' + controllerSlice[0].substring((int) Math.floor(this.diameter / 2.) + 1);
        for (int i = 0; i < this.diameter; i++) {
            topSlice[i] = topSlice[i].replace('A', 'I');
            bottomSlice[i] = bottomSlice[i].replace('A', 'O');
        }

        return FactoryBlockPattern.start(RelativeDirection.RIGHT, RelativeDirection.FRONT, RelativeDirection.UP)
                .aisle(bottomSlice)
                .aisle(interiorSlice).setRepeatable(heightBottom - 1)
                .aisle(controllerSlice)
                .aisle(interiorSlice).setRepeatable(heightTop - 1)
                .aisle(topSlice)
                .where('S', selfPredicate())
                .where('A', states(getFuelChannelState(), getControlRodChannelState(), getCoolantChannelState()))              //A for interior components
                .where('I', states(getVesselState()).or(abilities(MultiblockAbility.IMPORT_COOLANT, MultiblockAbility.IMPORT_FUEL_ROD, MultiblockAbility.CONTROL_ROD_PORT)))           //I for the inputs on the top
                .where('O', states(getVesselState()).or(abilities(MultiblockAbility.EXPORT_COOLANT, MultiblockAbility.EXPORT_FUEL_ROD)))        //O for the outputs on the bottom
                .where('B', states(getVesselState()).or(abilities(MultiblockAbility.MAINTENANCE_HATCH).setMinGlobalLimited(1).setMaxGlobalLimited(1))
                        .or(abilities(MultiblockAbility.EXPORT_FLUIDS).setMinGlobalLimited(1)))                   //B for the vessel blocks on the walls
                .where(' ', any())
                .build();
    }

    @Nonnull
    @Override
    public List<ITextComponent> getDataInfo() {
        List<ITextComponent> list = new ArrayList<>();
        list.add(new TextComponentTranslation("gregtech.multiblock.fission_reactor.diameter",
                new TextComponentTranslation(GTUtility.formatNumbers(this.diameter) + "m").setStyle(new Style().setColor(TextFormatting.YELLOW))));
        list.add(new TextComponentTranslation("gregtech.multiblock.fission_reactor.height",
                new TextComponentTranslation(GTUtility.formatNumbers(this.height) + "m").setStyle(new Style().setColor(TextFormatting.YELLOW))));
        return list;
    }

    @Nonnull
    protected IBlockState getVesselState() {
        return MetaBlocks.FISSION_CASING.getState(BlockFissionCasing.FissionCasingType.REACTOR_VESSEL);
    }

    @Nonnull
    protected IBlockState getFuelChannelState() {
        return MetaBlocks.FISSION_CASING.getState(BlockFissionCasing.FissionCasingType.FUEL_CHANNEL);
    }

    @Nonnull
    protected IBlockState getControlRodChannelState() {
        return MetaBlocks.FISSION_CASING.getState(BlockFissionCasing.FissionCasingType.CONTROL_ROD_CHANNEL);
    }

    @Nonnull
    IBlockState getCoolantChannelState() {
        return MetaBlocks.FISSION_CASING.getState(BlockFissionCasing.FissionCasingType.COOLANT_CHANNEL);
    }

    @Nonnull
    IBlockState getTopHatchState() {
        return MetaBlocks.FISSION_CASING.getState(BlockFissionCasing.FissionCasingType.COOLANT_CHANNEL);
    }

    @Override
    public ICubeRenderer getBaseTexture(IMultiblockPart sourcePart) {
        return Textures.FISSION_REACTOR_TEXTURE;
    }

    @Nonnull
    @Override
    protected ICubeRenderer getFrontOverlay() {
        return Textures.ASSEMBLER_OVERLAY;
    }

    @Override
    public void checkStructurePattern() {
        if (!this.isStructureFormed()) {
            reinitializeStructurePattern();
        }
        super.checkStructurePattern();
        for (IMultiblockPart part : this.getMultiblockParts()) {
            if (part instanceof IFissionReactorHatch hatchPart) {
                if (!hatchPart.checkValidity(height - 1)) {
                    this.invalidateStructure();
                    break;
                }
            }
        }
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound data) {
        data.setInteger("diameter", this.diameter);
        data.setInteger("heightTop", this.heightTop);
        data.setInteger("heightBottom", this.heightBottom);
        data.setBoolean("locked", this.locked);
        return super.writeToNBT(data);
    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
        super.readFromNBT(data);
        this.diameter = data.getInteger("diameter");
        this.heightTop = data.getInteger("heightTop");
        this.heightBottom = data.getInteger("heightBottom");
        this.height = this.heightTop + this.heightBottom + 1;
        this.locked = data.getBoolean("locked");
    }

    @Override
    public void writeInitialSyncData(PacketBuffer buf) {
        super.writeInitialSyncData(buf);
        buf.writeInt(this.diameter);
        buf.writeInt(this.heightTop);
        buf.writeInt(this.heightBottom);
        buf.writeBoolean(this.locked);
        if (this.locked) {
            this.lockAndPrepareReactor();
        }
    }

    @Override
    public void receiveInitialSyncData(PacketBuffer buf) {
        super.receiveInitialSyncData(buf);
        this.diameter = buf.readInt();
        this.heightTop = buf.readInt();
        this.heightBottom = buf.readInt();
        this.locked = buf.readBoolean();

    }

    @Override
    protected void addDisplayText(List<ITextComponent> textList) {
        super.addDisplayText(textList);
        ITextComponent toggleText = locked ?
                new TextComponentTranslation("gregtech.multiblock.fission_reactor.turn_off")
                : new TextComponentTranslation("gregtech.multiblock.fission_reactor.turn_on");
        toggleText.appendSibling(withButton(new TextComponentString(" [Toggle]"), "toggle"));
        textList.add(toggleText);
    }

    @Override
    protected void handleDisplayClick(String componentData, Widget.ClickData clickData) {
        super.handleDisplayClick(componentData, clickData);
        if (componentData.equals("toggle")) {
            this.locked = !this.locked;
            if (this.locked) {
                lockAndPrepareReactor();
            } else {
                for (ILockableHandler handler : this.getAbilities(MultiblockAbility.IMPORT_COOLANT)) {
                    handler.setLock(false);
                }
                for (ILockableHandler handler : this.getAbilities(MultiblockAbility.IMPORT_FUEL_ROD)) {
                    handler.setLock(false);
                }
            }
        }
    }

    private void lockAndPrepareReactor() {
        for (ILockableHandler handler : this.getAbilities(MultiblockAbility.IMPORT_COOLANT)) {
            handler.setLock(true);
        }
        for (ILockableHandler handler : this.getAbilities(MultiblockAbility.IMPORT_FUEL_ROD)) {
            handler.setLock(true);
        }
        fissionReactor = new FissionReactor(this.diameter - 2);
        int radius = this.diameter % 2 == 0 ? (int) Math.floor(this.diameter / 2.f) : Math.round((this.diameter - 1) / 2.f);
        radius--;
        BlockPos reactorOrigin = this.getPos().offset(this.frontFacing.getOpposite(), radius);
        for (int i = -radius; i < radius; i++) {
            for (int j = -radius; j < radius; j++) {
                if (Math.pow(i, 2) + Math.pow(j, 2) > Math.pow(radius, 2))
                    continue;
                BlockPos currentPos = reactorOrigin.offset(this.frontFacing.rotateYCCW(), i).offset(this.frontFacing.getOpposite(), j).offset(EnumFacing.UP, height - 2);
                if (getWorld().getTileEntity(currentPos) instanceof IGregTechTileEntity gtTe) {
                    MetaTileEntity mte = gtTe.getMetaTileEntity();
                    ReactorComponent component = null;

                    if (mte instanceof MetaTileEntityCoolantImportHatch coolantIn) {
                        FluidStack containedFluid = coolantIn.getImportFluids().getTankAt(0).getFluid();
                            if (containedFluid != null) {
                                Material mat = GregTechAPI.MaterialRegistry.get(coolantIn.getImportFluids().getTankAt(0).getFluid().getFluid().getName());
                                if (mat != null) component = new CoolantChannel(0, 0, mat);
                            }
                    } else if (mte instanceof MetaTileEntityFuelRodImportHatch fuelIn) {
                        ItemStack lockedFuel = fuelIn.getImportItems().getStackInSlot(0);
                        if (lockedFuel != null && !lockedFuel.isEmpty()) {
                            MaterialStack mat = OreDictUnifier.getMaterial(lockedFuel);
                            if (mat != null && OreDictUnifier.getPrefix(lockedFuel) == OrePrefix.dust) {
                                FissionFuelProperty property = mat.material.getProperty(PropertyKey.FISSION_FUEL);
                                if (property != null)
                                    component = new FuelRod(0, 1, property, 3);
                            }
                        }
                    } else if (mte instanceof MetaTileEntityControlRodPort controlIn) {
                        component = new ControlRod(0, true, 1, controlIn.getInsertionAmount());
                    }
                    if (component != null)
                        fissionReactor.addComponent(component, i, j);
                }
            }
        }
        fissionReactor.computeGeometry();
    }

    private void updateReactorState() {
        this.fissionReactor.updateTemperature();
        this.fissionReactor.updatePressure();
        this.fissionReactor.updateNeutronPoisoning();
        this.fissionReactor.updatePower();
    }

}
