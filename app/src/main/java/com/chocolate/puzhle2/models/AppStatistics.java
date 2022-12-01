package com.chocolate.puzhle2.models;

import com.chocolate.puzhle2.R;
import com.parse.ParseClassName;
import com.parse.ParseObject;

/**
 * Created by mahdi on 9/25/15.
 */
@ParseClassName("AppStatistics")
public class AppStatistics extends ParseObject {

    public static final PublishDestination publishDestination = PublishDestination.Myket;

    public static String getPublishPackage() {
        return publishDestination == PublishDestination.Bazaar ? "com.farsitel.bazaar" :
                publishDestination == PublishDestination.IrApps ? "ir.tgbs.android.iranapp" :
                        publishDestination == PublishDestination.Myket ? "ir.mservices.market" :
                                "com.ada.market";
    }

    public static String getPublishAction() {
        return publishDestination == PublishDestination.Bazaar ? "ir.cafebazaar.pardakht.InAppBillingService.BIND" :
                publishDestination == PublishDestination.IrApps ? "ir.tgbs.iranapps.billing.InAppBillingService.BIND" :
                        publishDestination == PublishDestination.Myket ? "ir.mservices.market.InAppBillingService.BIND" :
                                "com.ada.market.service.payment.BIND";
    }

    public static int getPublishPublicKey() {
        return publishDestination == PublishDestination.Bazaar ? R.string.bazaar_public_key :
                publishDestination == PublishDestination.IrApps ? R.string.irapps_public_key :
                publishDestination == PublishDestination.Myket ? R.string.myket_public_key :
                        R.string.cando_public_key;
    }

    public static int getStoreConnectionMsgId() {
        return publishDestination == PublishDestination.Bazaar ? R.string.bazaar_connecting_wait :
                publishDestination == PublishDestination.IrApps ? R.string.irapps_connecting_wait :
                        publishDestination == PublishDestination.Myket ? R.string.myket_connecting_wait :
                                R.string.cando_connecting_wait;
    }

    public static int getStoreLoginMsgId() {
        return publishDestination == PublishDestination.Bazaar ? R.string.login_bazaar :
                publishDestination == PublishDestination.IrApps ? R.string.login_irapps :
                        publishDestination == PublishDestination.Myket ? R.string.login_myket :
                                R.string.login_cando;
    }

    public static String getDownloadLink() {
        return publishDestination == PublishDestination.Bazaar ? "http://cafebazaar.ir/app/com.chocolate.puzhle2" :
                publishDestination == PublishDestination.IrApps ? "http://iranapps.ir/app/com.chocolate.puzhle2" :
                        publishDestination == PublishDestination.Myket ? "http://myket.ir/appdetail.aspx?id=com.chocolate.puzhle2" :
                               publishDestination == PublishDestination.Cando ? "http://cando.asr24.com/app.jsp?id=com.chocolate.puzhle2":
                                       "http://choc01ate.com/puzhle.apk";
    }

    public static String marketNotInstalledMsg() {
        return publishDestination == PublishDestination.Bazaar ? "کافه بازار نصب نیست" :
                publishDestination == PublishDestination.IrApps ? "ایران اپس نصب نیست" :
                        publishDestination == PublishDestination.Myket ? "مایکت نصب نیست" :
                                "کندو نصب نیست";
    }

    // -------------------------------------------------------------------------------------------------------

    public int getRandomPuzzlesCount() {
        return getInt("RandomPuzzlesCount");
    }

    public void incrementRandomPuzzlesCount() {
        increment("RandomPuzzlesCount");
    }

    public int getEasyWordsCount() {
        return getInt("EasyWordsCount");
    }

    public int getNormalWordsCount() {
        return getInt("NormalWordsCount");
    }

    public int getHardWordsCount() {
        return getInt("HardWordsCount");
    }

    public boolean isDiscountOn() {
        return getBoolean("DiscountOn");
    }

    public String getFullAdsPic() {
        return getString("FullAdsPic");
    }

    public String getFullAdsLink() {
        return getString("FullAdsLink");
    }
}
