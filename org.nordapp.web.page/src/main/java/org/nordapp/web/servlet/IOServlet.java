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


import static org.nordapp.web.servlet.ScriptVars.httpIOContainer;
import static org.nordapp.web.servlet.ScriptVars.httpProcessID;
import static org.nordapp.web.servlet.ScriptVars.httpSessionID;
import static org.nordapp.web.servlet.ScriptVars.openFileByID;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.UnavailableException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.i3xx.step.due.service.impl.SessionServiceImpl;
import org.i3xx.step.due.service.model.Session;
import org.i3xx.step.mongo.core.model.DbFile;
import org.i3xx.step.mongo.core.util.IdGen;
import org.i3xx.step.mongo.core.util.IdRep;
import org.i3xx.step.mongo.service.impl.DatabaseServiceImpl;
import org.i3xx.step.mongo.service.model.DatabaseService;
import org.i3xx.step.uno.impl.service.EngineBaseServiceImpl;
import org.i3xx.step.uno.model.daemon.Engine;
import org.i3xx.step.uno.model.service.EngineBaseService;
import org.i3xx.step.zero.service.impl.mandator.MandatorServiceImpl;
import org.i3xx.step.zero.service.model.mandator.Mandator;
import org.i3xx.util.basic.io.FilePath;
import org.nordapp.web.util.RequestPath;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class IOServlet extends HttpServlet {
	
	/**
	 * 
	 */
	static Logger logger = LoggerFactory.getLogger(IOServlet.class);

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	// upload settings
	private static final int MEMORY_THRESHOLD   = 1024 * 1024 * 3;  // 3MB
	private static final int MAX_FILE_SIZE      = 1024 * 1024 * 40; // 40MB
	private static final int MAX_REQUEST_SIZE   = 1024 * 1024 * 50; // 50MB
    
	private BundleContext context;

	public IOServlet(BundleContext context) {
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
		
		//The '0' session of the mandator
		Session mSession = SessionServiceImpl.getSession(context, cert, ctrl.getMandatorID(), "0");
		Integer baseIndex = ((Integer)mSession.getValue( Session.ENGINE_BASE_INDEX )).intValue();
		//String sessionId = session.getId();
		mSession = SessionServiceImpl.getSession(context, cert, ctrl.getMandatorID(), ctrl.decodeCert().toString());
		
		String[] elem = RequestPath.getPath(req);
		if(elem.length!=2)
			throw new MalformedURLException("The URL needs the form '"+
					req.getServletPath()+"/function-id/file-uuid' but was '"+
					req.getRequestURI()+"'");
		
		BigInteger engineId = ctrl.decodeCert();
		String functionId = elem.length>=1 ? elem[0] : null;
		String fileUuid = elem.length>=2 ? elem[1] : null;
		
		mSession = SessionServiceImpl.getSession(context, cert, ctrl.getMandatorID(), ctrl.getCertID());
		if(mSession==null){
			List<String> list = ctrl.getShortTimePassword();
			if( ctrl.getCertID()==null || (! list.contains(ctrl.getCertID())) )
				throw new UnavailableException("Needs a valid User-Session.");
		}
		
		DatabaseService dbService = DatabaseServiceImpl.getDatabase(context, cert, ctrl.getMandatorID());
		if(dbService==null){
			throw new UnavailableException("Needs a valid database service for mandator "+ctrl.getMandatorID()+".");
		}
		
		ResponseHandler rsHdl = new ResponseHandler(context, ctrl);
		Map<String, String> headers = new HashMap<String, String>();
		
		try{
			//
			// Gets the engine base service
			//
			EngineBaseService engineBaseService = 
					EngineBaseServiceImpl.getService(context, ctrl.getMandatorID(), String.valueOf(baseIndex));
			if(engineBaseService==null)
				throw new IOException("The mandator base service is not available (maybe down or a version conflict).");
			
			//
			// Run the step
			//
			Mandator mandator = MandatorServiceImpl.getMandator(context, ctrl.getMandatorID());
			if(mandator==null)
				throw new IOException("The mandator service is not available (maybe down or a version conflict).");
			
			Engine engine = engineBaseService.getEngine(engineId);
			if( !engine.isLogin())
				throw new IllegalAccessException("There is no login to this session.");
			
			//
			// Initialize the work
			//
			
			engine.setLocalValue(httpSessionID, engineId.toString()); //ctrl.decodeCert().toString()
			
			IdRep processUUID = IdGen.getUUID();
			engine.setLocalValue(httpProcessID, processUUID);
			
			IOContainer ioc = new IOContainer();
			ioc.setProcessUUID(processUUID.toString());
			ioc.setField(openFileByID, fileUuid);
			engine.setLocalValue(httpIOContainer, ioc);
			
			//
			// Call script
			//
			
			try{
				engine.call(functionId);
			}catch(Exception e){
				e.printStackTrace();
			}
			
			//Prints a file out
			//rsHdl.getSessionData(buffer, mSession);
			
			InputStream in = null;
			try{
				//DbDatabase dbs = dbService.getDatabase();
				//DbFileStore dbf = dbs.getFileStore();
				//DbFile file = dbf.getFileFromId( (String)engine.getLocalValue(openFileByRef) );
				String[] fn = ioc.getFilenames();
				if(fn.length==0)
					throw new IllegalArgumentException("There is no file specified to open (empty array).");
					
				DbFile file = (DbFile)ioc.getFile(fn[0]);
				
				in = file.getInputStream();
				
				headers.put("Content-Type", file.getMimetype());
				headers.put("Content-Length", String.valueOf(file.getLength()));
				
				rsHdl.avoidCaching(resp);
				rsHdl.sendFile(in, resp, headers);
			}finally{
				if(in!=null)
					in.close();
				
				ioc.cleanup();
				engine.setLocalValue(httpSessionID, null);
				engine.setLocalValue(httpProcessID, null);
				engine.setLocalValue(httpIOContainer, null);
			}
			
		}catch(Exception e) {
			logger.error("Error running the step.", e);
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
		
		//
		// Session and other services
		//
		String cert = null;
		
		//The '0' session of the mandator
		Session mSession = SessionServiceImpl.getSession(context, cert, ctrl.getMandatorID(), "0");
		Integer baseIndex = ((Integer)mSession.getValue( Session.ENGINE_BASE_INDEX )).intValue();
		
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
		
		EngineBaseService engineBaseService = null;
		try {
			engineBaseService = EngineBaseServiceImpl.getService(context, ctrl.getMandatorID(), String.valueOf(baseIndex));
		} catch (InvalidSyntaxException e1) {}
		if(engineBaseService==null)
			throw new IOException("The mandator base service is not available (maybe down or a version conflict).");
		
		//
		// Get some data
		//
		
		FilePath tmpLoc = FilePath.get(mandator.getPath()).add("temp");
		
		//
		// prepare the engine
		//
		
		String[] elem = RequestPath.getPath(req);
		if(elem.length==0)
			throw new MalformedURLException("The URL needs the form '"+
					req.getServletPath()+"/function-id' but was '"+
					req.getRequestURI()+"'");
		
		BigInteger engineId = ctrl.decodeCert();
		String functionId = elem.length>=1 ? elem[0] : null;
		
		Engine engine = null;
		try {
			engine = engineBaseService.getEngine(engineId);
		} catch (Exception e1) {
			throw new ServletException(e1);
		}
		if( !engine.isLogin())
			throw new ServletException("There is no login to this session.");
		
		//
		// Initialize the work
		//
		
		engine.setLocalValue(httpSessionID, engineId.toString()); //ctrl.decodeCert().toString()
		
		IdRep processUUID = IdGen.getUUID();
		engine.setLocalValue(httpProcessID, processUUID);
		
		IOContainer ioc = new IOContainer();
		ioc.setProcessUUID(processUUID.toString());
		engine.setLocalValue(httpIOContainer, ioc);
		
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
						ioc.setField(item.getFieldName(), item);
					}else{
						ioc.setFile(IdGen.getUUID().toString(), item);
					}//fi
				}//for
			}//fi
		} catch (Exception ex) {
			req.setAttribute("message", "There was an error: " + ex.getMessage());
		}
		
		//
		// Call script
		//
		
		try{
			engine.call(functionId);
		}catch(Exception e){
			e.printStackTrace();
		}
		
		ioc.cleanup();
		engine.setLocalValue(httpSessionID, null);
		engine.setLocalValue(httpProcessID, null);
		engine.setLocalValue(httpIOContainer, null);
		
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
