package com.chocolate.puzhle2;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.chocolate.puzhle2.models.GalleryPuzzle;
import com.chocolate.puzhle2.repos.GalleryPuzzleRepo;
import com.chocolate.puzhle2.repos.LocalRepo;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

public class GalleryActivity extends BaseActivity implements Target
{
	private static int NUM_PAGES = 0;
	// Class Fields
	List<GalleryPuzzle> downloadablePackages;
	MaterialDialog updatingProgress;
	GalleryPuzzle currentGalleryPuzzle;
	String dataDirectoryAddress, destinationFile;
	LocalRepo localRepo;
	int currentImageInPackage;
	private ViewPager mPager;
	private PagerAdapter mPagerAdapter;
	private TextView galleryPagerTextView;
	private ImageButton nextButton, backButton;

	@Override
	protected void onCreate (Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		dataDirectoryAddress = Environment.getDataDirectory().getPath() + "/data/com.chocolate.pu" +
				"zzlot/Gallery/";
		ScreenSlidePageFragment.setArguments(dataDirectoryAddress);
		localRepo = uow.getLocalRepo();
		setContentView(R.layout.activity_gallery);

		galleryPagerTextView = (TextView) findViewById(R.id.pager_text);
		mPager = (ViewPager) findViewById(R.id.pager);
		mPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener()
		{
			@Override
			public void onPageScrolled (int position, float positionOffset, int positionOffsetPixels)
			{
			}

			@Override
			public void onPageSelected (int position)
			{
				++position;
				String text = position + "/" + NUM_PAGES;
				galleryPagerTextView.setText(text);
			}

			@Override
			public void onPageScrollStateChanged (int state)
			{
			}
		});
		nextButton = (ImageButton) findViewById(R.id.gallery_pager_forw);
		nextButton.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick (View view)
			{
				if (mPager != null)
				{
					if (mPager.getCurrentItem() + 1 < NUM_PAGES)
					{
						mPager.setCurrentItem(mPager.getCurrentItem() + 1);
					}
				}
			}
		});
		backButton = (ImageButton) findViewById(R.id.gallery_pager_prev);
		backButton.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick (View view)
			{
				if (mPager != null)
				{
					if (mPager.getCurrentItem() > 0)
					{
						mPager.setCurrentItem(mPager.getCurrentItem() - 1);
					}
				}
			}
		});
		refreshGallery();
	}

	public void initializeGallery ()
	{
		NUM_PAGES = localRepo.getLastPackageRecieved();
		if (NUM_PAGES > 0)
		{
			ScreenSlidePagerAdapter.setCount(NUM_PAGES);
			galleryPagerTextView.setText("1/" + NUM_PAGES);
		} else
		{
			ScreenSlidePagerAdapter.setCount(0);
			galleryPagerTextView.setText("0/0");
		}
		mPagerAdapter = new ScreenSlidePagerAdapter(getSupportFragmentManager())
		{
			@Override
			public Fragment getItem (int i)
			{
				return ScreenSlidePageFragment.create(i);
			}

			@Override
			public int getCount ()
			{
				return NUM_PAGES;
			}
		};
		mPager.setAdapter(mPagerAdapter);
	}

	private void refreshGallery ()
	{
		final GalleryPuzzleRepo galleryPuzzleRepo = uow.getGalleryPuzzleRepo();
		final MaterialDialog serverProgress = dialogManager.showServerProgress();
		galleryPuzzleRepo.isUpdateAvailable((data, e) ->
			{
				serverProgress.dismiss();
				if (data)
				{
					dialogManager.doYouWantToUpdate(new MaterialDialog.ButtonCallback()
					{
						@Override
						public void onNegative (MaterialDialog dialog)
						{
							dialog.dismiss();
							initializeGallery();
						}

						@Override
						public void onPositive (MaterialDialog dialog)
						{
							dialog.dismiss();
							updatingProgress = dialogManager.showUpdatingProgress();
							galleryPuzzleRepo.getPackageLists((galleryPuzzles, e) ->
								                                  {
									                                  if (galleryPuzzles.size() > 1)
									                                  {
										                                  Collections.sort
												                                  (
														                                  galleryPuzzles,
																						  (object1, object2) -> {
                                                                                              int lhs = object1.getPackageID();
                                                                                              int rhs = object2.getPackageID();
                                                                                              return lhs - rhs;
                                                                                          }
																				  );
									                                  }
									                                  downloadablePackages = galleryPuzzles;
									                                  downloadRemainingPacks();
								                                  }
							);
						}
					});
				} else
				{
					initializeGallery();
					dialogManager.showGalleryAlreadyUpdated();
				}
		});
	}

	private void downloadRemainingPacks ()
	{
		if (currentGalleryPuzzle == null)
		{
			if (downloadablePackages != null)
			{
				if (downloadablePackages.size() > 0)
				{
					currentGalleryPuzzle = downloadablePackages.remove(0);
					currentImageInPackage = 0;
					downloadRemainingPacks();
				} else
				{
					downloadablePackages = null;
				}
			}
		} else
		{
			currentImageInPackage++;
			if (currentImageInPackage < 10)
			{
				String source = currentGalleryPuzzle.getBaseAddress() +
						currentGalleryPuzzle.getPackageID() + currentGalleryPuzzle.getName() +
						'/' + currentImageInPackage + ".jpg";
				String destination = dataDirectoryAddress + currentGalleryPuzzle.getPackageID() +
						' ' + currentGalleryPuzzle.getName() + '/' + currentImageInPackage + ".jpg";
				download(source, destination);
			} else
			{
				currentImageInPackage = 0;
				if (downloadablePackages.size() > 0)
				{
					currentGalleryPuzzle = downloadablePackages.remove(0);
					downloadRemainingPacks();
				} else
				{
					downloadablePackages = null;
					localRepo.setLastPackageRecieved(currentGalleryPuzzle.getPackageID());
					currentGalleryPuzzle = null;
					initializeGallery();
					updatingProgress.dismiss();
				}

			}
		}
	}

	private void download (String sourceURL, String destination)
	{
		destinationFile = destination;
		Log.d("Gallery Activity", sourceURL);
		Picasso.with(this).load(sourceURL).memoryPolicy(MemoryPolicy.NO_CACHE,
				MemoryPolicy.NO_STORE).config(Bitmap.Config.RGB_565).into(this);
	}

	@Override
	public void onBitmapLoaded (Bitmap bitmap, Picasso.LoadedFrom from)
	{
		try
		{
			File file = new File(destinationFile);
			if (!file.getParentFile().isDirectory())
			{
				file.getParentFile().mkdirs();
			}
			if (!file.exists())
			{
				file.createNewFile();
			}
			FileOutputStream ostream = new FileOutputStream(file);
			bitmap.compress(Bitmap.CompressFormat.PNG, 75, ostream);
			ostream.close();
		} catch (FileNotFoundException e)
		{
			dialogManager.showDirectoryOrFileCreationFailed();
		} catch (IOException e)
		{
			if (e != null)
			{
				e.printStackTrace();
			}
		} finally
		{
			downloadRemainingPacks();
		}
	}

	public int getNumberOfDirs (String path)
	{
		int counter = 0;
		File f = new File(path);
		if (f.exists())
		{
			File[] files = f.listFiles();
			for (File inFile : files)
			{
				if (inFile.isDirectory())
				{
					counter++;
				}
			}
		}
		return counter;
	}

	@Override
	public void onBitmapFailed (Drawable errorDrawable)
	{
		dialogManager.bitmapFailed();
	}

	@Override
	public void onPrepareLoad (Drawable placeHolderDrawable)
	{
	}
}
