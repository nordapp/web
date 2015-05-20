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
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.LinkedHashMap;
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
import org.i3xx.step.uno.impl.service.EngineBaseServiceImpl;
import org.i3xx.step.uno.model.daemon.Engine;
import org.i3xx.step.uno.model.service.EngineBaseService;
import org.i3xx.step.zero.service.impl.mandator.MandatorServiceImpl;
import org.i3xx.step.zero.service.model.mandator.Mandator;
import org.i3xx.util.basic.io.FilePath;
import org.nordapp.web.util.GsonHashMapDeserializer;
import org.nordapp.web.util.RequestPath;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * <p>Calls a function and gets the attributes from the session service.
 * Post puts the data to the session service before calling the script.</p>
 * 
 * @author Stefan
 *
 */
public class SessionCallServlet extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * 
	 */
	static Logger logger = LoggerFactory.getLogger(SessionCallServlet.class);
	
	// upload settings
	private static final int MEMORY_THRESHOLD   = 1024 * 1024 * 3;  // 3MB
	private static final int MAX_FILE_SIZE      = 1024 * 1024 * 40; // 40MB
	private static final int MAX_REQUEST_SIZE   = 1024 * 1024 * 50; // 50MB
	
	private BundleContext context;

	public SessionCallServlet(BundleContext context) {
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
		// Session handler
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
		
		process(ctrl, req, resp, null);
		
		}catch(Exception e){
			ResponseHandler rsHdl = new ResponseHandler(context, null);
			rsHdl.sendError(logger, e, resp, null);
		}
	}
	
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		
		try{
		
		//@SuppressWarnings("unused")
		final String mandatorId = RequestPath.getMandator(req);
		final String uuid = RequestPath.getSession(req);
		
		//
		// Session handler
		//
		//SessionControl ctrl = new HttpSessionControlImpl(context, req.getSession());
		SessionControl ctrl = new SessionControlImpl(context);
		ctrl.setMandatorID(mandatorId);
		ctrl.setCertID(uuid);
		
		ctrl.loadTempSession();
		ctrl.getAll();
		ctrl.incRequestCounter();
		ctrl.setAll();
		ctrl.saveTempSession();
		
		//
		// Process upload
		//
		
		// Create a factory for disk-based file items
		DiskFileItemFactory factory = new DiskFileItemFactory();
		
		// sets memory threshold - beyond which files are stored in disk
		factory.setSizeThreshold(MEMORY_THRESHOLD);		
        
		//The mandator
		Mandator mandator = MandatorServiceImpl.getMandator(context, ctrl.getMandatorID());
		if(mandator==null){
			throw new UnavailableException("Needs a valid mandator id:"+ctrl.getMandatorID()+".");
		}
		
		FilePath tmpLoc = FilePath.get(mandator.getPath()).add("temp");
		
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
		
		// Gets the JSON data
		GsonBuilder gsonBuilder = new GsonBuilder();
		gsonBuilder.registerTypeAdapter(LinkedHashMap.class, new GsonHashMapDeserializer());
		Gson gson = gsonBuilder.create();
		
		Map<String, Object> res = new HashMap<String, Object>();
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
						res.put(item.getFieldName(), item.getString());
						//ioc.setField(item.getFieldName(), item);
					}else{
						//Gets JSON as Stream
						Reader json = new InputStreamReader(item.getInputStream());
						//String json = item.getString();
						
						try{
							@SuppressWarnings("unchecked")
							LinkedHashMap<String, Object> mapJ = gson.fromJson(json, LinkedHashMap.class);
							for(String key : mapJ.keySet()) {
								Object val = mapJ.get(key);
								if(val==null)
									continue;
								
								res.put(key, val);
							}//for
						}finally{
							json.close();
						}
					}//fi
				}//for
				
			}//fi
		} catch (Exception ex) {
			req.setAttribute("message", "There was an error: " + ex.getMessage());
		}
		
		process(ctrl, req, resp, res);
		
		}catch(Exception e){
			ResponseHandler rsHdl = new ResponseHandler(context, null);
			rsHdl.sendError(logger, e, resp, null);
		}
	}
	
	/**
	 * @param req The request
	 * @param resp The response
	 * @param data The data
	 * @throws IOException 
	 */
	private void process(SessionControl ctrl, HttpServletRequest req, HttpServletResponse resp, Map<String, Object> data) throws IOException {
		
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
		if(elem.length==0)
			throw new MalformedURLException("The URL needs the form '"+
					req.getServletPath()+"/function-id' but was '"+
					req.getRequestURI()+"'");
		
		BigInteger engineId = ctrl.decodeCert();
		String functionId = elem.length>=1 ? elem[0] : null;
		
		StringBuffer buffer = new StringBuffer();
		ResponseHandler rsHdl = new ResponseHandler(context, ctrl);
		
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
			// Set the parameter to the user-session
			//
			if(mSession!=null && data!=null && data.size()>0) {
				for(String key : data.keySet()) {
					Object value = data.get(key);
					
					logger.trace("Set data to session mandatorId:{}, sessionId:{}, key:{}, value:{}", ctrl.getMandatorID(), ctrl.getCertID(), key, value);
					mSession.setValue(key, value);
				}//for
			}//fi
			
			try{
				engine.setLocalValue(Mandator.MANDATORID, ctrl.getMandatorID());
				engine.setLocalValue("sessionId", ctrl.decodeCert().toString());
				engine.setLocalValue("nativeSessionId", ctrl.decodeCert());
				
				engine.call(functionId);
			}catch(Exception e){
				e.printStackTrace();
			}
			
			rsHdl.getSessionData(buffer, mSession);
			
		}catch(Exception e) {
			logger.error("Error running the step.", e);
		}
		
		//
		//
		//
		byte[] bytes = buffer.toString().getBytes();
		
		//
		// Send the resource
		//
		rsHdl.avoidCaching(resp);
		rsHdl.sendData(bytes, resp);
		
	}/*process*/
}
