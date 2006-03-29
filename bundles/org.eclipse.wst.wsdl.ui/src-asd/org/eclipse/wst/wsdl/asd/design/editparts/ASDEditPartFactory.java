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
package org.eclipse.wst.wsdl.asd.design.editparts;

import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPartFactory;
import org.eclipse.jface.util.Assert;
import org.eclipse.wst.wsdl.asd.design.editparts.model.AbstractModelCollection;
import org.eclipse.wst.wsdl.asd.design.editparts.model.BindingColumn;
import org.eclipse.wst.wsdl.asd.facade.IBinding;
import org.eclipse.wst.wsdl.asd.facade.IBindingMessageReference;
import org.eclipse.wst.wsdl.asd.facade.IBindingOperation;
import org.eclipse.wst.wsdl.asd.facade.IDescription;
import org.eclipse.wst.wsdl.asd.facade.IEndPoint;
import org.eclipse.wst.wsdl.asd.facade.IInterface;
import org.eclipse.wst.wsdl.asd.facade.IMessageReference;
import org.eclipse.wst.wsdl.asd.facade.IOperation;
import org.eclipse.wst.wsdl.asd.facade.IParameter;
import org.eclipse.wst.wsdl.asd.facade.IService;

public class ASDEditPartFactory implements EditPartFactory
{
  public EditPart createEditPart(EditPart context, Object model)
  {
    EditPart child = null;
    if (model instanceof IDescription)
    {
      child = new DefinitionsEditPart();
    }
    else if (model instanceof AbstractModelCollection)
    {
      AbstractModelCollection collection = (AbstractModelCollection)model;
      if (collection instanceof BindingColumn)
      {
        child = new BindingColumnEditPart();
      }
      else
      {  
        child = new ColumnEditPart();
      }  
    }
    else if (model instanceof IEndPoint)
    {
      child = new EndPointEditPart();
    }
    else if (model instanceof IService)
    {
      child = new ServiceEditPart();
    }
    else if (model instanceof IBinding)
    {
      child = new BindingEditPart();
    }
    else if (model instanceof IBindingOperation ||
             model instanceof IBindingMessageReference)
    {
      child = new BindingContentEditPart();
    }  
    else if (model instanceof IInterface)
    {
      child = new InterfaceEditPart();
    }
    else if (model instanceof IMessageReference)
    {
      child = new MessageReferenceEditPart();
    }
    else if (model instanceof IOperation)
    {
      child = new OperationEditPart();
    }
    else if (model instanceof IParameter && context instanceof ParameterEditPart) {
    	child = new ParameterTypeEditPart();
    }
    else if (model instanceof IParameter && context instanceof MessageReferenceEditPart)
    {
      child = new ParameterEditPart();
    }
    if (child == null)
    {
      System.out.println("\nCould not create editpart for model: " + model);
      Thread.dumpStack();
    }
    Assert.isNotNull(child);
    child.setModel(model);
    return child;
  }
}
