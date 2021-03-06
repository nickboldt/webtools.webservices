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
package org.eclipse.wst.wsdl.ui.internal.adapters.commands;

import org.eclipse.wst.wsdl.Binding;
import org.eclipse.wst.wsdl.PortType;
import org.eclipse.wst.wsdl.ui.internal.Messages;

public class W11SetInterfaceCommand extends W11TopLevelElementCommand {
	private Binding binding;
	private PortType portType;
	
	public W11SetInterfaceCommand(Binding binding, PortType portType) {
        super(Messages._UI_ACTION_SET_PORTTYPE, binding.getEnclosingDefinition());
		this.binding = binding;
		this.portType = portType;
	}
	
	public void execute() {
		try {
			beginRecording(binding.getElement());
			binding.setEPortType(portType);
		}
		finally {
			endRecording(binding.getElement());
		}
	}
}