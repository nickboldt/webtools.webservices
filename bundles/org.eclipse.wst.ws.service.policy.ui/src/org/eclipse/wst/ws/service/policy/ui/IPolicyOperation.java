/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Corporation - initial API and implementation
 * yyyymmdd bug      Email and other contact information
 * -------- -------- -----------------------------------------------------------
 * 20071024   196997 pmoogk@ca.ibm.com - Peter Moogk, Initial coding.
 *******************************************************************************/
package org.eclipse.wst.ws.service.policy.ui;

import java.util.List;

import org.eclipse.wst.ws.service.policy.IDescriptor;
import org.eclipse.wst.ws.service.policy.IServicePolicy;

public interface IPolicyOperation
{
  public enum OperationKind { enumeration, selection, iconSelection, complex };
 
  public String getId();
  
  public IDescriptor getDescriptor();
  
  public OperationKind getOperationKind();
  
  public String getEnumerationId();
  
  public void launchOperation( List<IServicePolicy> selectedPolicies );
  
  public boolean isEnabled( List<IServicePolicy> selectedPolicies );
  
  public String getPolicyIdPattern();
}
