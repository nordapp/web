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
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.codec.binary.Base64;
import org.i3xx.step.mongo.core.util.IdGen;

public abstract class AbstractControlImpl implements SessionControl {
	
	/** The bit mask of the time prefix default:0xFFFFFF */
	public static final int maskTimePrefix = 0xFFFFFF;
	
	/** The size of the time prefix default:6 */
	public static final int sizeTimePrefix = 6;
	
	/** 
	 * The first index of the path part in the URL
	 * Default is 0, but if a mandatorId and a uuId is transmitted
	 * in the URL the value must be 2.
	 */
	protected int pathStartIndex;
	
	//The session counter
	protected Integer visitCount;
	//The short password timeout
	protected Integer passwordTimeout;
	//The session flags
	protected Long sessionFlags;
	//The mandator
	protected String mandatorID;
	//The user
	protected String userID;
	//The group
	protected String groupID;
	//The artifact
	protected String artifactID;
	//The certificate
	protected String certID;
	//The binary data
	protected byte[] field0;
	//The security session id
	protected String securityID;
	
	// The context properties change
	protected Map<String, Object> properties;
	
	// The session attributes
	protected Map<String, Object> attributes;

	public AbstractControlImpl() {
		
		this.pathStartIndex = 0;
		
		this.properties = new HashMap<String, Object>();
		this.attributes = new HashMap<String, Object>();
		
		setVisitCount(new Integer(0));
		setPasswordTimeout(new Integer(1000));
		setSessionFlags(new Long(0));
		setMandatorID(new String(""));
		setUserID(new String("anonymous"));
		setGroupID(new String(""));
		setArtifactID(new String(""));
		setCertID(new String(""));
	}
	
	/**
	 * Get all fields from the properties
	 */
	public void getAll() {
		visitCount(Action.READ);
		passwordTimeout(Action.READ);
		sessionFlags(Action.READ);
		userID(Action.READ);
		mandatorID(Action.READ);
		groupID(Action.READ);
		artifactID(Action.READ);
		certID(Action.READ);
		securityID(Action.READ);
	}
	
	/**
	 * Set all fields to the properties
	 */
	public void setAll() {
		visitCount(Action.WRITE);
		passwordTimeout(Action.WRITE);
		sessionFlags(Action.WRITE);
		mandatorID(Action.WRITE);
		userID(Action.WRITE);
		groupID(Action.WRITE);
		artifactID(Action.WRITE);
		certID(Action.WRITE);
		securityID(Action.WRITE);
	}
	
	/* (non-Javadoc)
	 * @see org.nordapp.web.servlet.SessionControl#isStateful()
	 */
	public boolean isStateful() {
		long sf = sessionFlags==null ? 0 : sessionFlags.longValue();
		return ( (sf & FLAG_SESSION_IS_STATEFUL) == FLAG_SESSION_IS_STATEFUL );
	}
	
	/**
	 * Increments the counter
	 */
	public void incRequestCounter() {
		//
		visitCount = new Integer( visitCount==null ? 1 : visitCount.intValue()+1 );
	}
	
	/**
	 * Generates the public key of a certificate into certID
	 */
	public void generateCert() {
		UUID uuid = UUID.randomUUID();
		byte[] buffer = ByteBuffer.allocate(16)
				.putLong(0, uuid.getMostSignificantBits())
				.putLong(8, uuid.getLeastSignificantBits())
				.array();		
		
		field0 = buffer;
		certID = Base64.encodeBase64URLSafeString(buffer);
	}
	
	/**
	 * Updates the data of the byte field if the certificate has changed.
	 */
	public void updateBinaryData() {
		byte[] buffer = Base64.decodeBase64( certID );
		field0 = buffer;
	}
	
