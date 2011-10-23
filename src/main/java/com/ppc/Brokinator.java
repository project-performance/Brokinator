package com.ppc;

import com.atlassian.confluence.core.BodyContent;
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
import com.atlassian.renderer.v2.macro.BaseMacro;
import com.atlassian.renderer.v2.macro.MacroException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

// TODO: This should be converted into an xwork action at some point in the near future
// TODO: Add options to the action screen so users can check spaces from a multiselect list; checkboxes for pages, blogposts, comments; input field to set path for output file
// TODO: Create a branch to add Confluence 4.0 support
public class Brokinator extends BaseMacro
{
    public static final String TEMPLATES_ERROR_REPORT = "templates/error-report.vm";

    private final PageManager pageManager;
    private final SpaceManager spaceManager;
    private final WikiStyleRenderer wikiStyleRenderer;

    public Brokinator(PageManager pageManager, SpaceManager spaceManager, WikiStyleRenderer wikiStyleRenderer)
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

    public String execute(Map params, String body, RenderContext renderContext) throws MacroException {
        // TODO: Output should be redirected to a file on disk because this operation can take a very long time on production systems -- especially when attempting to render high-load macros (like CustomWare's reporting macro)
        final Map<String,Object> context = MacroUtils.defaultVelocityContext();
        final List<ContentEntityObject> erroredEntities = new ArrayList<ContentEntityObject>();

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

        context.put("erroredEntities", erroredEntities);

        return VelocityUtils.getRenderedTemplate(TEMPLATES_ERROR_REPORT, context);
    }

    private void checkContentEntityObjects(final List<ContentEntityObject> list, final ContentEntityObject entity) {
        for (final Object obj2 : entity.getBodyContents()) {
            if (obj2 instanceof BodyContent) {
                final BodyContent content = (BodyContent) obj2;

                // TODO: The storage format is changing away from wiki markup to XHTML in Confluence 4, so the following check will fail. The ideal solution here is to put all this code into an xwork action rather than a macro
                final String pageContent = content.getBody();
                if (pageContent.contains("{brokinator}")) continue; // Don't render pages containing this macro

                final String renderedPage = wikiStyleRenderer.convertWikiToXHtml(entity.toPageContext(), pageContent);
                if (renderedPage.contains("Unknown macro")) {
                    list.add(entity);
                }

                for (final Comment comment : entity.getComments()) {
                    checkContentEntityObjects(list, comment);
                }
            }
        }
    }
}