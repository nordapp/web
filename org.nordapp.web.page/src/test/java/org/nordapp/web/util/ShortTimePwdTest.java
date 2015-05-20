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


import org.junit.Before;
import org.junit.Ignore;

public class ShortTimePwdTest {

	@Before
	public void setUp() throws Exception {
	}

	@Ignore
	public void test() throws InterruptedException {
		
		String stmt = makeTimePrefix()+"rest";
		for(int i=0;i<(20*15);i++) {
			
			int time = getTime(stmt);
			System.out.println( time );
			
			Thread.sleep(50);
		}
		//fail("Not yet implemented"); // TODO
	}
	
	private String makeTimePrefix() {
		long raw = System.currentTimeMillis();
		int time = (int)(raw & 0xFFFF); //0-65535
		
		StringBuffer buf = new StringBuffer( Integer.toHexString(time) );
		for(int i=buf.length();i<5;i++)
			buf.insert(0, '0');
		
		return buf.toString();
	}
	
	private int getTime(String prefix) {
		
		String pre = prefix.substring(0, 5);
		int ref = Integer.valueOf(pre, 16); //0-16383
		
		long raw = System.currentTimeMillis();
		int time = (int)(raw & 0x3FFF); //0-16383
		
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

}
