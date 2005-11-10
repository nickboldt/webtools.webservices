/*******************************************************************************
 * Copyright (c) 2001, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.wst.wsdl.validation.internal.eclipse;

import java.util.Iterator;
import java.util.List;

import org.eclipse.wst.wsdl.validation.internal.resolver.IExtensibleURIResolver;

/**
 * An Eclipse WSDL validator. This validator is the default validator
 * used in the validation framework. There is only a single instance of
 * this validator. When created, this validator registers all extension
 * URI resolvers.
 */
public class WSDLValidator extends org.eclipse.wst.wsdl.validation.internal.WSDLValidator
{
	private static WSDLValidator instance = null;

	/**
	 * The constructor registers all of the URI resolvers defined via the
	 * WSDL URI resolver extension point with the WSDL validator. 
	 * 
	 */
	private WSDLValidator()
	{
    super();
	  URIResolverRegistryReader uriRR = new URIResolverRegistryReader();
	  List resolvers = uriRR.readRegistry();
	  Iterator resolverIter = resolvers.iterator();
	  while(resolverIter.hasNext())
	  {
	  	IExtensibleURIResolver resolver = (IExtensibleURIResolver)resolverIter.next();
	  	addURIResolver(resolver);
	  }
	}
	
	/**
	 * Get the one and only instance of this Eclipse WSDL validator.
	 * 
	 * @return The one and only instance of this Eclipse WSDL validator.
	 */
	public static WSDLValidator getInstance()
	{
		if(instance == null)
		{
			instance = new WSDLValidator();
		}
		return instance;
	}
	
//	/**
//	 * Validate the specified WSDL file.
//	 * 
//	 * @param fileURI The URI of the WSDL file.
//	 * @return A validation report with the validation results.
//	 */
//	public IValidationReport validate(String fileURI)
//	{
//		return wsdlValidator.validate(fileURI);
//	}
//  /**
//	 * Validate the given WSDL InputStream
//	 * 
//	 * @param fileURI The URI of the WSDL file.
//	 * @param inputStream the InputStream to validate
//	 * @return A validation report with the validation results.
//	 */
//	public IValidationReport validate(String fileURI, InputStream inputStream)
//  {
//    return wsdlValidator.validate(fileURI, inputStream);
//  }

}