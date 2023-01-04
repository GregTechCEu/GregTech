package gregtech.common.metatileentities.multi.electric;

import codechicken.lib.vec.Vector3;
import gregtech.api.GTValues;
import gregtech.api.capability.GregtechDataCodes;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.IMultiblockPart;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.api.metatileentity.multiblock.RecipeMapMultiblockController;
import gregtech.api.pattern.BlockPattern;
import gregtech.api.pattern.FactoryBlockPattern;
import gregtech.api.recipes.RecipeMaps;
import gregtech.api.util.GTUtility;
import gregtech.client.particle.GTLaserBeamParticle;
import gregtech.client.particle.GTParticleManager;
import gregtech.client.renderer.ICubeRenderer;
import gregtech.client.renderer.texture.Textures;
import gregtech.common.ConfigHolder;
import gregtech.common.blocks.BlockGlassCasing;
import gregtech.common.blocks.BlockMetalCasing;
import gregtech.common.blocks.BlockMultiblockCasing;
import gregtech.common.blocks.MetaBlocks;
import gregtech.common.metatileentities.MetaTileEntities;
import net.minecraft.block.state.IBlockState;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import static gregtech.api.util.RelativeDirection.*;

public class MetaTileEntityAssemblyLine extends RecipeMapMultiblockController {

    private static final ResourceLocation laserLocation = new ResourceLocation(GTValues.MODID,"textures/fx/laser/laser.png");
    private static final ResourceLocation laserHeadLocation = new ResourceLocation(GTValues.MODID,"textures/fx/laser/laser_start.png");

