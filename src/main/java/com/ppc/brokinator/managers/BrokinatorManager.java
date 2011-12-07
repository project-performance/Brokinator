package com.ppc.brokinator.managers;

import com.atlassian.confluence.core.ContentEntityObject;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: Aseem Parikh
 * Date: 12/7/11
 * Time: 1:44 PM
 * To change this template use File | Settings | File Templates.
 */
public interface BrokinatorManager {
    public List<ContentEntityObject> getErroredEntities();
    public void searchAllSpaces();
}
