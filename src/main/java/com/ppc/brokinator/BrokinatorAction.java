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
import com.opensymphony.webwork.ServletActionContext;
import com.ppc.brokinator.managers.BrokinatorManager;
import org.apache.axis.utils.StringUtils;
import org.apache.felix.framework.resolver.Content;

import javax.servlet.http.HttpServletRequest;
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
// TODO: Create a branch to add Confluence 4.0 support
// TODO: Add options to the action screen so users can check spaces from a multiselect list; checkboxes for pages, blogposts, comments; input field to set path for output file
public class BrokinatorAction extends ConfluenceActionSupport {
    private final BrokinatorManager brokinatorManager;

    private List<ContentEntityObject> erroredEntities;

    public List<ContentEntityObject> getErroredEntities() {
        return brokinatorManager.getErroredEntities();
    }

    public void setErroredEntities(List<ContentEntityObject> erroredEntities) {
        this.erroredEntities = erroredEntities;
    }

    public BrokinatorAction(BrokinatorManager brokinatorManager)
    {
        this.brokinatorManager = brokinatorManager;
    }

    public boolean hasBody()
    {
        return false;
    }

    public RenderMode getBodyRenderMode()
    {
        return RenderMode.NO_RENDER;
    }

    @Override
    public String execute() {
        // TODO: Output should be redirected to a file on disk because this operation can take a very long time on production systems and produce a large amount of output.
        final HttpServletRequest req = ServletActionContext.getRequest();
        if (StringUtils.isEmpty(req.getParameter("confirmed"))) return SUCCESS;

        brokinatorManager.searchAllSpaces();
        
        return SUCCESS;
    }
}
