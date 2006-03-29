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

import org.eclipse.gef.commands.Command;
import org.eclipse.gef.commands.CommandStack;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.wst.wsdl.asd.editor.ASDEditorPlugin;
import org.eclipse.wst.wsdl.asd.facade.IEndPoint;
import org.eclipse.wst.wsdl.asd.facade.IService;

public class ASDAddEndPointAction extends BaseSelectionAction {	
	public static String ID = "ASDAddEndPointAction"; 
	
	public ASDAddEndPointAction(IWorkbenchPart part)	{
		super(part);
		setId(ID);
		setText("Add Port");
		setImageDescriptor(ASDEditorPlugin.getImageDescriptor("icons/port_obj.gif"));
	}
	
	public void run() {
		if (getSelectedObjects().size() > 0) {
			Object o = getSelectedObjects().get(0);
			IService service = null;
			
            if (o instanceof IService) {
            	service = (IService) o;
            }
            else if (o instanceof IEndPoint) {
            	service = ((IEndPoint) o).getOwnerService();
            }
            
            if (service != null) {
                Command command = service.getAddEndPointCommand();
                CommandStack stack = (CommandStack) ASDEditorPlugin.getActiveEditor().getAdapter(CommandStack.class);
                stack.execute(command);
            }
		}  
	}
	
	protected boolean calculateEnabled() {
		return true;
	}
}