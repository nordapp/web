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


import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.fileupload.ParameterParser;

/**
 * This is code from the Apache Jackrabbit Project
 * org.apache.jackrabbit.webdav.util.LinkHeaderFieldParser
 * https://jackrabbit.apache.org/jcr/index.html
 *
 */
/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
public class LinkRelation {

	private static Pattern P = Pattern.compile("\\s*<(.*)>\\s*(.*)");

    private String target;
    private Map<String, String> parameters;
	
    public LinkRelation() {
    	parameters = new HashMap<String, String>();
    }
    
	public LinkRelation(String field) throws Exception {
	
	    // find the link target using a regexp
	    Matcher m = P.matcher(field);
	    if (!m.matches()) {
	        throw new Exception("illegal Link header field value:" + field);
	    }
	
	    target = m.group(1);
	
	    // pass the remainder to the generic parameter parser
	    parameters = new ParameterParser().parse(m.group(2), ';');
	
	}
	
	public String getTarget() {
	    return target;
	}
	
	public Map<String, String> getParameters() {
	    return parameters;
	}
	
	public String toString() {
	    return target + " " + parameters;
	}
}