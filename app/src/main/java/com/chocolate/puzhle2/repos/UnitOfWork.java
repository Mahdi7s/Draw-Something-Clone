package com.chocolate.puzhle2.repos;

import android.content.Context;

import com.chocolate.puzhle2.Utils.DialogManager;
import com.chocolate.puzhle2.Utils.ToastManager;

/**
 * Created by choc01ate on 5/22/2015.
 */
public class UnitOfWork
{
	private Context context = null;
	private DialogManager dialogManager = null;
	private ToastManager toastManager = null;

	private GameUserRepo gameUserRepo;
	private UserPuzzleRepo userPuzzleRepo;
	private UserScoreRepo userScoreRepo;
	private LocalRepo localRepo;
	private GalleryPuzzleRepo galleryPuzzleRepo;
	private StoreDiscountRepo storeDiscountRepo;
	private DrawingWordRepo drawingWordRepo;
	private UserActivityRepo userActivityRepo;

	public UnitOfWork (Context context, DialogManager dialogManager, ToastManager toastManager)
	{
		context = context.getApplicationContext();
		this.context = context;
		this.dialogManager = dialogManager;
		this.toastManager = toastManager;

		localRepo = new LocalRepo(context);
		gameUserRepo = new GameUserRepo(context, dialogManager, toastManager);
		userActivityRepo = new UserActivityRepo(context, dialogManager, toastManager);
		userPuzzleRepo = new UserPuzzleRepo(context, dialogManager, toastManager, gameUserRepo, localRepo);
		userScoreRepo = new UserScoreRepo(context, dialogManager, toastManager, gameUserRepo);
		galleryPuzzleRepo = new GalleryPuzzleRepo(context, dialogManager, toastManager, localRepo);
		storeDiscountRepo = new StoreDiscountRepo(context, dialogManager, toastManager);
		drawingWordRepo = new DrawingWordRepo(context, dialogManager, toastManager);
	}

	public LocalRepo getLocalRepo ()
	{
		return localRepo;
	}

	public GalleryPuzzleRepo getGalleryPuzzleRepo ()
	{
		return galleryPuzzleRepo;
	}

	public GameUserRepo getUserRepo ()
	{
		return gameUserRepo;
	}

	public UserPuzzleRepo getPuzzleRepo ()
	{
		return userPuzzleRepo;
	}

	public UserScoreRepo getScoreRepo ()
	{
		return userScoreRepo;
	}

	public StoreDiscountRepo getStoreDiscountRepo ()
	{
		return storeDiscountRepo;
	}

	public DrawingWordRepo getDrawingWordRepo(){ return drawingWordRepo; }
	public UserActivityRepo getUserActivityRepo(){return userActivityRepo; }
}
