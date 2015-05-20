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


import java.io.IOException;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

public class PrintHttpDataServlet extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public PrintHttpDataServlet() {
	}
	
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		
		//
		// Session handler
		//
		HttpSession session = req.getSession(true);
		
		Integer visitCount = new Integer(0);
		String visitCountKey = new String("visitCount");
		String userIDKey = new String("userID");
		String userID = new String("ABCD");
	      
		// Check if this is new comer on your web page.
		if (session.isNew()){
			session.setAttribute(userIDKey, userID);
		} else {
			visitCount = (Integer)session.getAttribute(visitCountKey);
			visitCount = visitCount==null ? 0 : visitCount + 1;
			userID = (String)session.getAttribute(userIDKey);
		}
		session.setAttribute(visitCountKey,  visitCount);	      
		
		//
		//
		//
		Map<String, String> map = new LinkedHashMap<String, String>();
		
		map.put(userIDKey, userID);
		map.put(visitCountKey, Integer.toString(visitCount) );
		
		map.put("AuthType : ", req.getAuthType() );
		map.put("CharacterEncoding : ", req.getCharacterEncoding());
		map.put("ContentLength : ", Integer.toString(req.getContentLength()) );
		map.put("ContentType : ", req.getContentType());
		map.put("ContextPath : ", req.getContextPath());
		map.put("LocalAddr : ", req.getLocalAddr());
		map.put("LocalName : ", req.getLocalName());
		map.put("LocalPort : ", Integer.toString(req.getLocalPort()) );
		map.put("Method : ", req.getMethod());
		map.put("PathInfo : ", req.getPathInfo());
		map.put("PathTranslated : ", req.getPathTranslated());
		map.put("Protocol : ", req.getProtocol());
		map.put("QueryString : ", req.getQueryString());
		map.put("RemoteAddr : ", req.getRemoteAddr());
		map.put("RemoteHost : ", req.getRemoteHost());
		map.put("RemotePort : ", Integer.toString(req.getRemotePort()) );
		map.put("RemoteUser : ", req.getRemoteUser());
		map.put("RequestedSessionId : ", req.getRequestedSessionId());
		map.put("RequestURI : ", req.getRequestURI());
		map.put("RequestURL : ", req.getRequestURL().toString() );
		map.put("Scheme : ", req.getScheme());
		map.put("ServerName : ", req.getServerName());
		map.put("ServerPort : ", Integer.toString(req.getServerPort()) );
		map.put("ServletPath : ", req.getServletPath());
		
		Enumeration<String> en = req.getAttributeNames();
		while(en.hasMoreElements()){
			String name = en.nextElement();
			Object value = req.getAttribute(name);
			map.put("attr:"+name, String.valueOf(value));
		}
		
		en = req.getHeaderNames();
		while(en.hasMoreElements()){
			String name = en.nextElement();
			Object value = req.getHeader(name);
			map.put("header:"+name, String.valueOf(value));
		}
		
		en = getInitParameterNames();
		while(en.hasMoreElements()){
			String name = en.nextElement();
			String value = getInitParameter(name);
			map.put("init:"+name, value);
		}
		
		en = session.getAttributeNames();
		while(en.hasMoreElements()){
			String name = en.nextElement();
			Object value = session.getAttribute(name);
			map.put("session:"+name, String.valueOf(value));
		}
	    
		//
		//
		//
		StringBuffer buffer = new StringBuffer();
		
		buffer.append("<html>");
		buffer.append("<body>");
		buffer.append("<table>");
		
		for(Map.Entry<String, String> e : map.entrySet()){
			buffer.append("<tr><td>");
			buffer.append(e.getKey());
			buffer.append("</td><td>");
			buffer.append(e.getValue());
			buffer.append("</td></tr>");
		}
		
		buffer.append("</table>");
		buffer.append("<p><a href=\"/info/test/1\">Route back (1)</a></p>");
		buffer.append("<p><a href=\"/info/test/2\">Route back (2)</a></p>");
		buffer.append("<p><a href=\"/info/test/3\">Route back (3)</a></p>");
		buffer.append("</body>");
		buffer.append("</html>");
		
		resp.getWriter().write( buffer.toString() );
	}
}
