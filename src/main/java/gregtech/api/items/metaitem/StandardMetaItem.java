package gregtech.api.items.metaitem;

public class StandardMetaItem extends MetaItem<MetaItem<?>.MetaValueItem> {

    public StandardMetaItem() {
        super((short) 0);
    }

    public StandardMetaItem(short metaItemOffset) {
        super(metaItemOffset);
    }

    @Override
    protected MetaValueItem constructMetaValueItem(short metaValue, String unlocalizedName) {
        return new MetaValueItem(metaValue, unlocalizedName);
    }

}
