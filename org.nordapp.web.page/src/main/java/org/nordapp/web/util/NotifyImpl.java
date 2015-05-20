package org.nordapp.web.util;

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


import java.util.Dictionary;
import java.util.Hashtable;

import org.i3xx.step.clock.service.model.Notify;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

public abstract class NotifyImpl implements Notify {
	
	/** The registration */
	protected ServiceRegistration<Notify> serviceRegistration;
	
	/** The bundle context */
	protected BundleContext context;
	
	/** The symbol to search in the timer's whiteboard pattern */
	protected String timeSymbol;
	
	/** The id of the corresponding mandator */
	protected String mandatorId;
	
	/** The id of the corresponding session */
	protected String sessionId;
	
	/** unused */
	protected String cert;
	
	public NotifyImpl() {
		this.context = null;
		this.serviceRegistration = null;
		this.timeSymbol = null;
		this.mandatorId = null;
		this.sessionId = null;
		this.cert = null;
	}
	
	public void clear() {
		this.context = null;
		this.serviceRegistration = null;
		this.timeSymbol = null;
		this.mandatorId = null;
		this.sessionId = null;
		this.cert = null;
	}
	
	public void setService() {
		Dictionary<String, Object> props = new Hashtable<String, Object>();
		props.put(TIME_SYMBOL, timeSymbol);
		serviceRegistration = context.registerService(Notify.class, this, props);
	}
	
	public void resetService() {
		serviceRegistration.unregister();
		serviceRegistration = null;
	}

	/**
	 * @return the context
	 */
	public BundleContext getContext() {
		return context;
	}

	/**
	 * @param context the context to set
	 */
	public void setContext(BundleContext context) {
		this.context = context;
	}

	/**
	 * @return the timeSymbol
	 */
	public String getTimeSymbol() {
		return timeSymbol;
	}

	/**
	 * @param timeSymbol the timeSymbol to set
	 */
	public void setTimeSymbol(String timeSymbol) {
		this.timeSymbol = timeSymbol;
	}

	/**
	 * @return the sessionId
	 */
	public String getSessionId() {
		return sessionId;
	}

	/**
	 * @param sessionId the sessionId to set
	 */
	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}

	/**
	 * @return the mandatorId
	 */
	public String getMandatorId() {
		return mandatorId;
	}

	/**
	 * @param mandatorId the mandatorId to set
	 */
	public void setMandatorId(String mandatorId) {
		this.mandatorId = mandatorId;
	}

	/**
	 * @return the cert
	 */
	public String getCert() {
		return cert;
	}

	/**
	 * @param cert the cert to set
	 */
	public void setCert(String cert) {
		this.cert = cert;
	}

}
