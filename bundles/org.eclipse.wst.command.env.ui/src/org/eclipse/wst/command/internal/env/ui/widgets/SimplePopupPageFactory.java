/*******************************************************************************
 * Copyright (c) 2004, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.wst.command.internal.env.ui.widgets;


public class SimplePopupPageFactory implements WizardPageFactory
{   
  private String id_;
  
  public SimplePopupPageFactory( String id )
  {
    id_ = id;  
  }
  
  public PageWizardDataEvents getPage( PageInfo pageInfo, WizardPageManager manager )
  {
    return new SimplePopupWizardPage( pageInfo, manager, id_ );
  }
}
