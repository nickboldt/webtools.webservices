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
package org.eclipse.wst.wsdl.ui.internal.commands;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.wst.wsdl.Definition;
import org.eclipse.wst.wsdl.Message;
import org.eclipse.wst.wsdl.MessageReference;
import org.eclipse.wst.wsdl.Operation;
import org.eclipse.wst.wsdl.Part;
import org.eclipse.wst.wsdl.ui.internal.util.ComponentReferenceUtil;
import org.eclipse.wst.wsdl.ui.internal.util.NameUtil;
import org.eclipse.wst.wsdl.ui.internal.util.XSDComponentHelper;
import org.eclipse.xsd.XSDComplexTypeDefinition;
import org.eclipse.xsd.XSDElementDeclaration;
import org.eclipse.xsd.XSDModelGroup;
import org.eclipse.xsd.XSDNamedComponent;
import org.eclipse.xsd.XSDSchema;
import org.eclipse.xsd.XSDSimpleTypeDefinition;
import org.eclipse.xsd.XSDTypeDefinition;

public abstract class AddBaseParameterCommand {
	public static int PART_ELEMENT_SEQ_ELEMENT     = 0;
	public static int PART_ELEMENT                 = 1;
	public static int PART_COMPLEXTYPE_SEQ_ELEMENT = 2;
	public static int PART_COMPLEXTYPE             = 3;

	protected int style = 0;
	protected Operation operation;
	protected XSDElementDeclaration newXSDElement;
	
	protected String newAnonymousXSDElementName;
	protected String newXSDElementName;
	protected String newWSDLMessageName;
	protected String newWSDLPartName;
	
	public abstract void run();
	
	public AddBaseParameterCommand(Operation operation, int style) {
		this.operation = operation;
		this.style = style;
	}
	
	public void setStyle(int style) {
		this.style = style;
	}
	
	public XSDElementDeclaration getXSDElementDeclaration() {
		return newXSDElement;
	}
	
	protected boolean isPartElementReference() {
		if (style == PART_ELEMENT || style == PART_ELEMENT_SEQ_ELEMENT) {
			return true;
		}
		
		return false;
	}
	
	protected XSDElementDeclaration createPartElementReferenceComponents(Part part) {
		XSDElementDeclaration returnedXSDElement = null;
		XSDElementDeclaration partElement = part.getElementDeclaration();
		
		if (style == PART_ELEMENT_SEQ_ELEMENT) {
			XSDElementDeclaration originalElement = null;
			XSDElementDeclaration anonXSDElement = null;
			
			// Create the XSDElement (anonymous) referenced by the Part if necessary
			if (partElement == null || partElement.getAnonymousTypeDefinition() == null) {
				anonXSDElement = XSDComponentHelper.createAnonymousXSDElementDefinition(getAnonymousXSDElementBaseName(), part);
//				part.setElementDeclaration(anonXSDElement);
				String prefixedName = getPrefixedComponentName(part.getEnclosingDefinition(), anonXSDElement);
				ComponentReferenceUtil.setComponentReference(part, false, prefixedName);
				part.setTypeDefinition(null);
				
				if (partElement != null) {
					originalElement = partElement;
					// Remove the 'original' XSDElement as a Global Element
					partElement.getSchema().getContents().remove(partElement);
				}
			}
			else {
				anonXSDElement = partElement;
			}
			
			// Create a new XSDElement
			XSDModelGroup modelGroup = XSDComponentHelper.getXSDModelGroup(anonXSDElement, part.getEnclosingDefinition());
			returnedXSDElement = XSDComponentHelper.createXSDElementDeclarationCommand(null, getNewXSDElementBaseName(), modelGroup);
			
			// Add the newly created XSDElement to the ModelGroup
			XSDComponentHelper.addXSDElementToModelGroup(anonXSDElement, returnedXSDElement);
			
			// Add the 'original' XSDElement if it's type wasn't anonymous
			if (originalElement != null) {
				XSDComponentHelper.addXSDElementToModelGroup(anonXSDElement, originalElement);
			}
		}
		else if (style == PART_ELEMENT) {
			if (partElement == null) {
				returnedXSDElement = XSDComponentHelper.createXSDElementDeclarationCommand(part.getEnclosingDefinition(), getNewXSDElementBaseName(), part);
			}
			else {
				// TODO: What should we do here.....  We can default to the PART_ELEMENT_SEQ_ELEMENT style
				// since it handles 'multiple' XSDElements.... OR ..... we can 'overwrite and set a new
				// XSDElement
			}
			
			if (returnedXSDElement != null && !returnedXSDElement.equals(part.getElementDeclaration())) {
//				part.setElementDeclaration(returnedXSDElement);
				String prefixedName = getPrefixedComponentName(part.getEnclosingDefinition(), returnedXSDElement);
				ComponentReferenceUtil.setComponentReference(part, false, prefixedName);

			}	
		}
		
		return returnedXSDElement;
	}
	
