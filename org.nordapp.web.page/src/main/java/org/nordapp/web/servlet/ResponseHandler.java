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


import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.Writer;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletResponse;

import org.i3xx.step.due.service.impl.SessionServiceImpl;
import org.i3xx.step.due.service.model.ResourceService;
import org.i3xx.step.due.service.model.Session;
import org.i3xx.step.due.service.model.VelocityService;
import org.i3xx.step.fileinfo.service.model.FileinfoService;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.slf4j.Logger;

import com.google.gson.Gson;

public class ResponseHandler {
	
	public static final String SESSION_CERT_ID= "session_cert_id";
	public static final String SESSION_IS_VALID= "session_is_valid";
	public static final String SESSION_IS_ALIVE= "session_is_alive";
	public static final String REQUEST_ID= "rqid";
	
	/** Response counter */
	private static long counter = 0;
	
	/** The bundle context */
	private BundleContext context;
	
	/** The session control */
	private SessionControl ctrl;
	
	public ResponseHandler(BundleContext context, SessionControl ctrl) {
		this.context = context;
		this.ctrl = ctrl;
	}
	
	/**
	 * Sets header to avoid caching
	 * 
	 * @param resp
	 */
	public void avoidCaching(HttpServletResponse resp) {
		resp.setHeader("Expires", "Sat, 01 Jan 2000 01:00:00 GMT");
		resp.setHeader( "Cache-Control", "no-store, no-cache, must-revalidate, max-age=0, private" );
		resp.setHeader( "Pragma", "no-cache" );
	}
	
	/**
	 * Sets the header section with the RESTful response links.
	 * 
	 * @param resp
	 * @param linkHeader The link header
	 */
	public void setLinkHeader(HttpServletResponse resp, String linkHeader) {
		resp.setHeader( "Link", linkHeader );
	}
	
	/**
	 * Gets the attributes of the session and put it into the buffer
	 * as a JSON String.
	 * 
	 * @param buffer The buffer to print the data to
	 * @param session
	 */
	public void getSessionData(StringBuffer buffer, Session session) {
		
		Gson gson = new Gson();
		int cP = buffer.length();
		
		//
		// Prints the result from the user-session
		//
		if(session!=null) {
			Set<String> keySet = session.getKeys();
			for(String key : keySet){
				
				buffer.append(',');
				buffer.append( gson.toJson(key) );
				buffer.append(':');
				
				Object val = session.getValue(key);
				
				buffer.append( gson.toJson(val) );
				
			}//for
		}//fi
		
		if(buffer.length()==0){
			buffer.append("{}");
		}else{
			buffer.setCharAt(cP, '{');
			buffer.append('}');
		}//fi
	}
	
	/**
	 * Send the data to the servlet response.
	 * 
	 * @param buffer The buffered data
	 * @param resp The HttpResponse
	 * @throws IOException
	 */
	public void sendData(byte[] buffer, HttpServletResponse resp) throws IOException {
		
		//
		// Send the response
		//
		InputStream in = new ByteArrayInputStream(buffer);
		
		int c = 0;
		byte[] buf = new byte[512];
		OutputStream out = resp.getOutputStream();
		
		try{
			while((c=in.read(buf))>-1)
				out.write(buf, 0, c);
		}finally{
			try{
				in.close();
			}finally{
				out.close();
			}
		}
	}
	
	/**
	 * Send an error message
	 * 
	 * @param logger The logger (Optional: null)
	 * @param e The exception
	 * @param resp The HttpResponse
	 * @param headers The response headers (Optional: null)
	 * @throws IOException
	 */
	public void sendError(Logger logger, Exception e, HttpServletResponse resp, Map<String, String> headers) throws IOException {
		
		if(logger!=null)
			logger.warn("", e);
		
		StringBuffer b = new StringBuffer();
		b.append("<html>");
		b.append("<title>");
		b.append("Message");
		b.append("</title>");
		b.append("<body>");
		b.append("<h1>Oops,</h1>");
		b.append("<p>something is going wrong.</p>");
		b.append("<p>The reason is:<br/>");
		b.append(e.getMessage());
		b.append("<br/><br/>");
		b.append("Maybe, there are more informations in the server log.<br/>");
		b.append("</p>");
		b.append("</body>");
		b.append("</html>");
		
		//Set the headers
		if(headers!=null)
			for(Map.Entry<String, String> en : headers.entrySet())
				resp.setHeader(en.getKey(), en.getValue());
		
		int c = 0;
		char[] buf = new char[512];
		Writer out = resp.getWriter();
		Reader in = new StringReader( b.toString() );
		
		try{
			while((c=in.read(buf))>-1)
				out.write(buf, 0, c);
		}finally{
			out.close();
		}
	}
	
	/**
	 * Send the data to the servlet response.
	 * 
	 * @param in The input stream (no close inside)
	 * @param resp The HttpResponse
	 * @param headers The response headers (Optional: null)
	 * @throws IOException
	 */
	public void sendFile(InputStream in, HttpServletResponse resp, Map<String, String> headers) throws IOException {
		
		//Set the headers
		if(headers!=null)
			for(Map.Entry<String, String> e : headers.entrySet())
				resp.setHeader(e.getKey(), e.getValue());
		
		int c = 0;
		byte[] buf = new byte[512];
		OutputStream out = resp.getOutputStream();
		
		try{
			while((c=in.read(buf))>-1)
				out.write(buf, 0, c);
		}finally{
			out.close();
		}
	}
	
