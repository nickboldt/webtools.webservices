/*******************************************************************************
 * Copyright (c) 2001, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.wst.ws.parser.wsil;

public class IllegalArgumentsException extends Exception
{
  private static final long serialVersionUID = -2533981176285561234L;

  public IllegalArgumentsException(String arg)
  {
    super(arg);
  }
}
