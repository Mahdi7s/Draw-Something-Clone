package com.chocolate.puzhle2.events;

public class EventMessage
{
	private EventMsgType msgType;
	private Object msgData;
	private Object extraData;

	public EventMessage (EventMsgType type, Object data, Object extraData)
	{
		msgType = type;
		msgData = data;
		this.extraData = extraData;
	}

	public EventMsgType getMsgType ()
	{
		return msgType;
	}

	public Object getMsgData ()
	{
		return msgData;
	}
	public Object getExtraData(){return extraData;}
}
