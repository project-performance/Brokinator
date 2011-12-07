package com.ppc.brokinator.managers.impl;

import com.atlassian.confluence.core.BodyContent;
import com.atlassian.confluence.core.ContentEntityObject;
import com.atlassian.confluence.pages.Comment;
import com.atlassian.confluence.pages.PageManager;
import com.atlassian.confluence.spaces.Space;
import com.atlassian.confluence.spaces.SpaceManager;
import com.atlassian.renderer.WikiStyleRenderer;
import com.ppc.brokinator.managers.BrokinatorManager;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: Aseem Parikh
 * Date: 12/7/11
 * Time: 1:46 PM
 * To change this template use File | Settings | File Templates.
 */
public class BrokinatorManagerImpl implements BrokinatorManager {
    private static final String[] ERROR_TEXTS = {
        "<span class=\"error\">",
        "Unknown macro"
    };

    private List<ContentEntityObject> erroredEntities;

    private final PageManager pageManager;
    private final SpaceManager spaceManager;
    private final WikiStyleRenderer wikiStyleRenderer;

    public BrokinatorManagerImpl(SpaceManager spaceManager, PageManager pageManager, WikiStyleRenderer wikiStyleRenderer) {
        this.spaceManager = spaceManager;
        this.pageManager = pageManager;
        this.wikiStyleRenderer = wikiStyleRenderer;
        erroredEntities = new ArrayList<ContentEntityObject>();
    }

    @Override
    public List<ContentEntityObject> getErroredEntities() {
        return erroredEntities;
    }

    @Override
    public void searchAllSpaces() {
        erroredEntities.clear();

        for (final Space space : spaceManager.getAllSpaces()) {
            for (final Object obj : pageManager.getPages(space, true)) {
                if (obj instanceof ContentEntityObject) {
                    checkContentEntityObjects(erroredEntities, (ContentEntityObject) obj);
                }
            }
            for (final Object obj : pageManager.getBlogPosts(space, true)) {
                if (obj instanceof ContentEntityObject) {
                    checkContentEntityObjects(erroredEntities, (ContentEntityObject) obj);
                }
            }
        }
    }

    private void checkContentEntityObjects(final List<ContentEntityObject> list, final ContentEntityObject entity) {
        for (final Object obj2 : entity.getBodyContents()) {
            if (obj2 instanceof BodyContent) {
                final BodyContent content = (BodyContent) obj2;

                final String pageContent = content.getBody();

                final String renderedPage = wikiStyleRenderer.convertWikiToXHtml(entity.toPageContext(), pageContent);
                for (final String errorText : ERROR_TEXTS) {
                    if (renderedPage.contains(errorText)) {
                        list.add(entity);
                        break;
                    }
                }

                for (final Comment comment : entity.getComments()) {
                    checkContentEntityObjects(list, comment);
                }
            }
        }
    }
}
