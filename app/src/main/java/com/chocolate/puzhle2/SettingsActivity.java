package com.chocolate.puzhle2;

import android.content.Intent;
import android.content.IntentSender;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.chocolate.puzhle2.Utils.AdsUtils;
import com.chocolate.puzhle2.Utils.SfxResource;
//import com.google.android.gms.common.ConnectionResult;
//import com.google.android.gms.common.api.GoogleApiClient;
//import com.google.android.gms.plus.Plus;


public class SettingsActivity extends BaseActivity implements /*GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,*/ View.OnClickListener
{
	private static final int SIGN_IN_REQUEST_CODE = 10;
	private static final int ERROR_DIALOG_REQUEST_CODE = 11;
	private static final int REQ_SIGN_IN_REQUIRED = 55664;
	private ImageButton googleAccount, soundOn;
	private MaterialDialog progressDlg;
	// For communicating with Google APIs
//	private GoogleApiClient mGoogleApiClient;
	private boolean mSignInClicked;
	private boolean mIntentInProgress;
	// contains all possible error codes for when a client fails to connect to
	// Google Play services
//	private ConnectionResult mConnectionResult;

	@Override
	protected void onCreate (Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_settings);
		AdsUtils.setListenerForBanner(this, R.id.banner_ad_view);

		googleAccount = (ImageButton) findViewById(R.id.btnGoogleAccount);
		soundOn = (ImageButton) findViewById(R.id.btnSfxOn);

//		googleAccount.setOnClickListener(this);
		soundOn.setOnClickListener(this);

		findViewById(R.id.btnAbout).setOnClickListener(this);

		((ImageView)findViewById(R.id.btnIntroduce)).setImageResource(uow.getLocalRepo().getSharePuzzleText() ? R.drawable.btn_introduce_enabled : R.drawable.btn_introduce_disabled);
		soundOn.setImageResource(uow.getLocalRepo().getMute() ? R.drawable.btn_sound_off : R.drawable.btn_sound_on);

//		mGoogleApiClient = buildGoogleAPIClient();
	}

	/**
	 * API to return GoogleApiClient Make sure to create new after revoking
	 * access or for first time sign in
	 *
	 * @return
	 */
//	private GoogleApiClient buildGoogleAPIClient ()
//	{
//		return new GoogleApiClient.Builder(this).addConnectionCallbacks(this)
//				.addOnConnectionFailedListener(this)
//				.addApi(Plus.API, Plus.PlusOptions.builder().build())
//				.addScope(Plus.SCOPE_PLUS_LOGIN).build();
//	}

//	@Override
//	protected void onStart ()
//	{
//		super.onStart();
//		// make sure to initiate connection
//		if (uow.getUserRepo().isUserLoggedIn())
//		{
//			googleAccount.setImageResource(R.drawable.btn_connected);
//			googleAccount.setEnabled(false);
//		} else
//		{
//			mGoogleApiClient.connect();
//
//			googleAccount.setImageResource(R.drawable.btn_disconnected);
//			googleAccount.setEnabled(true);
//		}
//	}

//	@Override
//	protected void onStop ()
//	{
//		super.onStop();
//		// disconnect api if it is connected
//		disconnectAccount();
//	}

	public void disconnectAccount ()
	{
//		if (mGoogleApiClient.isConnected())
//			mGoogleApiClient.disconnect();
	}

	@Override
	public void onClick (View view)
	{
		switch (view.getId())
		{
			case R.id.btnGoogleAccount:
//				processSignIn();
				break;
			case R.id.btnSfxOn:
				sfxPlayer.Play(SfxResource.Toggle);

				if (sfxPlayer.getMuted())
				{ // so turn music on
					soundOn.setImageResource(R.drawable.btn_sound_on);
					soundOn.setTag("on");
					sfxPlayer.setMuted(false);
				} else
				{
					soundOn.setImageResource(R.drawable.btn_sound_off);
					soundOn.setTag("off");
					sfxPlayer.setMuted(true);
				}
				break;
			case R.id.btnIntroduce:
				sfxPlayer.Play(SfxResource.Toggle);

				if(uow.getLocalRepo().getSharePuzzleText()){
					((ImageView)findViewById(R.id.btnIntroduce)).setImageResource(R.drawable.btn_introduce_disabled);
					uow.getLocalRepo().setSharePuzzleText(false);
				} else {
					((ImageView)findViewById(R.id.btnIntroduce)).setImageResource(R.drawable.btn_introduce_enabled);
					uow.getLocalRepo().setSharePuzzleText(true);
				}
				break;
			case R.id.btnAbout:
				startActivity(new Intent(this,InformationActivity.class));
				break;
			case R.id.btnHelp:
				try
				{
					Intent telIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://telegram.me/puzhle_support"));
					telIntent.setPackage("org.telegram.messenger");
					startActivity(telIntent);
				} catch(Exception ex) {
					toastManager.show("مشکل در ارتباط با تلگرام");
				}
				break;
		}
	}

	/**
	 * API to update layout views based upon user signed in status
	 */
	public void processUIUpdate (boolean isLoggedIn)
	{
		if (isLoggedIn)//(uow.getUserRepo().isUserLoggedIn())
		{
			googleAccount.setImageResource(R.drawable.btn_connected);
			googleAccount.setEnabled(false);
		} else
		{
			googleAccount.setImageResource(R.drawable.btn_disconnected);
			googleAccount.setEnabled(true);
		}
	}

	/**
	 * Handle results for your startActivityForResult() calls. Use requestCode
	 * to differentiate.
	 */
	@Override
	protected void onActivityResult (int requestCode, int resultCode, Intent data)
	{
		if (requestCode == REQ_SIGN_IN_REQUIRED && resultCode == RESULT_OK)
		{
			// We had to sign in - now we can finish off the token request.
//			final String email = Plus.AccountApi.getAccountName(mGoogleApiClient);
//			uow.getUserRepo().attachToGooglePlusMail(this, email, progressDlg);
			return;
		}

		if (requestCode == SIGN_IN_REQUEST_CODE)
		{
			if (resultCode != RESULT_OK)
			{
				mSignInClicked = false;
			}

			mIntentInProgress = false;

//			if (!mGoogleApiClient.isConnecting())
//			{
//				mGoogleApiClient.connect();
//			}
		}
	}

	/**
	 * API to revoke granted access After revoke user will be asked to grant
	 * permission on next sign in
	 */
