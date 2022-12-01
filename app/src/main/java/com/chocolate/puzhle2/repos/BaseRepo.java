package com.chocolate.puzhle2.repos;

import android.content.Context;

import com.chocolate.puzhle2.Utils.DialogManager;
import com.chocolate.puzhle2.Utils.ToastManager;
import com.parse.ParseObject;

/**
 * Created by choc01ate on 5/22/2015.
 */
public abstract class BaseRepo<T extends ParseObject>
{
	protected Context context;
	protected DialogManager dialogManager;
	protected ToastManager toastManager;

	private Class<T> tClass;

	public BaseRepo (Context context, DialogManager dialogManager, ToastManager toastManager, Class<T> tClass)
	{
		this.context = context.getApplicationContext();
		this.dialogManager = dialogManager;
		this.toastManager = toastManager;
		this.tClass = tClass;
	}
}
