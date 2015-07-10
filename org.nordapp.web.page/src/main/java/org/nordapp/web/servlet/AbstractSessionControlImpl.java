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


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.i3xx.step.clock.service.model.ClockService;
import org.i3xx.step.clock.service.model.Notify;
import org.i3xx.step.due.service.impl.SessionServiceImpl;
import org.i3xx.step.due.service.model.Session;
import org.nordapp.web.util.NotifyImpl;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;

public abstract class AbstractSessionControlImpl extends AbstractControlImpl {

	
	// The bundle context
	private BundleContext context;
	
	public AbstractSessionControlImpl(BundleContext context) {
		super();
		
		this.context = context;
	}
	
	/**
	 * Watches the short time password
	 * @throws InvalidSyntaxException 
	 */
	public void watchShortTimePassword() {
		String cert = null;
		String symbol = "session-control-"+mandatorID+"-0";
		
		//return if the service exists
		String filter = "("+Notify.TIME_SYMBOL+"="+symbol+")";
		try {
			Collection<ServiceReference<Notify>> refs = context.getServiceReferences(Notify.class, filter);
			if( ! refs.isEmpty()) {
				logger.debug("SKIP: The notification of the symbol, filter:{} is already set.", filter);
				
				//the service may be restarted
				ServiceReference<ClockService> csref = context.getServiceReference(ClockService.class);
				ClockService clock = csref!=null ? context.getService(csref) : null;
				if(clock!=null && (! clock.hasMapping(symbol))) {
					clock.addMapping("/1 * * * *", symbol);
				}//fi
				
				return;
			}
		} catch (InvalidSyntaxException e) {
			logger.error(filter, e);
			return;
		}
		
		ServiceReference<ClockService> csref = context.getServiceReference(ClockService.class);
		ClockService clock = csref!=null ? context.getService(csref) : null;
		if(clock!=null) {
			//create the receiver of the time tick
			NotifyImpl timeTick = new NotifyImpl(){
				public void notify(Map<String, Object> param) {
					//Test session id
					Session mSession = SessionServiceImpl.getSession(context, cert, mandatorId, "0");
					if(mSession==null) {
						logger.warn("The '0' session of the mandator {} is not available;", mandatorID);
						resetService();
						clear();
						return;
					}//fi
					
					@SuppressWarnings("unchecked")
					List<String> shortTimeKeys = (List<String>)mSession.getValue(certIDKey);
					if(shortTimeKeys!=null){
						for(int i=0;i<shortTimeKeys.size();i++) {
							String key = shortTimeKeys.get(i);
							int time = getTime(key);
							if(time>passwordTimeout){
								shortTimeKeys.remove(i);
								logger.trace("Remove short time password:'{}', timeout:{}", key, time);
								i--;
							}//fi
						}//for
					}else{
						logger.warn("The session '0' of the mandator {} has no shortTimeKeys.", mandatorID);
					}//fi
				}//
			};
			timeTick.setContext(context);
			timeTick.setTimeSymbol(symbol);
			timeTick.setCert(cert);
			timeTick.setMandatorId(mandatorID);
			timeTick.setSessionId("0");
			timeTick.setService();
			
			//initialize the clock
			if(clock.hasMapping(symbol))
				clock.removeMapping(symbol);
			
			clock.addMapping("/1 * * * *", symbol);
		}else{
			logger.warn("No available clock service.");
		}//fi
	}
	
	/**
	 * Sets certID as short time password into the value
	 * certIDKey of the '0' session of the mandator.
	 * Needs the certID and the mandatorID set.
	 */
	public void setShortTimePassword() {
		String cert = null; //TODO
		//The '0' session of the mandator
		Session mSession = SessionServiceImpl.getSession(context, cert, mandatorID, "0");
		if(mSession==null) {
			logger.warn("The '0' session of the mandator {} is not available;", mandatorID);
			return;
		}
		Object obj = mSession.getValue(certIDKey);
		
		@SuppressWarnings("unchecked")
		List<String> shortTimeKeys = (List<String>)obj;
		if(shortTimeKeys==null){
			shortTimeKeys = new ArrayList<String>();
			mSession.setValue(certIDKey, shortTimeKeys);
		}
		
		//
		String pwd = makeTimePrefix()+certID;
		shortTimeKeys.add(pwd);
		logger.trace("Add short time password:'{}'", pwd);
	}
	
