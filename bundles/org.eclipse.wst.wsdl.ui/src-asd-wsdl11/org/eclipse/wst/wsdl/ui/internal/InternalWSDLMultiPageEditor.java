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
package org.eclipse.wst.wsdl.ui.internal;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.Assert;
import org.eclipse.emf.common.notify.Adapter;
import org.eclipse.emf.common.notify.Notifier;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.gef.ui.actions.ActionRegistry;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.viewers.IPostSelectionProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.wst.sse.core.StructuredModelManager;
import org.eclipse.wst.sse.core.internal.provisional.INodeNotifier;
import org.eclipse.wst.sse.core.internal.provisional.IStructuredModel;
import org.eclipse.wst.sse.core.internal.provisional.text.IStructuredDocument;
import org.eclipse.wst.wsdl.Definition;
import org.eclipse.wst.wsdl.ui.internal.adapters.WSDLBaseAdapter;
import org.eclipse.wst.wsdl.ui.internal.adapters.actions.W11AddPartAction;
import org.eclipse.wst.wsdl.ui.internal.adapters.actions.W11SetExistingElementAction;
import org.eclipse.wst.wsdl.ui.internal.adapters.actions.W11SetExistingMessageAction;
import org.eclipse.wst.wsdl.ui.internal.adapters.actions.W11SetExistingTypeAction;
import org.eclipse.wst.wsdl.ui.internal.adapters.actions.W11SetNewElementAction;
import org.eclipse.wst.wsdl.ui.internal.adapters.actions.W11SetNewMessageAction;
import org.eclipse.wst.wsdl.ui.internal.adapters.actions.W11SetNewTypeAction;
import org.eclipse.wst.wsdl.ui.internal.adapters.basic.W11Description;
import org.eclipse.wst.wsdl.ui.internal.adapters.basic.W11Type;
import org.eclipse.wst.wsdl.ui.internal.asd.ASDMultiPageEditor;
import org.eclipse.wst.wsdl.ui.internal.asd.actions.ASDAddMessageAction;
import org.eclipse.wst.wsdl.ui.internal.asd.actions.BaseSelectionAction;
import org.eclipse.wst.wsdl.ui.internal.asd.facade.IDescription;
import org.eclipse.wst.wsdl.ui.internal.asd.util.ASDEditPartFactoryHelper;
import org.eclipse.wst.wsdl.ui.internal.asd.util.IOpenExternalEditorHelper;
import org.eclipse.wst.wsdl.ui.internal.edit.W11BindingReferenceEditManager;
import org.eclipse.wst.wsdl.ui.internal.edit.W11InterfaceReferenceEditManager;
import org.eclipse.wst.wsdl.ui.internal.edit.W11MessageReferenceEditManager;
import org.eclipse.wst.wsdl.ui.internal.edit.WSDLXSDElementReferenceEditManager;
import org.eclipse.wst.wsdl.ui.internal.edit.WSDLXSDTypeReferenceEditManager;
import org.eclipse.wst.wsdl.ui.internal.text.WSDLModelAdapter;
import org.eclipse.wst.wsdl.ui.internal.util.ComponentReferenceUtil;
import org.eclipse.wst.wsdl.ui.internal.util.W11OpenExternalEditorHelper;
import org.eclipse.wst.wsdl.ui.internal.util.WSDLAdapterFactoryHelper;
import org.eclipse.wst.wsdl.ui.internal.util.WSDLEditorUtil;
import org.eclipse.wst.wsdl.ui.internal.util.WSDLResourceUtil;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMModel;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMNode;
import org.eclipse.wst.xsd.ui.internal.editor.XSDElementReferenceEditManager;
import org.eclipse.wst.xsd.ui.internal.editor.XSDTypeReferenceEditManager;
import org.eclipse.xsd.XSDSchema;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;


public class InternalWSDLMultiPageEditor extends ASDMultiPageEditor
{
	ResourceSet resourceSet;
	Resource wsdlResource;
	
	protected WSDLEditorResourceChangeHandler resourceChangeHandler;
	
