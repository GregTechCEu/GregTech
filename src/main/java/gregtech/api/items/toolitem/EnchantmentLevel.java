package gregtech.api.items.toolitem;

public class EnchantmentLevel {

    private double level;
    private double levelGrowth;

    public EnchantmentLevel(double level, double levelGrowth) {
        this.level = level;
        this.levelGrowth = levelGrowth;
    }

    public int getLevel(int harvestTier) {
        return (int) Math.min(Byte.MAX_VALUE, Math.floor(this.level + this.levelGrowth * harvestTier));
    }
}
