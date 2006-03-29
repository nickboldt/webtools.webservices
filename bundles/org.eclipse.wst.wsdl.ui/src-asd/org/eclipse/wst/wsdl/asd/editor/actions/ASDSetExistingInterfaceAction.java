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
package org.eclipse.wst.wsdl.asd.editor.actions;

import org.eclipse.jface.window.Window;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.wst.common.ui.internal.search.dialogs.ComponentSpecification;
import org.eclipse.wst.wsdl.asd.editor.ASDEditorPlugin;
import org.eclipse.wst.wsdl.asd.editor.ASDMultiPageEditor;
import org.eclipse.wst.wsdl.asd.facade.IBinding;
import org.eclipse.wst.xsd.adt.edit.ComponentReferenceEditManager;
import org.eclipse.wst.xsd.adt.edit.IComponentDialog;

public class ASDSetExistingInterfaceAction extends BaseSelectionAction {
	public static String ID = "ASDSetExistingInterfaceAction";
	protected IBinding binding;
	
	public ASDSetExistingInterfaceAction(IWorkbenchPart part)	{
		super(part);
		setId(ID);
		setText("Set Existing PortType...");
//		setImageDescriptor(WSDLEditorPlugin.getImageDescriptor("icons/service_obj.gif"));
	}
	
	public void setIBinding(IBinding binding) {
		this.binding = binding;
	}
	
	public void run() {		
		if (binding == null) {
			if (getSelectedObjects().size() > 0) {
				Object o = getSelectedObjects().get(0);
				if (o instanceof IBinding) {
					binding = (IBinding) o;
				}
			}
		}
		
		if (binding != null) {
			IEditorPart editor = ASDEditorPlugin.getActiveEditor();
			ComponentReferenceEditManager refManager = ((ASDMultiPageEditor) editor).getSetInterfaceHelper(binding);
			
			IComponentDialog dialog = refManager.getBrowseDialog();
			if (dialog.createAndOpen() == Window.OK) {
				ComponentSpecification spec = dialog.getSelectedComponent();
				refManager.modifyComponentReference(binding, spec);
			}
		}
		
		binding = null;
	}
	
	protected boolean calculateEnabled() {
		return true;
	}
}
