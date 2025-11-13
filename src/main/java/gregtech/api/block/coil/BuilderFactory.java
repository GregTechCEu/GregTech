package gregtech.api.block.coil;

public interface BuilderFactory {

    CoilBlockBuilder makeBuilder(int id, String name);
}
