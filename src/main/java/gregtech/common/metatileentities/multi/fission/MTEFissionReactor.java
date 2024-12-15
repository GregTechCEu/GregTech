package gregtech.common.metatileentities.multi.fission;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;

import gregtech.api.fission.FissionReactorController;
import gregtech.api.fission.component.FissionComponent;
import gregtech.api.fission.reactor.FissionReactor;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.IMultiblockPart;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.api.metatileentity.multiblock.MultiblockWithDisplayBase;
import gregtech.api.pattern.BlockPattern;
import gregtech.api.pattern.FactoryBlockPattern;
import gregtech.api.pattern.PatternMatchContext;
import gregtech.api.util.math.Vec2i;
import gregtech.client.renderer.ICubeRenderer;

import gregtech.client.renderer.texture.Textures;
import gregtech.common.blocks.BlockMetalCasing;
import gregtech.common.blocks.MetaBlocks;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;

import net.minecraft.util.math.BlockPos;

import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class MTEFissionReactor extends MultiblockWithDisplayBase implements FissionReactorController {

    private final Map<FissionComponent, Vec2i> componentPositions = new Object2ObjectOpenHashMap<>();

    private FissionReactor reactor;
    private int size = 3;

    public MTEFissionReactor(@NotNull ResourceLocation metaTileEntityId) {
        super(metaTileEntityId);
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity tileEntity) {
        return new MTEFissionReactor(metaTileEntityId);
    }

    @Override
    protected void updateFormedValid() {
        if (getWorld().isRemote) {
            return;
        }

        if (getOffsetTimer() % 20 == 0) {
            if (reactor.run()) {
                System.out.println("Running");
            } else {
                System.out.println("Not running");
            }
        }
    }

    @Override
    protected void formStructure(PatternMatchContext context) {
        super.formStructure(context);
        determineComponentLayout();
        this.reactor = new FissionReactor(size, size * 100); //TODO maxHeat from wall component
    }

    @Override
    public void invalidateStructure() {
        super.invalidateStructure();
        this.componentPositions.clear();
        this.reactor = null;
    }

    @Override
    public boolean hasMaintenanceMechanics() {
        return false; // TODO
    }

    private void determineComponentLayout() {
        int x = getPos().getX();
        int z = getPos().getZ();

        // north: facing pos Z, components neg Z, left of components pos X
        // south: facing neg Z, components pos Z, left of components neg X
        // east: facing pos X, components neg X, left of components pos Z
        // west: facing neg X, components pos X, left of components neg Z

        EnumFacing facing = getFrontFacing();
        int half = size / 2;

        for (FissionComponent component : getAbilities(MultiblockAbility.FISSION_COMPONENT)) {
            BlockPos pos = component.getPos();
            int dX = x - pos.getX();
            int dZ = z - pos.getZ();
            assert dX >= -size;
            assert dZ >= -size;
            assert dX < size;
            assert dZ < size;

            int r;
            int c;
            switch (facing) {
                case NORTH -> {
                    r = size - dZ;
                    c = dX + half;
                }
                case SOUTH -> {
                    r = size + dZ;
                    c = dX - half;
                }
                case EAST -> {
                    r = size - dX;
                    c = dZ + half;
                }
                case WEST -> {
                    r = size + dX;
                    c = dZ - half;
                }
                default -> throw new IllegalStateException("invalid MTE front facing");
            }

            assert r >= 0;
            assert c >= 0;
            componentPositions.put(component, new Vec2i(r, c));
        }
    }

    public void finalizeStructure() {
        FissionComponent[][] matrix = reactor.matrix();
        for (var entry : componentPositions.entrySet()) {
            Vec2i pos = entry.getValue();
            matrix[pos.x()][pos.y()] = entry.getKey();
        }
        this.reactor.computeGeometry();
    }

    @Override
    public void scheduleRuntimeRecompute() {
        if (reactor != null) {
            reactor.triggerRuntimeRecompute();
        }
    }

    @Override
    protected @NotNull BlockPattern createStructurePattern() {
        return FactoryBlockPattern.start()
                .aisle(" XXX ", " XXX ", " XXX ", " XXX ", " XXX ")
                .aisle("XXXXX", "X   X", "X   X", "X   X", "XCCCX").setRepeatable(3)
                .aisle(" XSX ", " XXX ", " XXX ", " XXX ", " XXX ")
                .where('S', selfPredicate())
                .where('X', states(MetaBlocks.METAL_CASING.getState(BlockMetalCasing.MetalCasingType.INVAR_HEATPROOF)))
                .where('C', abilities(MultiblockAbility.FISSION_COMPONENT))
                .where(' ', air())
                .build();
    }

    @Override
    public ICubeRenderer getBaseTexture(IMultiblockPart sourcePart) {
        return Textures.HEAT_PROOF_CASING;
    }

    @Override
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        super.renderMetaTileEntity(renderState, translation, pipeline);
        // TODO
    }
}
