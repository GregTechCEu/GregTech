package gregtech.api.metatileentity;

import net.minecraft.util.text.ITextComponent;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface IDataInfoProvider {

    @NotNull
    List<ITextComponent> getDataInfo();
}
