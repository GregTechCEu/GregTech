package gregtech.common.metatileentities.multi.electric;

import gregtech.api.GTValues;
import gregtech.api.capability.IEnergyContainer;
import gregtech.api.capability.IMultipleTankHandler;
import gregtech.api.capability.impl.*;
import gregtech.api.metatileentity.IFastRenderMetaTileEntity;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.MetaTileEntityHolder;
import gregtech.api.metatileentity.multiblock.IMultiblockPart;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.api.metatileentity.multiblock.RecipeMapMultiblockController;
import gregtech.api.multiblock.BlockPattern;
import gregtech.api.multiblock.FactoryBlockPattern;
import gregtech.api.multiblock.PatternMatchContext;
import gregtech.api.recipes.MatchingMode;
import gregtech.api.recipes.Recipe;
import gregtech.api.recipes.RecipeMaps;
import gregtech.api.recipes.recipeproperties.FusionEUToStartProperty;
import gregtech.api.render.ICubeRenderer;
import gregtech.api.render.ICustomRenderFast;
import gregtech.api.render.OrientedOverlayRenderer;
import gregtech.api.render.Textures;
import gregtech.api.util.RenderBufferHelper;
import gregtech.common.asm.hooks.BloomRenderLayerHooks;
import gregtech.common.blocks.*;
import gregtech.common.metatileentities.MetaTileEntities;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.text.*;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.IItemHandlerModifiable;
import org.lwjgl.opengl.GL11;

import javax.annotation.Nonnull;
import java.util.List;

import static gregtech.api.util.RelativeDirection.*;

public class MetaTileEntityFusionReactor extends RecipeMapMultiblockController implements IFastRenderMetaTileEntity {

    private final int tier;
    private EnergyContainerList inputEnergyContainers;
    private long heat = 0; // defined in TileEntityFusionReactor but serialized in FusionRecipeLogic

    public MetaTileEntityFusionReactor(ResourceLocation metaTileEntityId, int tier) {
        super(metaTileEntityId, RecipeMaps.FUSION_RECIPES);
        this.recipeMapWorkable = new FusionRecipeLogic(this);
        this.tier = tier;
        this.reinitializeStructurePattern();
        this.energyContainer = new EnergyContainerHandler(this, Integer.MAX_VALUE, 0, 0, 0, 0) {
            @Override
            public String getName() {
                return "EnergyContainerInternal";
            }
        };
    }

    @Override
    public MetaTileEntity createMetaTileEntity(MetaTileEntityHolder holder) {
        return new MetaTileEntityFusionReactor(metaTileEntityId, tier);
    }

    @Override
    protected BlockPattern createStructurePattern() {
        FactoryBlockPattern.start();
        return FactoryBlockPattern.start(LEFT, DOWN, BACK)
                .aisle("###############", "######OGO######", "###############")
                .aisle("######ICI######", "####GG###GG####", "######ICI######")
                .aisle("####CC###CC####", "###E##OSO##E###", "####CC###CC####")
                .aisle("###C#######C###", "##EcEC###CEcE##", "###C#######C###")
                .aisle("##C#########C##", "#G#E#######E#G#", "##C#########C##")
                .aisle("##C#########C##", "#G#C#######C#G#", "##C#########C##")
                .aisle("#I###########I#", "O#O#########O#O", "#I###########I#")
                .aisle("#C###########C#", "G#C#########C#G", "#C###########C#")
                .aisle("#I###########I#", "O#O#########O#O", "#I###########I#")
                .aisle("##C#########C##", "#G#C#######C#G#", "##C#########C##")
                .aisle("##C#########C##", "#G#E#######E#G#", "##C#########C##")
                .aisle("###C#######C###", "##EcEC###CEcE##", "###C#######C###")
                .aisle("####CC###CC####", "###E##OCO##E###", "####CC###CC####")
                .aisle("######ICI######", "####GG###GG####", "######ICI######")
                .aisle("###############", "######OGO######", "###############")
                .where('S', selfPredicate())
                .where('G', statePredicate(MetaBlocks.TRANSPARENT_CASING.getState(BlockTransparentCasing.CasingType.REINFORCED_GLASS)).or(statePredicate(getCasingState())))
                .where('C', statePredicate(getCasingState()))
                .where('c', statePredicate(getCoilState()))
                .where('O', statePredicate(getCasingState()).or(abilityPartPredicate(MultiblockAbility.EXPORT_FLUIDS)))
                .where('E', statePredicate(getCasingState()).or(tilePredicate((state, tile) -> {
                    for (int i = tier; i < GTValues.UV + 1; i++) {
                        if (tile.metaTileEntityId.equals(MetaTileEntities.ENERGY_INPUT_HATCH[i].metaTileEntityId))
                            return true;
                    }
                    return false;
                })))
                .where('I', statePredicate(getCasingState()).or(abilityPartPredicate(MultiblockAbility.IMPORT_FLUIDS)))
                .where('#', (tile) -> true)
                .build();
    }

