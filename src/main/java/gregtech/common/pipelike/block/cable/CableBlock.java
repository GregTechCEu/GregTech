package gregtech.common.pipelike.block.cable;

import gregtech.api.damagesources.DamageSources;
import gregtech.api.graphnet.pipenet.WorldPipeNetNode;
import gregtech.api.graphnet.pipenet.physical.IBurnable;
import gregtech.api.graphnet.pipenet.physical.block.PipeMaterialBlock;
import gregtech.api.graphnet.pipenet.physical.tile.PipeTileEntity;
import gregtech.api.items.toolitem.ToolClasses;
import gregtech.api.unification.material.registry.MaterialRegistry;
import gregtech.api.util.GTUtility;
import gregtech.common.creativetab.GTCreativeTabs;
import gregtech.common.pipelike.net.energy.EnergyFlowData;
import gregtech.common.pipelike.net.energy.EnergyFlowLogic;
import gregtech.common.pipelike.net.energy.SuperconductorLogic;
import gregtech.common.pipelike.net.energy.WorldEnergyNet;
import gregtech.core.advancement.AdvancementTriggers;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.FMLCommonHandler;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class CableBlock extends PipeMaterialBlock implements IBurnable {

    private static final Map<MaterialRegistry, Map<CableStructure, CableBlock>> CACHE = new Object2ObjectOpenHashMap<>();

    public CableBlock(CableStructure structure, MaterialRegistry registry) {
        super(structure, registry);
        CACHE.compute(registry, (k, v) -> {
            if (v == null) v = new Object2ObjectOpenHashMap<>();
            v.put(structure, this);
            return v;
        });
        setCreativeTab(GTCreativeTabs.TAB_GREGTECH_CABLES);
    }

    @Override
    public CableStructure getStructure() {
        return (CableStructure) super.getStructure();
    }

    @Override
    public String getToolClass() {
        return ToolClasses.WIRE_CUTTER;
    }

    @Override
    protected String getConnectLangKey() {
        return "gregtech.tool_action.wire_cutter.connect";
    }

    @Override
    public void partialBurn(IBlockState state, World world, BlockPos pos) {
        CableStructure structure = getStructure();
        if (structure.partialBurnStructure() != null) {
            CableBlock newBlock = CACHE.get(registry).get(structure.partialBurnStructure());
            // noinspection deprecation
            world.setBlockState(pos, newBlock.getStateFromMeta(this.getMetaFromState(state)));
        }
    }

    @Override
    public void onEntityCollision(@NotNull World worldIn, @NotNull BlockPos pos, @NotNull IBlockState state,
                                  @NotNull Entity entityIn) {
        super.onEntityCollision(worldIn, pos, state, entityIn);
        if (worldIn.isRemote || getStructure().isInsulated() || !(entityIn instanceof EntityLivingBase living)) return;
        PipeTileEntity tile = getTileEntity(worldIn, pos);
        if (tile != null && tile.getFrameMaterial() == null && tile.getOffsetTimer() % 10 == 0) {
            WorldPipeNetNode node = WorldEnergyNet.getWorldNet(worldIn).getNode(pos);
            if (node != null) {
                if (node.getData().getLogicEntryNullable(SuperconductorLogic.INSTANCE) != null) return;
                EnergyFlowLogic logic = node.getData().getLogicEntryNullable(EnergyFlowLogic.INSTANCE);
                if (logic != null) {
                    long tick = FMLCommonHandler.instance().getMinecraftServerInstance().getTickCounter();
                    long cumulativeDamage = 0;
                    for (EnergyFlowData data : logic.getFlow(tick)) {
                        cumulativeDamage += (GTUtility.getTierByVoltage(data.voltage()) + 1) * data.amperage() * 4;
                    }
                    if (cumulativeDamage != 0) {
                        living.attackEntityFrom(DamageSources.getElectricDamage(), cumulativeDamage);
                        if (living instanceof EntityPlayerMP playerMP) {
                            AdvancementTriggers.ELECTROCUTION_DEATH.trigger(playerMP);
                        }
                    }
                }
            }
        }
    }
}
