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

package com.wms.ezmusicbox.sdk.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.wms.ezmusicbox.sdk.R;
import com.wms.ezmusicbox.sdk.activity.MusicListActivity;
import com.wms.ezmusicbox.sdk.entity.Music;
import com.wms.ezmusicbox.sdk.util.MusicUtil;

import java.util.ArrayList;
import java.util.List;

public class MusicArrayAdapter extends ArrayAdapter<Music> {
	
	private Context context = null;
	private List<Music> musics = new ArrayList<>();
		
	public MusicArrayAdapter(Context context, int resource, int textViewResourceId, List<Music> musics) {
		super(context, resource, textViewResourceId);
		this.context = context;
		this.musics = musics;
	}

	public int getCount() {
		return musics.size();
	}
	
	public Music getItem(int position) {
		return musics.get(position);
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View rowView = inflater.inflate(R.layout.music_list_item, parent, false);
		
		TextView textView = (TextView) rowView.findViewById(R.id.textViewMusicItem);
		textView.setText(musics.get(position).getAuthor() + " - " + musics.get(position).getName());
		if(position == ((MusicListActivity)getContext()).getPlayingMusicIndex()) {
			textView.setTextColor(Color.GRAY);
		}
		else {
			textView.setTextColor(Color.BLACK);
		}

		ImageView imageViewMusicIcon = (ImageView) rowView.findViewById(R.id.imageViewMusicIcon);

		if(MusicUtil.musicFileExists(getContext(), musics.get(position))) {
			imageViewMusicIcon.setImageResource(R.drawable.music_downloaded);
		}
		else {
			imageViewMusicIcon.setImageResource(R.drawable.music_not_downloaded);
		}

		return rowView;
	}

}
