package gregtech.api.metatileentity;

import net.minecraft.util.text.ITextComponent;

import java.util.List;

import javax.annotation.Nonnull;

public interface IDataInfoProvider {

    @Nonnull
    List<ITextComponent> getDataInfo();
}