    @Override
    public ICubeRenderer getBaseTexture(IMultiblockPart sourcePart) {
        if (this.recipeMapWorkable.isActive()) {
            return Textures.ACTIVE_FUSION_TEXTURE;
        } else {
            return Textures.FUSION_TEXTURE;
        }
    }

    private IBlockState getCasingState() {
        switch (tier) {
            case 6:
                return MetaBlocks.MACHINE_CASING.getState(BlockMachineCasing.MachineCasingType.LuV);
            case 7:
                return MetaBlocks.MULTIBLOCK_CASING.getState(BlockMultiblockCasing.MultiblockCasingType.FUSION_CASING);
            case 8:
            default:
                return MetaBlocks.MULTIBLOCK_CASING.getState(BlockMultiblockCasing.MultiblockCasingType.FUSION_CASING_MK2);
        }
    }

    private IBlockState getCoilState() {
        switch (tier) {
            case 6:
                return MetaBlocks.FUSION_COIL.getState(BlockFusionCoil.CoilType.SUPERCONDUCTOR);
            case 7:
            case 8:
            default:
                return MetaBlocks.FUSION_COIL.getState(BlockFusionCoil.CoilType.FUSION_COIL);
        }
    }

    @Override
    protected void formStructure(PatternMatchContext context) {
        long energyStored = this.energyContainer.getEnergyStored();
        super.formStructure(context);
        this.initializeAbilities();
        ((EnergyContainerHandler) this.energyContainer).setEnergyStored(energyStored);
    }

    private void initializeAbilities() {
        this.inputInventory = new ItemHandlerList(getAbilities(MultiblockAbility.IMPORT_ITEMS));
        this.inputFluidInventory = new FluidTankList(true, getAbilities(MultiblockAbility.IMPORT_FLUIDS));
        this.outputInventory = new ItemHandlerList(getAbilities(MultiblockAbility.EXPORT_ITEMS));
        this.outputFluidInventory = new FluidTankList(true, getAbilities(MultiblockAbility.EXPORT_FLUIDS));
        List<IEnergyContainer> energyInputs = getAbilities(MultiblockAbility.INPUT_ENERGY);
        this.inputEnergyContainers = new EnergyContainerList(energyInputs);
        long euCapacity = energyInputs.size() * 10000000L * (long) Math.pow(2, tier - 6);
        this.energyContainer = new EnergyContainerHandler(this, euCapacity, GTValues.V[tier], 0, 0, 0) {
            @Override
            public String getName() {
                return "EnergyContainerInternal";
            }
        };
    }

    @Override
    protected void updateFormedValid() {
        if (!getWorld().isRemote) {
            if (this.inputEnergyContainers.getEnergyStored() > 0) {
                long energyAdded = this.energyContainer.addEnergy(this.inputEnergyContainers.getEnergyStored());
                if (energyAdded > 0) this.inputEnergyContainers.removeEnergy(energyAdded);
            }
            super.updateFormedValid();
        }
    }

    @Override
    protected void addDisplayText(List<ITextComponent> textList) {
        if (!this.isStructureFormed()) {
            textList.add(new TextComponentTranslation("gregtech.multiblock.invalid_structure").setStyle(new Style().setColor(TextFormatting.RED)));
        }
        if (this.isStructureFormed()) {
            if (!this.recipeMapWorkable.isWorkingEnabled()) {
                textList.add(new TextComponentTranslation("gregtech.multiblock.work_paused"));
            } else if (this.recipeMapWorkable.isActive()) {
                textList.add(new TextComponentTranslation("gregtech.multiblock.running"));
                int currentProgress;
                if (energyContainer.getEnergyCapacity() > 0) {
                    currentProgress = (int) (this.recipeMapWorkable.getProgressPercent() * 100.0D);
                    textList.add(new TextComponentTranslation("gregtech.multiblock.progress", currentProgress));
                } else {
                    currentProgress = -this.recipeMapWorkable.getRecipeEUt();
                    textList.add(new TextComponentTranslation("gregtech.multiblock.generation_eu", currentProgress));
                }
            } else {
                textList.add(new TextComponentTranslation("gregtech.multiblock.idling"));
            }

            if (this.recipeMapWorkable.isHasNotEnoughEnergy()) {
                textList.add(new TextComponentTranslation("gregtech.multiblock.not_enough_energy").setStyle(new Style().setColor(TextFormatting.RED)));
            }
        }

        textList.add(new TextComponentString("EU: " + this.energyContainer.getEnergyStored() + " / " + this.energyContainer.getEnergyCapacity()));
        textList.add(new TextComponentTranslation("gregtech.multiblock.fusion_reactor.heat", heat));
    }

