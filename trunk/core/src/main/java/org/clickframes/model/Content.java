package org.clickframes.model;

import java.util.ArrayList;
import java.util.List;

import org.clickframes.util.ClickframeUtils;
import org.clickframes.xmlbindings.ContentType;
import org.clickframes.xmlbindings.ContentsType;

/**
 *
 * @author Vineet Manohar
 */
public class Content extends AbstractElement{
	public Content(String id, AppspecElement parent) {
		super(id, parent);
	}
    /**
     * should the text be displayed verbatim
     */
    private boolean verbatim;

    /**
     * the text to be displayed
     */
    private String text;

    public boolean isVerbatim() {
        return verbatim;
    }

    public void setVerbatim(boolean verbatim) {
        this.verbatim = verbatim;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = ClickframeUtils.normalize(text);
    }


    public static Content create(ContentType contentType, AppspecElement parent) {
        Content content = new Content(contentType.getId(), parent);
        content.setText(contentType.getValue());
        content.setVerbatim(contentType.isVerbatim());

        return content;
    }

    public static List<Content> createList(ContentsType contents, AppspecElement parent) {
        List<Content> list = new ArrayList<Content>();
        if (contents != null) {
            for (ContentType contentType : contents.getContent()) {
                Content content = Content.create(contentType, parent);
                list.add(content);
            }
        }
        return list;
    }

	@Override
	public String getMetaName() {
		return "content";
	}
}