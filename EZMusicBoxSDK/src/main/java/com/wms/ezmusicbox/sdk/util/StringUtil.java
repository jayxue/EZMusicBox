/*
 * Copyright 2016 Waterloo Mobile Studio
 *
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
 */

package com.wms.ezmusicbox.sdk.util;

public class StringUtil {

	/**
	 * From a URL like http://.../.../a.b, return file name a.b
	 */
	public static String getFileNameFromUrl(String url) {
		int lastSeparaterPosition = url.lastIndexOf("/");
		return url.substring(lastSeparaterPosition + 1);		
	}
	
}