/*******************************************************************************
 * Copyright (c) 2001, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.wst.wsdl.asd.editor.properties.sections;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.views.properties.tabbed.TabbedPropertySheetWidgetFactory;
import org.eclipse.wst.common.ui.internal.search.dialogs.ComponentSpecification;
import org.eclipse.wst.wsdl.asd.editor.ASDEditorPlugin;
import org.eclipse.wst.wsdl.asd.editor.ASDMultiPageEditor;
import org.eclipse.wst.wsdl.asd.editor.actions.ASDSetExistingInterfaceAction;
import org.eclipse.wst.wsdl.asd.editor.actions.ASDSetNewInterfaceAction;
import org.eclipse.wst.wsdl.asd.facade.IBinding;
import org.eclipse.wst.wsdl.asd.facade.IInterface;
import org.eclipse.wst.xsd.adt.edit.ComponentReferenceEditManager;

public class BindingSection extends ReferenceSection {
	protected ComponentReferenceEditManager refManager;
	
	public void createControls(Composite parent, TabbedPropertySheetWidgetFactory factory) {
		super.createControls(parent, factory);
		comboLabel.setText("Port Type:");
	}
	
	protected List getComboItems() {
		if (refManager == null) {
			IEditorPart editor = ASDEditorPlugin.getActiveEditor();
			refManager = ((ASDMultiPageEditor) editor).getSetInterfaceHelper((IBinding) getModel());
		}
		
		List items = new ArrayList();
		items.add(BROWSE_STRING);
		items.add(NEW_STRING);

		ComponentSpecification[] comboItems = refManager.getQuickPicks();
		for (int index = 0; index < comboItems.length; index++) {
			items.add(comboItems[index]);
		}
		
		return items;
	}

	protected Object getCurrentComboItem() {
		IBinding binding = getIBinding();
		return binding.getInterface();
	}

	protected String getComboItemName(Object item) {
		String name = "";
		if (item instanceof ComponentSpecification) {
			name = ((ComponentSpecification) item).getName();
		}
		else if (item instanceof IInterface) {
			name = ((IInterface) item).getName();
		}
		else if (item instanceof String) {
			name = (String) item;
		}
		
		return name;
	}

	protected void performComboSelection(Object item) {
		ComponentSpecification spec = null;
		
		if (item instanceof ComponentSpecification) {
			spec = (ComponentSpecification) item;
			refManager.modifyComponentReference((IBinding) getModel(), spec);
		}
		else if (item instanceof String) {
			if (item.equals(BROWSE_STRING)) {
				IWorkbenchPart part = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActivePart();
				ASDSetExistingInterfaceAction action = new ASDSetExistingInterfaceAction(part);
				action.setIBinding((IBinding) getModel());
				action.run();
			}
			else if (item.equals(NEW_STRING)) {
				IWorkbenchPart part = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActivePart();
				ASDSetNewInterfaceAction action = new ASDSetNewInterfaceAction(part);
				action.setIBinding((IBinding) getModel());
				action.run();
			}
		}

		refresh();
	}

	private IBinding getIBinding() {
		return (IBinding) getModel();
	}
}