	protected XSDElementDeclaration createPartComplexTypeReference(Part part) {
		XSDElementDeclaration returnedXSDElement = null;
		
		if (style == PART_COMPLEXTYPE_SEQ_ELEMENT) {
			XSDTypeDefinition originalType = null;
			XSDComplexTypeDefinition topLevelType = null;
			
			// Create the ComplexType referenced by the Part if necessary
			if (part.getTypeDefinition() == null || part.getTypeDefinition() instanceof XSDSimpleTypeDefinition) {
				XSDSchema schema = XSDComponentHelper.getXSDSchema(part.getEnclosingDefinition());
				String topLevelName = NameUtil.getXSDComplexTypeName(part.getName(), schema);
				topLevelType = XSDComponentHelper.createXSDComplexTypeDefiniion(topLevelName, part);
				
				if (part.getTypeDefinition() instanceof XSDSimpleTypeDefinition) {
					originalType = part.getTypeDefinition();
				}
			}
			else if (part.getTypeDefinition() instanceof XSDComplexTypeDefinition){
				topLevelType = (XSDComplexTypeDefinition) part.getTypeDefinition();
			}			
			
			// Create a new XSDElement
			XSDModelGroup modelGroup = XSDComponentHelper.getXSDModelGroup(topLevelType);
			returnedXSDElement = XSDComponentHelper.createXSDElementDeclarationCommand(null, getNewXSDElementBaseName(), modelGroup);
			
			// Add the 'original' XSDElement if it's type wasn't anonymous
			if (originalType != null) {
				// Create another new XSDElement to 'contain' the originally referenced XSDSimpleType
				XSDElementDeclaration origXSDElement = XSDComponentHelper.createXSDElementDeclarationCommand(null, getNewXSDElementBaseName(), modelGroup);
				origXSDElement.setTypeDefinition(originalType);
			}
			
			// Change Part reference
//			part.setTypeDefinition(topLevelType);
			String prefixedName = getPrefixedComponentName(part.getEnclosingDefinition(), topLevelType);
			ComponentReferenceUtil.setComponentReference(part, true, prefixedName);

			
		}
		else if (style == PART_COMPLEXTYPE) {
			XSDComplexTypeDefinition complexType = null;
			if (part.getTypeDefinition() == null) {
				// Create a new ComplexType
				XSDSchema schema = XSDComponentHelper.getXSDSchema(part.getEnclosingDefinition());
				String complexName = NameUtil.getXSDComplexTypeName(part.getName(), schema);
				complexType = XSDComponentHelper.createXSDComplexTypeDefiniion(complexName, part);
				
				// Create an XSDElement for the ComplexType
				XSDModelGroup xsdModelGroup = XSDComponentHelper.getXSDModelGroup(complexType);
				returnedXSDElement = XSDComponentHelper.createXSDElementDeclarationCommand(null, getNewXSDElementBaseName(), xsdModelGroup);
			}
			else {
				// TODO: What should we do here.....  We can default to the PART_ELEMENT_SEQ_ELEMENT style
				// since it handles 'multiple' XSDElements.... OR ..... we can 'overwrite and set a new
				// XSDElement
			}
			
			if (complexType != null && !complexType.equals(part.getTypeDefinition())) {
//				part.setTypeDefinition(complexType);
				String prefixedName = getPrefixedComponentName(part.getEnclosingDefinition(), complexType);
				ComponentReferenceUtil.setComponentReference(part, true, prefixedName);
			}	
		}
		
		return returnedXSDElement;
	}	
	/*
	 * Create if necessary a Message and Part for the given MessageReference
	 * and return it's Part
	 */
	protected Part createWSDLComponents(MessageReference messageRef) {
		Message message = messageRef.getEMessage();
		Part part = null;
		
		if (message == null) {
			// Create Message
			AddMessageCommand command = new AddMessageCommand(messageRef.getEnclosingDefinition(), getWSDLMessageName());
			command.run();
			message = (Message) command.getWSDLElement();
			messageRef.setEMessage(message);
		}
		
		if (message.getEParts().size() == 0) {
			// Create Part
			AddPartCommand command = new AddPartCommand(message, getWSDLPartName());
			command.run();
			part = (Part) command.getWSDLElement();
		}
		else {
			part = (Part) message.getEParts().get(0);
		}
		
		return part;
	}
	
	protected XSDElementDeclaration createXSDObjects(Part part) {
		XSDElementDeclaration returnedXSDElement = null;
		if (isPartElementReference()) {
			// Is a Part --> Element reference
			returnedXSDElement = createPartElementReferenceComponents(part);
		}
		else {
			// Is a Part --> Complex Type reference
			returnedXSDElement = createPartComplexTypeReference(part);
		}
		
		return returnedXSDElement;
	}
	
    private String getPrefixedComponentName(Definition definition, XSDNamedComponent component) {
    	String name = component.getName();
    	String tns = component.getTargetNamespace();
        List prefixes = getPrefixes(definition, tns);
        if (prefixes.size() > 0) {
            name = prefixes.get(0) + ":" + name;
        }
        
        return name;
    }
    
    private List getPrefixes(Definition definition, String namespace) {
        List list = new ArrayList();
        Map map = definition.getNamespaces();
        for (Iterator i = map.keySet().iterator(); i.hasNext();) {
            String prefix = (String) i.next();
            String theNamespace = (String) map.get(prefix);
            if (theNamespace != null && theNamespace.equals(namespace)) {
                list.add(prefix);
            }
        }
        return list;
    }

	protected abstract String getAnonymousXSDElementBaseName();
	protected abstract String getNewXSDElementBaseName();
	protected abstract String getWSDLMessageName();
	protected abstract String getWSDLPartName();
}