	protected WSDLModelAdapter modelAdapter;
	protected SourceEditorSelectionListener fSourceEditorSelectionListener;
	protected WSDLSelectionManagerSelectionListener fWSDLSelectionListener;

  private IStructuredModel structuredModel;
  
  public IDescription buildModel(IFileEditorInput editorInput) {   
	  try {
		  // ISSUE: This code which deals with the structured model is similar to the one in the XSD editor. 
		  // It could be refactored into the base class.

		  Document document = null;
		  IDocument doc = structuredTextEditor.getDocumentProvider().getDocument(editorInput);
		  if (doc instanceof IStructuredDocument) {
			  IStructuredModel model = StructuredModelManager.getModelManager().getExistingModelForEdit(doc);
			  if (model == null) {
				  model = StructuredModelManager.getModelManager().getModelForEdit((IStructuredDocument) doc);
			  }
			  structuredModel = model;
			  document = ((IDOMModel) model).getDocument();
		  }
		  Assert.isNotNull(document);

		  Object obj = null;

		  if (document instanceof INodeNotifier) {
			  INodeNotifier notifier = (INodeNotifier) document;
			  modelAdapter = (WSDLModelAdapter) notifier.getAdapterFor(WSDLModelAdapter.class);
			  if (modelAdapter == null) {
				  modelAdapter = new WSDLModelAdapter();
				  notifier.addAdapter(modelAdapter);
				  obj = modelAdapter.createDefinition(document.getDocumentElement(), document);
			  }
			  if (obj == null) {
				  obj = modelAdapter.createDefinition(document.getDocumentElement(), document);
			  }
		  }

		  if (obj instanceof Definition) {
			  Definition definition = (Definition) obj;
			  model = (IDescription) WSDLAdapterFactoryHelper.getInstance().adapt(definition);
		  }
	  }
	  catch (Exception e) {
		  e.printStackTrace();
	  }    

	  return model;
  }
	
	private XSDSchema[] getInlineSchemas() {
		List types = getModel().getTypes();
		XSDSchema[] schemas = new XSDSchema[types.size()];
		for (int index = 0; index < types.size(); index++) {
			W11Type type = (W11Type) types.get(index);
			schemas[index] = (XSDSchema) type.getTarget();
		}
		
		return schemas;
	}
	
	public Object getAdapter(Class type) {
		if (type == Definition.class)
		{
			return ((W11Description) getModel()).getTarget();
		}
		else if (type == ISelectionMapper.class)
		{
			return new WSDLSelectionMapper();
		}
        else if (type == Definition.class && model instanceof Adapter)
        {
          return ((Adapter)model).getTarget(); 
        }
        else if (type == XSDTypeReferenceEditManager.class)
        {
          IEditorInput editorInput = getEditorInput();
          if (editorInput instanceof IFileEditorInput)
          {
            IFileEditorInput fileEditorInput = (IFileEditorInput) editorInput;
            WSDLXSDTypeReferenceEditManager refManager = new WSDLXSDTypeReferenceEditManager(fileEditorInput.getFile(), null);
            refManager.setSchemas(getInlineSchemas());
            return refManager;
          }
        }
        else if (type == XSDElementReferenceEditManager.class)
        {
          IEditorInput editorInput = getEditorInput();
          if (editorInput instanceof IFileEditorInput)
          {
            IFileEditorInput fileEditorInput = (IFileEditorInput) editorInput;
            WSDLXSDElementReferenceEditManager refManager = new WSDLXSDElementReferenceEditManager(fileEditorInput.getFile(), null);
            refManager.setSchemas(getInlineSchemas());
            return refManager;
          }
        }
        else if (type == W11BindingReferenceEditManager.class) {
            IEditorInput editorInput = getEditorInput();
            if (editorInput instanceof IFileEditorInput)
            {
            	IFileEditorInput fileEditorInput = (IFileEditorInput) editorInput;
            	return new W11BindingReferenceEditManager((W11Description) getModel(), fileEditorInput.getFile());
            }
        }
        else if (type == W11InterfaceReferenceEditManager.class) {
            IEditorInput editorInput = getEditorInput();
            if (editorInput instanceof IFileEditorInput)
            {
            	IFileEditorInput fileEditorInput = (IFileEditorInput) editorInput;
            	return new W11InterfaceReferenceEditManager((W11Description) getModel(), fileEditorInput.getFile());
            }        	
        }
        else if (type == W11MessageReferenceEditManager.class) {
            IEditorInput editorInput = getEditorInput();
            if (editorInput instanceof IFileEditorInput)
            {
            	IFileEditorInput fileEditorInput = (IFileEditorInput) editorInput;
            	return new W11MessageReferenceEditManager((W11Description) getModel(), fileEditorInput.getFile());
            }        	
        }
		
		return super.getAdapter(type);
	}
	
