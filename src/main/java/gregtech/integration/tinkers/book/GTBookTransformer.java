package gregtech.integration.tinkers.book;

import slimeknights.mantle.client.book.BookTransformer;
import slimeknights.mantle.client.book.data.BookData;
import slimeknights.mantle.client.book.data.PageData;
import slimeknights.mantle.client.book.data.SectionData;
import slimeknights.mantle.client.book.data.content.PageContent;
import slimeknights.tconstruct.library.TinkerRegistry;
import slimeknights.tconstruct.library.book.content.ContentListing;
import slimeknights.tconstruct.library.book.content.ContentModifier;
import slimeknights.tconstruct.library.modifiers.IModifier;

public class GTBookTransformer extends BookTransformer {

    @Override
    public void transform(BookData bookData) {
        SectionData modSectionMain = null, modSectionNew = null;
        for (SectionData section : bookData.sections) {
            if (section.name.equals("modifiers")) {
                modSectionMain = section;
            }
            if (section.name.equals("gt_modifiers")) {
                modSectionNew = section;
            }
        }
        if (modSectionMain != null && modSectionNew != null) {
            for (PageData page : modSectionNew.pages) {
                page.parent = modSectionMain;
                modSectionMain.pages.add(page);
            }
            PageData pageData = modSectionMain.pages.get(0);
            PageContent content = pageData.content;
            if (content instanceof ContentListing listing) {
                for (PageData page : modSectionNew.pages) {
                    page.parent = modSectionMain;
                    if (page.content instanceof ContentModifier) {
                        IModifier modifier = TinkerRegistry.getModifier(((ContentModifier) page.content).modifierName);
                        if (modifier != null) {
                            page.name = "page-gt-" + modifier.getIdentifier();
                            listing.addEntry(modifier.getLocalizedName(), page);
                        }
                    }
                }
            }
            modSectionNew.pages.clear();
            bookData.sections.remove(modSectionNew);
        }
    }
}