	/**
	 * Send the page to the servlet response.
	 * 
	 * @param dest The path and filename of the page
	 * @param resp The HttpResponse
	 * @throws IOException
	 */
	public void sendFile(String dest, HttpServletResponse resp) throws IOException {
		
		//Get the resource
		ServiceReference<ResourceService> rsr = context.getServiceReference(ResourceService.class);
		if(rsr==null)
			throw new IOException("The resource service is not available (maybe down or a version conflict).");

		ResourceService rs = context.getService(rsr);
		if(rs==null)
			throw new IOException("The resource service is not available (maybe down or a version conflict).");
		
		Map<String, String> props = new HashMap<String, String>();
		
		InputStream in = rs.getResourceAsStream(
				ctrl.getMandatorID(), ctrl.getGroupID(), ctrl.getArtifactID(),
				dest, ResourceService.FILE_RESOURCE, props);
		
		
		//use an input stream that supports mark.
		String contentType = resp.getContentType();
		if(contentType==null) {
			
			//Get the resource
			ServiceReference<FileinfoService> fir = context.getServiceReference(FileinfoService.class);
			if(fir==null)
				throw new IOException("The fileinfo service is not available (maybe down or a version conflict).");

			FileinfoService fi = context.getService(fir);
			if(fi==null)
				throw new IOException("The fileinfo service is not available (maybe down or a version conflict).");
			
			contentType = fi.getMimetype(dest, in);
			resp.setContentType(contentType);
		}
		
		/*
		String contentType = resp.getContentType();
		if(contentType==null) {
			Tika tika = new Tika();
			contentType = tika.detect(dest);
			if(contentType==null) {
				in = new BufferedInputStream(in);
				contentType = tika.detect(in);
			}
			resp.setContentType(contentType);
		}
		*/
		
		int c = 0;
		byte[] buf = new byte[1024];
		OutputStream out = resp.getOutputStream();
		
		try{
			while((c=in.read(buf))>-1)
				out.write(buf, 0, c);
		}finally{
			try{
				in.close();
			}finally{
				out.close();
			}
		}
		
	}
	
	/**
	 * Send the page to the servlet response.
	 * 
	 * @param dest The path and filename of the page
	 * @param resp The HttpResponse
	 * @throws IOException
	 */
	public void sendPage(String dest, HttpServletResponse resp) throws IOException {
		
		//Get the resource
		ServiceReference<VelocityService> rsr = context.getServiceReference(VelocityService.class);
		if(rsr==null)
			throw new IOException("The velocity service is not available (maybe down or a version conflict).");

		ResourceService rs = context.getService(rsr);
		if(rs==null)
			throw new IOException("The velocity service is not available (maybe down or a version conflict).");
		
		Map<String, String> props = new HashMap<String, String>();
		
		//
		// Get the attributes of the user session
		//
		String attrData = (String)ctrl.getAttribute(SessionControl.READ_SESSION_DATA);
		if( attrData==null || Boolean.parseBoolean(attrData) ) {
			String cert = null;
			Session session = SessionServiceImpl.getSession(context, cert, ctrl.getMandatorID(), ctrl.getCertID());
			if(session!=null){
				for(String key : session.getKeys()){
					Object value = session.getValue(key);
					props.put(key, String.valueOf(value));
				}//for
			}//fi
		}//fi
		
		//
		// Get the attributes of the HttpSession
		//
		props.put(SESSION_CERT_ID, ctrl.getCertID());
		for(String key : ctrl.getAttributeNames()){
			Object value = ctrl.getAttribute(key);
			props.put(key, String.valueOf(value));
		}//for
		
		//
		// Add the response counter (maybe used also for session handling in future)
		//
		String rqid = null;
		if(ctrl.getCertID()==null) {
			//this is not a valid configuration
			rqid = "";
		}else{
			AbstractControlImpl ac = (AbstractControlImpl) ctrl;
			if(ac.getField0()==null)
				ac.updateBinaryData();
			
			byte[] buffer = ByteBuffer.allocate(24)
					.put( ac.getField0() )
					.putLong(16, (++counter))
					.array();
			rqid = ctrl.toBase64(buffer);
		}//fi
		props.put(REQUEST_ID, rqid);
		
		//
		//
		//
		
		InputStream in = rs.getResourceAsStream(ctrl.getMandatorID(), ctrl.getGroupID(), ctrl.getArtifactID(),
				dest, ResourceService.FILE_RESOURCE, props);
		
		String contentType = resp.getContentType();
		if(contentType==null) {
			
			//Get the resource
			ServiceReference<FileinfoService> fir = context.getServiceReference(FileinfoService.class);
			if(fir==null)
				throw new IOException("The fileinfo service is not available (maybe down or a version conflict).");

			FileinfoService fi = context.getService(fir);
			if(fi==null)
				throw new IOException("The fileinfo service is not available (maybe down or a version conflict).");
			
			contentType = fi.getMimetype(dest, in);
			resp.setContentType(contentType);
		}
		/*
		String contentType = resp.getContentType();
		if(contentType==null) {
			Tika tika = new Tika();
			contentType = tika.detect(dest);
			if(contentType==null) {
				in = new BufferedInputStream(in);
				contentType = tika.detect(in);
			}
			resp.setContentType(contentType);
		}
		*/
		
		int c = 0;
		byte[] buf = new byte[1024];
		OutputStream out = resp.getOutputStream();
		
		try{
			while((c=in.read(buf))>-1)
				out.write(buf, 0, c);
		}finally{
			try{
				in.close();
			}finally{
				out.close();
			}
		}
		
	}
	
	/**
	 * Adds a link, relative links starting with '/' are completed.
	 * 
	 * @param link
	 */
	public void addLink(String link) {
		//links.add(  );
	}

}
