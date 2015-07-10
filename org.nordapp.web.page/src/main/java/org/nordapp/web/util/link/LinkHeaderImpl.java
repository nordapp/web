package org.nordapp.web.util.link;

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


import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class LinkHeaderImpl implements LinkHeader {
	
	/** The default link protocol */
	private String link_protocol;
	
	/** The default link host */
	private String link_host;
	
	/** The default link port */
	private int link_port;
	
	/** The destination to the servlet to process the link */
	private String link_servlet;
	
	/** The current resource mandator */
	private String link_mandator;
	
	/** The current resource uuid */
	private String link_uuid;
	
	/** The default link root path */
	private String link_path;
	
	/** The parts of the link header */
	private List<LinkRelation> relations;
	
	public LinkHeaderImpl() {
		relations = new ArrayList<LinkRelation>();
		
		this.link_protocol = "http";
		this.link_host = "localhost";
		this.link_port = -1;
		this.link_servlet = "";
		this.link_mandator = null;
		this.link_uuid = "0";
		this.link_path = null;
	}
	
	/**
	 * @return the link_protocol
	 */
	public String getLink_protocol() {
		return link_protocol;
	}

	/**
	 * @param link_protocol the link_protocol to set
	 */
	public void setLink_protocol(String link_protocol) {
		if(link_protocol!=null && link_protocol.indexOf('/')>-1) {
			//skip version
			int i = link_protocol.indexOf('/');
			this.link_protocol = link_protocol.substring(0, i).toLowerCase();
		}else if(link_protocol!=null) {
			this.link_protocol = link_protocol.toLowerCase();
		}else{
			this.link_protocol = link_protocol;
		}
	}

	/**
	 * @return the link_host
	 */
	public String getLink_host() {
		return link_host;
	}

	/**
	 * @param link_host the link_host to set
	 */
	public void setLink_host(String link_host) {
		this.link_host = link_host;
	}

	/**
	 * @return the link_port
	 */
	public int getLink_port() {
		return link_port;
	}

	/**
	 * @param link_port the link_port to set
	 */
	public void setLink_port(int link_port) {
		this.link_port = link_port;
	}

	/**
	 * @return the link_servlet
	 */
	public String getLink_servlet() {
		return link_servlet;
	}

	/**
	 * @param link_servlet the link_servlet to set
	 */
	public void setLink_servlet(String link_servlet) {
		// The '/' is set by complete()
		if(link_servlet!=null && link_servlet.charAt(0)=='/') {
			this.link_servlet = link_servlet.substring(1);
		}else{
			this.link_servlet = link_servlet;
		}
	}

	/**
	 * @return the link_path
	 */
	public String getLink_path() {
		return link_path;
	}

	/**
	 * @param link_path the link_path to set
	 */
	public void setLink_path(String link_path) {
		this.link_path = link_path;
	}
	
	/**
	 * @return the link_mandator
	 */
	public String getLink_mandator() {
		return link_mandator;
	}

	/**
	 * @param link_mandator the link_mandator to set
	 */
	public void setLink_mandator(String link_mandator) {
		this.link_mandator = link_mandator;
	}
	
	/**
	 * @return the link_uuid
	 */
	public String getLink_uuid() {
		return link_uuid;
	}

	/**
	 * @param link_uuid the link_uuid to set
	 */
	public void setLink_uuid(String link_uuid) {
		this.link_uuid = link_uuid;
	}

	/**
	 * Adds a link
	 * 
	 * @param href The href
	 * @param rel The relation (e.g self, next, ...)
	 * @param verb The verb (get, post)
	 */
	public void add(String href, String rel, String verb) {
		LinkRelation link = new LinkRelation();
		link.getParameters().put("href", complete(href));
		link.getParameters().put("rel", rel);
		link.getParameters().put("verb", verb);
		relations.add(link);
	}
	
	/**
	 * Adds a link
	 * 
	 * @param href The href
	 * @param rel The relation (e.g self, next, ...)
	 */
	public void add(String href, String rel) {
		LinkRelation link = new LinkRelation();
		link.getParameters().put("href", complete(href));
		link.getParameters().put("rel", rel);
		relations.add(link);
	}
	
	/**
	 * Adds a link
	 * 
	 * @param href The href
	 * @param rel The relation (e.g self, next, ...)
	 * @param type The type (mime) of the link
	 * @param title The title of the link
	 * @param verb The verb (get, post)
	 */
	public void add(String href, String rel, String type, String title, String verb) {
		LinkRelation link = new LinkRelation();
		link.getParameters().put("href", href);
		link.getParameters().put("rel", rel);
		link.getParameters().put("type", type);
		link.getParameters().put("title", title);
		link.getParameters().put("verb", verb);
		relations.add(link);
	}
	
	/**
	 * Completes a link beginning with '/'
	 * protocol '://' host [ ':' port ]? '/' path '/' link 
	 * 
	 * @param link The link to be completed
	 * @return The completed link
	 */
	protected String complete(String link) {
		if(link!=null && link.charAt(0)=='/') {
			StringBuffer buf = new StringBuffer();
			buf.append(link_protocol);
			buf.append("://");
			buf.append(link_host);
			if(link_port>-1) {
				buf.append(':');
				buf.append(link_port);
			}//fi
			if(link_servlet!=null) {
				buf.append('/');
				buf.append(link_servlet);
			}//fi
			if(link_mandator!=null) {
				buf.append('/');
				buf.append(link_mandator);
			}//fi
			if(link_uuid!=null) {
				buf.append('/');
				buf.append(link_uuid);
			}//fi
			if(link_path!=null) {
				if(link_path.charAt(0)!='/')
					buf.append('/');
				buf.append(link_path);
			}//fi
			buf.append(link);
			
			return buf.toString();
		}else{
			return link;
		}//fi
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		
		if(relations.isEmpty())
			return "";
		
		StringBuffer buf = new StringBuffer();
		for(LinkRelation r : relations) {
			if(buf.length()>0)
				buf.append(',');
			
			Map<String, String> param = r.getParameters();
			buf.append('<');
			buf.append( param.get("href") );
			buf.append('>');
			
			for(Map.Entry<String,String> en : param.entrySet()) {
				String key = en.getKey();
				String val = en.getValue();
				
				if(key.equalsIgnoreCase("href")) {
					continue;
				}else{
					buf.append(';');
					buf.append(key);
					buf.append('=');
					buf.append('"');
					buf.append(val);
					buf.append('"');
				}
			}
		}
		
		return buf.toString();
	}
 }