	/**
	 * Decodes the public key of the certificate to a BigInteger
	 * 
	 * @return
	 */
	public BigInteger decodeCert() {
		byte[] buffer = Base64.decodeBase64( certID );
		BigInteger id = new BigInteger(buffer);
		
		//certID = Base64.encodeBase64URLSafeString(buffer);
		return id;
	}
	
	/**
	 * Decodes the security id to a BigInteger
	 * 
	 * @return
	 */
	public BigInteger decodeSecurityID() {
		return IdGen.fromString(securityID).toBigInteger();
	}
	
	/**
	 * Gets the byte field of the certificate
	 * 
	 * @return
	 */
	protected byte[] getField0() {
		return field0;
	}
	
	//
	//
	//
	
	/**
	 * Reads or writes the visitCount
	 * 
	 * @param a READ | WRITE
	 */
	public void visitCount(Action a) {
		if(a==Action.READ) {
			visitCount = (Integer)properties.get(visitCountKey);
		}else{
			properties.put(visitCountKey, visitCount);
		}
	}
	
	/**
	 * Reads or writes the passwordTimeout
	 * 
	 * @param a READ | WRITE
	 */
	public void passwordTimeout(Action a) {
		if(a==Action.READ) {
			passwordTimeout = (Integer)properties.get(passwordTimeoutKey);
		}else{
			properties.put(passwordTimeoutKey, passwordTimeout);
		}
	}
	
	/**
	 * Reads or writes the passwordTimeout
	 * 
	 * @param a READ | WRITE
	 */
	public void sessionFlags(Action a) {
		if(a==Action.READ) {
			sessionFlags = (Long)properties.get(sessionFlagsKey);
		}else{
			properties.put(sessionFlagsKey, sessionFlags);
		}
	}
	
	/**
	 * Reads or writes the mandatorID
	 * 
	 * @param a READ | WRITE
	 */
	public void mandatorID(Action a) {
		if(a==Action.READ) {
			mandatorID = (String)properties.get(mandatorIDKey);
		}else{
			properties.put(mandatorIDKey, mandatorID);
		}
	}
	
	/**
	 * Reads or writes the userID
	 * 
	 * @param a READ | WRITE
	 */
	public void userID(Action a) {
		if(a==Action.READ) {
			userID = (String)properties.get(userIDKey);
		}else{
			properties.put(userIDKey, userID);
		}
	}
	
	/**
	 * Reads or writes the certID
	 * 
	 * @param a READ | WRITE
	 */
	public void certID(Action a) {
		if(a==Action.READ) {
			certID = (String)properties.get(certIDKey);
		}else{
			properties.put(certIDKey, certID);
		}
	}
	
	/**
	 * Reads or writes the securityID
	 * 
	 * @param a READ | WRITE
	 */
	public void securityID(Action a) {
		if(a==Action.READ) {
			securityID = (String)properties.get(securityIDKey);
		}else{
			properties.put(securityIDKey, securityID);
		}
	}
	
	/**
	 * Reads or writes the groupID
	 * 
	 * @param a READ | WRITE
	 */
	public void groupID(Action a) {
		if(a==Action.READ) {
			groupID = (String)properties.get(groupIDKey);
		}else{
			properties.put(groupIDKey, groupID);
		}
	}
	
	/**
	 * Reads or writes the artifactID
	 * 
	 * @param a READ | WRITE
	 */
	public void artifactID(Action a) {
		if(a==Action.READ) {
			artifactID = (String)properties.get(artifactIDKey);
		}else{
			properties.put(artifactIDKey, artifactID);
		}
	}
	
	/**
	 * Reads or writes the attributes
	 * 
	 * @param a READ | WRITE
	 */
	public void attributes(Action a) {
		if(a==Action.READ) {
			Iterator<String> names = properties.keySet().iterator();
			while(names.hasNext()) {
				String attrName = names.next();
				if(attrName.startsWith(attributePrefix)){
					attrName = attrName.substring(attributePrefix.length());
					Object value = properties.get(attrName);
					attributes.put(attrName, value);
				}//fi
			}//for
		}else{
			for(Map.Entry<String, Object> e : attributes.entrySet()){
				properties.put(attributePrefix+e.getKey(), e.getValue());
			}//for
		}//fi
	}

