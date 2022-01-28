package gregtech.common.metatileentities.multi.electric;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import gregtech.api.metatileentity.IDataInfoProvider;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.MetaTileEntityHolder;
import gregtech.api.metatileentity.multiblock.*;
import gregtech.api.pattern.BlockPattern;
import gregtech.api.pattern.FactoryBlockPattern;
import gregtech.api.pattern.TraceabilityPredicate;
import gregtech.client.renderer.ICubeRenderer;
import gregtech.client.renderer.texture.Textures;
import gregtech.common.blocks.BlockCleanroomCasing;
import gregtech.common.blocks.MetaBlocks;
import net.minecraft.block.BlockDoor;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public class MetaTileEntityCleanroom extends MultiblockWithDisplayBase implements ICleanroom, IDataInfoProvider {

    public static final int MIN_DIAMETER = 5;
    public static final int MAX_DIAMETER = 15;
    private int width = 5;
    private int height = 5;
    private int depth = 5;

    public MetaTileEntityCleanroom(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId);
    }

    @Override
    protected void updateFormedValid() {

    }

    @Override
    public MetaTileEntity createMetaTileEntity(MetaTileEntityHolder holder) {
        return new MetaTileEntityCleanroom(metaTileEntityId);
    }

    @Override
    protected BlockPattern createStructurePattern() {
        // build each row of the structure
        StringBuilder border = new StringBuilder("B"); //      BBBBB
        StringBuilder wall = new StringBuilder("B"); //        BXXXB
        StringBuilder inside = new StringBuilder("X"); //      X   X
        StringBuilder roof = new StringBuilder("B"); //        BFFFB
        StringBuilder controller = new StringBuilder("B"); //  BFSFB

        // these can sometimes get exceeded when loading the game, breaking JEI
        width = 5;
        height = 5;
        depth = 5;

        // start with block after left edge, do not include right edge with -1
        for (int i = 1; i < width - 1; i++) {
            border.append("B");
            wall.append("X");
            inside.append(" ");
            roof.append("F");
            if (i == width / 2) controller.append("S"); // controller is always centered
            else controller.append("F");
        }
        border.append("B");
        wall.append("B");
        inside.append("X");
        roof.append("B");
        controller.append("B");


        // build each slice of the structure
        String B = border.toString();
        String W = wall.toString();
        String I = inside.toString();
        String R = roof.toString();
        String C = controller.toString();

        String[] frontBack = new String[height];
        String[] inner = new String[height];
        String[] center = new String[height];

        // bottom and top
        frontBack[0] = B;
        frontBack[height - 1] = B;
        inner[0] = B;
        inner[height - 1] = R;
        center[0] = B;
        center[height - 1] = C;

        // central sections
        for (int i = 1; i < height - 1; i++) {
            frontBack[i] = W;
            inner[i] = I;
            center[i] = I;
        }

        TraceabilityPredicate casing = states(getCasingState()).setMinGlobalLimited(40)
                .or(abilities(MultiblockAbility.INPUT_ENERGY).setMinGlobalLimited(1).setMaxGlobalLimited(3))
                .or(autoAbilities());

        // layer the slices one behind the next
        return FactoryBlockPattern.start()
                .aisle(frontBack)
                .aisle(inner).setRepeatable((depth - 3) / 2, (depth - 3) / 2) // excludes controller row, edge rows
                .aisle(center)
                .aisle(inner).setRepeatable((depth - 3) / 2, (depth - 3) / 2)
                .aisle(frontBack)
                .where('S', selfPredicate())
                .where('B', casing)
                .where('X', casing.or(doorPredicate().setMaxGlobalLimited(8).setPreviewCount(0)))
                .where('F', states(getFilterState()))
                .where(' ', innerPredicate())
                .build();
    }

    @Override
    public ICubeRenderer getBaseTexture(IMultiblockPart sourcePart) {
        return Textures.PLASCRETE;
    }

    // protected to allow easy addition of addon "cleanrooms"
    protected IBlockState getCasingState() {
        return MetaBlocks.CLEANROOM_CASING.getState(BlockCleanroomCasing.CasingType.PLASCRETE);
    }

    // protected to allow easy addition of addon "cleanrooms"
    protected static IBlockState getFilterState() {
        return MetaBlocks.CLEANROOM_CASING.getState(BlockCleanroomCasing.CasingType.FILTER_CASING);
    }

    @Nonnull
    protected static TraceabilityPredicate doorPredicate() {
        return new TraceabilityPredicate(blockWorldState -> blockWorldState.getBlockState().getBlock() instanceof BlockDoor);
    }

    // protected to allow easy addition of addon "cleanrooms"
    @Nonnull
    protected TraceabilityPredicate innerPredicate() {
        return new TraceabilityPredicate(blockWorldState -> {
            TileEntity tileEntity = blockWorldState.getTileEntity();
            if (!(tileEntity instanceof MetaTileEntityHolder))
                return true;

            MetaTileEntity metaTileEntity = ((MetaTileEntityHolder) tileEntity).getMetaTileEntity();
//            if (!(metaTileEntity instanceof ICleanroomReceiver))
//                return true;
//
//            ICleanroomReceiver cleanroomReceiver = (ICleanroomReceiver) metaTileEntity;
//            if (!cleanroomReceiver.hasCleanroom())
//                cleanroomReceiver.setCleanroom(this); TODO
            return true;
        });
    }

    @Nonnull
    @Override
    public List<ITextComponent> getDataInfo() {
        return new ArrayList<>(); //TODO
    }

    @Override
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        super.renderMetaTileEntity(renderState, translation, pipeline);
        this.getFrontOverlay().renderOrientedState(renderState, translation, pipeline, getFrontFacing(), /*recipeMapWorkable.isActive()*/ false, /*recipeMapWorkable.isWorkingEnabled()*/ true); //TODO
    }

    @Nonnull
    @Override
    protected ICubeRenderer getFrontOverlay() {
        return Textures.CLEANROOM_OVERLAY;
    }

    @Override
    public CleanroomType getType() {
        return CleanroomType.CLEANROOM;
    }
}
