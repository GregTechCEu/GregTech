package gregtech.integration.thaumcraft;

import java.util.List;

public enum TCAspects {

    AER(),
    TERRA(),
    IGNIS(),
    AQUA(),
    ORDO(),
    PERDITIO(),
    VACUOS(),
    LUX(),
    MOTUS(),
    GELUM(),
    VITREUS(),
    METALLUM(),
    VICTUS(),
    MORTUUS(),
    POTENTIA(),
    PERMUTATIO(),
    PRAECANTATIO(),
    AURAM(),
    ALKIMIA(),
    VITIUM(),
    TENEBRAE(),
    ALIENIS(),
    VOLATUS(),
    HERBA(),
    INSTRUMENTUM(),
    FABRICO(),
    MACHINA(),
    VINCULUM(),
    SPIRITUS(),
    COGNITIO(),
    SENSUS(),
    AVERSIO(),
    PRAEMUNIO(),
    DESIDERIUM(),
    EXANIMIS(),
    BESTIA(),
    HUMANUS(),

    // Thaumic Bases
    FAMES(),
    ITER(),
    SANO(),
    PANNUS(),
    MESSIS(),

    // Thaumic Additions
    FLUCTUS(),
    INFERNUM(),
    SONUS(),
    VENTUS(),
    CAELES(),
    EXITIUM(),
    IMPERIUM(),
    VISUM(),
    DRACO(),
    ;

    // todo addons?

    public Object tcAspect;

    public static class TCAspectStack {

        public TCAspects aspect;
        public long amount;

        public TCAspectStack(TCAspects aspect, long amount) {
            this.aspect = aspect;
            this.amount = amount;
        }

        public TCAspectStack copy() {
            return new TCAspectStack(aspect, amount);
        }

        public TCAspectStack copy(long amount) {
            return new TCAspectStack(aspect, amount);
        }

        public List<TCAspectStack> addToAspectList(List<TCAspectStack> list) {
            if (amount == 0) return list;
            for (TCAspectStack aspect : list) {
                if (aspect.aspect == this.aspect) {
                    aspect.amount += this.amount;
                    return list;
                }
            }
            list.add(copy());
            return list;
        }

        public boolean removeFromAspectList(List<TCAspectStack> list) {
            for (TCAspectStack aspect : list) {
                if (aspect.aspect == this.aspect) {
                    if (aspect.amount > this.amount) {
                        aspect.amount -= this.amount;
                        return true;
                    } else {
                        list.remove(aspect);
                        return true;
                    }
                }
            }
            return false;
        }
    }
}
