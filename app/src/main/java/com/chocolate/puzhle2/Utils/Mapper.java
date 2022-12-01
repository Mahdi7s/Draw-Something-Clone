package com.chocolate.puzhle2.Utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Created by mahdi on 5/29/15.
 */
public class Mapper
{
	public static final List<String> allPeLetters = Arrays.asList("آ،ا،ب،پ،ت،ث،ج،چ،ح،خ،د،ذ،ر،ز،ژ،س،ش،ص،ض،ط،ظ،ع،غ،ف،ق،ک،گ،ل،م،ن،و،ه،ی".split("،"));
	public static final List<String> allEnLetters = Arrays.asList("a,b,c,d,e,f,g,h,i,j,k,l,m,n,o,p,q,r,s,t,u,v,w,x,y,z,A,B,C,D,E,F,G".split(","));

	public static String mapToEnglish (String farsiText)
	{
		farsiText = farsiText.trim();
		String engText = "";
		for (char c : farsiText.toCharArray())
		{
			if (Character.isWhitespace(c))
			{
				engText += " ";
			} else
			{
				engText += allEnLetters.get(allPeLetters.indexOf(c + ""));
			}
		}
		return simpleEncrypt(engText);
	}

	public static String simpleEncrypt (String enStr)
	{
		int randIdx = (int) (Math.random() * allEnLetters.size());
		String randChar = allEnLetters.get(randIdx);
		List<String> tempEns = getTempEncryptionEnArr(randChar, randIdx);

		String retval = "";
		for (char c : enStr.toCharArray())
		{
			if (allEnLetters.contains(c + ""))
			{
				int orginalIdx = allEnLetters.indexOf(c + "");
				String fakeChar = tempEns.get(orginalIdx);
				retval += fakeChar;
			} else
			{
				retval += c + "";
			}
		}

		retval += randChar;
		return retval;
	}

	public static String mapToFarsi (String engText)
	{
		engText = simpleDecrypt(engText).trim();
		String faText = "";
		for (char c : engText.toCharArray())
		{
			if (Character.isWhitespace(c))
			{
				faText += " ";
			} else
			{
				faText += allPeLetters.get(allEnLetters.indexOf(c + ""));
			}
		}
		return faText;
	}

	public static String simpleDecrypt (String enStr)
	{

		String randChar = enStr.charAt(enStr.length() - 1) + "";
		enStr = enStr.substring(0, enStr.length() - 1);
		List<String> tempEns = getTempEncryptionEnArr(randChar, allEnLetters.indexOf(randChar));
		String retval = "";
		for (char c : enStr.toCharArray())
		{
			if (allEnLetters.contains(c + ""))
			{
				int fakeIdx = tempEns.indexOf(c + "");
				String orgChar = allEnLetters.get(fakeIdx);
				retval += orgChar;
			} else
			{
				retval += c + "";
			}
		}
		return retval;
	}

	private static List<String> getTempEncryptionEnArr (String randChar, int randIdx)
	{
		ArrayList<String> tempEns = new ArrayList<>(allEnLetters.subList(randIdx, allEnLetters.size()));
		Collections.reverse(tempEns);
		ArrayList<String> seg2 = new ArrayList<>(allEnLetters.subList(0, randIdx));
		Collections.reverse(seg2);
		tempEns.addAll(seg2);

		return tempEns;
	}
}
