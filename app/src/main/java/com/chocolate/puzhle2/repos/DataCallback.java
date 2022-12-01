package com.chocolate.puzhle2.repos;

/**
 * Created by mahdi on 5/22/15.
 */
public interface DataCallback<T>
{
	void dataReceived (T data, Exception ex);
}