//	private void processRevokeRequest ()
//	{
//		if (mGoogleApiClient.isConnected())
//		{
//			Plus.AccountApi.clearDefaultAccount(mGoogleApiClient);
//			Plus.AccountApi.revokeAccessAndDisconnect(mGoogleApiClient)
//					.setResultCallback(arg0 -> {
//                        Toast.makeText(getApplicationContext(),
//                                "User permissions revoked",
//                                Toast.LENGTH_LONG).show();
//                        mGoogleApiClient = buildGoogleAPIClient();
//                        mGoogleApiClient.connect();
//                        //processUIUpdate(false);
//                    });
//		}
//	}

	/**
	 * API to handler sign in of user If error occurs while connecting process
	 * it in processSignInError() api
	 */
//	private void processSignIn ()
//	{
//		if (!mGoogleApiClient.isConnecting())
//		{
//			progressDlg = dialogManager.showAppProgress();
//			processSignInError();
//			mSignInClicked = true;
//		}
//	}

	/**
	 * API to process sign in error Handle error based on ConnectionResult
	 */
//	public void processSignInError ()
//	{
//		if (mConnectionResult != null && mConnectionResult.hasResolution())
//		{
//			try
//			{
//				mIntentInProgress = true;
//				mConnectionResult.startResolutionForResult(this,
//						SIGN_IN_REQUEST_CODE);
//			} catch (IntentSender.SendIntentException e)
//			{
//				mIntentInProgress = false;
//				mGoogleApiClient.connect();
//			}
//		}
//	}

//	@Override
//	public void onConnected (Bundle bundle)
//	{
//		mSignInClicked = false;
////        Toast.makeText(getApplicationContext(), "Signed In Successfully",
////                Toast.LENGTH_LONG).show();
//
//		final String email = Plus.AccountApi.getAccountName(mGoogleApiClient);
//		if (progressDlg != null) // if user clicked login
//		{
//			uow.getUserRepo().attachToGooglePlusMail(this, email, progressDlg);
//		}
//	}

//	@Override
//	public void onConnectionSuspended (int i)
//	{
//		mGoogleApiClient.connect();
//		//if(progressDlg != null) progressDlg.dismiss();
//	}

//	@Override
//	public void onConnectionFailed (ConnectionResult connectionResult)
//	{
//		if(progressDlg != null) progressDlg.dismiss();
//
//		if (!connectionResult.hasResolution())
//		{
//			GooglePlayServicesUtil.getErrorDialog(connectionResult.getErrorCode(), this,
//					ERROR_DIALOG_REQUEST_CODE).show();
//			return;
//		}
//		if (!mIntentInProgress)
//		{
//			mConnectionResult = connectionResult;
//
//			if (mSignInClicked)
//			{
//				processSignInError();
//			}
//		}
//	}

/*    private class RetrieveTokenTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            String accountName = params[0];
            String scopes = "oauth2:" + Scopes.PLUS_LOGIN;
            String token = null;
            try {
                token = GoogleAuthUtil.getToken(getApplicationContext(), accountName, scopes);
                // TODO: dismiss dialog here
            } catch (IOException e) {
                Log.e("G+", e.getMessage());
            } catch (UserRecoverableAuthException e) {
                startActivityForResult(e.getIntent(), REQ_SIGN_IN_REQUIRED);
            } catch (GoogleAuthException e) {
                Log.e("G+", e.getMessage());
            }
            return accountName;
        }

        @Override
        protected void onPostExecute(String mail) {
            super.onPostExecute(mail);
            uow.getUserRepo().attachToGooglePlusMail(mail, progressDlg);
        }
    }
    */
}