	/**
	 * Gets the field visitCount
	 * 
	 * @return the visitCount
	 */
	public Integer getVisitCount() {
		return visitCount;
	}


	/**
	 * Sets the field visitCount
	 * 
	 * @param visitCount the visitCount to set
	 */
	public void setVisitCount(Integer visitCount) {
		this.visitCount = visitCount;
	}

	/**
	 * Sets the field visitCount
	 * 
	 * @param visitCount the visitCount to set
	 * @param c The condition when the set should be done
	 */
	public void setVisitCount(Integer visitCount, Condition c) {
		if( (c==Condition.IF_NULL && this.visitCount==null) ||
				(c==Condition.IF_NOT_NULL && this.visitCount!=null) ||
				(c==Condition.IF_VAR_NOT_NULL && visitCount!=null) ||
				(c==Condition.IF_SET_NOT_NULL && this.visitCount!=null && visitCount!=null) )
		this.visitCount = visitCount;
	}


	/**
	 * Gets the field mandatorID
	 * 
	 * @return the mandatorID
	 */
	public String getMandatorID() {
		return mandatorID;
	}


	/**
	 * Sets the field visitCount
	 * 
	 * @param mandatorID the mandatorID to set
	 */
	public void setMandatorID(String mandatorID) {
		this.mandatorID = mandatorID;
	}

	/**
	 * Sets the field visitCount
	 * 
	 * @param mandatorID the mandatorID to set
	 * @param c The condition when the set should be done
	 */
	public void setMandatorID(String mandatorID, Condition c) {
		if( (c==Condition.IF_NULL && this.mandatorID==null) ||
				(c==Condition.IF_NOT_NULL && this.mandatorID!=null) ||
				(c==Condition.IF_VAR_NOT_NULL && mandatorID!=null) ||
				(c==Condition.IF_SET_NOT_NULL && this.mandatorID!=null && mandatorID!=null) )
		this.mandatorID = mandatorID;
	}


	/**
	 * Gets the field userID
	 * 
	 * @return the userID
	 */
	public String getUserID() {
		return userID;
	}


	/**
	 * Sets the field userID
	 * 
	 * @param userID the userID to set
	 */
	public void setUserID(String userID) {
		this.userID = userID;
	}

	/**
	 * Sets the field userID
	 * 
	 * @param userID the userID to set
	 * @param c The condition when the set should be done
	 */
	public void setUserID(String userID, Condition c) {
		if( (c==Condition.IF_NULL && this.userID==null) ||
				(c==Condition.IF_NOT_NULL && this.userID!=null) ||
				(c==Condition.IF_VAR_NOT_NULL && userID!=null) ||
				(c==Condition.IF_SET_NOT_NULL && this.userID!=null && userID!=null) )
		this.userID = userID;
	}


	/**
	 * Gets the field groupID
	 * 
	 * @return the groupID
	 */
	public String getGroupID() {
		return groupID;
	}


	/**
	 * Sets the field groupID
	 * 
	 * @param groupID the groupID to set
	 */
	public void setGroupID(String groupID) {
		this.groupID = groupID;
	}

	/**
	 * Sets the field groupID
	 * 
	 * @param groupID the groupID to set
	 * @param c The condition when the set should be done
	 */
	public void setGroupID(String groupID, Condition c) {
		if( (c==Condition.IF_NULL && this.groupID==null) ||
				(c==Condition.IF_NOT_NULL && this.groupID!=null) ||
				(c==Condition.IF_VAR_NOT_NULL && groupID!=null) ||
				(c==Condition.IF_SET_NOT_NULL && this.groupID!=null && groupID!=null) )
		this.groupID = groupID;
	}


