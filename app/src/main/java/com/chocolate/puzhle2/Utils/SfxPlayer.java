package com.chocolate.puzhle2.Utils;

import android.annotation.TargetApi;
import android.content.Context;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Build;

import com.chocolate.puzhle2.R;
import com.chocolate.puzhle2.repos.LocalRepo;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Ariana Gostar on 4/19/2015.
 */
public class SfxPlayer
{
	private static boolean muted = false;
	private static SfxPlayer instance = null;
	private static LocalRepo localRepo = null;

	private Map<SfxResource, Integer> sfxIds;
	private SoundPool soundPool;


	private SfxPlayer (Context context)
	{
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
		{
			(new LollipopPoolClass()).createNewSoundPool();
		} else
		{
			(new OldPoolClass()).createOldSoundPool();
		}

		sfxIds = new HashMap<>();
		sfxIds.put(SfxResource.Button, soundPool.load(context, R.raw.menu_click, 1));
		sfxIds.put(SfxResource.BuyCoin, soundPool.load(context, R.raw.buy_item, 1));
		sfxIds.put(SfxResource.BuyItem, soundPool.load(context, R.raw.coin_pay, 1));
		sfxIds.put(SfxResource.Lamp, soundPool.load(context, R.raw.lamp, 1));
		sfxIds.put(SfxResource.Toggle, soundPool.load(context, R.raw.toggle, 1));
		sfxIds.put(SfxResource.Tip, soundPool.load(context, R.raw.popup, 1));
		sfxIds.put(SfxResource.Win, soundPool.load(context, R.raw.win, 1));
		sfxIds.put(SfxResource.Lose, soundPool.load(context, R.raw.lose, 1));
		sfxIds.put(SfxResource.Letter, soundPool.load(context, R.raw.letters, 1));
	}

	public void setMuted(boolean muted){
		SfxPlayer.muted = muted;
		localRepo.setMute(muted);
	}

	public boolean getMuted(){
		return muted;
	}

	public static SfxPlayer getInstance (Context context, LocalRepo localRep)
	{
		if (instance == null)
		{
			instance = new SfxPlayer(context);
			localRepo = localRep;
			muted = localRep.getMute();
		}
		return instance;
	}

	public void Play (SfxResource sfxResource)
	{
		if (!muted)
		{
			soundPool.play(sfxIds.get(sfxResource), 1, 1, 0, 0, 1);
		}
	}

	private class LollipopPoolClass {
		@TargetApi (Build.VERSION_CODES.LOLLIPOP)
		public void createNewSoundPool ()
		{
			AudioAttributes attributes = new AudioAttributes.Builder()
					.setUsage(AudioAttributes.USAGE_GAME)
					.setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
					.build();
			soundPool = new SoundPool.Builder()
					.setAudioAttributes(attributes)
					.build();
		}
	}

	private class OldPoolClass {
		@SuppressWarnings("deprecation")
		protected void createOldSoundPool() {
			soundPool = new SoundPool(5, AudioManager.STREAM_MUSIC, 0);
		}
	}
}
