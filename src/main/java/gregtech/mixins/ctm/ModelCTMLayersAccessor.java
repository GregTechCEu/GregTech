package gregtech.mixins.ctm;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import team.chisel.ctm.client.model.ModelCTM;

@Mixin(value = ModelCTM.class, remap = false)
public interface ModelCTMLayersAccessor {

    @Accessor("layers")
    byte getLayers();
}