	/**
	 * Listener on SSE's source editor's selections that converts DOM
	 * selections into xsd selections and notifies WSDL selection manager
	 */
	private class SourceEditorSelectionListener implements ISelectionChangedListener {
		/**
		 * Determines WSDL node based on object (DOM node)
		 * 
		 * @param object
		 * @return
		 */
		private Object getWSDLNode(Object object) {
			// get the element node
			Element element = null;
			if (object instanceof Node) {
				Node node = (Node) object;
				if (node != null) {
					if (node.getNodeType() == Node.ELEMENT_NODE) {
						element = (Element) node;
					}
					else if (node.getNodeType() == Node.ATTRIBUTE_NODE) {
						element = ((Attr) node).getOwnerElement();
					}
				}
			}
			Object o = element;
			if (element != null) {
				Definition def = (Definition) ((W11Description) model).getTarget();
				Object modelObject = WSDLEditorUtil.getInstance().findModelObjectForElement(def, element);
				if (modelObject != null) {
					o = WSDLAdapterFactoryHelper.getInstance().adapt((Notifier) modelObject);
				}
			}
			return o;
		}
		
		public void selectionChanged(SelectionChangedEvent event) {
			if (getSelectionManager().getEnableNotify() && getActivePage() == 1)
			{
				ISelection selection = event.getSelection();
				if (selection instanceof IStructuredSelection)
				{
					List selections = new ArrayList();
					for (Iterator i = ((IStructuredSelection) selection).iterator(); i.hasNext();)
					{
						Object domNode = i.next();
						Object node = getWSDLNode(domNode);
						if (node != null)
						{
							selections.add(node);
						}
					}
					
					if (!selections.isEmpty())
					{
						StructuredSelection wsdlSelection = new StructuredSelection(selections);
						getSelectionManager().setSelection(wsdlSelection, getTextEditor().getSelectionProvider());
					}
				}
			}
		}
	}
	
	/**
	 * Listener on WSDL's selection manager's selections that converts WSDL
	 * selections into DOM selections and notifies SSE's selection provider
	 */
	private class WSDLSelectionManagerSelectionListener implements ISelectionChangedListener {
		/**
		 * Determines DOM node based on object (wsdl node)
		 * 
		 * @param object
		 * @return
		 */
		private Object getObjectForOtherModel(Object object) {
			Node node = null;
			
			if (object instanceof Node) {
				node = (Node) object;
			}
			else if (object instanceof String) {
				// The string is expected to be a URI fragment used to identify a WSDL element.
				// The URI fragment should be relative to the definition being edited in this editor.
				String uriFragment = (String)object;
				EObject definition = ((Definition)((W11Description)getModel()).getTarget());
				EObject modelObject = definition.eResource().getEObject(uriFragment);

				if (modelObject != null) {
					node = WSDLEditorUtil.getInstance().getNodeForObject(modelObject);
				}        
			}
			else {
				node = WSDLEditorUtil.getInstance().getNodeForObject(object);
			}
			
			// the text editor can only accept sed nodes!
			//
			if (!(node instanceof IDOMNode)) {
				node = null;
			}
			return node;
		}
		
