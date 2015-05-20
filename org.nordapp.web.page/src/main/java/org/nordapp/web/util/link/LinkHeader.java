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


public interface LinkHeader {
	
	/**
	 * @return the link_protocol
	 */
	String getLink_protocol();

	/**
	 * @param link_protocol the link_protocol to set
	 */
	void setLink_protocol(String link_protocol);

	/**
	 * @return the link_host
	 */
	String getLink_host();

	/**
	 * @param link_host the link_host to set
	 */
	void setLink_host(String link_host);

	/**
	 * @return the link_port
	 */
	int getLink_port();

	/**
	 * @param link_port the link_port to set
	 */
	void setLink_port(int link_port);

	/**
	 * @return the link_servlet
	 */
	String getLink_servlet();

	/**
	 * @param link_servlet the link_servlet to set
	 */
	void setLink_servlet(String link_servlet);

	/**
	 * @return the link_path
	 */
	String getLink_path();

	/**
	 * @param link_path the link_path to set
	 */
	void setLink_path(String link_path);
	
	/**
	 * @return the link_mandator
	 */
	String getLink_mandator();

	/**
	 * @param link_mandator the link_mandator to set
	 */
	void setLink_mandator(String link_mandator);
	
	/**
	 * @return the link_uuid
	 */
	String getLink_uuid();

	/**
	 * @param link_uuid the link_uuid to set
	 */
	void setLink_uuid(String link_uuid);

	/**
	 * Adds a link
	 * 
	 * @param href The href
	 * @param rel The relation (e.g self, next, ...)
	 * @param verb The verb (get, post)
	 */
	void add(String href, String rel, String verb);
	
	/**
	 * Adds a link
	 * 
	 * @param href The href
	 * @param rel The relation (e.g self, next, ...)
	 */
	void add(String href, String rel);
	
	/**
	 * Adds a link
	 * 
	 * @param href The href
	 * @param rel The relation (e.g self, next, ...)
	 * @param type The type (mime) of the link
	 * @param title The title of the link
	 * @param verb The verb (get, post)
	 */
	void add(String href, String rel, String type, String title, String verb);

}