	/**
	 * Gets a list of short time passwords from the value certIDKey
	 * of the '0' session of the mandator. The list will be created
	 * if it is not present.
	 * 
	 * @return The list of short time passwords
	 */
	public List<String> getShortTimePassword() {
		String cert = null;
		//The '0' session of the mandator
		Session mSession = SessionServiceImpl.getSession(context, cert, mandatorID, "0");
		if(mSession==null) {
			logger.warn("The '0' session of the mandator {} is not available;", mandatorID);
			return null;
		}
		@SuppressWarnings("unchecked")
		List<String> shortTimeKeys = (List<String>)mSession.getValue(certIDKey);
		if(shortTimeKeys==null){
			shortTimeKeys = new ArrayList<String>();
			mSession.setValue(certIDKey, shortTimeKeys);
		}
		
		List<String> result = new ArrayList<String>();
		if(shortTimeKeys!=null){
			for(int i=0;i<shortTimeKeys.size();i++) {
				String key = shortTimeKeys.get(i);
				int time = getTime(key);
				if(time<=passwordTimeout){
					String pwd = key.substring(sizeTimePrefix);
					logger.trace("Get short time password:'{}', timeout:{}", pwd, time);
					result.add( pwd );
				}else{
					String pwd = key.substring(sizeTimePrefix);
					logger.trace("Skip short time password:'{}', timeout:{}", pwd, time);
				}//fi
			}//for
		}//fi		
		
		return result;
	}
	
	/**
	 * Adds a call to the logout servlet
	 * 
	 * @param uri
	 * @param timeout
	 */
	public void addAutoLogout(String sessionId, final String uri, long timeout) {
		String cert = null;
		String symbol = "session-control-"+mandatorID+"-"+sessionId;
		
		//set the timeout
		Session mSession = SessionServiceImpl.getSession(context, cert, mandatorID, sessionId);
		if(mSession==null)
			return;
		
		//each login restarts the session runtime
		mSession.setValue(logonTimeoutKey, new Long(System.currentTimeMillis()+timeout));
		
		//return if the service exists
		String filter = "("+Notify.TIME_SYMBOL+"="+symbol+")";
		try {
			Collection<ServiceReference<Notify>> refs = context.getServiceReferences(Notify.class, filter);
			if( ! refs.isEmpty()) {
				logger.debug("SKIP: The notification of the symbol, filter:{} is already set.", filter);
				
				//the service may be restarted
				ServiceReference<ClockService> csref = context.getServiceReference(ClockService.class);
				ClockService clock = csref!=null ? context.getService(csref) : null;
				if(clock!=null && (! clock.hasMapping(symbol))) {
					clock.addMapping("/1 * * * *", symbol);
				}//fi
				
				return;
			}
		} catch (InvalidSyntaxException e) {
			logger.error(filter, e);
			return;
		}
		
		ServiceReference<ClockService> csref = context.getServiceReference(ClockService.class);
		ClockService clock = csref!=null ? context.getService(csref) : null;
		if(clock!=null) {
			//create the receiver of the time tick
			NotifyImpl timeTick = new NotifyImpl(){
				public void notify(Map<String, Object> param) {
					//
					// Test session id
					//
					// We suggest the logout may fail. In this case we try to logout
					// until the service is no longer available. If the service is not
					// available. We don't need to logout. (whiteboard)
					//
					Session mSession = SessionServiceImpl.getSession(context, cert, mandatorId, sessionId);
					if(mSession==null) {
						logger.debug("The '{}' session of the mandator {} is not available - stop logout.", sessionId, mandatorID);
						resetService();
						clear();
						return;
					}//fi
					
					Long logonTimeout = (Long)mSession.getValue(logonTimeoutKey);
					if(logonTimeout!=null){
						long remaining = logonTimeout.longValue()-System.currentTimeMillis();
						if(remaining<0) {
							//
							//TODO: call logout URI
							//
							try {
								URL url = new URL(uri);
								HttpURLConnection uc = (HttpURLConnection)url.openConnection();
								uc.setRequestMethod("GET");
								uc.setDoInput(true);
								uc.setDoOutput(false);
								uc.setUseCaches(false);
								BufferedReader in = new BufferedReader(new InputStreamReader(uc.getInputStream()));
								@SuppressWarnings("unused")
								String line;
								try{
									while ((line = in.readLine()) != null) {
										 //do nothing yet - just read
									}
								}catch(IOException io){
									logger.info("Logout fail; session: '{}', timeout: {}, remaining: {}, uri: {}, reason: {}.",
											sessionId, logonTimeout, remaining, uri, io);
								}finally{
									in.close();
								}
								
								logger.debug("Logout session: '{}', timeout: {}, remaining: {}, uri: {}", sessionId, logonTimeout, remaining, uri);
							} catch (Exception e) {
								logger.info("Logout fail; session: '{}', timeout: {}, remaining: {}, uri: {}, reason: {}.",
										sessionId, logonTimeout, remaining, uri, e);
							}
						}//fi
					}else{
						logger.warn("The session '{}' of the mandator {} has no logonTimeKey.", sessionId, mandatorID);
					}//fi
				}//
			};
			timeTick.setContext(context);
			timeTick.setTimeSymbol(symbol);
			timeTick.setCert(cert);
			timeTick.setMandatorId(mandatorID);
			timeTick.setSessionId(sessionId);
			timeTick.setService();
			
			//initialize the clock
			if(clock.hasMapping(symbol))
				clock.removeMapping(symbol);
			
			clock.addMapping("/1 * * * *", symbol);
		}else{
			logger.warn("No available clock service.");
		}//fi
	}
	
}
