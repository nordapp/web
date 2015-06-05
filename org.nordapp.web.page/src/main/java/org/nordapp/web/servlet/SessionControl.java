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


import java.math.BigInteger;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public interface SessionControl {
	
	/**  */
	public static final String JETTY_REQUEST_ATTR_NAME = "org.ops4j.pax.web.service.internal.jettyRequest";
	
	/**  */
	public static final String JETTY_NEW_SESSIONID_ATTR_NAME = "org.eclipse.jetty.server.newSessionId";
	
	/**
	 * This is a flag [true|false] that causes the session data to be put
	 * into the data map of the resource servlet. Than the velocity service
	 * is able to process the data. The type of the flag is String
	 */
	public static final String READ_SESSION_DATA = "org.nordapp.web.server.getSessionData";
	
	/**
	 * The flag that indicates that the session is belongs to a stateful client (HATEOAS)
	 * (true=stateful, false=static or not stateful)
	 */
	public static final int FLAG_SESSION_IS_STATEFUL = 0x1;

	
	enum Action { 
		/**
		 * Reads the value
		 */
		READ,
		
		/**
		 * Writes the value
		 */
		WRITE 
	}
	
	enum Condition {
		/**
		 * The value to be set is null (the new value may be null).
		 */
		IF_NULL,
		
		/**
		 * The value to be set is not null (the new value may be null).
		 */
		IF_NOT_NULL, 
		
		/**
		 * The new value is not null (the value may be null).
		 */
		IF_VAR_NOT_NULL,
	
		/**
		 * The value to be set is not null and the new value is not null.
		 */
		IF_SET_NOT_NULL
	}
	
	/**
	 * The key of the visit counter
	 */
	public static final String visitCountKey = new String("visitCount");
	
	/**
	 * The key of the password timeout
	 */
	public static final String passwordTimeoutKey = new String("passwordTimeout");
	
	/**
	 * The key of the logon timeout
	 */
	public static final String logonTimeoutKey = new String("logonTimeout");
	
	/**
	 * The key of the mandator
	 */
	public static final String mandatorIDKey = new String("mandatorID");
	
	/**
	 * The key of the user
	 */
	public static final String userIDKey = new String("userID");
	
	/**
	 * The key of the bundle group
	 */
	public static final String groupIDKey = new String("groupID");
	
	/**
	 * The key of the bundle artifact
	 */
	public static final String artifactIDKey = new String("artifactID");
	
	/**
	 * The key of the public key of the certificate 
	 */
	public static final String certIDKey = new String("certID");
	
	/**
	 * The key of the public key of the security 
	 */
	public static final String securityIDKey = new String("securityID");
	
	/**
	 * The key of the session flags 
	 */
	public static final String sessionFlagsKey = new String("sessionFlags");
	
	/**
	 * The prefix of the attribute
	 */
	public static final String attributePrefix = new String("attr:");
	
	/** The logger */
	static Logger logger = LoggerFactory.getLogger(SessionControl.class);
	
	/**
	 * Get all fields from the properties
	 */
	void getAll();
	
	/**
	 * Set all fields to the properties
	 */
	void setAll();
	
	/**
	 * Increments the counter
	 */
	void incRequestCounter();
	
	/**
	 * Tests whether the session is valid or not.
	 * 
	 * @return
	 */
	boolean isValid();
	
	/**
	 * Returns true if the WebApp is stateful.
	 * @return
	 */
	boolean isStateful();
	
	/**
	 * Ensure the session is a new session
	 * 
	 * @param factory The factory to get a new session
	 * 
	 * @return
	 */
	boolean ensureNew(Factory factory);
	
	/**
	 * Generates the public key of a certificate into certID
	 */
	void generateCert();
	
	/**
	 * Updates the data of the byte field if the certificate has changed.
	 */
	void updateBinaryData();
	
	/**
	 * Decodes the public key of the certificate to a BigInteger
	 * 
	 * @return
	 */
	BigInteger decodeCert();
	
	/**
	 * Decodes the security id to a BigInteger
	 * 
	 * @return
	 */
	BigInteger decodeSecurityID();
	
	//
	// 
	//
	
	/**
	 * Sets certID as short time password into the value
	 * certIDKey of the '0' session of the mandator.
	 * Needs the certID and the mandatorID set.
	 * 
	 * <p>The shortTimePassword isn't decoded. It is the <b>certificate</b>.</p>
	 * 
	 * @param context The bundle context to get the service.
	 */
	void setShortTimePassword();
	
	/**
	 * Gets a list of short time passwords from the value certIDKey
	 * of the '0' session of the mandator. The list will be created
	 * if it is not present.
	 * 
	 * <p>The shortTimePassword isn't decoded. It is the <b>certificate</b>.</p>
	 * 
	 * @param context The bundle context to get the service.
	 * @return
	 */
	List<String> getShortTimePassword();
	
	/**
	 * Watches the list of short time passwords
	 */
	void watchShortTimePassword();
	
	/**
	 * Saves the session data to the '0' session.
	 */
	void saveTempSession();
	
	/**
	 * Loads the session data from the '0' session
	 */
	void loadTempSession();
	
	/**
	 * Clears the session data from the |0| session
	 */
	void clearTempSession();
	
	/**
	 * Adds a time based call to the logout servlet
	 * 
	 * @param sessionId The current session id
	 * @param uri The URL to call
	 * @param timeout The timeout
	 */
	void addAutoLogout(String sessionId, final String uri, long timeout);
	
	//
	//
	//
	
	/**
	 * Reads or writes the visitCount
	 * 
	 * @param a READ | WRITE
	 */
	void visitCount(Action a);
	
	/**
	 * Reads or writes the mandatorID
	 * 
	 * @param a READ | WRITE
	 */
	void mandatorID(Action a);
	
	/**
	 * Reads or writes the userID
	 * 
	 * @param a READ | WRITE
	 */
	void userID(Action a);
	
	/**
	 * Reads or writes the certID
	 * 
	 * @param a READ | WRITE
	 */
	void certID(Action a);
	
	/**
	 * Reads or writes the securityID
	 * 
	 * @param a READ | WRITE
	 */
	void securityID(Action a);
	
	/**
	 * Reads or writes the groupID
	 * 
	 * @param a READ | WRITE
	 */
	void groupID(Action a);
	
	/**
	 * Reads or writes the artifactID
	 * 
	 * @param a READ | WRITE
	 */
	void artifactID(Action a);
	
	/**
	 * Reads or writes the attributes
	 * 
	 * @param a READ | WRITE
	 */
	void attributes(Action a);

	/**
	 * Gets the field visitCount
	 * 
	 * @return the visitCount
	 */
	Integer getVisitCount();


	/**
	 * Sets the field visitCount
	 * 
	 * @param visitCount the visitCount to set
	 */
	void setVisitCount(Integer visitCount);

	/**
	 * Sets the field visitCount
	 * 
	 * @param visitCount the visitCount to set
	 * @param c The condition when the set should be done
	 */
	void setVisitCount(Integer visitCount, Condition c);


	/**
	 * Gets the field mandatorID
	 * 
	 * @return the mandatorID
	 */
	String getMandatorID();


	/**
	 * Sets the field visitCount
	 * 
	 * @param mandatorID the mandatorID to set
	 */
	void setMandatorID(String mandatorID);

	/**
	 * Sets the field visitCount
	 * 
	 * @param mandatorID the mandatorID to set
	 * @param c The condition when the set should be done
	 */
	void setMandatorID(String mandatorID, Condition c);


	/**
	 * Gets the field userID
	 * 
	 * @return the userID
	 */
	String getUserID();


	/**
	 * Sets the field userID
	 * 
	 * @param userID the userID to set
	 */
	void setUserID(String userID);

	/**
	 * Sets the field userID
	 * 
	 * @param userID the userID to set
	 * @param c The condition when the set should be done
	 */
	void setUserID(String userID, Condition c);


	/**
	 * Gets the field groupID
	 * 
	 * @return the groupID
	 */
	String getGroupID();


	/**
	 * Sets the field groupID
	 * 
	 * @param groupID the groupID to set
	 */
	void setGroupID(String groupID);

	/**
	 * Sets the field groupID
	 * 
	 * @param groupID the groupID to set
	 * @param c The condition when the set should be done
	 */
	void setGroupID(String groupID, Condition c);


	/**
	 * Gets the field artifactID
	 * 
	 * @return the artifactID
	 */
	String getArtifactID();


	/**
	 * Sets the field artifactID
	 * 
	 * @param artifactID the artifactID to set
	 */
	void setArtifactID(String artifactID);

	/**
	 * Sets the field artifactID
	 * 
	 * @param artifactID the artifactID to set
	 * @param c The condition when the set should be done
	 */
	void setArtifactID(String artifactID, Condition c);


	/**
	 * Gets the field certID
	 * 
	 * @return the certID
	 */
	String getCertID();

	/**
	 * Sets the field certID
	 * 
	 * @param certID the certID to set
	 */
	void setCertID(String certID);

	/**
	 * Sets the field certID
	 * 
	 * @param certID the certID to set
	 * @param c The condition when the set should be done
	 */
	void setCertID(String certID, Condition c);

	/**
	 * Gets the field securityID
	 * 
	 * @return the securityID
	 */
	String getSecurityID();

	/**
	 * Sets the field securityID
	 * 
	 * @param securityID the securityID to set
	 */
	void setSecurityID(String securityID);

	/**
	 * Sets the field securityID
	 * 
	 * @param securityID the securityID to set
	 * @param c The condition when the set should be done
	 */
	void setSecurityID(String securityID, Condition c);
	
	/**
	 * Gets a sorted array with all attribute names in ascending order.
	 * 
	 * @return
	 */
	String[] getAttributeNames();
	
	/**
	 * Gets an attribute
	 * 
	 * @param key The key of the attribute
	 * @return
	 */
	Object getAttribute(String key);
	
	/**
	 * Sets an attribute
	 * 
	 * @param key The key of the attribute
	 * @param value The value of the attribute
	 */
	void setAttribute(String key, Object value);
	
	/**
	 * Sets an attribute
	 * 
	 * @param key The key of the attribute
	 * @param value The value of the attribute
	 * @param c The condition when the set should be done
	 */
	void setAttribute(String key, Object value, Condition c);
	
	/**
	 * @return the passwordTimeout
	 */
	Integer getPasswordTimeout();

	/**
	 * @param passwordTimeout the passwordTimeout to set
	 */
	void setPasswordTimeout(Integer passwordTimeout);

	/**
	 * @return the sessionFlags
	 */
	Long getSessionFlags();

	/**
	 * @param sessionFlags the sessionFlags to set
	 */
	void setSessionFlags(Long sessionFlags);

	/**
	 * @param flag The flag to set or reset
	 * @param value The value; true to set, false to reset the flag
	 */
	void setSessionFlags(int flag, boolean value);
	
	/**
	 * The interface to create a new session
	 * @author Stefan
	 *
	 */
	public static interface Factory {
		
		Object create();
	}
	
	/**
	 * Converts the buffer to an URL save base 64 String.
	 * 
	 * @param buffer
	 * @return
	 */
	String toBase64(byte[] buffer);
	
	/**
	 * Converts a base 64 String to a byte array.
	 * 
	 * @param data
	 * @return
	 */
	byte[] fromBase64(String data);
	
	/**
	 * The first index of the path part in the URL
	 * Default is 0, but if a mandatorId and a uuId is transmitted
	 * in the URL the value must be 2.
	 * 
	 * @return The start index of the path in the URL
	 */
	int getPathStartIndex();

}