    @Nonnull
    @Override
    protected OrientedOverlayRenderer getFrontOverlay() {
        return Textures.FUSION_REACTOR_OVERLAY;
    }

    @Override
    public boolean hasMaintenanceMechanics() {
        return false;
    }

    public long getHeat() {
        return heat;
    }

    private class FusionRecipeLogic extends MultiblockRecipeLogic {

        public FusionRecipeLogic(MetaTileEntityFusionReactor tileEntity) {
            super(tileEntity);
            this.allowOverclocking = false;
        }

        @Override
        public void updateWorkable() {
            super.updateWorkable();
            if (!isActive && heat > 0) {
                heat = heat <= 10000 ? 0 : (heat - 10000);
            }
        }

        @Override
        protected Recipe findRecipe(long maxVoltage, IItemHandlerModifiable inputs, IMultipleTankHandler fluidInputs, MatchingMode mode) {
            Recipe recipe = super.findRecipe(maxVoltage, inputs, fluidInputs, mode);
            return (recipe != null && recipe.getProperty(FusionEUToStartProperty.getInstance(), 0L)
                    <= energyContainer.getEnergyCapacity()) ? recipe : null;
        }

        @Override
        protected boolean setupAndConsumeRecipeInputs(Recipe recipe, IItemHandlerModifiable importInventory) {
            long heatDiff = recipe.getProperty(FusionEUToStartProperty.getInstance(), 0L) - heat;
            if (heatDiff <= 0) {
                return super.setupAndConsumeRecipeInputs(recipe, importInventory);
            }
            if (energyContainer.getEnergyStored() < heatDiff || !super.setupAndConsumeRecipeInputs(recipe, importInventory)) {
                return false;
            }
            energyContainer.removeEnergy(heatDiff);
            heat += heatDiff;
            return true;
        }

        @Override
        public NBTTagCompound serializeNBT() {
            NBTTagCompound tag = super.serializeNBT();
            tag.setLong("Heat", heat);
            return tag;
        }

        @Override
        public void deserializeNBT(NBTTagCompound compound) {
            super.deserializeNBT(compound);
            heat = compound.getLong("Heat");
        }
    }

    @Override
    public void renderMetaTileEntity(double x, double y, double z, float partialTicks) {
        if (MinecraftForgeClient.getRenderPass() == 0 && recipeMapWorkable.isWorking()) {
            BloomRenderLayerHooks.requestRenderFast(RENDER_HANDLER, (buffer)->{
                final float r = 1;
                final float g = .2f;
                final float b = .2f;
                Entity entity = Minecraft.getMinecraft().getRenderViewEntity();
                if (entity != null) {
                    buffer.begin(GL11.GL_QUAD_STRIP, DefaultVertexFormats.POSITION_COLOR);
                    RenderBufferHelper.renderRing(buffer,
                            x + getFrontFacing().getXOffset() * 5 + 0.5,
                            y + 0.5,
                            z + getFrontFacing().getZOffset() * 5 + 0.5,
                            6, 0.2, 10, 20,
                            r, g, b, 1, EnumFacing.Axis.Y);
                    Tessellator.getInstance().draw();
                }
            });
        }
    }

    @Override
    public AxisAlignedBB getRenderBoundingBox() {
        return new AxisAlignedBB(this.getPos().offset(getFrontFacing().getOpposite()).offset(getFrontFacing().rotateY(),6),
                this.getPos().offset(getFrontFacing(), 12).offset(getFrontFacing().rotateY().getOpposite(),6));
    }

    @Override
    public boolean shouldRenderInPass(int pass) {
        return pass == 0;
    }

    @Override
    public boolean isGlobalRenderer() {
        return true;
    }

    static ICustomRenderFast RENDER_HANDLER = new ICustomRenderFast(){
        float lastBrightnessX;
        float lastBrightnessY;

        @Override
        @SideOnly(Side.CLIENT)
        public void preDraw(BufferBuilder buffer) {
            lastBrightnessX = OpenGlHelper.lastBrightnessX;
            lastBrightnessY = OpenGlHelper.lastBrightnessY;
            GlStateManager.color(1,1,1,1);
            OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240.0F, 240.0F);
            GlStateManager.disableTexture2D();
        }

        @Override
        @SideOnly(Side.CLIENT)
        public void postDraw(BufferBuilder buffer) {
            GlStateManager.enableTexture2D();
            OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, lastBrightnessX, lastBrightnessY);
        }
    };
}
