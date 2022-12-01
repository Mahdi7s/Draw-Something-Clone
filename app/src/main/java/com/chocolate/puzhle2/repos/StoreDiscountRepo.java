package com.chocolate.puzhle2.repos;

import android.content.Context;

import com.chocolate.puzhle2.Utils.DialogManager;
import com.chocolate.puzhle2.Utils.ToastManager;
import com.chocolate.puzhle2.events.BiFunction;
import com.chocolate.puzhle2.models.StoreDiscount;
import com.parse.GetCallback;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import org.joda.time.DateTime;

import java.util.List;

/**
 * Created by System 1 on 05/09/2015.
 */
public class StoreDiscountRepo extends BaseRepo<StoreDiscount>
{
	private static boolean initialized = false;
	private static List<StoreDiscount> discounts = null;

	public StoreDiscountRepo (Context context, DialogManager dialogManager, ToastManager toastManager)
	{
		super(context, dialogManager, toastManager, StoreDiscount.class);
	}

	public void getTodayDiscount (GetCallback<StoreDiscount> callback)
	{
		final String pinName = "Discounts";

		BiFunction<List<StoreDiscount>, Object> callCallback = (discs) -> {
			for (StoreDiscount storeDiscount : discs) {
				if (storeDiscount.getFromDate().getTime() >= DateTime.now().getMillis() && storeDiscount.getToDate().getTime() <= DateTime.now().getMillis()) {
					callback.done(storeDiscount, null);
					return null;
				}
			}
			return null;
		};

		if (discounts != null && discounts.size() > 0) {
			callCallback.run(discounts);
			return;
		}

		ParseQuery<StoreDiscount> queryServer = ParseQuery.getQuery(StoreDiscount.class).whereGreaterThanOrEqualTo("FromDate", DateTime.now().toDate());
		ParseQuery<StoreDiscount> queryLocal = ParseQuery.getQuery(StoreDiscount.class).whereGreaterThanOrEqualTo("FromDate", DateTime.now().toDate()).fromPin(pinName);

		queryLocal.findInBackground((ldiscounts, e) -> {
			if (e == null && ldiscounts.size() > 0) {
				discounts.clear();
				discounts.addAll(ldiscounts);
				callCallback.run(ldiscounts);
			} else {
				ParseObject.unpinAllInBackground(pinName);
				queryServer.findInBackground((sdiscounts, e2) -> {
					if (e2 == null && sdiscounts.size() > 0) {
						ParseObject.pinAllInBackground(pinName, sdiscounts);

						discounts.clear();
						discounts.addAll(sdiscounts);
						callCallback.run(sdiscounts);
					}else{
						callback.done(null, e);
					}
				});
			}
		});
	}
}
