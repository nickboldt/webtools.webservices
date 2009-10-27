/*******************************************************************************
 * Copyright (c) 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Corporation - initial API and implementation
 * yyyymmdd bug      Email and other contact information
 * -------- -------- -----------------------------------------------------------
 * 20091021   291954 ericdp@ca.ibm.com - Eric D. Peters, JAX-RS: Implement JAX-RS Facet
 *******************************************************************************/
package org.eclipse.jst.ws.jaxrs.core.internal.project.facet;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IClasspathAttribute;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jst.j2ee.classpathdep.ClasspathDependencyUtil;
import org.eclipse.jst.j2ee.classpathdep.IClasspathDependencyConstants;
import org.eclipse.jst.j2ee.model.IModelProvider;
import org.eclipse.jst.j2ee.model.ModelProviderManager;
import org.eclipse.jst.javaee.web.Servlet;
import org.eclipse.jst.javaee.web.WebApp;
import org.eclipse.jst.ws.jaxrs.core.internal.JAXRSCorePlugin;
import org.eclipse.jst.ws.jaxrs.core.internal.Messages;
import org.eclipse.jst.ws.jaxrs.core.internal.jaxrslibraryconfig.JAXRSLibraryInternalReference;
import org.eclipse.jst.ws.jaxrs.core.internal.jaxrslibraryconfig.JAXRSLibraryRegistryUtil;
import org.eclipse.jst.ws.jaxrs.core.internal.jaxrssharedlibraryconfig.SharedLibraryConfigurator;
import org.eclipse.jst.ws.jaxrs.core.internal.jaxrssharedlibraryconfig.SharedLibraryConfiguratorUtil;
import org.eclipse.jst.ws.jaxrs.core.jaxrslibraryconfiguration.JAXRSLibraryConfigurationHelper;
import org.eclipse.osgi.util.NLS;
import org.eclipse.wst.common.frameworks.datamodel.IDataModel;
import org.eclipse.wst.common.project.facet.core.IDelegate;
import org.eclipse.wst.common.project.facet.core.IProjectFacetVersion;

/**
 * JAXRS Facet Install Delegate for WTP faceted web projects. Deals with 2.3,
 * 2.4 and 2.5 web app models.
 * 
 * Uses
 * <code>com.eclispe.jst.ws.jaxrs.core.internal.project.facet.JAXRSFacetInstallDataModelProvider<code> for model
 * 	 <li> updates web.xml for: servlet, servlet-mapping and context-param
 * 	 <li> adds implementation jars to WEB-INF/lib if user requests
 * 
 * @see org.eclipse.jst.ws.jaxrs.core.internal.project.facet.JAXRSFacetInstallDataModelProvider
 * @since 1.0
 */
