package com.chocolate.puzhle2.Utils;

import android.content.Context;
import android.widget.Toast;

import com.chocolate.puzhle2.R;

/**
 * Created by choc01ate on 5/12/2015.
 */
public class ToastManager
{
	private Context context;

	public ToastManager (Context context)
	{
		this.context = context.getApplicationContext();
	}

	public void successfulLogin ()
	{
		Toast.makeText(context, R.string.toast_successful_login, Toast.LENGTH_LONG).show();
	}

	public void notAnyAppToShate ()
	{
		Toast.makeText(context, R.string.toast_not_any_app_share, Toast.LENGTH_LONG).show();
	}

	public void useOnlyThreeWords ()
	{
		Toast.makeText(context, R.string.toast_only_three_words, Toast.LENGTH_SHORT).show();
	}

	public void useOnlyFarsiLetters ()
	{
		Toast.makeText(context, R.string.toast_only_farsi_letters, Toast.LENGTH_SHORT).show();
	}

	public void eachWordsShouldHaveTwoLetter ()
	{
		Toast.makeText(context, R.string.toast_two_letter_word, Toast.LENGTH_SHORT).show();
	}

	public void puzzleNotSupported ()
	{
		Toast.makeText(context, R.string.toast_puzzle_not_supported, Toast.LENGTH_LONG).show();
	}

	public void errorWhileImageLoad ()
	{
		Toast.makeText(context, R.string.toast_err_while_img_load, Toast.LENGTH_LONG).show();
	}

	public void youCouldNotSolveYourPuzzle ()
	{
		Toast.makeText(context, R.string.toast_cant_solve_yourself, Toast.LENGTH_LONG).show();
	}

	public void puzzleLifeFinished ()
	{
		Toast.makeText(context, R.string.toast_puzzle_life_end, Toast.LENGTH_LONG).show();
	}

	public void canSolveButNoScore ()
	{
		Toast.makeText(context, R.string.toast_puzzle_solve_no_achieve, Toast.LENGTH_LONG).show();
	}

	public void youSolvedPuzzleBefore ()
	{
		Toast.makeText(context, R.string.toast_puzzle_solved_before, Toast.LENGTH_LONG).show();
	}

	public void drawViewIsEmpty() {
		Toast.makeText(context, "تو که چیزی نکشیدی :|", Toast.LENGTH_LONG).show();
	}

	public void youGotRateGift() {
		Toast.makeText(context, "دمت گرم :)", Toast.LENGTH_LONG).show();
	}

	public void show(String text){
		Toast.makeText(context, text, Toast.LENGTH_LONG).show();
	}
}
