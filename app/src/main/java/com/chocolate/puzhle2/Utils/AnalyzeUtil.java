package com.chocolate.puzhle2.Utils;

/**
 * Created by home pc on 01/09/2015.
 */
public class AnalyzeUtil
{
	public static final int ANALYZE_VERSION = 114;

	public static final String ACTIVITY_OPEN = "ACTIVITY_OPEN";
	public static final String STORE_PURCHASE = "STORE_PURCHASE";
	public static final String CREATE_PUZZLE_ACTION = "CREATE_PUZZLE_ACTION";
	public static final String SOLVE_PUZZLE_ACTION = "SOLVE_PUZZLE_ACTION";

	public static void track(String event) {
		track(event, null);
	}

	public static void track (String event, String dimension)
	{
//		Map<String, String> map = new HashMap<>();
//		map.put("version", ANALYZE_VERSION + "");
//		if(!TextUtils.isEmpty(dimension)) {
//			map.put("value", dimension);
//		}
//		ParseAnalytics.trackEventInBackground(event, map);
	}
}
