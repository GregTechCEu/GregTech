package gregtech.api.metatileentity.multiblock;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.ColourMultiplier;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import gregtech.api.capability.GregtechCapabilities;
import gregtech.api.capability.IMaintenanceHatch;
import gregtech.api.capability.IMultiblockController;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.MetaTileEntityHolder;
import gregtech.api.multiblock.BlockPattern;
import gregtech.api.multiblock.BlockWorldState;
import gregtech.api.multiblock.IPatternCenterPredicate;
import gregtech.api.multiblock.PatternMatchContext;
import gregtech.api.render.ICubeRenderer;
import gregtech.api.render.OrientedOverlayRenderer;
import gregtech.api.render.Textures;
import gregtech.api.util.GTUtility;
import gregtech.common.metatileentities.electric.multiblockpart.MetaTileEntityRotorHolder;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;

public abstract class MultiblockControllerBase extends MetaTileEntity implements IMultiblockController {

    protected BlockPattern structurePattern;

    private final Map<MultiblockAbility<Object>, List<Object>> multiblockAbilities = new HashMap<>();
    private final List<IMultiblockPart> multiblockParts = new ArrayList<>();
    private boolean structureFormed;

    public MultiblockControllerBase(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId);
        reinitializeStructurePattern();
        this.setPaintingColor(0xFFFFFF);
    }

    protected void reinitializeStructurePattern() {
        this.structurePattern = createStructurePattern();
    }

    @Override
    public void update() {
        super.update();
        if (!getWorld().isRemote) {
            if (getOffsetTimer() % 20 == 0 || isFirstTick()) {
                checkStructurePattern();
            }
            if (isStructureFormed()) {
                updateFormedValid();
            }
        }
    }

    /**
     * Called when the multiblock is formed and validation predicate is matched
     */
    protected abstract void updateFormedValid();

    /**
     * @return structure pattern of this multiblock
     */
    protected abstract BlockPattern createStructurePattern();

    public abstract ICubeRenderer getBaseTexture(IMultiblockPart sourcePart);

    public boolean shouldRenderOverlay(IMultiblockPart sourcePart) {
        return true;
    }

    /**
     * Override this method to change the Controller overlay
     *
     * @return The overlay to render on the Multiblock Controller
     */
    @Nonnull
    protected OrientedOverlayRenderer getFrontOverlay() {
        return Textures.MULTIBLOCK_WORKABLE_OVERLAY;
    }

    public int getLightValueForPart(IMultiblockPart sourcePart) {
        return 0;
    }

    protected boolean checkStructureComponents(List<IMultiblockPart> parts, Map<MultiblockAbility<Object>, List<Object>> abilities) {
        return true;
    }

    @Override
    public final int getActualLightValue() {
        return getLightValueForPart(null);
    }

    public static Predicate<BlockWorldState> tilePredicate(BiFunction<BlockWorldState, MetaTileEntity, Boolean> predicate) {
        return blockWorldState -> {
            TileEntity tileEntity = blockWorldState.getTileEntity();
            if (!(tileEntity instanceof MetaTileEntityHolder))
                return false;
            MetaTileEntity metaTileEntity = ((MetaTileEntityHolder) tileEntity).getMetaTileEntity();
            if (predicate.apply(blockWorldState, metaTileEntity)) {
                if (metaTileEntity instanceof IMultiblockPart) {
                    Set<IMultiblockPart> partsFound = blockWorldState.getMatchContext().getOrCreate("MultiblockParts", HashSet::new);
                    partsFound.add((IMultiblockPart) metaTileEntity);
                }
                return true;
            }
            return false;
        };
    }

    public static Predicate<BlockWorldState> abilityPartPredicate(MultiblockAbility<?>... allowedAbilities) {
        return tilePredicate((state, tile) -> tile instanceof IMultiblockAbilityPart<?> &&
                ArrayUtils.contains(allowedAbilities, ((IMultiblockAbilityPart<?>) tile).getAbility()));
    }

    public static Predicate<BlockWorldState> partPredicate(Class<? extends IMultiblockPart> baseClass) {
        return tilePredicate((state, tile) -> tile instanceof IMultiblockPart && baseClass.isAssignableFrom(tile.getClass()));
    }

    public static Predicate<BlockWorldState> statePredicate(IBlockState... allowedStates) {
        return blockWorldState -> ArrayUtils.contains(allowedStates, blockWorldState.getBlockState());
    }

    public static Predicate<BlockWorldState> blockPredicate(Block... block) {
        return blockWorldState -> ArrayUtils.contains(block, blockWorldState.getBlockState().getBlock());
    }

    public static Predicate<BlockWorldState> isAirPredicate() {
        return blockWorldState -> blockWorldState.getBlockState().getBlock().isAir(blockWorldState.getBlockState(), blockWorldState.getWorld(), blockWorldState.getPos());
    }

    public IPatternCenterPredicate selfPredicate() {
        return BlockWorldState.wrap(tilePredicate((state, tile) -> tile.metaTileEntityId.equals(metaTileEntityId)));
    }

    public Predicate<BlockWorldState> countMatch(String key, Predicate<BlockWorldState> original) {
        return blockWorldState -> {
            if (original.test(blockWorldState)) {
                blockWorldState.getLayerContext().increment(key, 1);
                return true;
            }
            return false;
        };
    }

    @Override
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        getBaseTexture(null).render(renderState, translation, ArrayUtils.add(pipeline, new ColourMultiplier(GTUtility.convertRGBtoOpaqueRGBA_CL(getPaintingColorForRendering()))));
    }

    @Override
    public Pair<TextureAtlasSprite, Integer> getParticleTexture() {
        return Pair.of(getBaseTexture(null).getParticleSprite(), getPaintingColor());
    }

    /**
     * Override to disable MultiblockPart sharing for this Multiblock. (Rotor Holders always disallowed).
     */
    public boolean canShare() {
        return true;
    }

    /**
     * Used if MultiblockPart Abilities need to be sorted a certain way, like
     * Distillation Tower and Assembly Line.
     */
    protected Function<BlockPos, Integer> multiblockPartSorter() {
        return BlockPos::hashCode;
    }

    protected void checkStructurePattern() {
        EnumFacing facing = getFrontFacing().getOpposite();
        PatternMatchContext context = structurePattern.checkPatternAt(getWorld(), getPos(), facing);
        if (context != null && !structureFormed) {
            Set<IMultiblockPart> rawPartsSet = context.getOrCreate("MultiblockParts", HashSet::new);
            ArrayList<IMultiblockPart> parts = new ArrayList<>(rawPartsSet);
            parts.sort(Comparator.comparing(it -> multiblockPartSorter().apply(((MetaTileEntity) it).getPos())));
            for (IMultiblockPart part : parts) {
                if (part.isAttachedToMultiBlock()) {
                    if (!canShare() || part instanceof MetaTileEntityRotorHolder || part instanceof IMaintenanceHatch) {
                        return;
                    }
                }
            }
            Map<MultiblockAbility<Object>, List<Object>> abilities = new HashMap<>();
            for (IMultiblockPart multiblockPart : parts) {
                if (multiblockPart instanceof IMultiblockAbilityPart) {
                    IMultiblockAbilityPart<Object> abilityPart = (IMultiblockAbilityPart<Object>) multiblockPart;
                    List<Object> abilityInstancesList = abilities.computeIfAbsent(abilityPart.getAbility(), k -> new ArrayList<>());
                    abilityPart.registerAbilities(abilityInstancesList);
                }
            }
            if (checkStructureComponents(parts, abilities)) {
                parts.forEach(part -> part.addToMultiBlock(this));
                parts.forEach(part -> part.setupNotifiableMetaTileEntity(this));
                this.multiblockParts.addAll(parts);
                this.multiblockAbilities.putAll(abilities);
                this.structureFormed = true;
                writeCustomData(400, buf -> buf.writeBoolean(true));
                formStructure(context);
            }
        } else if (context == null && structureFormed) {
            invalidateStructure();
        }
    }

    protected void formStructure(PatternMatchContext context) {
    }

    public void invalidateStructure() {
        this.multiblockParts.forEach(part -> part.removeFromMultiBlock(this));
        this.multiblockAbilities.clear();
        this.multiblockParts.clear();
        this.structureFormed = false;
        writeCustomData(400, buf -> buf.writeBoolean(false));
    }

    @Override
    public void onRemoval() {
        super.onRemoval();
        if (!getWorld().isRemote && structureFormed) {
            invalidateStructure();
        }
    }

    @SuppressWarnings("unchecked")
    public <T> List<T> getAbilities(MultiblockAbility<T> ability) {
        List<T> rawList = (List<T>) multiblockAbilities.getOrDefault(ability, Collections.emptyList());
        return Collections.unmodifiableList(rawList);
    }

    public List<IMultiblockPart> getMultiblockParts() {
        return Collections.unmodifiableList(multiblockParts);
    }

    @Override
    public void writeInitialSyncData(PacketBuffer buf) {
        super.writeInitialSyncData(buf);
        buf.writeBoolean(structureFormed);
    }

    @Override
    public void receiveInitialSyncData(PacketBuffer buf) {
        super.receiveInitialSyncData(buf);
        this.structureFormed = buf.readBoolean();
    }

    @Override
    public void receiveCustomData(int dataId, PacketBuffer buf) {
        super.receiveCustomData(dataId, buf);
        if (dataId == 400) {
            this.structureFormed = buf.readBoolean();
        }
    }

    @Override
    public <T> T getCapability(Capability<T> capability, EnumFacing side) {
        T result = super.getCapability(capability, side);
        if (result != null)
            return result;
        if (capability == GregtechCapabilities.CAPABILITY_MULTIBLOCK_CONTROLLER) {
            return GregtechCapabilities.CAPABILITY_MULTIBLOCK_CONTROLLER.cast(this);
        }
        return null;
    }

    public boolean isStructureFormed() {
        return structureFormed;
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World player, List<String> tooltip, boolean advanced) {
        super.addInformation(stack, player, tooltip, advanced);
        tooltip.add(I18n.format("gregtech.machine.multiblock.universal.controller_information", I18n.format(getMetaFullName())));
    }
}
