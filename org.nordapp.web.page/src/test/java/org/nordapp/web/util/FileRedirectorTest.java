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


import static org.junit.Assert.assertTrue;

import java.io.File;

import org.i3xx.util.basic.io.FilePath;
import org.junit.Before;
import org.junit.Test;

import test.Workspace;


public class FileRedirectorTest {
	
	static FilePath RES_LOC = null;
	
	@Before
	public void setUp() throws Exception {
		
		String loc = Workspace.location().replace(File.separatorChar, '/');
		RES_LOC = FilePath.get(loc).add("/src/test/resources");
	}
	
	@Test
	public void test() {
		
		String root = RES_LOC.getPath();
		String dest = "FileRedirectorTest.txt";
		FileRedirector r = new FileRedirector(dest, FileRedirector.TYPE_URI_PATH);
		File file = r.getFileDestination(root);
		
		assertTrue( file.exists() );
	}

}
