package gregtech.api.metatileentity.multiblock;

import java.util.Collection;
import java.util.List;

public interface IMultiblockAbilityPart extends IMultiblockPart {

    Collection<MultiblockAbility<?>> getAbilities();

    void registerAbilities(List<Object> abilityList);

}
