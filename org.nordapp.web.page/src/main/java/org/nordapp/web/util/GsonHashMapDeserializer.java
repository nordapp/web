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


import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;

/**
 * @author Benny
 * @since 17.01.2014 Stefan, Use java.util.LinkedHashMap to keep insertion order.
 *
 */
public class GsonHashMapDeserializer implements JsonDeserializer<Object> {
	
	public Object deserialize(JsonElement json, Type typeOfT, 
	      JsonDeserializationContext context) throws JsonParseException {
		  
		if(json.isJsonNull()) return null;
		else if(json.isJsonPrimitive()) return handlePrimitive(json.getAsJsonPrimitive());
		else if(json.isJsonArray()) return handleArray(json.getAsJsonArray(), context);
		else return handleObject(json.getAsJsonObject(), context);
	}
	
	private Object handlePrimitive(JsonPrimitive json) {
		if(json.isBoolean())
			return json.getAsBoolean();
		else if(json.isString())
			return json.getAsString();
		else {
			BigDecimal bigDec = json.getAsBigDecimal();
			if(json.getAsString().indexOf('.')==-1){
				// Find out if it is an int type
				try {
					return bigDec.longValue();
				} catch(ArithmeticException e) {}	    	  
			}
			// Just return it as a double
			return bigDec.doubleValue();
		}
	}
	
	private Object handleArray(JsonArray json, JsonDeserializationContext context) {
		ArrayList<Object> array = new ArrayList<Object>();
		for(int i = 0; i < json.size(); i++)
			array.add( deserialize(json.get(i), Object.class,context) );
		
		return array;
	}
	
	private Object handleObject(JsonObject json, JsonDeserializationContext context) {
		Map<String, Object> map = new LinkedHashMap<String, Object>();
		for(Map.Entry<String, JsonElement> entry : json.entrySet()){
			map.put(entry.getKey(), deserialize(entry.getValue(), Object.class,context));
		}
		return map;
	}
}