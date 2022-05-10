package gregtech.common.pipelike.fluidpipe;

import gregtech.api.pipenet.block.material.BlockMaterialPipe;
import gregtech.api.pipenet.block.material.ItemBlockMaterialPipe;
import gregtech.api.recipes.ModHandler;
import gregtech.api.unification.material.properties.FluidPipeProperties;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Keyboard;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public class ItemBlockFluidPipe extends ItemBlockMaterialPipe<FluidPipeType, FluidPipeProperties> {

    public ItemBlockFluidPipe(BlockFluidPipe block) {
        super(block);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(@Nonnull ItemStack stack, @Nullable World worldIn, @Nonnull List<String> tooltip, @Nonnull ITooltipFlag flagIn) {
        super.addInformation(stack, worldIn, tooltip, flagIn);
        FluidPipeProperties pipeProperties = blockPipe.createItemProperties(stack);
        tooltip.add(I18n.format("gregtech.fluid_pipe.throughput", pipeProperties.getThroughput()));
        tooltip.add(I18n.format("gregtech.fluid_pipe.capacity", pipeProperties.getThroughput() * 20));
        tooltip.add(I18n.format("gregtech.fluid_pipe.max_temperature", pipeProperties.getMaxFluidTemperature()));
        if (pipeProperties.getTanks() > 1) tooltip.add(I18n.format("gregtech.fluid_pipe.channels", pipeProperties.getTanks()));

        if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT)) {
            if (pipeProperties.isGasProof()) tooltip.add(I18n.format("gregtech.fluid_pipe.gas_proof"));
            else if (ModHandler.isMaterialWood(((BlockMaterialPipe<?, ?, ?>) blockPipe).getItemMaterial(stack)))
                tooltip.add(I18n.format("gregtech.fluid_pipe.not_gas_proof"));
            if (pipeProperties.isAcidProof()) tooltip.add(I18n.format("gregtech.fluid_pipe.acid_proof"));
            if (pipeProperties.isCryoProof()) tooltip.add(I18n.format("gregtech.fluid_pipe.cryo_proof"));
            if (pipeProperties.isPlasmaProof()) tooltip.add(I18n.format("gregtech.fluid_pipe.plasma_proof"));
        } else {
            tooltip.add(I18n.format("gregtech.tooltip.fluid_pipe_hold_shift"));
        }

        if (flagIn.isAdvanced()) {
            tooltip.add("MetaItem Id: " + ((BlockMaterialPipe<?, ?, ?>) blockPipe).getPrefix().name + ((BlockMaterialPipe<?, ?, ?>) blockPipe).getItemMaterial(stack).toCamelCaseString());
        }
    }
}