    public MetaTileEntityAssemblyLine(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId, RecipeMaps.ASSEMBLY_LINE_RECIPES);
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity tileEntity) {
        return new MetaTileEntityAssemblyLine(metaTileEntityId);
    }

    @Override
    protected BlockPattern createStructurePattern() {
        return FactoryBlockPattern.start(FRONT, UP, RIGHT)
                .aisle("FIF", "RTR", "SAG", "#Y#")
                .aisle("FIF", "RTR", "GAG", "#Y#").setRepeatable(3, 15)
                .aisle("FOF", "RTR", "GAG", "#Y#")
                .where('S', selfPredicate())
                .where('F', states(getCasingState())
                        .or(autoAbilities(false, true, false, false, false, false, false))
                        .or(abilities(MultiblockAbility.IMPORT_FLUIDS).setMaxGlobalLimited(4)))
                .where('O', abilities(MultiblockAbility.EXPORT_ITEMS).addTooltips("gregtech.multiblock.pattern.location_end"))
                .where('Y', states(getCasingState()).or(abilities(MultiblockAbility.INPUT_ENERGY).setMinGlobalLimited(1).setMaxGlobalLimited(3)))
                .where('I', metaTileEntities(MetaTileEntities.ITEM_IMPORT_BUS[0]))
                .where('G', states(MetaBlocks.MULTIBLOCK_CASING.getState(BlockMultiblockCasing.MultiblockCasingType.GRATE_CASING)))
                .where('A', states(MetaBlocks.MULTIBLOCK_CASING.getState(BlockMultiblockCasing.MultiblockCasingType.ASSEMBLY_CONTROL)))
                .where('R', states(MetaBlocks.TRANSPARENT_CASING.getState(BlockGlassCasing.CasingType.LAMINATED_GLASS)))
                .where('T', states(MetaBlocks.MULTIBLOCK_CASING.getState(BlockMultiblockCasing.MultiblockCasingType.ASSEMBLY_LINE_CASING)))
                .where('#', any())
                .build();
    }

    @Override
    public ICubeRenderer getBaseTexture(IMultiblockPart sourcePart) {
        return Textures.SOLID_STEEL_CASING;
    }

    protected IBlockState getCasingState() {
        return MetaBlocks.METAL_CASING.getState(BlockMetalCasing.MetalCasingType.STEEL_SOLID);
    }

    private int beamCount;
    @SideOnly(Side.CLIENT)
    private GTLaserBeamParticle[][] beamParticles;

    @Override
    public void update() {
        super.update();
        if(ConfigHolder.client.shader.assemblyLineParticles) {
            if (getRecipeMapWorkable().isWorking()) {
                int maxBeams = getAbilities(MultiblockAbility.IMPORT_ITEMS).size() + 1;
                int maxProgress = getRecipeMapWorkable().getMaxProgress();

                // Each beam should be visible for an equal amount of time, which is derived from the maximum number of
                // beams and the maximum progress in the recipe.
                int beamTime = Math.max(1, maxProgress / maxBeams);

                int currentBeamCount = Math.min(maxBeams, getRecipeMapWorkable().getProgress() / beamTime);

                if (currentBeamCount != beamCount) {
                    beamCount = currentBeamCount;
                    writeCustomData(GregtechDataCodes.UPDATE_PARTICLE, this::writeParticles);
                }
                //return;
            }
            else if (beamCount != 0) {
                beamCount = 0;
                writeCustomData(GregtechDataCodes.UPDATE_PARTICLE, this::writeParticles);
            }
        }
    }

    @Override
    public void writeInitialSyncData(PacketBuffer buf) {
        super.writeInitialSyncData(buf);
        writeParticles(buf);
    }

    @Override
    public void receiveInitialSyncData(PacketBuffer buf) {
        super.receiveInitialSyncData(buf);
        readParticles(buf);
    }

    @Override
    public void receiveCustomData(int dataId, PacketBuffer buf) {
        if (dataId == GregtechDataCodes.UPDATE_PARTICLE) {
            readParticles(buf);
        } else {
            super.receiveCustomData(dataId, buf);
        }
    }

    @Override
    public void onRemoval() {
        super.onRemoval();
        if (getWorld().isRemote && beamParticles != null) {
            for (GTLaserBeamParticle[] particle : beamParticles) {
                if (particle[0] != null) {
                    particle[0].setExpired();
                    particle[1].setExpired();
                }
            }
        }
        beamParticles = null;
    }

    private void writeParticles(PacketBuffer buf) {
        buf.writeVarInt(beamCount);
    }

    private void readParticles(PacketBuffer buf) {
        beamCount = buf.readVarInt();
        if (beamParticles == null) {
            beamParticles = new GTLaserBeamParticle[17][2];
        }
        for (int i = 0; i < beamParticles.length; i++) {
            GTLaserBeamParticle particle = beamParticles[i][0];
            if (i < beamCount && particle == null) {
                Vector3 startPos = new Vector3().add(
                                getPos().offset(getFrontFacing().rotateY().getOpposite(), i))
                        .add(0.5, 0, 0.5);
                Vector3 endPos = startPos.copy().subtract(0, 1, 0);

                beamParticles[i][0] = new GTLaserBeamParticle(getWorld(), startPos, endPos)
                        .setBody(laserLocation)
                        .setBeamHeight(0.125f)
                        // Try commenting or adjusting on the next four lines to see what happens
                        .setDoubleVertical(true)
                        .setHead(laserHeadLocation)
                        .setHeadWidth(0.1f)
                        .setEmit(0.2f);
                beamParticles[i][0].setOnUpdate(p -> { // remove it if machine is inValid
                    if (!isValid() || getWorld().getTileEntity(getPos()) != this.getHolder()) p.setExpired();
                });

                startPos = new Vector3().add(
                                getPos().offset(getFrontFacing().rotateY().getOpposite(), i).offset(getFrontFacing().getOpposite(), 2))
                        .add(0.5, 0, 0.5);
                endPos = startPos.copy().subtract(0, 1, 0);
                beamParticles[i][1] = new GTLaserBeamParticle(getWorld(), startPos, endPos)
                        .setBody(laserLocation)
                        .setBeamHeight(0.125f)
                        // Try commenting or adjusting on the next four lines to see what happens
                        .setDoubleVertical(true)
                        .setHead(laserHeadLocation)
                        .setHeadWidth(0.1f)
                        .setEmit(0.15f);
                beamParticles[i][1].setOnUpdate(p -> { // remove it if machine is inValid
                    if (!isValid() || !GTUtility.isPosChunkLoaded(getWorld(), getPos())) p.setExpired();
                });

                // Don't forget to add particles
                GTParticleManager.INSTANCE.addEffect(beamParticles[i][0], beamParticles[i][1]);

            } else if (i >= beamCount && particle != null) {
                particle.setExpired();
                beamParticles[i][0] = null;
                beamParticles[i][1].setExpired();
                beamParticles[i][1] = null;
            }
        }
    }
}