		public void selectionChanged(SelectionChangedEvent event) {
			// do not fire selection in source editor if selection event came
			// from source editor
			if (event.getSource() != getTextEditor().getSelectionProvider()) {
				ISelection selection = event.getSelection();
				if (selection instanceof IStructuredSelection) {
					List otherModelObjectList = new ArrayList();
					for (Iterator i = ((IStructuredSelection) selection).iterator(); i.hasNext();) {
						Object wsdlObject = i.next();

						if (wsdlObject instanceof WSDLBaseAdapter) {
							wsdlObject = ((WSDLBaseAdapter) wsdlObject).getTarget();
						}

						Object otherModelObject = getObjectForOtherModel(wsdlObject);
						if (otherModelObject != null) {
							otherModelObjectList.add(otherModelObject);
						}
					}
					if (!otherModelObjectList.isEmpty()) {
						StructuredSelection nodeSelection = new StructuredSelection(otherModelObjectList);
						getTextEditor().getSelectionProvider().setSelection(nodeSelection);
					}
				}
			}
		}
	}
	
	protected void configureGraphicalViewer() {
		super.configureGraphicalViewer();
		setEditPartFactory(ASDEditPartFactoryHelper.getInstance().getEditPartFactory());
	}
	
	protected void createPages() {
		super.createPages();
		
		if (resourceChangeHandler == null) {
			resourceChangeHandler = new WSDLEditorResourceChangeHandler(this);
			resourceChangeHandler.attach();
		}
		
		fSourceEditorSelectionListener = new SourceEditorSelectionListener();
		ISelectionProvider provider = getTextEditor().getSelectionProvider();
		if (provider instanceof IPostSelectionProvider) {
			((IPostSelectionProvider) provider).addPostSelectionChangedListener(fSourceEditorSelectionListener);
		}
		else {
			provider.addSelectionChangedListener(fSourceEditorSelectionListener);
		}
		
		fWSDLSelectionListener = new WSDLSelectionManagerSelectionListener();
		getSelectionManager().addSelectionChangedListener(fWSDLSelectionListener);
	}
	
	public void dispose() {
		if (structuredModel != null) {
			structuredModel.releaseFromEdit();      
		}
		if (resourceChangeHandler != null) {
			resourceChangeHandler.dispose();
		}
		getSelectionManager().removeSelectionChangedListener(fWSDLSelectionListener);
		super.dispose();
	}
	
	public void reloadDependencies() {
		try {
			Definition definition = (Definition) ((W11Description) getModel()).getTarget();
			if (definition != null) {
				WSDLResourceUtil.reloadDirectives(definition);
				ComponentReferenceUtil.updateBindingReferences(definition);
				ComponentReferenceUtil.updatePortTypeReferences(definition);
				ComponentReferenceUtil.updateMessageReferences(definition);
				ComponentReferenceUtil.updateSchemaReferences(definition);
				// the line below simply causes a notification in order to
				// update our
				// views
				//
				definition.setDocumentationElement(definition.getDocumentationElement());
			}
		}
		finally {
		}
	}
	
	protected void createActions() {
		super.createActions();
	    ActionRegistry registry = getActionRegistry();

	    BaseSelectionAction action = new ASDAddMessageAction(this);
	    action.setSelectionProvider(getSelectionManager());
	    registry.registerAction(action);

	    action = new W11AddPartAction(this);
	    action.setSelectionProvider(getSelectionManager());
	    registry.registerAction(action);
	    
	    action = new W11SetNewMessageAction(this);
	    action.setSelectionProvider(getSelectionManager());
	    registry.registerAction(action);

	    action = new W11SetExistingMessageAction(this);
	    action.setSelectionProvider(getSelectionManager());
	    registry.registerAction(action);
	    
	    action = new W11SetNewTypeAction(this);
	    action.setSelectionProvider(getSelectionManager());
	    registry.registerAction(action);
	    
	    action = new W11SetExistingTypeAction(this);
	    action.setSelectionProvider(getSelectionManager());
	    registry.registerAction(action);
	    
	    action = new W11SetNewElementAction(this);
	    action.setSelectionProvider(getSelectionManager());
	    registry.registerAction(action);
	    
	    action = new W11SetExistingElementAction(this);
	    action.setSelectionProvider(getSelectionManager());
	    registry.registerAction(action);
	  }
	
  public IOpenExternalEditorHelper getOpenExternalEditorHelper() {
    return new W11OpenExternalEditorHelper(((IFileEditorInput) getEditorInput()).getFile());
  }
}
