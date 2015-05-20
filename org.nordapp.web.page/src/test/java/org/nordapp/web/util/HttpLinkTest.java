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


import static org.junit.Assert.assertEquals;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.nordapp.web.util.link.LinkHeaderImpl;
import org.nordapp.web.util.link.LinkHeaderFieldParser;

public class HttpLinkTest {
	
	@Before
	public void setUp() throws Exception {
		
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void test() {
		
		LinkHeaderImpl hdr = new LinkHeaderImpl();
		hdr.setLink_host("127.0.0.1");
		hdr.setLink_servlet("dest");
		hdr.setLink_uuid("12345");
		
		hdr.add("/my-resource", "get");
		hdr.add("/my-next-resource", "next");
		
		String resl = "<http://127.0.0.1/dest/12345/my-resource>;rel=\"get\",<http://127.0.0.1/dest/12345/my-next-resource>;rel=\"next\"";
		
		assertEquals( hdr.toString(), resl );
		
		LinkHeaderFieldParser psr = new LinkHeaderFieldParser(hdr.toString());
		
		assertEquals( psr.getFirstTargetForRelation("get"), "http://127.0.0.1/dest/12345/my-resource" );
		assertEquals( psr.getFirstTargetForRelation("next"), "http://127.0.0.1/dest/12345/my-next-resource" );
		
		//fail("Not yet implemented"); // TODO
	}

}
