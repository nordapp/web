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


import java.net.MalformedURLException;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

public class RequestPath {
	
	/** This delimiter is not used in any supported character-set. */
	public static final String CHAR_DELIM = "\uFF25";
	
	/**
	 * Searches a string in an array and returns the matching index.
	 * 
	 * @param stmt The statement to search
	 * @param array The array to search in.
	 * @return Returns the matching index
	 */
	public static int indexOf(String stmt, int off, String[] array){
		for(int i=off;i<array.length;i++){
			if(array[i].equals(stmt))
				return i;
		}
		return -1;
	}

	/**
	 * Searches a string in an array and returns the matching index.
	 * 
	 * @param pattern The pattern to match
	 * @param array The array to search in.
	 * @return Returns the matching index
	 */
	public static int indexOf(Pattern pattern, int off, String[] array){
		
		for(int i=off;i<array.length;i++){
			if(pattern.matcher(array[i]).matches())
				return i;
		}
		return -1;
	}

	/**
	 * Searches a string in an array and returns the matching index.
	 * 
	 * @param filter The filter that matches
	 * @param array The array to search in.
	 * @return Returns the matching index
	 */
	public static int indexOf(Filter filter, int off, String[] array){
		
		for(int i=off;i<array.length;i++){
			if(filter.match(i, array))
				return i;
		}
		return -1;
	}
	
	/**
	 * Returns the path of the request URI behind the servlet path
	 * 
	 * @param req The request.
	 * @return The path of the request URI
	 */
	public static String[] getPath(HttpServletRequest req) {
		
		String uri = req.getRequestURI();
		if(uri==null)
			return new String[0];
		
		String srv = req.getServletPath();
		if(srv==null)
			return new String[0];
		
		int s = srv.length(), e = uri.length();
		if(s>=e)
			return new String[0];
		
		if(uri.charAt(s)=='/')
			s++;
		if(uri.endsWith("/"))
			e--;
		if(s==e)
			return new String[0];
		
		String res = uri.substring(s, e);
		return res.split("/");
	}
	
	/**
	 * Returns the path of the request URI behind the servlet path
	 * 
	 * @param req The request
	 * @param startPathIndex The index of the path '/path' or '/id-of-the-mandator/id-of-the-session/path'
	 * @return Returns the matching index
	 */
	public static String[] getPath(HttpServletRequest req, int startPathIndex) {
		
		String uri = req.getRequestURI();
		if(uri==null)
			return new String[0];
		
		String srv = req.getServletPath();
		if(srv==null)
			return new String[0];
		
		int s = srv.length(), e = uri.length();
		if(s>=e)
			return new String[0];
		
		if(uri.charAt(s)=='/')
			s++;
		if(uri.endsWith("/"))
			e--;
		if(s==e)
			return new String[0];
		
		String res = uri.substring(s, e);
		String[] arr1 = res.split("/");
		
		if(startPathIndex==0)
			return arr1;
		
		int len = arr1.length-startPathIndex;
		if(len<1)
			return new String[0];
		
		String[] arr2 = new String[len];
		System.arraycopy(arr1, startPathIndex, arr2, 0, len);
		return arr2;
	}
	
	/**
	 * Replaces every occurrence of a field as a key in the map by the value of the map.
	 * 
	 * @param array The array to look in.
	 * @param off The search offset
	 * @param mapping The key value mapping
	 * @return The processed array.
	 */
	public static String[] replace(String[] array, int off, Map<String, String> mapping) {
		String[] res = new String[array.length];
		System.arraycopy(array, 0, res, 0, array.length);
		
		for(int i=off;i<array.length;i++)
			if(res[i]!=null && mapping.containsKey(res[i]))
				res[i] = mapping.get(res[i]);
		
		return res;
	}
	
	/**
	 * Replaces every occurrence of the placeholder '{}' by the value of
	 * the next argument and return the result as a String.
	 * 
	 * @param stmt The input string.
	 * @param args The arguments
	 * @return The processed statement
	 */
	public static String replacePath(String stmt, String... args) {
		StringBuffer res = new StringBuffer();
		
		int i = 0;
		StringTokenizer tok = new StringTokenizer(stmt.replace("{}", CHAR_DELIM), CHAR_DELIM, true);
		while(tok.hasMoreTokens()) {
			String t = tok.nextToken();
			if(t.equals(CHAR_DELIM) && i<args.length){
				res.append(args[i++]);
			}else{
				res.append(t);
			}//fi
		}//while
		
		return res.toString();
	}
	
	/**
	 * Returns the mandator
	 * 
	 * @param req The request object
	 * @return The mandator
	 * @throws MalformedURLException 
	 */
	public static String getMandator(HttpServletRequest req) throws MalformedURLException {
		
		String[] elems = getPath(req);
		
		if(elems.length==0)
			throw new MalformedURLException("The URL needs the form '/"+
					req.getServletPath()+"/id-of-the-mandator' but has '"+
					req.getRequestURI()+"'");
		
		return elems[0];
	}
	
	/**
	 * Returns the session
	 * 
	 * @param req The request object
	 * @return The session
	 * @throws MalformedURLException 
	 */
	public static String getSession(HttpServletRequest req) throws MalformedURLException {
		
		String[] elems = getPath(req);
		
		if(elems.length<2)
			throw new MalformedURLException("The URL needs the form '/"+
					req.getServletPath()+"/id-of-the-mandator/id-of-the-session' but has '"+
					req.getRequestURI()+"'");
		
		return elems[1];
	}

	/**
	 * @author Stefan
	 *
	 */
	public static interface Filter {
		
		/**
		 * Returns true if the filter matches
		 * 
		 * @param index The current index of the iteration
		 * @param array The iterated array
		 * @return The flag
		 */
		public boolean match(int index, String[] array);
		
	}/*IF*/
}