public final class JAXRSFacetInstallDelegate implements IDelegate {

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.wst.common.project.facet.core.IDelegate#execute(org.eclipse
	 * .core.resources.IProject,
	 * org.eclipse.wst.common.project.facet.core.IProjectFacetVersion,
	 * java.lang.Object, org.eclipse.core.runtime.IProgressMonitor)
	 */
	public void execute(final IProject project, final IProjectFacetVersion fv,
			final Object cfg, final IProgressMonitor monitor)
			throws CoreException

	{

		if (monitor != null) {
			monitor.beginTask("", 1); //$NON-NLS-1$
		}

		try {
			IDataModel config = null;

			if (cfg != null) {
				config = (IDataModel) cfg;
			} else {
				throw new JAXRSFacetException(
						Messages.JAXRSFacetInstallDelegate_InternalErr);
			}

			// Before we do any configuration, verify that web.xml is available
			// for update
			IModelProvider provider = JAXRSUtils.getModelProvider(project);
			if (provider == null) {
				throw new JAXRSFacetException(NLS.bind(
						Messages.JAXRSFacetInstallDelegate_ConfigErr, project
								.getName()));
			} else if (!(provider.validateEdit(null, null).isOK())) {
				// checks for web.xml file being read-only and allows user to set writeable
				if (!(provider.validateEdit(null, null).isOK())) {
					throw new JAXRSFacetException(
							NLS
									.bind(
											Messages.JAXRSFacetInstallDelegate_NonUpdateableWebXML,
											project.getName())); //$NON-NLS-2$
				}
			}

			
//			if (Boolean.parseBoolean((config.getProperty(IJAXRSFacetInstallDataModelProperties.SHAREDLIBRARY).toString())))
//				// Create JAXRS libraries as shared library entries
//				createSharedLibraries(project, fv, monitor, config);
//			
//			// Create JAXRS libraries as as classpath entries (as a deploy / publish dependency if "Deploy" selected)
//			createClasspathEntries(project, fv, config, monitor);
				

			// Update web model
			createServletAndModifyWebXML(project, config, monitor);

			if (monitor != null) {
				monitor.worked(1);
			}

		} finally {
			if (monitor != null) {
				monitor.done();
			}
		}
	}

	private void createSharedLibraries(IProject project,
			IProjectFacetVersion fv, IProgressMonitor monitor, IDataModel config) throws CoreException {

		String targetRuntimeID = config
				.getStringProperty(IJAXRSFacetInstallDataModelProperties.TARGETRUNTIME);
		JAXRSLibraryInternalReference libref = (JAXRSLibraryInternalReference) config
				.getProperty(IJAXRSFacetInstallDataModelProperties.IMPLEMENTATION);

		SharedLibraryConfiguratorUtil
				.getInstance();
		java.util.List<SharedLibraryConfigurator> configurators = SharedLibraryConfiguratorUtil.getConfigurators();

		Iterator<SharedLibraryConfigurator> sharedLibConfiguratorIterator = configurators
				.iterator();
		while (sharedLibConfiguratorIterator.hasNext()) {
			SharedLibraryConfigurator thisConfigurator = sharedLibConfiguratorIterator
					.next();
			if (targetRuntimeID.equals(thisConfigurator.getRuntimeID())) {
				IProject earProject = getEARProject(config);
				Boolean addToEar = getAddToEar(config);
				if (thisConfigurator.getIsSharedLibSupported(project,
						earProject, addToEar, libref.getID())) {
					thisConfigurator.installSharedLibs(project, earProject,
							monitor, libref.getID());
					break;
				}
			}

		}
	}

	/**
	 * Adds the JAXRS Library references specified in the wizard to the project
	 * as classpath containers. Marks the containers as J2EE module dependencies
	 * as required
	 * 
	 * @param project
	 * @param config
	 * @param monitor
	 */
	private void createClasspathEntries(final IProject project,
			final IProjectFacetVersion fv, final IDataModel config,
			final IProgressMonitor monitor) {
		IJavaProject javaProject = JavaCore.create(project);
		List<IClasspathEntry> cpEntries = new ArrayList<IClasspathEntry>();
		try {
			for (int i = 0; i < javaProject.getRawClasspath().length; i++) {
				cpEntries.add(javaProject.getRawClasspath()[i]);
			}
		} catch (JavaModelException e) {
			JAXRSCorePlugin.log(e, "Unable to read classpath"); //$NON-NLS-1$
		}

		IPath path, cp = null;
		IClasspathEntry entry = null;
		JAXRSLibraryInternalReference libref = null;

		cp = new Path(
				JAXRSLibraryConfigurationHelper.JAXRS_LIBRARY_CP_CONTAINER_ID);
		libref = (JAXRSLibraryInternalReference) config
				.getProperty(IJAXRSFacetInstallDataModelProperties.IMPLEMENTATION);
		path = cp.append(new Path(libref.getID()));
		entry = getNewCPEntry(path, libref);
		cpEntries.add(entry);
		JAXRSLibraryRegistryUtil.setRawClasspath(javaProject, cpEntries,
				monitor);

	}

	/**
	 * @param path
	 * @param lib
	 * @return creates new IClasspathEntry with WTP dependency attribute set, if
	 *         required
	 */
	private IClasspathEntry getNewCPEntry(final IPath path,
			final JAXRSLibraryInternalReference lib) {

		IClasspathEntry entry = null;
		if (lib.isCheckedToBeDeployed()) {
			IClasspathAttribute depAttrib = JavaCore
					.newClasspathAttribute(
							IClasspathDependencyConstants.CLASSPATH_COMPONENT_DEPENDENCY,
							ClasspathDependencyUtil.getDefaultRuntimePath(true)
									.toString());
			entry = JavaCore.newContainerEntry(path, null,
					new IClasspathAttribute[] { depAttrib }, true);
		} else {
			entry = JavaCore.newContainerEntry(path);
		}

		return entry;
	}

	/**
	 * @param config
	 * @return list of URL patterns from the datamodel
	 */
	private List<String> getServletMappings(final IDataModel config) {
		List<String> mappings = new ArrayList<String>();
		String[] patterns = (String[]) config
				.getProperty(IJAXRSFacetInstallDataModelProperties.SERVLET_URL_PATTERNS);
		for (int i = 0; i < patterns.length; i++) {
			String pattern = patterns[i];
			mappings.add(pattern);
		}

		return mappings;
	}

	/**
	 * Create servlet and URL mappings and update the webapp
	 * 
	 * @param project
	 * @param config
	 * @param monitor
	 */
	private void createServletAndModifyWebXML(final IProject project,
			final IDataModel config, final IProgressMonitor monitor) {

		IModelProvider provider = JAXRSUtils.getModelProvider(project);
		IPath webXMLPath = new Path("WEB-INF").append("web.xml"); //$NON-NLS-1$ //$NON-NLS-2$
		if (JAXRSJEEUtils.isWebApp25(provider.getModelObject())) {
			provider.modify(new UpdateWebXMLForJavaEE(project, config),
					doesDDFileExist(project, webXMLPath) ? webXMLPath
							: IModelProvider.FORCESAVE);
		} else {// must be 2.3 or 2.4
			provider.modify(new UpdateWebXMLForJ2EE(project, config),
					webXMLPath);
		}

	}

	private boolean doesDDFileExist(IProject project, IPath webXMLPath) {
		return project.getProjectRelativePath().append(webXMLPath).toFile()
				.exists();
	}

	private IProject getEARProject(IDataModel config) {
		String projName = config.getStringProperty(IJAXRSFacetInstallDataModelProperties.EARPROJECT_NAME);
		if (projName == null || "".equals(projName))
			return null;
	
		IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(
				projName);
		return project;
	}

	private boolean getAddToEar(IDataModel config) {
		return  config.getBooleanProperty(IJAXRSFacetInstallDataModelProperties.ADD_TO_EAR);
	}

	private class UpdateWebXMLForJavaEE implements Runnable {
		private IProject project;
		private IDataModel config;

		UpdateWebXMLForJavaEE(final IProject project, final IDataModel config) {
			this.project = project;
			this.config = config;
		}

		public void run() {
			WebApp webApp = (WebApp) ModelProviderManager.getModelProvider(
					project).getModelObject();
			// create or update servlet ref
			Servlet servlet = JAXRSJEEUtils.findJAXRSServlet(webApp);// check to
																		// see
			// if already

			servlet = JAXRSJEEUtils.createOrUpdateServletRef(webApp, config,
					servlet);

			// init mappings
			List<String> listOfMappings = getServletMappings(config);
			JAXRSJEEUtils.setUpURLMappings(webApp, listOfMappings, servlet);

		}
	}

	private class UpdateWebXMLForJ2EE implements Runnable {
		private IProject project;
		private IDataModel config;

		UpdateWebXMLForJ2EE(IProject project, final IDataModel config) {
			this.project = project;
			this.config = config;
		}

		public void run() {
			org.eclipse.jst.j2ee.webapplication.WebApp webApp = (org.eclipse.jst.j2ee.webapplication.WebApp) ModelProviderManager
					.getModelProvider(project).getModelObject();
			// create or update servlet ref
			org.eclipse.jst.j2ee.webapplication.Servlet servlet = JAXRSJ2EEUtils
					.findJAXRSServlet(webApp);// check to see
			// if already
			// present

			servlet = JAXRSJ2EEUtils.createOrUpdateServletRef(webApp, config,
					servlet);

			// init mappings
			List<String> listOfMappings = getServletMappings(config);
			JAXRSJ2EEUtils.setUpURLMappings(webApp, listOfMappings, servlet);

		}

	}

}