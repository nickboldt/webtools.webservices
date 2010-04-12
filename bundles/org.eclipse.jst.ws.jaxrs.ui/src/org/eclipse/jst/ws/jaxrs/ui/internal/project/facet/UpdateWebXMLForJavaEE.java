/*******************************************************************************
 * Copyright (c) 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Corporation - initial API and implementation
 * yyyymmdd bug      Email and other contact information
 * -------- -------- -----------------------------------------------------------
 * 20100303   291954 kchong@ca.ibm.com - Keith Chong, JAX-RS: Implement JAX-RS Facet
 * 20100408   308565 kchong@ca.ibm.com - Keith Chong, JAX-RS: Servlet name and class not updated
 *******************************************************************************/
package org.eclipse.jst.ws.jaxrs.ui.internal.project.facet;

import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.jst.j2ee.model.ModelProviderManager;
import org.eclipse.jst.javaee.web.Servlet;
import org.eclipse.jst.javaee.web.ServletMapping;
import org.eclipse.jst.javaee.web.WebApp;
import org.eclipse.jst.ws.jaxrs.core.internal.project.facet.JAXRSJEEUtils;

public class UpdateWebXMLForJavaEE extends UpdateWebXMLBase implements Runnable {
	private IProject project;
	private String newServletName, newServletClass;
	private List<String> listOfMappings;

	/**
	 * @deprecated
	 * @param project
	 * @param servlet
	 * @param servletMapping
	 * @param newServletName
	 * @param newServletClass
	 * @param listOfMappings
	 */
	public UpdateWebXMLForJavaEE(final IProject project, Servlet servlet, ServletMapping servletMapping, String newServletName, String newServletClass, List<String> listOfMappings) {
		this.project = project;
		this.newServletName = newServletName;
		this.newServletClass = newServletClass;
		this.listOfMappings = listOfMappings;
	}

	public UpdateWebXMLForJavaEE(final IProject project, String newServletName, String newServletClass, List<String> listOfMappings) {
		this.project = project;
		this.newServletName = newServletName;
		this.newServletClass = newServletClass;
		this.listOfMappings = listOfMappings;
	}
	
	public void run() {
		WebApp webApp = (WebApp) ModelProviderManager.getModelProvider(project).getModelObject();
		Servlet servlet = JAXRSJEEUtils.findJAXRSServlet(webApp);
		if (servlet != null) {
	  	    JAXRSJEEUtils.createOrUpdateServletRef(webApp, this.newServletName, this.newServletClass, servlet);
			JAXRSJEEUtils.updateURLMappings(webApp, listOfMappings, servlet);
		}
	}
}
