package gregtech.api.graphnet.pipenet.traverse;

import net.minecraft.util.EnumFacing;

import com.github.bsideup.jabel.Desugar;

@Desugar
public record LocalTransferInformation<T, C> (EnumFacing facing, T controller, C container) {}