	/**
	 * Gets the field artifactID
	 * 
	 * @return the artifactID
	 */
	public String getArtifactID() {
		return artifactID;
	}


	/**
	 * Sets the field artifactID
	 * 
	 * @param artifactID the artifactID to set
	 */
	public void setArtifactID(String artifactID) {
		this.artifactID = artifactID;
	}

	/**
	 * Sets the field artifactID
	 * 
	 * @param artifactID the artifactID to set
	 * @param c The condition when the set should be done
	 */
	public void setArtifactID(String artifactID, Condition c) {
		if( (c==Condition.IF_NULL && this.artifactID==null) ||
				(c==Condition.IF_NOT_NULL && this.artifactID!=null) ||
				(c==Condition.IF_VAR_NOT_NULL && artifactID!=null) ||
				(c==Condition.IF_SET_NOT_NULL && this.artifactID!=null && artifactID!=null) )
		this.artifactID = artifactID;
	}


	/**
	 * Gets the field certID
	 * 
	 * @return the certID
	 */
	public String getCertID() {
		return certID;
	}


	/**
	 * Sets the field certID
	 * 
	 * @param certID the certID to set
	 */
	public void setCertID(String certID) {
		this.certID = certID;
	}

	/**
	 * Sets the field certID
	 * 
	 * @param certID the certID to set
	 * @param c The condition when the set should be done
	 */
	public void setCertID(String certID, Condition c) {
		if( (c==Condition.IF_NULL && this.certID==null) ||
				(c==Condition.IF_NOT_NULL && this.certID!=null) ||
				(c==Condition.IF_VAR_NOT_NULL && certID!=null) ||
				(c==Condition.IF_SET_NOT_NULL && this.certID!=null && certID!=null) )
		this.certID = certID;
	}

	/**
	 * Gets the field securityID
	 * 
	 * @return the securityID
	 */
	public String getSecurityID() {
		return securityID;
	}


	/**
	 * Sets the field securityID
	 * 
	 * @param securityID the securityID to set
	 */
	public void setSecurityID(String securityID) {
		this.securityID = securityID;
	}

	/**
	 * Sets the field securityID
	 * 
	 * @param securityID the securityID to set
	 * @param c The condition when the set should be done
	 */
	public void setSecurityID(String securityID, Condition c) {
		if( (c==Condition.IF_NULL && this.securityID==null) ||
				(c==Condition.IF_NOT_NULL && this.securityID!=null) ||
				(c==Condition.IF_VAR_NOT_NULL && securityID!=null) ||
				(c==Condition.IF_SET_NOT_NULL && this.securityID!=null && securityID!=null) )
		this.securityID = securityID;
	}
	
	/**
	 * Gets a sorted array with all attribute names in ascending order.
	 * 
	 * @return
	 */
	public String[] getAttributeNames() {
		String[] arr = attributes.keySet().toArray(new String[attributes.size()]);
		Arrays.sort(arr);
		
		return arr;
	}
	
	/**
	 * Gets an attribute
	 * 
	 * @param key The key of the attribute
	 * @return
	 */
	public Object getAttribute(String key) {
		return attributes.get(key);
	}
	
	/**
	 * Sets an attribute
	 * 
	 * @param key The key of the attribute
	 * @param value The value of the attribute
	 */
	public void setAttribute(String key, Object value) {
		attributes.put(key, value);
	}
	
	/**
	 * Sets an attribute
	 * 
	 * @param key The key of the attribute
	 * @param value The value of the attribute
	 * @param c The condition when the set should be done
	 */
	public void setAttribute(String key, Object value, Condition c) {
		if( (c==Condition.IF_NULL && attributes.get(key)==null) ||
				(c==Condition.IF_NOT_NULL && attributes.get(key)!=null) ||
				(c==Condition.IF_VAR_NOT_NULL && value!=null) ||
				(c==Condition.IF_SET_NOT_NULL && attributes.get(key)!=null && value!=null) )
		attributes.put(key, value);
	}
	
