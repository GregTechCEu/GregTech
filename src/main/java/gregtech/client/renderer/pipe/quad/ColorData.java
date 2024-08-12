package gregtech.client.renderer.pipe.quad;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import com.github.bsideup.jabel.Desugar;

@Desugar
@SideOnly(Side.CLIENT)
public record ColorData(int... colorsARGB) {}
