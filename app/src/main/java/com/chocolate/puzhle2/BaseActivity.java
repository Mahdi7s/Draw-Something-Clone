package com.chocolate.puzhle2;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.WindowManager;

import com.afollestad.materialdialogs.MaterialDialog;
import com.chocolate.puzhle2.Utils.DialogManager;
import com.chocolate.puzhle2.Utils.SfxPlayer;
import com.chocolate.puzhle2.Utils.ToastManager;
import com.chocolate.puzhle2.Utils.Utility;
import com.chocolate.puzhle2.events.HandleErrorCallback;
import com.chocolate.puzhle2.events.HandleErrorOnErrorCallback;
import com.chocolate.puzhle2.repos.UnitOfWork;
import com.parse.ParseException;

import java.util.HashMap;
import java.util.Map;

import de.greenrobot.event.EventBus;

public abstract class BaseActivity extends AppCompatActivity {
    private Map<String, ErrorBlockData> errorHandleMap = new HashMap<>();

    protected DialogManager dialogManager;
    public ToastManager toastManager;
    protected UnitOfWork uow;
    protected SfxPlayer sfxPlayer;

    @Override
    protected void onCreate(Bundle savedInstance) {
        super.onCreate(savedInstance);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setTheme(R.style.AppTheme);

        toastManager = new ToastManager(this);
        dialogManager = new DialogManager(this, toastManager);
        uow = new UnitOfWork(this, dialogManager, toastManager);

        sfxPlayer = SfxPlayer.getInstance(this, uow.getLocalRepo());
        dialogManager.sfxPlayer = sfxPlayer;
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();

        EventBus.getDefault().unregister(this);
    }

    public class ErrorBlockData {
        public String CallbackId;
        public int InnerCallbackCounts;
        public HandleErrorCallback actionCallback;
        public HandleErrorOnErrorCallback onErrorCallback;
        public Object PassingErrorData = null;
    }

    private MaterialDialog serverProgressDlg = null;

    protected void errorableBlock(final String cid, final HandleErrorCallback actionCallback, final HandleErrorOnErrorCallback onErrorCallback) {
        ErrorBlockData errorBlockData;

        errorBlockData = new ErrorBlockData();
        errorBlockData.CallbackId = cid;
        errorBlockData.InnerCallbackCounts = 0; //innerCallbackCounts;
        errorBlockData.actionCallback = (cidtt) -> {
            serverProgressDlg = dialogManager.showServerProgress();
            if (!Utility.isConnectingToInternet(this)) {
                handleError(cidtt, new ParseException(ParseException.TIMEOUT, null));
                return null;
            }

            errorBlockData.PassingErrorData = actionCallback.handle(cidtt);
            return errorBlockData.PassingErrorData;
        };
        errorBlockData.onErrorCallback = onErrorCallback;
        errorHandleMap.put(cid, errorBlockData);
        errorBlockData.actionCallback.handle(cid);
    }

    protected boolean handleError(String callbackId, Exception e) {
        return handleError(callbackId, e, false);
    }

    protected boolean handleError(String callbackId, Exception e, boolean isLast) {
        // use dialogManager to show appropriate dialog
        if (e == null) {
            if (isLast) {
                if (serverProgressDlg != null) {
                    serverProgressDlg.dismiss();
                }
            }
            return true;
        }
        if (serverProgressDlg != null) {
            serverProgressDlg.dismiss();
        }

        ErrorBlockData callbackData = errorHandleMap.get(callbackId);

        int msgCode = -2;
        if (e instanceof ParseException) {
            msgCode = ((ParseException) e).getCode();
        }

        MaterialDialog.ButtonCallback btnCallback = new MaterialDialog.ButtonCallback() {
            @Override
            public void onPositive(MaterialDialog dialog) { // retry
                callbackData.actionCallback.handle(callbackId);
            }

            @Override
            public void onNegative(MaterialDialog dialog) {
                finish();
            }
        };

        switch (msgCode) {
            case ParseException.OBJECT_NOT_FOUND:
                dialogManager.showServerReceiveError(R.string.server_receive_not_found, btnCallback);
                break;
            case ParseException.INTERNAL_SERVER_ERROR:
                dialogManager.showServerReceiveError(R.string.server_receive_busy, btnCallback);
                break;
            case ParseException.OTHER_CAUSE:
                // dont show dialog
//                dialogManager.showApril("err", e.getMessage(), "ok", null);
                break;
            default:
                dialogManager.showServerReceiveError(btnCallback);
                break;
        }

        callbackData.onErrorCallback.onErrorAction(callbackData.PassingErrorData);

        return false;
    }
}
