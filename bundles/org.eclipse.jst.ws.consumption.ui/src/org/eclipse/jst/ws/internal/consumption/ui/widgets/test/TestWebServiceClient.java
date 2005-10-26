package org.eclipse.jst.ws.internal.consumption.ui.widgets.test;

import org.eclipse.wst.command.internal.env.core.ICommandFactory;
import org.eclipse.wst.common.environment.IEnvironment;
import org.eclipse.wst.ws.internal.wsrt.AbstractWebServiceClient;
import org.eclipse.wst.ws.internal.wsrt.IContext;
import org.eclipse.wst.ws.internal.wsrt.ISelection;
import org.eclipse.wst.ws.internal.wsrt.WebServiceClientInfo;

public class TestWebServiceClient extends AbstractWebServiceClient {
  
  public TestWebServiceClient(WebServiceClientInfo clientInfo){
	super(clientInfo);  
  }
  
  public ICommandFactory assemble(IEnvironment env, IContext ctx, ISelection sel,
      String project, String module, String earProject, String ear){
    return null;	  
  }
			
  public ICommandFactory deploy(IEnvironment env, IContext ctx, ISelection sel,
      String project, String module, String earProject, String ear){
    return null;	  
  }
			

  public ICommandFactory develop(IEnvironment env, IContext ctx, ISelection sel,
		      String project, String module, String earProject, String ear){
    return null;	  
  }
				
  public ICommandFactory install(IEnvironment env, IContext ctx, ISelection sel,
		      String project, String module, String earProject, String ear){
    return null;
  }
			

  public ICommandFactory run(IEnvironment env, IContext ctx, ISelection sel,
		      String project, String module, String earProject, String ear){
    return null;	  
  }
  
}
