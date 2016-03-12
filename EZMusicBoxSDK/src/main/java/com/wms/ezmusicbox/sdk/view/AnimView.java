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

package com.wms.ezmusicbox.sdk.view;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.TranslateAnimation;

import com.wms.ezmusicbox.sdk.R;
import com.wms.ezmusicbox.sdk.drawable.AnimateDrawable;


public class AnimView extends View {

	private int iconCount = 50;
	private final List<Drawable> drawables = new ArrayList<>();
	private int[][] coords;
	private Drawable icon1;
	private Drawable icon2;

	public AnimView(Context context) {
		super(context);
		setFocusable(true);
		setFocusableInTouchMode(true);

		icon1 = context.getResources().getDrawable(R.drawable.icon1);
		icon2 = context.getResources().getDrawable(R.drawable.icon2);
		icon1.setBounds(0, 0, icon1.getIntrinsicWidth(), icon1.getIntrinsicHeight());
		icon2.setBounds(0, 0, icon2.getIntrinsicWidth() / 2, icon2.getIntrinsicHeight() / 2);
	}

    @Override
    protected void onSizeChanged(int width, int height, int oldw, int oldh) {	// width and height refer to screen width and height
		super.onSizeChanged(width, height, oldw, oldh);
		Random random = new Random();
		LinearInterpolator interpolator = new LinearInterpolator();
		iconCount = Math.max(width, height) / 30;

		coords = new int[iconCount][];
		drawables.clear();
		for (int i = 0; i < iconCount; i++) {
			Animation animation = null;
			animation = new TranslateAnimation(0, height / 10 - random.nextInt(height / 5), 0, height + 184);	// screen height plus height of the image
			animation.setDuration(10 * height + random.nextInt(5 * height));
			animation.setRepeatCount(-1);
			animation.initialize(10, 10, 10, 10);
			animation.setInterpolator(interpolator);

			coords[i] = new int[] { random.nextInt(width - 30), -196 };	// The second number controls the start position in y axis

			if(i % 2 == 0) {
			    drawables.add(new AnimateDrawable(icon1, animation));
			}
			else {
			    drawables.add(new AnimateDrawable(icon2, animation));
			}

			animation.setStartOffset(random.nextInt(20 * height));
			animation.startNow();
		}
    }

    @Override
    protected void onDraw(Canvas canvas) {
		for (int i = 0; i < iconCount; i++) {
			Drawable drawable = drawables.get(i);
			canvas.save();
			canvas.translate(coords[i][0], coords[i][1]);
			drawable.draw(canvas);
			canvas.restore();
		}

		invalidate();
    }

    public boolean onTouchEvent (MotionEvent event) {
        return false;
    }
	    
}
