package gregtech.api.metatileentity.multiblock;

import java.util.Collection;
import java.util.List;

public interface IMultiblockAbilityPart<T> extends IMultiblockPart {

    Collection<MultiblockAbility<T>> getAbilities();

    void registerAbilities(List<T> abilityList);

}
