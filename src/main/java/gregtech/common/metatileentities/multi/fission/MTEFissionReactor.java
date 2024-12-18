package gregtech.common.metatileentities.multi.fission;

import gregtech.api.fission.FissionReactorController;
import gregtech.api.fission.component.FissionComponent;
import gregtech.api.fission.reactor.FissionReactor;
import gregtech.api.fission.reactor.ReactionSite;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.ModularUI;
import gregtech.api.gui.Widget;
import gregtech.api.gui.widgets.ClickButtonWidget;
import gregtech.api.gui.widgets.ImageCycleButtonWidget;
import gregtech.api.gui.widgets.ImageWidget;
import gregtech.api.gui.widgets.WidgetGroup;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.IMultiblockPart;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.api.metatileentity.multiblock.MultiblockDisplayText;
import gregtech.api.metatileentity.multiblock.MultiblockWithDisplayBase;
import gregtech.api.pattern.BlockPattern;
import gregtech.api.pattern.FactoryBlockPattern;
import gregtech.api.pattern.PatternMatchContext;
import gregtech.api.util.TextComponentUtil;
import gregtech.api.util.math.Vec2i;
import gregtech.client.renderer.ICubeRenderer;
import gregtech.client.renderer.texture.Textures;
import gregtech.common.blocks.BlockMetalCasing;
import gregtech.common.blocks.MetaBlocks;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class MTEFissionReactor extends MultiblockWithDisplayBase implements FissionReactorController {

    private final Map<FissionComponent, Vec2i> componentPositions = new Object2ObjectOpenHashMap<>();

    private @Nullable FissionReactor reactor;
    private int radius = 1;
    private int size = 3;

    private boolean isLocked;
    private boolean isStarted;
    private boolean hasFailure;

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

        if (isLocked && isStarted && getOffsetTimer() % 20 == 0) {
            assert reactor != null;
            this.hasFailure = reactor.run();
        }
    }

    @Override
    protected void formStructure(PatternMatchContext context) {
        super.formStructure(context);
        determineComponentLayout();
        this.reactor = new FissionReactor(size, size * 100); // TODO maxHeat from wall component
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

    protected void finalizeStructure(boolean lock) {
        if (isStarted || !isStructureFormed()) {
            return;
        }

        if (lock) {
            assert reactor != null;
            FissionComponent[][] matrix = reactor.matrix();
            for (var entry : componentPositions.entrySet()) {
                Vec2i pos = entry.getValue();
                matrix[pos.x()][pos.y()] = entry.getKey();
            }
            this.reactor.computeGeometry();
            this.isLocked = true;

            this.reactor.sites().forEach(s -> s.setTargetHeat(400)); // TODO user controlled

            System.out.println(Arrays.deepToString(matrix));
        } else {
            this.isLocked = false;
        }
    }

    private void adjustRadius(int radius) {
        if (isStarted || isLocked) {
            return;
        }

        int nextRadius = this.radius + radius;
        if (nextRadius < 1 || nextRadius > 5) {
            return;
        }
        this.radius = nextRadius;
        this.size = nextRadius * 2 + 1;
    }

    private void start(boolean started) {
        if (this.isStarted) {
            return;
        }
        this.isStarted = started;
    }

    @Override
    public void scheduleRuntimeRecompute() {
        if (reactor != null) {
            reactor.triggerRuntimeRecompute();
        }
    }

    @Override
    public boolean isLocked() {
        return isLocked;
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
    protected ModularUI.Builder createUITemplate(EntityPlayer entityPlayer) {
        ModularUI.Builder builder = super.createUITemplate(entityPlayer);

        // TODO THESE RUN ON CLIENT AND CRASH
        builder.widget(new ImageCycleButtonWidget(173, 161, 18, 18, GuiTextures.BUTTON_VOID_NONE,
                this::isLocked, this::finalizeStructure)
                        .setTooltipHoverString("gregtech.multiblock.fission.lock"));

        builder.widget(new ImageCycleButtonWidget(173, 183, 18, 18, GuiTextures.BUTTON_POWER,
                () -> this.isStarted, this::start)
                        .setTooltipHoverString("gregtech.multiblock.fission.start"));
        builder.widget(new ImageWidget(173, 201, 18, 6, GuiTextures.BUTTON_POWER_DETAIL));

        return builder;
    }

    @Override
    protected void addDisplayText(List<ITextComponent> textList) {
        MultiblockDisplayText.builder(textList, isStructureFormed())
                .setWorkingStatus(isStarted, isLocked)
                .addCustom(tl -> {
                    if (isStructureFormed()) {
                        tl.add(TextComponentUtil.translationWithColor(TextFormatting.AQUA,
                                "gregtech.multiblock.fission.size", size, size));
                        if (isLocked) {
                            assert reactor != null;
                            tl.add(TextComponentUtil.translationWithColor(TextFormatting.RED,
                                    "gregtech.multiblock.fission.heat", reactor.avgHeat(), reactor.maxHeat()));

                            var sites = reactor.sites();
                            for (int i = 0; i < sites.size(); i++) {
                                ReactionSite site = sites.get(i);
                                tl.add(TextComponentUtil.translationWithColor(TextFormatting.GRAY,
                                        "gregtech.multiblock.fission.site", i, site.heat(), site.targetHeat(),
                                        site.lifespan() * 100));
                            }

                            if (hasFailure) {
                                // TODO specific failure reasons
                                tl.add(TextComponentUtil.translationWithColor(TextFormatting.DARK_RED,
                                        "gregtech.multiblock.fission.failure"));
                            }
                        }
                    }
                })
                .setWorkingStatusKeys("gregtech.multiblock.fission.idling", "gregtech.multiblock.fission.work_paused",
                        "gregtech.multiblock.fission.running")
                .addWorkingStatusLine();
    }

    @Override
    protected @NotNull Widget getFlexButton(int x, int y, int width, int height) {
        WidgetGroup group = new WidgetGroup(x, y, width, height);
        group.addWidget(new ClickButtonWidget(0, 0, 9, 18, "", i -> adjustRadius(-1))
                .setButtonTexture(GuiTextures.BUTTON_THROTTLE_MINUS)
                .setTooltipText("gregtech.multiblock.large_boiler.throttle_decrement"));
        group.addWidget(new ClickButtonWidget(9, 0, 9, 18, "", i -> adjustRadius(1))
                .setButtonTexture(GuiTextures.BUTTON_THROTTLE_PLUS)
                .setTooltipText("gregtech.multiblock.large_boiler.throttle_increment"));
        return group;
    }

    @Override
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        super.renderMetaTileEntity(renderState, translation, pipeline);
        // TODO dedicated texture
        Textures.BLAST_FURNACE_OVERLAY.renderOrientedState(renderState, translation, pipeline, getFrontFacing(),
                isLocked, isStarted);
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound data) {
        super.writeToNBT(data);
        if (reactor != null) {
            NBTTagCompound tag = new NBTTagCompound();
            reactor.writeToNBT(tag);
            data.setTag("reactor", tag);
        }
        data.setInteger("radius", radius);
        data.setBoolean("isLocked", isLocked);
        data.setBoolean("isStarted", isStarted);
        data.setBoolean("hasFailure", hasFailure);
        return data;
    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
        super.readFromNBT(data);
        if (reactor != null) {
            this.reactor.readFromNBT(data.getCompoundTag("reactor"));
        }
        this.radius = data.getInteger("radius");
        this.size = radius * 2 + 1;
        this.isLocked = data.getBoolean("isLocked");
        this.isStarted = data.getBoolean("isStarted");
        this.hasFailure = data.getBoolean("hasFailure");
    }
}
