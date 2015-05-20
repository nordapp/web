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


/**
 * Variables defined to be used for scripting.
 * 
 * @author Stefan
 *
 */
public interface ScriptVars {
	
	/**
	 * <p>The UUID of the HTTP process. The UUID has the type 
	 * org.i3xx.step.mongo.core.util.IdRep</p>
	 * 
	 * <p>The id is generated by the servlet that receives the request
	 * to identify each single request. This is not a session id.</p>
	 * 
	 * @see org.i3xx.step.mongo.core.util.IdRep
	 */
	public static final String httpProcessID = "http.processUUID";
	
	/**
	 * <p>The id that is used by the session of step.due.</p>
	 * 
	 * <p>Usually the session id is equals to the id used in the
	 * HTTP session in the attribute 'certID' defined by the interface
	 * SessionControl. To use the sessionId, that is the key of the
	 * certificate 'certID', the value must be decoded to a BigInteger.
	 * Use the toString() method to get the String representation.</p>
	 * 
	 * <p>The HTTP session uses an own session ID that is not available
	 * to script.</p>
	 */
	public static final String httpSessionID = "http.sessionID";
	
	/**
	 * <p>A container that holds the references of the HTTP request.</p>
	 */
	public static final String httpIOContainer = "http.io";
	
	/**
	 * The file id of the file to open.
	 */
	public static final String openFileByID = "file.openByID";
	
	/**
	 * The database file reference of the file to open.
	 */
	public static final String openFileByRef = "file.openByRef";
}
