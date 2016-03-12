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

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.ProgressDialog;
import android.content.Context;

import com.wms.ezmusicbox.sdk.R;

public class DialogUtil {

	public static ProgressDialog showWaitingProgressDialog(Context context, int style, String message, boolean cancelable) {
		ProgressDialog progressDialog = new ProgressDialog(context);
		progressDialog.setProgressStyle(style);
		progressDialog.setMessage(message);
		progressDialog.setCancelable(cancelable);
		try {
			// Avoid window leaking exception after existing an activity
			progressDialog.show();
		}
		catch(Exception e) {
			// Ignore exception
		}
		return progressDialog;
	}

	public static void showExceptionAlertDialog(Context context, String title, String message) {
		Builder exceptionAlertDialogBuilder = new Builder(context);
		exceptionAlertDialogBuilder.setTitle(title).setMessage(message).setCancelable(true).setNeutralButton(context.getString(R.string.ok), null);
		AlertDialog alert = exceptionAlertDialogBuilder.create();
		try {
			// Avoid window leaking exception after existing an activity
			alert.show();
		}
		catch(Exception e) {
			// Ignore exception
		}			
	}

}
