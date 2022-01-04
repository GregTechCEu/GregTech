package gregtech.api.metatileentity;


import net.minecraft.util.text.ITextComponent;

import javax.annotation.Nonnull;
import java.util.List;

public interface IDataInfoProvider {

    @Nonnull
    List<ITextComponent> getDataInfo();
}
