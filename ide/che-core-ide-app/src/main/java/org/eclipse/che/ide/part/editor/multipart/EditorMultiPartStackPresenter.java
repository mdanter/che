/*******************************************************************************
 * Copyright (c) 2012-2016 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.ide.part.editor.multipart;

import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.web.bindery.event.shared.EventBus;

import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.ide.api.constraints.Constraints;
import org.eclipse.che.ide.api.editor.EditorPartPresenter;
import org.eclipse.che.ide.api.event.ActivePartChangedEvent;
import org.eclipse.che.ide.api.event.ActivePartChangedHandler;
import org.eclipse.che.ide.api.parts.EditorMultiPartStack;
import org.eclipse.che.ide.api.parts.EditorPartStack;
import org.eclipse.che.ide.api.parts.EditorTab;
import org.eclipse.che.ide.api.parts.PartPresenter;
import org.eclipse.che.ide.part.editor.EditorPartStackFactory;

import javax.validation.constraints.NotNull;
import java.util.LinkedList;

/**
 * Presenter to control the displaying of multi editors.
 *
 * @author Roman Nikitenko
 */
@Singleton
public class EditorMultiPartStackPresenter implements EditorMultiPartStack,
                                                      ActivePartChangedHandler {
    private PartPresenter   activeEditor;
    private EditorPartStack activeEditorPartStack;

    private final EditorPartStackFactory      editorPartStackFactory;
    private final EditorMultiPartStackView    view;
    private final LinkedList<EditorPartStack> partStackPresenters;

    @Inject
    public EditorMultiPartStackPresenter(EventBus eventBus,
                                         EditorMultiPartStackView view,
                                         EditorPartStackFactory editorPartStackFactory) {
        this.view = view;
        this.editorPartStackFactory = editorPartStackFactory;
        this.partStackPresenters = new LinkedList<>();

        eventBus.addHandler(ActivePartChangedEvent.TYPE, this);
    }


    @Override
    public void go(AcceptsOneWidget container) {
        container.setWidget(view);
    }

    @Override
    public boolean containsPart(PartPresenter part) {
        for (EditorPartStack partStackPresenter : partStackPresenters) {
            if (partStackPresenter.containsPart(part)) {
                return true;
            }
        }
        return false;
    }

    /** {@inheritDoc} */
    @Override
    public void addPart(@NotNull PartPresenter part) {
        if (activeEditorPartStack != null) {
            //open the part in the active editorPartStack
            activeEditorPartStack.addPart(part);
            return;
        }

        //open the part in the new editorPartStack
        addEditorPartStack(part, null, null);
    }

    /** {@inheritDoc} */
    @Override
    public void addPart(@NotNull PartPresenter part, Constraints constraint) {
        if (constraint == null) {
            addPart(part);
            return;
        }

        EditorPartStack relativePartStack = getPartStackByTabId(constraint.relativeId);
        if (relativePartStack != null) {
            //view of relativePartStack will be split corresponding to constraint on two areas and part will be added into created area
            addEditorPartStack(part, relativePartStack, constraint);
        }
    }

    private void addEditorPartStack(final PartPresenter part, final EditorPartStack relativePartStack, final Constraints constraints) {
        final EditorPartStack editorPartStack = editorPartStackFactory.create();
        partStackPresenters.add(editorPartStack);

        view.addPartStack(editorPartStack, relativePartStack, constraints);

        if (part != null) {
            editorPartStack.addPart(part);
        }
    }

    @Override
    public void setFocus(boolean focused) {
        if (activeEditorPartStack != null) {
            activeEditorPartStack.setFocus(focused);
        }
    }

    /** {@inheritDoc} */
    @Override
    public PartPresenter getActivePart() {
        return activeEditor;
    }

    /** {@inheritDoc} */
    @Override
    public void setActivePart(@NotNull PartPresenter part) {
        activeEditor = part;
        EditorPartStack editorPartStack = getPartStackByPart(part);
        if (editorPartStack != null) {
            activeEditorPartStack = editorPartStack;
            editorPartStack.setActivePart(part);
        }
    }

    @Override
    public void hidePart(PartPresenter part) {
        EditorPartStack editorPartStack = getPartStackByPart(part);
        if (editorPartStack != null) {
            editorPartStack.hidePart(part);
        }
    }

    /** {@inheritDoc} */
    @Override
    public void removePart(PartPresenter part) {
        EditorPartStack editorPartStack = getPartStackByPart(part);
        if (editorPartStack == null) {
            return;
        }

        editorPartStack.removePart(part);

        if (editorPartStack.getActivePart() == null) {
            removePartStack(editorPartStack);
        }
    }

    private void removePartStack(EditorPartStack editorPartStack) {
        if (activeEditorPartStack == editorPartStack) {
            activeEditorPartStack = null;
        }

        view.removePartStack(editorPartStack);
        partStackPresenters.remove(editorPartStack);

        if (!partStackPresenters.isEmpty()) {
            EditorPartStack lastStackPresenter = partStackPresenters.getLast();
            lastStackPresenter.openPreviousActivePart();
        }
    }

    @Override
    public void openPreviousActivePart() {
        if (activeEditor != null) {
            getPartStackByPart(activeEditor).openPreviousActivePart();
        }
    }

    @Override
    public void updateStack() {
        for (EditorPartStack editorPartStack : partStackPresenters) {
            editorPartStack.updateStack();
        }
    }

    @Override
    public EditorPartStack getActivePartStack() {
        return activeEditorPartStack;
    }

    @Nullable
    public EditorPartStack getPartStackByPart(PartPresenter part) {
        if (part == null) {
            return null;
        }

        for (EditorPartStack editorPartStack : partStackPresenters) {
            if (editorPartStack.containsPart(part)) {
                return editorPartStack;
            }
        }
        return null;
    }

    @Nullable
    private EditorPartStack getPartStackByTabId(@NotNull String tabId) {
        for (EditorPartStack editorPartStack : partStackPresenters) {
            PartPresenter editorPart = editorPartStack.getPartByTabId(tabId);
            if (editorPart != null) {
                return editorPartStack;
            }
        }
        return null;
    }

    @Override
    public EditorPartPresenter getPartByTabId(@NotNull String tabId) {
        for (EditorPartStack editorPartStack : partStackPresenters) {
            EditorPartPresenter editorPart = editorPartStack.getPartByTabId(tabId);
            if (editorPart != null) {
                return editorPart;
            }
        }
        return null;
    }

    @Override
    public EditorTab getTabByPart(EditorPartPresenter editorPart) {
        for (EditorPartStack editorPartStack : partStackPresenters) {
            EditorTab editorTab = editorPartStack.getTabByPart(editorPart);
            if (editorTab != null) {
                return editorTab;
            }
        }
        return null;
    }

    @Override
    public EditorPartPresenter getNextFor(EditorPartPresenter editorPart) {
        for (EditorPartStack editorPartStack : partStackPresenters) {
            if (editorPartStack.containsPart(editorPart)) {
                return editorPartStack.getNextFor(editorPart);
            }
        }
        return null;
    }

    @Override
    public EditorPartPresenter getPreviousFor(EditorPartPresenter editorPart) {
        for (EditorPartStack editorPartStack : partStackPresenters) {
            if (editorPartStack.containsPart(editorPart)) {
                return editorPartStack.getPreviousFor(editorPart);
            }
        }
        return null;
    }

    @Override
    public void onActivePartChanged(ActivePartChangedEvent event) {
        PartPresenter activePart = event.getActivePart();
        if (activePart instanceof EditorPartPresenter) {
            activeEditor = activePart;
            activeEditorPartStack = getPartStackByPart(activePart);
        }
    }
}
