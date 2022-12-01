package com.chocolate.puzhle2.Utils;

import com.chocolate.puzhle2.R;
import com.chocolate.puzhle2.models.AppStatistics;
import com.chocolate.puzhle2.models.GameUser;
import com.chocolate.puzhle2.models.UserScore;

import java.util.Random;

/**
 * Created by mahdi on 10/10/15.
 */
public class MessageUtils {
    private final String Like = "\u2764";
    private final String Lamp = "\uD83D\uDCA1";
    private final String Coins = "\uD83D\uDCB2";
    private final String GiftCoins = "\uD83D\uDCB0";
    private final String Solve = "\u2705";
    private final String Create = "\u270F";
    private final String Balloon = "\uD83C\uDF88";
    private final String PartyPopper = "\uD83C\uDF89";
    private final String WinDislike = "\uD83D\uDC94";
    private final String WinLike = "\uD83D\uDC96";
    private final String LOUDSPEAKER = "\uD83D\uDCE2";
    private final String LeftPoint = "\uD83D\uDC48";
    private final String InstallToMobile = "\uD83D\uDCF2";
    private final String Seperator = "\u2796";
    private final String League = "\uD83C\uDFC6";
    private final String User = "\uD83D\uDC64";
    private final String Game = "\uD83C\uDFAE";
    private final String RightPoint = "\uD83D\uDC49";
    private final String DownPoint = "\uD83D\uDC47";
    private final String WINKING = "\uD83D\uDE1C";

    // -----------------------------------------------------------------

    public String getIntroduceMessage() {
        String[] fakeNames = new String[]{"هستی؟", "اللووو", "ببین", "آغااا", "چه هوایی!", "شلام!", "چطوری؟", "خوبی؟", "چیکار میکنی؟", "کجایی؟", "ج خبرا؟"};
        String name = fakeNames[(new Random()).nextInt(fakeNames.length)];
        String text = String.format("%s بازی زیرو نصب کن تا باهم پازل بسازیم و حل کنیم%s", name + LOUDSPEAKER, LeftPoint + "\n" + AppStatistics.getDownloadLink());
        return text;
    }

    public String getSolveMessage(boolean supportsEmoji) {
        return getSolveHead() + getUserInfos() + getFooter();
    }

    public String getCreateMessage(boolean supportsEmoji, int gift) {
        return getCreateHead(gift) + getUserInfos() + getFooter();
    }

    private String getUserInfos() {
        GameUser gameUser = GameUser.getGameUser();
        UserScore score = gameUser.getScore();

        String infos = String.format("%s%s  %s%s\n", "ا> " + User, gameUser.getScore().getDisplayName(), League, getLeagueName(score.getLeague()) + " <ا");
        infos += String.format("%s%s%s%s%s%s\n", "ا" + Seperator,Seperator,Seperator,Seperator,Seperator,Seperator + "ا");
        infos += String.format("%s%s  %s%s  %s%s\n", "ا> " + Create, score.getCreatedCount(), Solve, score.getSolvedCount() + "", Like, score.getLikesCount() + " <ا");
//        infos += String.format("%s%s%s%s%s%s\n", Seperator,Seperator,Seperator,Seperator,Seperator,Seperator);
        return infos;
    }

    private String getLeagueName(int league) {
        int leagueRes = -1;
        String leagueName = "";
        switch (league) {
            case 0:
                leagueRes = R.drawable.league1;
                leagueName = "آکبند";
                break;
            case 1:
                leagueRes = R.drawable.league2;
                leagueName = "آی کیو";
                break;
            case 2:
                leagueRes = R.drawable.league3;
                leagueName = "عقل کل";
                break;
            case 3:
                leagueRes = R.drawable.league4;
                leagueName = "دانشمند";
                break;
            case 4:
                leagueRes = R.drawable.league5;
                leagueName = "نابغ";
                break;
            case 5:
                leagueRes = R.drawable.league6;
                leagueName = "اعجوب";
                break;
        }
        return leagueName;
    }

    private String getCreateHead(int gift) {
        //return String.format("%s%s%s %s%d %s%s%s\n", LOUDSPEAKER,LOUDSPEAKER,LOUDSPEAKER, GiftCoins, gift, LOUDSPEAKER, LOUDSPEAKER, LOUDSPEAKER);
        return "";
    }

    private String getSolveHead() {
        //return String.format("%s%s%s %s%s%s %s%s%s\n", PartyPopper,PartyPopper,PartyPopper, Balloon,Balloon,Balloon, PartyPopper, PartyPopper, PartyPopper);
        return "";
    }

    private String getFooter() {
        String footer = String.format("واسه اینکه پازل بسازی%s یا حل کنی%s بازی%s %sپاژل%s رو نصب%s کن%s", "", "", "", LeftPoint, RightPoint, "", DownPoint + "\n");
        footer += AppStatistics.getDownloadLink();

        return footer;
    }

//    GameUser gameUser = GameUser.getGameUser();
//
//    String shareText = null;
//    shareText = String.format("%s:%s|امتیاز:%d", winShare ? "حل کنند" : "سازند", gameUser.getDisplayName(), gameUser.getScore().getTotalScore());
//    shareText += "\n--------------------------\n";
//
//    if (winShare)
//    {
//        shareText += "با پاژل پازل بسازید و وش دوستاتون رو بسنجید.";
//    } else {
//        shareText += "برای حل پازل بازی پاژل رو از کاف بازار دریافت کنید.";
//    }
//    shareText += "\n" + "http://cafebazaar.ir/app/com.chocolate.wm/?l=fa";
}
