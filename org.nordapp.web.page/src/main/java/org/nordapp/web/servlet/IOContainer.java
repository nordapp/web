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


import java.util.LinkedHashMap;
import java.util.Map;

/**
 * This container is used for scripting
 * 
 * @author Stefan
 * 
 * Note: The IOContainer is NOT serializable
 */
public final class IOContainer {
	
	/**
	 * <p>The process UUID</p>
	 * 
	 * @see org.nordapp.web.servlet.ScriptVars
	 */
	private String processUUID;
	
	/**
	 * <p>The files of the request.</p>
	 * 
	 * <p>The map stores the types of the request handler. Use
	 * the API of the request handler to get the data.</p>
	 */
	Map<String, Object> files;
	
	/**
	 * <p>The fields of the request.</p>
	 * 
	 * <p>The map stores the types of the request handler. Use
	 * the API of the request handler to get the data.</p>
	 */
	Map<String, Object> fields;

	/**
	 * 
	 */
	public IOContainer() {
		setProcessUUID(null);
		files = new LinkedHashMap<String, Object>();
		fields = new LinkedHashMap<String, Object>();
	}
	
	/**
	 * @return the processUUID
	 */
	public String getProcessUUID() {
		return processUUID;
	}

	/**
	 * @param processUUID the processUUID to set
	 */
	public void setProcessUUID(String processUUID) {
		this.processUUID = processUUID;
	}
	
	/**
	 * @return The field names as an array
	 */
	public String[] getFieldnames() {
		return fields.keySet().toArray(new String[fields.size()]);
	}
	
	/**
	 * @param fieldname The field name
	 * @param field The field value
	 */
	public void setField(String fieldname, Object field) {
		fields.put(fieldname, field);
	}
	
	/**
	 * @param fieldname The field name
	 * @return The field value
	 */
	public Object getField(String fieldname) {
		return fields.get(fieldname);
	}
	
	/**
	 * @return The filenames as an array
	 */
	public String[] getFilenames() {
		return files.keySet().toArray(new String[fields.size()]);
	}
	
	/**
	 * @param filename The filename
	 * @param file The file
	 */
	public void setFile(String filename, Object file) {
		files.put(filename, file);
	}
	
	/**
	 * @param filename The filename
	 * @return The file
	 */
	public Object getFile(String filename) {
		return files.get(filename);
	}
	
	/**
	 * Clears the whole content. Every collection is
	 * empty and all fields are set to null;
	 */
	public void cleanup() {
		processUUID = null;
		files.clear();
		fields.clear();
	}

}
