package test;

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
import java.net.URL;

public class Workspace {

	public static String location() {
		
		ClassLoader classloader = Thread.currentThread().getContextClassLoader();
		String resource = "test/Workspace.class";
		
		URL url = classloader.getResource(resource);
		if(url==null)
			return null;
		
		String srcPath = url.getFile();
		if(srcPath.length()<=1)
			return null;
		
		srcPath = srcPath.substring(1);
		srcPath = nibble(srcPath, resource);
		srcPath = nibble(srcPath, "/target/test-classes");
		
		return srcPath.replace('/', File.separatorChar);
	}
	
	/**
	 * Remove the tail from the statement
	 * 
	 * @param stmt The statement
	 * @param skip The tail to remove
	 * @return
	 */
	public static String nibble(String stmt, String tail) {
		if( stmt.indexOf(tail)<0)
			throw new IllegalArgumentException("Parameter missmatch '"+stmt+
					"' doesn't contains '"+tail+"'.");
		
		return stmt.substring(0, stmt.length()-tail.length());
	}
	
}
