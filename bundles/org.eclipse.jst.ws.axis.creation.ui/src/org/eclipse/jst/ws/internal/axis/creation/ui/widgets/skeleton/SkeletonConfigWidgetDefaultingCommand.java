/*******************************************************************************
 * Copyright (c) 2004, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Corporation - initial API and implementation
 * yyyymmdd bug      Email and other contact information
 * -------- -------- -----------------------------------------------------------
 * 20060221   119111 rsinha@ca.ibm.com - Rupam Kuehner
 *******************************************************************************/
package org.eclipse.jst.ws.internal.axis.creation.ui.widgets.skeleton;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jst.ws.internal.axis.consumption.core.common.JavaWSDLParameter;
import org.eclipse.jst.ws.internal.axis.creation.ui.AxisCreationUIMessages;
import org.eclipse.jst.ws.internal.common.J2EEUtils;
import org.eclipse.jst.ws.internal.common.ResourceUtils;
import org.eclipse.jst.ws.internal.common.ServerUtils;
import org.eclipse.osgi.util.NLS;
import org.eclipse.wst.command.internal.env.core.common.StatusUtils;
import org.eclipse.wst.common.environment.IEnvironment;
import org.eclipse.wst.common.frameworks.datamodel.AbstractDataModelOperation;

public class SkeletonConfigWidgetDefaultingCommand extends AbstractDataModelOperation
{
  private String wsdlURI;
  private IProject serverProject;
  private JavaWSDLParameter javaWSDLParam;
  private String serviceServerTypeID_;

  public SkeletonConfigWidgetDefaultingCommand( )
  {
  }
  
	public IStatus execute( IProgressMonitor monitor, IAdaptable adaptable ) 
	{
		IEnvironment environment = getEnvironment();
		IStatus status = Status.OK_STATUS;
		
		String outputDir =	ResourceUtils.findResource(J2EEUtils.getWebInfPath( serverProject )).getLocation().toString();
		javaWSDLParam.setOutput( outputDir );
//		Do not base Java output directory on workspace root since the project could be not physically located in the workspace root
//		javaWSDLParam.setJavaOutput(getRootURL() + getOutputJavaFolder()); 
		String javaOutput =	ResourceUtils.findResource(getOutputJavaFolder()).getLocation().toString();
		javaWSDLParam.setJavaOutput(javaOutput);


    String projectURL = null;
	if (serviceServerTypeID_ != null && serviceServerTypeID_.length()>0)
    {
	  projectURL = ServerUtils.getEncodedWebComponentURL(serverProject, serviceServerTypeID_);
    }
    else
    {
      projectURL = "http://tempuri.org/";
    }
	
	if (projectURL == null) {
	    status = StatusUtils.errorStatus(NLS.bind(AxisCreationUIMessages.MSG_ERROR_PROJECT_URL, new String[] { serverProject.toString()}));
	    environment.getStatusHandler().reportError(status);
	    return status;		  
	} else {
		javaWSDLParam.setProjectURL(projectURL);
	}
	
    return Status.OK_STATUS;
    
  }
  
  public void setWsdlURI(String wsdlURI)
  {
    this.wsdlURI = wsdlURI;
  }
  
  public void setServerProject(String serverProject)
  {
    this.serverProject = ResourcesPlugin.getWorkspace().getRoot().getProject(serverProject);
  }

  public String getEndpointURI()
  {
    return null;
  }
  
  public String getOutputWSDLFolder()
  {
	  IPath wsdlPath = J2EEUtils.getWebContentPath(serverProject ).append("wsdl");
      return wsdlPath.toString();
  }
  
  public String getOutputWSDLFile()
  {
    int index = wsdlURI.lastIndexOf('/');
    if (index == -1)
      index = wsdlURI.lastIndexOf('\\');
    return wsdlURI.substring(index+1, wsdlURI.length());
  }
  
  public String getOutputJavaFolder()
  {
    if (serverProject!=null){
      return ResourceUtils.getJavaSourceLocation(serverProject).toString();
    }
    return null;
  }

  public boolean getShowMapping()
  {
    return false;
  }
  
  public boolean isShowMapping()
  {
    return getShowMapping();
  }
  
  public void setJavaWSDLParam(JavaWSDLParameter javaWSDLParam)
  {
  	this.javaWSDLParam = javaWSDLParam;
  }
  
  public JavaWSDLParameter getJavaWSDLParam()
  {
    return javaWSDLParam;
  }

public String getServiceServerTypeID() {
	return serviceServerTypeID_;
}

public void setServiceServerTypeID(String serviceServerTypeID) {
	this.serviceServerTypeID_ = serviceServerTypeID;
}
}