	//
	// The default size is 6 chars and a mask of FFFFFF (16.777.215 ms => 04:39:37,215)
	//
	
	/**
	 * @return
	 */
	protected String makeTimePrefix() {
		long raw = System.currentTimeMillis();
		int time = (int)(raw & maskTimePrefix); //0-65535
		
		StringBuffer buf = new StringBuffer( Integer.toHexString(time) );
		for(int i=buf.length();i<sizeTimePrefix;i++)
			buf.insert(0, '0');
		
		return buf.toString();
	}
	
	/**
	 * @param prefix
	 * @return
	 */
	protected int getTime(String prefix) {
		
		String pre = prefix.substring(0, sizeTimePrefix);
		int ref = Integer.valueOf(pre, 16); //0-16383
		
		long raw = System.currentTimeMillis();
		int time = (int)(raw & maskTimePrefix); //0-16383
		
		// 0 1 2 3 4 0 1 2 3 4
		// 0-1-2
		//   1-2-3
		//     2-3-4
		//       3-4-0
		//         4-0-1
		//           0-1-2
		
		if(ref < time) {
			return (time-ref);
		}else
		if(ref > time){
			return (0x3FFF-ref+time);
		}else
		return 0;
	}

	/**
	 * @return the passwordTimeout
	 */
	public Integer getPasswordTimeout() {
		return passwordTimeout;
	}

	/**
	 * @param passwordTimeout the passwordTimeout to set
	 */
	public void setPasswordTimeout(Integer passwordTimeout) {
		this.passwordTimeout = passwordTimeout;
	}

	/**
	 * Sets the field passwordTimeout
	 * 
	 * @param passwordTimeout the passwordTimeout to set
	 * @param c The condition when the set should be done
	 */
	public void setPasswordTimeout(Integer passwordTimeout, Condition c) {
		if( (c==Condition.IF_NULL && this.passwordTimeout==null) ||
				(c==Condition.IF_NOT_NULL && this.passwordTimeout!=null) ||
				(c==Condition.IF_VAR_NOT_NULL && passwordTimeout!=null) ||
				(c==Condition.IF_SET_NOT_NULL && this.passwordTimeout!=null && passwordTimeout!=null) )
		this.passwordTimeout = passwordTimeout;
	}

	/**
	 * @return the sessionFlags
	 */
	public Long getSessionFlags() {
		return sessionFlags;
	}

	/**
	 * @param sessionFlags the sessionFlags to set
	 */
	public void setSessionFlags(Long sessionFlags) {
		this.sessionFlags = sessionFlags;
	}

	/**
	 * @param flag The flag to set or reset
	 * @param value The value; true to set, false to reset the flag
	 */
	public void setSessionFlags(int flag, boolean value) {
		long sf = sessionFlags==null ? 0 : sessionFlags.longValue();
		if(value) {
			sf |= flag;
		}else{
			sf &= (~flag);
		}
		sessionFlags = new Long(sf);
	}
	
	//
	// To have it in the interface
	//
	
	/**
	 * Converts the buffer to an URL save base 64 String.
	 * 
	 * @param buffer
	 * @return
	 */
	public String toBase64(byte[] buffer) {
		return Base64.encodeBase64URLSafeString(buffer);
	}
	
	/**
	 * Converts a base 64 String to a byte array.
	 * 
	 * @param data
	 * @return
	 */
	public byte[] fromBase64(String data) {
		return Base64.decodeBase64( data );
	}
	
	/**
	 * The first index of the path part in the URL
	 * Default is 0, but if a mandatorId and a uuId is transmitted
	 * in the URL the value must be 2.
	 * 
	 * @return The start index of the path in the URL
	 */
	public int getPathStartIndex() { return this.pathStartIndex; }
	
}
