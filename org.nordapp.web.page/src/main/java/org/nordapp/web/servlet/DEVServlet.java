package org.nordapp.web.servlet;

/*
 * #%L
 * NordApp OfficeBase :: Web
 * %%
 * Copyright (C) 2014 - 2015 I.D.S. DialogSysteme GmbH
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */


import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.UnavailableException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.io.IOUtils;
import org.i3xx.step.due.service.impl.SessionServiceImpl;
import org.i3xx.step.due.service.model.DeployService;
import org.i3xx.step.due.service.model.Session;
import org.i3xx.step.mongo.core.util.IdGen;
import org.i3xx.step.zero.service.impl.mandator.MandatorServiceImpl;
import org.i3xx.step.zero.service.model.mandator.Mandator;
import org.i3xx.util.basic.io.FilePath;
import org.nordapp.web.util.RequestPath;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class DEVServlet extends HttpServlet {
	
	/**
	 * 
	 */
	static Logger logger = LoggerFactory.getLogger(DEVServlet.class);

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	// upload settings
	private static final int MEMORY_THRESHOLD   = 1024 * 1024 * 3;  // 3MB
	private static final int MAX_FILE_SIZE      = 1024 * 1024 * 40; // 40MB
	private static final int MAX_REQUEST_SIZE   = 1024 * 1024 * 50; // 50MB
    
	private BundleContext context;

	public DEVServlet(BundleContext context) {
		super();
		
		this.context = context;
	}
	
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		
		try{
		
		//@SuppressWarnings("unused")
		final String mandatorId = RequestPath.getMandator(req);
		final String uuid = RequestPath.getSession(req);
		
		//
		// Session handler (HTTP) and session control (OSGi)
		//
		//SessionControl ctrl = new HttpSessionControlImpl(context, req.getSession());
		SessionControl ctrl = new SessionControlImpl(context);
		ctrl.setMandatorID(mandatorId);
		ctrl.setCertID(uuid);
		
		//RequestHandler rqHdl = new RequestHandler(context, ctrl);
		
		ctrl.loadTempSession();
		ctrl.getAll();
		ctrl.incRequestCounter();
		ctrl.setAll();
		ctrl.saveTempSession();
		
		//
		// Session service
		//
		String cert = null;
		
		String[] elem = RequestPath.getPath(req);
		if(elem.length!=1)
			throw new MalformedURLException("The URL needs the form '"+
					req.getServletPath()+"/[create|load]' but was '"+
					req.getRequestURI()+"'");
		
		Session mSession = SessionServiceImpl.getSession(context, cert, ctrl.getMandatorID(), ctrl.getCertID());
		if(mSession==null){
			List<String> list = ctrl.getShortTimePassword();
			if( ctrl.getCertID()==null || (! list.contains(ctrl.getCertID())) )
				throw new UnavailableException("Needs a valid User-Session.");
		}
		
		ServiceReference<DeployService> srDeploy = context.getServiceReference(DeployService.class);
		if(srDeploy==null)
			throw new IOException("The deploy service reference is not available (maybe down or a version conflict).");
		DeployService svDeploy = context.getService(srDeploy);
		if(svDeploy==null)
			throw new IOException("The deploy service is not available (maybe down or a version conflict).");
		
		String processID = IdGen.getURLSafeString( IdGen.getUUID() );
		String mandatorID = ctrl.getMandatorID();
		String groupID = ctrl.getGroupID();
		String artifactID = ctrl.getArtifactID();
		
		
		File zip = null;
		if(elem[0].equalsIgnoreCase("create")) {
			zip = svDeploy.createEmptyZip(processID, mandatorID, groupID, artifactID);
		}
		else if(elem[0].equalsIgnoreCase("load")){
			zip = svDeploy.zipFromData(processID, mandatorID, groupID, artifactID);
		}else{
			throw new MalformedURLException("The URL needs the form '"+
					req.getServletPath()+"/[create|load]' but was '"+
					req.getRequestURI()+"'");
		}
		
		ResponseHandler rsHdl = new ResponseHandler(context, ctrl);
		rsHdl.avoidCaching(resp);
		
		resp.setContentType("application/octet-stream");
		resp.setHeader("Content-Type", "application/octet-stream");
		resp.setHeader("Content-Disposition", "attachment; filename=\""+processID+".zip\"");
		InputStream is = new FileInputStream(zip);
		try{
			rsHdl.sendFile(is, resp, null);
		}finally{
			is.close();
		}
		
		
		}catch(Exception e){
			ResponseHandler rsHdl = new ResponseHandler(context, null);
			rsHdl.sendError(logger, e, resp, null);
		}
	}/* doGET */
	
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		
		try{
		
		//@SuppressWarnings("unused")
		final String mandatorId = RequestPath.getMandator(req);
		final String uuid = RequestPath.getSession(req);
		
		//
		// Session handler (HTTP) and session control (OSGi)
		//
		//SessionControl ctrl = new HttpSessionControlImpl(context, req.getSession());
		SessionControl ctrl = new SessionControlImpl(context);
		ctrl.setMandatorID(mandatorId);
		ctrl.setCertID(uuid);
		
		//RequestHandler rqHdl = new RequestHandler(context, ctrl);
		
		ctrl.loadTempSession();
		ctrl.getAll();
		ctrl.incRequestCounter();
		ctrl.setAll();
		ctrl.saveTempSession();
		
		//
		// Session and other services
		//
		String cert = null;
		
		//The '0' session of the mandator
		Session mSession = SessionServiceImpl.getSession(context, cert, ctrl.getMandatorID(), "0");
		
		//The 'user' session
		mSession = SessionServiceImpl.getSession(context, cert, ctrl.getMandatorID(), ctrl.decodeCert().toString());
		if(mSession==null){
			List<String> list = ctrl.getShortTimePassword();
			if( ctrl.getCertID()==null || (! list.contains(ctrl.getCertID())) )
				throw new UnavailableException("Needs a valid User-Session.");
		}
		
		//The mandator
		Mandator mandator = MandatorServiceImpl.getMandator(context, ctrl.getMandatorID());
		if(mandator==null){
			throw new UnavailableException("Needs a valid mandator id:"+ctrl.getMandatorID()+".");
		}
		
		//
		// Get some data
		//
		
		FilePath tmpLoc = FilePath.get(mandator.getPath()).add("temp");
		
		//
		// prepare the engine
		//
		
		String[] elem = RequestPath.getPath(req);
		if(elem.length!=0)
			throw new MalformedURLException("The URL needs the form '"+
					req.getServletPath()+"' but was '"+
					req.getRequestURI()+"'");
		
		//
		// Initialize the work
		//
		
		ServiceReference<DeployService> srDeploy = context.getServiceReference(DeployService.class);
		if(srDeploy==null)
			throw new IOException("The deploy service reference is not available (maybe down or a version conflict).");
		DeployService svDeploy = context.getService(srDeploy);
		if(svDeploy==null)
			throw new IOException("The deploy service is not available (maybe down or a version conflict).");
		
		String processID = IdGen.getURLSafeString( IdGen.getUUID() );
		String mandatorID = ctrl.getMandatorID();
		String groupID = ctrl.getGroupID();
		String artifactID = ctrl.getArtifactID();
		
		//
		// Process upload
		//
		
		// Create a factory for disk-based file items
		DiskFileItemFactory factory = new DiskFileItemFactory();
		
		// sets memory threshold - beyond which files are stored in disk
		factory.setSizeThreshold(MEMORY_THRESHOLD);		
        
		File repository = tmpLoc.add("http-upload").toFile();
		if( ! repository.exists()){
			repository.mkdirs();
		}
		factory.setRepository(repository);

		// Create a new file upload handler
		ServletFileUpload upload = new ServletFileUpload(factory);
        
		// sets maximum size of upload file
		upload.setFileSizeMax(MAX_FILE_SIZE);
		 
		// sets maximum size of request (include file + form data)
		upload.setSizeMax(MAX_REQUEST_SIZE);
		
		try {
			// parses the request's content to extract file data
			//@SuppressWarnings("unchecked")
			List<FileItem> formItems = upload.parseRequest(req);
			
			if (formItems != null && formItems.size() > 0) {
				// iterates over form's fields
				for (FileItem item : formItems) {
					// processes only fields that are not form fields
					if (item.isFormField()) {
						//data
					}else{
						File zip = svDeploy.createEmptyZip(processID, mandatorID, groupID, artifactID);
						OutputStream os = new FileOutputStream(zip);
						InputStream is = item.getInputStream();
						try{
							IOUtils.copy(is, os);
						}finally{
							IOUtils.closeQuietly(is);
							IOUtils.closeQuietly(os);
						}
						svDeploy.zipToData(processID, mandatorID, groupID, artifactID, zip.getName());
					}//fi
				}//for
			}//fi
		} catch (Exception ex) {
			req.setAttribute("message", "There was an error: " + ex.getMessage());
		}
		
		//
		// Prints the result from the user-session
		//
		StringBuffer buffer = new StringBuffer();
		ResponseHandler rsHdl = new ResponseHandler(context, ctrl);
		
		logger.debug("The user session is{}found mandatorId:{}, sessionId:{}.", (mSession==null?" not ":" "), ctrl.getMandatorID(), ctrl.getCertID());
		rsHdl.getSessionData(buffer, mSession);
		
		//
		//
		//
		byte[] bytes = buffer.toString().getBytes();
		
		//
		// Send the resource
		//
		rsHdl.avoidCaching(resp);
		rsHdl.sendData(bytes, resp);
		
		}catch(Exception e){
			ResponseHandler rsHdl = new ResponseHandler(context, null);
			rsHdl.sendError(logger, e, resp, null);
		}
	}/* doPOST */
	
}
