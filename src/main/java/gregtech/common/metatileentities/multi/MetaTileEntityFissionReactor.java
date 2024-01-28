package gregtech.common.metatileentities.multi;

import gregtech.api.GregTechAPI;
import gregtech.api.capability.ICoolantHandler;
import gregtech.api.capability.IFuelRodHandler;
import gregtech.api.capability.ILockableHandler;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.ModularUI;
import gregtech.api.gui.Widget;
import gregtech.api.gui.widgets.AdvancedTextWidget;
import gregtech.api.gui.widgets.SliderWidget;
import gregtech.api.gui.widgets.ToggleButtonWidget;
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
import gregtech.api.unification.material.Materials;
import gregtech.api.unification.material.properties.FissionFuelProperty;
import gregtech.api.unification.material.properties.PropertyKey;
import gregtech.api.unification.ore.OrePrefix;
import gregtech.api.unification.stack.MaterialStack;
import gregtech.api.util.RelativeDirection;
import gregtech.api.util.TextFormattingUtil;
import gregtech.client.renderer.ICubeRenderer;
import gregtech.client.renderer.texture.Textures;
import gregtech.common.blocks.BlockFissionCasing;
import gregtech.common.blocks.MetaBlocks;
import gregtech.common.metatileentities.multi.multiblockpart.MetaTileEntityControlRodPort;
import gregtech.common.metatileentities.multi.multiblockpart.MetaTileEntityCoolantExportHatch;
import gregtech.common.metatileentities.multi.multiblockpart.MetaTileEntityCoolantImportHatch;
import gregtech.common.metatileentities.multi.multiblockpart.MetaTileEntityFuelRodImportHatch;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.*;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class MetaTileEntityFissionReactor extends MultiblockWithDisplayBase implements IDataInfoProvider {

    private FissionReactor fissionReactor;
    private int diameter;
    private int heightTop;
    private int heightBottom;
    private int height;
    private int flowRate = 1;
    private int controlRodInsertionValue = 4;
    private LockingState lockingState = LockingState.UNLOCKED;

    public MetaTileEntityFissionReactor(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId);
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity tileEntity) {
        return new MetaTileEntityFissionReactor(metaTileEntityId);
    }

    @Override
    protected ModularUI.Builder createUITemplate(EntityPlayer entityPlayer) {
        ModularUI.Builder builder = ModularUI.builder(GuiTextures.BACKGROUND, 176, 266).shouldColor(false)
                .widget(new SliderWidget("Flow Rate", 50, 50, 100, 18, 0.0f, 10000.f, flowRate, this::setFlowRate))
                .widget(new ToggleButtonWidget(50, 80, 18, 18, this::isLocked, this::tryLocking))
                .widget(new SliderWidget("Control Rod Depth", 40, 30, 80, 18, 0.0f, 15.0f,
                        controlRodInsertionValue, this::setControlRodInsertionValue));
        builder.widget(new AdvancedTextWidget(50, 110, getLockingStateText(), 0xFFFFFF));
        builder.bindPlayerInventory(entityPlayer.inventory, 150);
        return builder;
    }

    private void setFlowRate(float flowrate) {
        this.flowRate = (int) flowrate;
        if (flowRate < 1) flowRate = 1;
    }

    private void setControlRodInsertionValue(float value) {
        if(lockingState == LockingState.LOCKED)
            return;
        this.controlRodInsertionValue = (int) value;
    }

    private boolean isLocked() {
        return lockingState == LockingState.LOCKED;
    }

    private void tryLocking(boolean lock) {
        if (!isStructureFormed())
            return;

        if (lock)
            lockAndPrepareReactor();
        else
            unlockAll();
    }

    private Consumer<List<ITextComponent>> getLockingStateText() {
        return (list) -> {
            list.add(new TextComponentString("Locking State: " + lockingState.toString()));
        };
    }

    public boolean isBlockEdge(@NotNull World world, @NotNull BlockPos pos, @NotNull EnumFacing direction) {
        return this.isBlockEdge(world, pos, direction, 1);
    }

    public boolean isBlockEdge(@NotNull World world, @NotNull BlockPos pos, @NotNull EnumFacing direction, int steps) {
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

        if (this.lockingState == LockingState.LOCKED) {

            // Coolant handling
            for (ICoolantHandler coolantImport : this.getAbilities(MultiblockAbility.IMPORT_COOLANT)) {
                // TODO: Move into coolant import hatch
                this.fissionReactor.heatRemoved += coolantImport.getCoolant().getProperty(PropertyKey.COOLANT)
                        .getCoolingFactor() * this.flowRate;
                coolantImport.getFluidTank().drain(this.flowRate, true);
            }
            for (ICoolantHandler coolantExport : this.getAbilities(MultiblockAbility.EXPORT_COOLANT)) {
                // TODO: Move into coolant export hatch
                coolantExport.getFluidTank().fill(coolantExport.getCoolant().getProperty(PropertyKey.COOLANT)
                        .getHotHPCoolant().getFluid(this.flowRate), true);
            }

            // Fuel handling
            if (this.fissionReactor.fuelDepletion == 1.) {
                boolean hasEmpty = false;
                for (IFuelRodHandler fuelImport : this.getAbilities(MultiblockAbility.IMPORT_FUEL_ROD)) {
                    if (fuelImport.getStackHandler().extractItem(0, 1, true).isEmpty()) hasEmpty = true;
                }
                if (!hasEmpty) {
                    for (IFuelRodHandler fuelImport : this.getAbilities(MultiblockAbility.IMPORT_FUEL_ROD)) {
                        fuelImport.getStackHandler().extractItem(0, 1, false);
                    }
                    this.fissionReactor.fuelDepletion = 0.;
                }
            }

            this.updateReactorState();

            if (this.fissionReactor.checkForMeltdown()) {
                this.performMeltdownEffects();
            }

            if (this.fissionReactor.checkForExplosion()) {
                this.performPrimaryExplosion();
                if (this.fissionReactor.checkForSecondaryExplosion()) {
                    this.performSecondaryExplosion();
                }
            }
        }
    }

    protected void performMeltdownEffects() {
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos(this.getPos());
        pos = pos.move(this.getFrontFacing().getOpposite(), Math.floorDiv(diameter, 2));
        for (int i = 0; i <= this.heightBottom; i++) {
            this.getWorld().setBlockState(pos.add(0, -i, 0), Materials.Corium.getFluid().getBlock().getDefaultState());
            this.getWorld().setBlockState(pos.add(1, -i, 0), Materials.Corium.getFluid().getBlock().getDefaultState());
            this.getWorld().setBlockState(pos.add(-1, -i, 0), Materials.Corium.getFluid().getBlock().getDefaultState());
            this.getWorld().setBlockState(pos.add(0, -i, 1), Materials.Corium.getFluid().getBlock().getDefaultState());
            this.getWorld().setBlockState(pos.add(0, -i, -1), Materials.Corium.getFluid().getBlock().getDefaultState());
        }
        this.getWorld().setBlockState(pos.add(0, 1, 0), Materials.Corium.getFluid().getBlock().getDefaultState());
    }

    protected void performPrimaryExplosion() {
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos(this.getPos());
        pos = pos.move(this.getFrontFacing().getOpposite(), Math.floorDiv(diameter, 2));
        this.getWorld().createExplosion(null, pos.getX(), pos.getY() + heightTop, pos.getZ(), 4.f, true);
    }

    protected void performSecondaryExplosion() {
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos(this.getPos());
        pos = pos.move(this.getFrontFacing().getOpposite(), Math.floorDiv(diameter, 2));
        this.getWorld().newExplosion(null, pos.getX(), pos.getY() + heightTop + 3, pos.getZ(), 10.f, true, true);
    }

    public boolean updateStructureDimensions() {
        return false;
    }

    @NotNull
    @Override
    protected BlockPattern createStructurePattern() {
        this.heightTop = Math.max(Math.min(this.getWorld() != null ? this.findHeight(true) : 1, 7), 1);
        this.heightBottom = Math.max(Math.min(this.getWorld() != null ? this.findHeight(false) : 1, 7), 1);

        this.height = heightTop + heightBottom + 1;

        this.diameter = this.getWorld() != null ? Math.max(Math.min(this.findDiameter(), 15), 5) : 5;

        int radius = this.diameter % 2 == 0 ? (int) Math.floor(this.diameter / 2.f) :
                Math.round((this.diameter - 1) / 2.f);

        StringBuilder interiorBuilder = new StringBuilder();

        String[] interiorSlice = new String[this.diameter];
        String[] controllerSlice;
        String[] topSlice;
        String[] bottomSlice;

        // First loop over the matrix
        for (int i = 0; i < this.diameter; i++) {
            for (int j = 0; j < this.diameter; j++) {

                if (Math.pow(i - Math.floor(this.diameter / 2.), 2) + Math.pow(j - Math.floor(this.diameter / 2.), 2) <
                        Math.pow(radius + 0.5f, 2)) {
                    interiorBuilder.append('A');
                } else {
                    interiorBuilder.append(' ');
                }
            }

            interiorSlice[i] = interiorBuilder.toString();
            interiorBuilder.setLength(0);
        }

        // Second loop is to detect where to put walls, the controller and I/O, two fewer iterations are needed because
        // two strings always represent two walls on opposite sides
        interiorSlice[this.diameter - 1] = interiorSlice[0] = interiorSlice[0].replace('A', 'B');
        for (int i = 1; i < this.diameter - 1; i++) {
            for (int j = 0; j < this.diameter; j++) {
                if (j > 0 && j + 1 < this.diameter) {
                    if ((interiorSlice[i].charAt(j) == 'A' && interiorSlice[i].charAt(j - 1) == ' ') ||
                            (interiorSlice[i].charAt(j) == 'A' && interiorSlice[i].charAt(j + 1) == ' ')) {
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
        controllerSlice[0] = controllerSlice[0].substring(0, (int) Math.floor(this.diameter / 2.)) + 'S' +
                controllerSlice[0].substring((int) Math.floor(this.diameter / 2.) + 1);
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
                // A for interior components
                .where('A', states(getFuelChannelState(), getControlRodChannelState(), getCoolantChannelState()))
                // I for the inputs on the top
                .where('I',
                        states(getVesselState()).or(abilities(MultiblockAbility.IMPORT_COOLANT,
                                MultiblockAbility.IMPORT_FUEL_ROD, MultiblockAbility.CONTROL_ROD_PORT)))
                // O for the outputs on the bottom
                .where('O',
                        states(getVesselState())
                                .or(abilities(MultiblockAbility.EXPORT_COOLANT, MultiblockAbility.EXPORT_FUEL_ROD)))
                // B for the vessel blocks on the walls
                .where('B',
                        states(getVesselState())
                                .or(abilities(MultiblockAbility.MAINTENANCE_HATCH).setMinGlobalLimited(1)
                                        .setMaxGlobalLimited(1))
                                .or(abilities(MultiblockAbility.EXPORT_FLUIDS).setMinGlobalLimited(1)))
                .where(' ', any())
                .build();
    }

    @NotNull
    @Override
    public List<ITextComponent> getDataInfo() {
        List<ITextComponent> list = new ArrayList<>();
        list.add(new TextComponentTranslation("gregtech.multiblock.fission_reactor.diameter",
                new TextComponentTranslation(TextFormattingUtil.formatNumbers(this.diameter) + "m")
                        .setStyle(new Style().setColor(TextFormatting.YELLOW))));
        list.add(new TextComponentTranslation("gregtech.multiblock.fission_reactor.height",
                new TextComponentTranslation(TextFormattingUtil.formatNumbers(this.height) + "m")
                        .setStyle(new Style().setColor(TextFormatting.YELLOW))));
        return list;
    }

    @NotNull
    protected IBlockState getVesselState() {
        return MetaBlocks.FISSION_CASING.getState(BlockFissionCasing.FissionCasingType.REACTOR_VESSEL);
    }

    @NotNull
    protected IBlockState getFuelChannelState() {
        return MetaBlocks.FISSION_CASING.getState(BlockFissionCasing.FissionCasingType.FUEL_CHANNEL);
    }

    @NotNull
    protected IBlockState getControlRodChannelState() {
        return MetaBlocks.FISSION_CASING.getState(BlockFissionCasing.FissionCasingType.CONTROL_ROD_CHANNEL);
    }

    @NotNull
    IBlockState getCoolantChannelState() {
        return MetaBlocks.FISSION_CASING.getState(BlockFissionCasing.FissionCasingType.COOLANT_CHANNEL);
    }

    @NotNull
    IBlockState getTopHatchState() {
        return MetaBlocks.FISSION_CASING.getState(BlockFissionCasing.FissionCasingType.COOLANT_CHANNEL);
    }

    @Override
    public ICubeRenderer getBaseTexture(IMultiblockPart sourcePart) {
        return Textures.FISSION_REACTOR_TEXTURE;
    }

    @SideOnly(Side.CLIENT)
    @NotNull
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
    public void invalidateStructure() {
        if (lockingState == LockingState.LOCKED) {
            this.unlockAll();
        }
        super.invalidateStructure();
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound data) {
        data.setInteger("diameter", this.diameter);
        data.setInteger("heightTop", this.heightTop);
        data.setInteger("heightBottom", this.heightBottom);
        data.setBoolean("locked", this.lockingState == LockingState.LOCKED);
        return super.writeToNBT(data);
    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
        super.readFromNBT(data);
        this.diameter = data.getInteger("diameter");
        this.heightTop = data.getInteger("heightTop");
        this.heightBottom = data.getInteger("heightBottom");
        this.height = this.heightTop + this.heightBottom + 1;
        if (data.getBoolean("locked")) {
            this.lockingState = LockingState.SHOULD_LOCK;
        }
    }

    @Override
    public void writeInitialSyncData(PacketBuffer buf) {
        super.writeInitialSyncData(buf);
        buf.writeInt(this.diameter);
        buf.writeInt(this.heightTop);
        buf.writeInt(this.heightBottom);
        if (this.lockingState == LockingState.SHOULD_LOCK) {
            this.lockAndPrepareReactor();
        }
        buf.writeBoolean(this.lockingState == LockingState.LOCKED);
    }

    @Override
    public void receiveInitialSyncData(PacketBuffer buf) {
        super.receiveInitialSyncData(buf);
        this.diameter = buf.readInt();
        this.heightTop = buf.readInt();
        this.heightBottom = buf.readInt();
        if (buf.readBoolean()) {
            this.lockingState = LockingState.LOCKED;
        }
    }

    @Override
    protected void addDisplayText(List<ITextComponent> textList) {
        super.addDisplayText(textList);
        ITextComponent toggleText = null;
        if (!this.isStructureFormed())
            toggleText = new TextComponentTranslation("gregtech.multiblock.fission_reactor.structure_incomplete");
        textList.add(toggleText);
    }

    protected void lockAll() {
        for (ILockableHandler handler : this.getAbilities(MultiblockAbility.IMPORT_COOLANT)) {
            handler.setLock(true);
        }
        for (ILockableHandler handler : this.getAbilities(MultiblockAbility.EXPORT_COOLANT)) {
            handler.setLock(true);
        }
        for (ILockableHandler handler : this.getAbilities(MultiblockAbility.IMPORT_FUEL_ROD)) {
            handler.setLock(true);
        }
    }

    protected void unlockAll() {
        for (ILockableHandler handler : this.getAbilities(MultiblockAbility.IMPORT_COOLANT)) {
            handler.setLock(false);
        }
        for (ILockableHandler handler : this.getAbilities(MultiblockAbility.EXPORT_COOLANT)) {
            handler.setLock(false);
        }
        for (ILockableHandler handler : this.getAbilities(MultiblockAbility.IMPORT_FUEL_ROD)) {
            handler.setLock(false);
        }
        //this.fissionReactor = null;
        this.lockingState = LockingState.UNLOCKED;
    }

    @Override
    protected void handleDisplayClick(String componentData, Widget.ClickData clickData) {
        super.handleDisplayClick(componentData, clickData);
        if (componentData.equals("turn_on")) lockAndPrepareReactor();
        else if (componentData.equals("turn_off")) unlockAll();
    }

    private void lockAndPrepareReactor() {
        this.lockAll();
        fissionReactor = new FissionReactor(this.diameter - 2, this.height - 2, controlRodInsertionValue);
        int radius = (int) this.diameter / 2;     // This is the floor of the radius, the actual radius is 0.5 blocks
                                                  // larger
        BlockPos reactorOrigin = this.getPos().offset(this.frontFacing.getOpposite(), radius);
        radius--;
        for (int i = -radius; i <= radius; i++) {
            for (int j = -radius; j <= radius; j++) {
                if (Math.pow(i, 2) + Math.pow(j, 2) > Math.pow(radius, 2) + radius)         // (radius + .5)^2 =
                                                                                            // radius^2 + radius + .25
                    continue;
                BlockPos currentPos = reactorOrigin.offset(this.frontFacing.rotateYCCW(), i)
                        .offset(this.frontFacing.getOpposite(), j).offset(EnumFacing.UP, height - 2);
                if (getWorld().getTileEntity(currentPos) instanceof IGregTechTileEntity gtTe) {
                    MetaTileEntity mte = gtTe.getMetaTileEntity();
                    ReactorComponent component = null;
                    boolean foundPort = true;

                    if (mte instanceof MetaTileEntityCoolantImportHatch coolantIn) {
                        FluidStack containedFluid = coolantIn.getImportFluids().getTankAt(0).getFluid();
                        if (containedFluid != null) {
                            Material mat = GregTechAPI.materialManager.getMaterial(
                                    coolantIn.getImportFluids().getTankAt(0).getFluid().getFluid().getName());
                            if (mat != null && mat.hasProperty(PropertyKey.COOLANT)) {
                                component = new CoolantChannel(100050, 0, mat);
                                coolantIn.setCoolant(mat);
                                BlockPos exportHatchPos = currentPos.offset(EnumFacing.DOWN, height - 1);
                                if (getWorld().getTileEntity(
                                        exportHatchPos) instanceof IGregTechTileEntity coolantOutCandidate) {
                                    MetaTileEntity coolantOutMTE = coolantOutCandidate.getMetaTileEntity();
                                    if (coolantOutMTE instanceof MetaTileEntityCoolantExportHatch coolantOut) {
                                        coolantOut.setCoolant(mat);
                                    }
                                }
                            }
                        }
                    } else if (mte instanceof MetaTileEntityFuelRodImportHatch fuelIn) {
                        ItemStack lockedFuel = fuelIn.getImportItems().getStackInSlot(0);
                        if (lockedFuel != null && !lockedFuel.isEmpty()) {
                            MaterialStack mat = OreDictUnifier.getMaterial(lockedFuel);
                            if (mat != null && OreDictUnifier.getPrefix(lockedFuel) == OrePrefix.fuelRod) {
                                FissionFuelProperty property = mat.material.getProperty(PropertyKey.FISSION_FUEL);
                                if (property != null)
                                    component = new FuelRod(property.getMaxTemperature(), 1, property, 3);
                            }
                        }
                    } else if (mte instanceof MetaTileEntityControlRodPort controlIn) {
                        component = new ControlRod(100000, true, 1, controlIn.getInsertionAmount());
                    } else {
                        foundPort = false;
                    }

                    if (component != null) {
                        if (component.isValid()) {
                            fissionReactor.addComponent(component, i + radius, j + radius);
                        } else {                            // Invalid component located
                            this.unlockAll();
                            fissionReactor = null;
                            this.lockingState = LockingState.INVALID_COMPONENT;
                            return;
                        }
                    } else if (foundPort) {               // This implies that a port was found, but it didn't generate
                                                          // a component because of mismatched inputs
                        this.unlockAll();
                        fissionReactor = null;
                        this.lockingState = LockingState.MISSING_INPUTS;
                        return;
                    }
                }
            }
        }
        fissionReactor.prepareThermalProperties();
        fissionReactor.computeGeometry();
        this.lockingState = LockingState.LOCKED;
    }

    private void updateReactorState() {
        this.fissionReactor.updatePower();
        this.fissionReactor.updateTemperature();
        this.fissionReactor.updatePressure();
        this.fissionReactor.updateNeutronPoisoning();
    }

    private enum LockingState {
        // The reactor is locked
        LOCKED,
        // The reactor is unlocked
        UNLOCKED,
        // The reactor is supposed to be locked, but the locking logic is yet to run
        SHOULD_LOCK,
        // The reactor can't lock because it is missing inputs
        MISSING_INPUTS,
        // The reactor can't lock because components are flagged as invalid
        INVALID_COMPONENT
    }
}
