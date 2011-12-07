package com.ppc.brokinator;

import com.atlassian.confluence.core.BodyContent;
import com.atlassian.confluence.core.ConfluenceActionSupport;
import com.atlassian.confluence.core.ContentEntityObject;
import com.atlassian.confluence.pages.Comment;
import com.atlassian.confluence.pages.PageManager;
import com.atlassian.confluence.renderer.radeox.macros.MacroUtils;
import com.atlassian.confluence.spaces.Space;
import com.atlassian.confluence.spaces.SpaceManager;
import com.atlassian.confluence.util.velocity.VelocityUtils;
import com.atlassian.renderer.RenderContext;
import com.atlassian.renderer.WikiStyleRenderer;
import com.atlassian.renderer.v2.RenderMode;
import com.atlassian.renderer.v2.macro.MacroException;
import org.apache.felix.framework.resolver.Content;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: Aseem Parikh
 * Date: 12/6/11
 * Time: 3:12 PM
 * To change this template use File | Settings | File Templates.
 */
public class BrokinatorAction extends ConfluenceActionSupport {
    public static final String TEMPLATES_ERROR_REPORT = "templates/error-report.vm";

    private static final String[] ERROR_TEXTS = {
        "<span class=\"error\">",
        "Unknown macro"
    };

    private final PageManager pageManager;
    private final SpaceManager spaceManager;
    private final WikiStyleRenderer wikiStyleRenderer;

    private ArrayList<ContentEntityObject> erroredEntities;

    public ArrayList<ContentEntityObject> getErroredEntities() {
        return erroredEntities;
    }

    public void setErroredEntities(ArrayList<ContentEntityObject> erroredEntities) {
        this.erroredEntities = erroredEntities;
    }

    public BrokinatorAction(PageManager pageManager, SpaceManager spaceManager, WikiStyleRenderer wikiStyleRenderer)
    {
        this.pageManager = pageManager;
        this.spaceManager = spaceManager;
        this.wikiStyleRenderer = wikiStyleRenderer;
    }

    public boolean hasBody()
    {
        return false;
    }

    public RenderMode getBodyRenderMode()
    {
        return RenderMode.NO_RENDER;
    }

    private void checkContentEntityObjects(final List<ContentEntityObject> list, final ContentEntityObject entity) {
        for (final Object obj2 : entity.getBodyContents()) {
            if (obj2 instanceof BodyContent) {
                final BodyContent content = (BodyContent) obj2;

                // TODO: The storage format is changing away from wiki markup to XHTML in Confluence 4, so the following check will fail. The ideal solution here is to put all this code into an xwork action rather than a macro
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

    @Override
    public String execute() {
        // TODO: Output should be redirected to a file on disk because this operation can take a very long time on production systems -- especially when attempting to render high-load macros (like CustomWare's reporting macro)
        final Map<String,Object> context = MacroUtils.defaultVelocityContext();
        erroredEntities = new ArrayList<ContentEntityObject>();

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

        return SUCCESS;
    }
}
