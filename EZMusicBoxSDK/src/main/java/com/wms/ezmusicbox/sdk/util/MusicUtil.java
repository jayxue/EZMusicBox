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

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;

import com.wms.ezmusicbox.sdk.entity.Music;

public class MusicUtil {

	// Load music list from assets/music_list.json
	public static List<Music> loadMustList(Context context) {
		List<Music> musicList = new ArrayList<>();
		
		BufferedReader br = null;
		try {
			// Reading json file from assets folder
			StringBuffer sb = new StringBuffer();
			br = new BufferedReader(new InputStreamReader(context.getAssets().open("music_list.json")));
			String temp;
			while ((temp = br.readLine()) != null) {
				sb.append(temp);
			}
			
			// Creating JSONObject from String
			JSONObject jsonObjMain = new JSONObject(sb.toString());

			// Creating JSONArray from JSONObject
			JSONArray jsonArray = jsonObjMain.getJSONArray("list");

			// JSONArray has four JSONObject
			for (int i = 0; i < jsonArray.length(); i++) {

				// Creating JSONObject from JSONArray
				JSONObject jsonObj = jsonArray.getJSONObject(i);

				// Getting data from individual JSONObject
				String author = jsonObj.getString("author");
				String name = jsonObj.getString("name");
				String url = jsonObj.getString("url");
				int size = jsonObj.getInt("size");
				int duration = jsonObj.getInt("duration");
				Music music = new Music(author, name, url, size, duration);
				musicList.add(music);
			}

		}
		catch (JSONException | IOException e) {
		
		}
		finally {
			try {
				// Close buffer reader
				br.close();
			}
			catch (IOException e) {

			}
		}
		return musicList;
	}
	
	/**
	 * Check if a music file with given name is in the app's data storage path
	 */
	public static boolean musicFileExists(Context context, Music music) {
		File musicFile = new File(FileUtil.getAppExternalStoragePath(context), StringUtil.getFileNameFromUrl(music.getUrl()));
		return musicFile.exists();
	}

}
