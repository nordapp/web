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


import java.io.File;

import org.i3xx.util.basic.io.FilePath;


public class FileRedirector {
	
	public static final String TYPE_URI_PATH = "URI:PATH";
	
	/**
	 * The source path is the path that is originally requested.
	 * 
	 * A source path may be the path of an URL. To request a resource
	 * file the path must be requested to the current location the
	 * file can be found.
	 */
	private String sourcePath;
	
	/**
	 * The description of the sourcePath
	 */
	private String typeDesc;
	
	/**
	 * @param sourcePath
	 * @param typeDesc
	 */
	public FileRedirector(String sourcePath, String typeDesc) {
		this.sourcePath = sourcePath;
		this.typeDesc = typeDesc;
	}
	
	/**
	 * @param sourcePath
	 * @param typeDesc
	 */
	public FileRedirector(String[] sourcePath, int off, String typeDesc) {
		StringBuffer buffer = new StringBuffer();
		this.typeDesc = typeDesc;
	
		for(int i=off;i<sourcePath.length;i++){
			if(i>off)
				buffer.append('/');
			
			buffer.append(sourcePath[i]);
		}//for
		this.sourcePath = buffer.toString();
	}
	
	/**
	 * Returns the source path.
	 * 
	 * @return
	 */
	public String getSourcePath() {
		return sourcePath;
	}
	
	/**
	 * Returns the description of the type of the sourcePath.
	 * @return
	 */
	public String getTypeDescription(){
		return typeDesc;
	}
	
	/**
	 * The file path of the resource.
	 * 
	 * @param root The root path of the file.
	 * @return
	 */
	public FilePath getFilePath(String root) {
		
		FilePath p = FilePath.get(root);
		p = p.add(sourcePath);
		
		return p;
	}
	
	/**
	 * The file of the resource if the resource is a file.
	 * 
	 * @param root The root path of the file.
	 * @return
	 */
	public File getFileDestination(String root) {
		
		FilePath p = FilePath.get(root);
		p = p.add(sourcePath);
		
		return p.toFile();
	}

}
