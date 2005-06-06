package org.eclipse.wst.wsdl.tests.performance;

import java.io.File;
import java.io.FileFilter;
import java.net.MalformedURLException;
import javax.wsdl.WSDLException;
import junit.framework.Assert;
import org.eclipse.test.performance.Dimension;
import org.eclipse.test.performance.PerformanceTestCase;
import org.eclipse.wst.ws.internal.ui.plugin.WSUIPlugin;
import org.eclipse.wst.ws.internal.ui.wsi.preferences.PersistentWSIContext;
import org.eclipse.wst.wsdl.validation.internal.IValidationReport;
import org.eclipse.wst.wsdl.validation.internal.ui.eclipse.WSDLValidator;

public class ValidateOAGISWSITestcase extends PerformanceTestCase
{
  private WSDLValidator validator;
  
  /* (non-Javadoc)
   * @see junit.framework.TestCase#setUp()
   */
  protected void setUp() throws Exception 
  {
    super.setUp();
    // Set the WS-I preference to ignore so only WSDL errors will be tested.
    WSUIPlugin wsui = WSUIPlugin.getInstance();
    PersistentWSIContext wsicontext = wsui.getWSISSBPContext();
    wsicontext.updateWSICompliances(PersistentWSIContext.STOP_NON_WSI);
    wsicontext = wsui.getWSIAPContext();
    wsicontext.updateWSICompliances(PersistentWSIContext.STOP_NON_WSI);
  } 

  public void testValidateWSDL() throws MalformedURLException, WSDLException
  {
    String oagis80Dir = System.getProperty("oagis80Dir");
    Assert.assertNotNull(oagis80Dir);
    if (!oagis80Dir.endsWith("/") && !oagis80Dir.endsWith("\\"))
      oagis80Dir = oagis80Dir + "/";
    File dir = new File(oagis80Dir + "OAGIS8.0/ws/wsdl");
    if (dir.exists() && dir.isDirectory())
    {
      File[] wsdls = dir.listFiles
      (
        new FileFilter()
        {
          public boolean accept(File pathname)
          {
            return pathname.getName().endsWith(".wsdl");
          }
        }
      );
      tagAsSummary("Validate OAGIS WSDL with WS-I", new Dimension[] {Dimension.ELAPSED_PROCESS, Dimension.WORKING_SET});
	  validator = WSDLValidator.getInstance();
	  startMeasuring();
      for (int i = 0; i < wsdls.length; i++)
        validateWSDL(wsdls[i].toURL().toString());
      stopMeasuring();
      commitMeasurements();
      assertPerformance();
    }
    else
      fail(dir.toString());
  }

  private void validateWSDL(String location) throws WSDLException
  {
    IValidationReport valreport = validator.validate(location);
  }
}
