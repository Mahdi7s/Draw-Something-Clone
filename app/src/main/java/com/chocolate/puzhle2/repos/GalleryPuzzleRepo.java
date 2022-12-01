package com.chocolate.puzhle2.repos;

import android.content.Context;

import com.chocolate.puzhle2.Utils.DialogManager;
import com.chocolate.puzhle2.Utils.ToastManager;
import com.chocolate.puzhle2.models.GalleryPuzzle;
import com.parse.ParseQuery;

import java.util.List;

/**
 * Created by choc01ate on 14/07/2015.
 */
public class GalleryPuzzleRepo extends BaseRepo<GalleryPuzzle>
{
	private LocalRepo localRepo;

	public GalleryPuzzleRepo (Context context, DialogManager dialogManager, ToastManager toastManager, LocalRepo localRepo)
	{
		super(context, dialogManager, toastManager, GalleryPuzzle.class);
		this.localRepo = localRepo;
	}

	public void isUpdateAvailable (final DataCallback<Boolean> callback)
	{
		ParseQuery<GalleryPuzzle> query = ParseQuery.getQuery(GalleryPuzzle.class);
		query.addDescendingOrder("PackageID").setLimit(1);
		query.findInBackground((data,e)->
			{
				int lastPackage = localRepo.getLastPackageRecieved();
				if (data != null)
				{
					if (data.size() > 0)
					{
						int currentPackage = data.get(0).getPackageID();
						if (lastPackage < currentPackage)
						{
							callback.dataReceived(true, e);
							return;
						}
					}
				}
				callback.dataReceived(false,e);
		});
	}

	public void getPackageLists (final DataCallback<List<GalleryPuzzle>> callback)
	{
		ParseQuery<GalleryPuzzle> query = ParseQuery.getQuery(GalleryPuzzle.class);
		query.whereGreaterThan("PackageID", localRepo.getLastPackageRecieved());
		query.findInBackground((data, e)->
			{
				callback.dataReceived(data, e);
		});
	}
}
