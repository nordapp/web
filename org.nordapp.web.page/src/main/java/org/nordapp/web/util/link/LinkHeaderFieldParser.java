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
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
public class LinkHeaderFieldParser {
	
	static Logger logger = LoggerFactory.getLogger(LinkHeaderFieldParser.class);
	
	/** The parts of the link header */
	private List<LinkRelation> relations;

	public LinkHeaderFieldParser(String headerValue) {
		List<LinkRelation> tmp = new ArrayList<LinkRelation>();
        addFields(tmp, headerValue);
        relations = Collections.unmodifiableList(tmp);		
	}
	
	public LinkHeaderFieldParser(List<String> fieldValues) {
		List<LinkRelation> tmp = new ArrayList<LinkRelation>();
        if (fieldValues != null) {
            for (String value : fieldValues) {
                addFields(tmp, value);
            }
        }
        relations = Collections.unmodifiableList(tmp);		
	}
	
	public LinkHeaderFieldParser(Enumeration<?> en) {
        if (en != null && en.hasMoreElements()) {
            List<LinkRelation> tmp = new ArrayList<LinkRelation>();

            while (en.hasMoreElements()) {
                addFields(tmp, en.nextElement().toString());
            }
            relations = Collections.unmodifiableList(tmp);
        } else {
            // optimize case of no Link headers
            relations = Collections.emptyList();
        }
	}
	
	/**
	 * @param relationType
	 * @return The first target for the relation
	 */
	public String getFirstTargetForRelation(String relationType) {

        for (LinkRelation lr : relations) {
            String relationNames = lr.getParameters().get("rel");
            if (relationNames != null) {

                // split rel value on whitespace
                for (String rn : relationNames.toLowerCase(Locale.ENGLISH)
                        .split("\\s")) {
                    if (relationType.equals(rn)) {
                        return lr.getTarget();
                    }
                }
            }
        }

        return null;
    }
	
    // A single header field instance can contain multiple, comma-separated
    // fields.
    private void addFields(List<LinkRelation> l, String fieldValue) {

        boolean insideAngleBrackets = false;
        boolean insideDoubleQuotes = false;

        for (int i = 0; i < fieldValue.length(); i++) {

            char c = fieldValue.charAt(i);

            if (insideAngleBrackets) {
                insideAngleBrackets = c != '>';
            } else if (insideDoubleQuotes) {
                insideDoubleQuotes = c != '"';
                if (c == '\\' && i < fieldValue.length() - 1) {
                    // skip over next character
                    c = fieldValue.charAt(++i);
                }
            } else {
                insideAngleBrackets = c == '<';
                insideDoubleQuotes = c == '"';

                if (c == ',') {
                    String v = fieldValue.substring(0, i);
                    if (v.length() > 0) {
                        try {
                            l.add(new LinkRelation(v));
                        } catch (Exception ex) {
                        	logger.warn("parse error in Link Header field value", ex);
                        }
                    }
                    addFields(l, fieldValue.substring(i + 1));
                    return;
                }
            }
        }

        if (fieldValue.length() > 0) {
            try {
                l.add(new LinkRelation(fieldValue));
            } catch (Exception ex) {
                logger.warn("parse error in Link Header field value", ex);
            }
        }
    }

}
