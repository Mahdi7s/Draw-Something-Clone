package com.chocolate.puzhle2.events;


import com.parse.ParseException;

/**
 * Created by mahdi on 5/23/15.
 */
public class ServerEvent
{
	private ServerAction action;
	private ParseException exception;
	private Object data = null;

	public ServerEvent (ServerAction action, ParseException exception, Object data)
	{

		this.action = action;
		this.exception = exception;
		this.data = data;
	}

	public ServerAction getAction ()
	{
		return action;
	}

	public ParseException getException ()
	{
		return exception;
	}

	public Object getData ()
	{
		return data;
	}
}
