/*******************************************************************************
 * Copyright (c) 2003-2009 Plant Bioinformatics Group, IPK Gatersleben
 *******************************************************************************/
/*
 * Created on Jul 16, 2010 by Christian Klukas
 */

package org;

import java.awt.Color;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.TreeSet;
import java.util.Vector;

/**
 * @author klukas
 */
public class StringManipulationTools implements HelperClass {
	
	public static final String Unicode = "UTF-8";
	
	@SuppressWarnings("nls")
	public static final String[][] htmlNamedEntity2Unicode = {
			{ "&nbsp;", "\u00A0" },
			{ "&iexcl;", "\u00A1" },
			{ "&cent;", "\u00A2" },
			{ "&pound;", "\u00A3" },
			{ "&curren;", "\u00A4" },
			{ "&yen;", "\u00A5" },
			{ "&brvbar;", "\u00A6" },
			{ "&sect;", "\u00A7" },
			{ "&uml;", "\u00A8" },
			{ "&copy;", "\u00A9" },
			{ "&ordf;", "\u00AA" },
			{ "&laquo;", "\u00AB" },
			{ "&not;", "\u00AC" },
			{ "&shy;", "\u00AD" },
			{ "&reg;", "\u00AE" },
			{ "&macr;", "\u00AF" },
			{ "&deg;", "\u00B0" },
			{ "&plusmn;", "\u00B1" },
			{ "&sup2;", "\u00B2" },
			{ "&sup3;", "\u00B3" },
			{ "&acute;", "\u00B4" },
			{ "&micro;", "\u00B5" },
			{ "&para;", "\u00B6" },
			{ "&middot;", "\u00B7" },
			{ "&cedil;", "\u00B8" },
			{ "&sup1;", "\u00B9" },
			{ "&ordm;", "\u00BA" },
			{ "&raquo;", "\u00BB" },
			{ "&frac14;", "\u00BC" },
			{ "&frac12;", "\u00BD" },
			{ "&frac34;", "\u00BE" },
			{ "&iquest;", "\u00BF" },
			{ "&Agrave;", "\u00C0" },
			{ "&Aacute;", "\u00C1" },
			{ "&Acirc;", "\u00C2" },
			{ "&Atilde;", "\u00C3" },
			{ "&Auml;", "\u00C4" },
			{ "&Aring;", "\u00C5" },
			{ "&AElig;", "\u00C6" },
			{ "&Ccedil;", "\u00C7" },
			{ "&Egrave;", "\u00C8" },
			{ "&Eacute;", "\u00C9" },
			{ "&Ecirc;", "\u00CA" },
			{ "&Euml;", "\u00CB" },
			{ "&Igrave;", "\u00CC" },
			{ "&Iacute;", "\u00CD" },
			{ "&Icirc;", "\u00CE" },
			{ "&Iuml;", "\u00CF" },
			{ "&ETH;", "\u00D0" },
			{ "&Ntilde;", "\u00D1" },
			{ "&Ograve;", "\u00D2" },
			{ "&Oacute;", "\u00D3" },
			{ "&Ocirc;", "\u00D4" },
			{ "&Otilde;", "\u00D5" },
			{ "&Ouml;", "\u00D6" },
			{ "&times;", "\u00D7" },
			{ "&Oslash;", "\u00D8" },
			{ "&Ugrave;", "\u00D9" },
			{ "&Uacute;", "\u00DA" },
			{ "&Ucirc;", "\u00DB" },
			{ "&Uuml;", "\u00DC" },
			{ "&Yacute;", "\u00DD" },
			{ "&THORN;", "\u00DE" },
			{ "&szlig;", "\u00DF" },
			{ "&agrave;", "\u00E0" },
			{ "&aacute;", "\u00E1" },
			{ "&acirc;", "\u00E2" },
			{ "&atilde;", "\u00E3" },
			{ "&auml;", "\u00E4" },
			{ "&aring;", "\u00E5" },
			{ "&aelig;", "\u00E6" },
			{ "&ccedil;", "\u00E7" },
			{ "&egrave;", "\u00E8" },
			{ "&eacute;", "\u00E9" },
			{ "&ecirc;", "\u00EA" },
			{ "&euml;", "\u00EB" },
			{ "&igrave;", "\u00EC" },
			{ "&iacute;", "\u00ED" },
			{ "&icirc;", "\u00EE" },
			{ "&iuml;", "\u00EF" },
			{ "&eth;", "\u00F0" },
			{ "&ntilde;", "\u00F1" },
			{ "&ograve;", "\u00F2" },
			{ "&oacute;", "\u00F3" },
			{ "&ocirc;", "\u00F4" },
			{ "&otilde;", "\u00F5" },
			{ "&ouml;", "\u00F6" },
			{ "&divide;", "\u00F7" },
			{ "&oslash;", "\u00F8" },
			{ "&ugrave;", "\u00F9" },
			{ "&uacute;", "\u00FA" },
			{ "&ucirc;", "\u00FB" },
			{ "&uuml;", "\u00FC" },
			{ "&yacute;", "\u00FD" },
			{ "&thorn;", "\u00FE" },
			{ "&yuml;", "\u00FF" },
			{ "&OElig;", "\u0152" },
			{ "&oelig;", "\u0153" },
			{ "&quot;", "\\u0022" },
			{ "&Scaron;", "\u0160" },
			{ "&scaron;", "\u0161" },
			{ "&Yuml;", "\u0178" },
			{ "&amp;", "\u0026" },
			{ "&apos;", "\u0027" },
			{ "&fnof;", "\u0192" },
			{ "&lt;", "\u003C" },
			{ "&gt;", "\u003E" },
			{ "&circ;", "\u02C6" },
			{ "&tilde;", "\u02DC" },
			{ "&ensp;", "\u2002" },
			{ "&emsp;", "\u2003" },
			{ "&thinsp;", "\u2009" },
			{ "&zwnj;", "\u200C" },
			{ "&zwj;", "\u200D" },
			{ "&lrm;", "\u200E" },
			{ "&rlm;", "\u200F" },
			{ "&ndash;", "\u2013" },
			{ "&mdash;", "\u2014" },
			{ "&lsquo;", "\u2018" },
			{ "&rsquo;", "\u2019" },
			{ "&sbquo;", "\u201A" },
			{ "&ldquo;", "\u201C" },
			{ "&rdquo;", "\u201D" },
			{ "&bdquo;", "\u201E" },
			{ "&dagger;", "\u2020" },
			{ "&Dagger;", "\u2021" },
			{ "&bull;", "\u2022" },
			{ "&hellip;", "\u2026" },
			{ "&permil;", "\u2030" },
			{ "&prime;", "\u2032" },
			{ "&Prime;", "\u2033" },
			{ "&lsaquo;", "\u2039" },
			{ "&rsaquo;", "\u203A" },
			{ "&oline;", "\u203E" },
			{ "&frasl;", "\u2044" },
			{ "&euro;", "\u20AC" },
			{ "&image;", "\u2111" },
			{ "&weierp;", "\u2118" },
			{ "&real;", "\u211C" },
			{ "&trade;", "\u2122" },
			{ "&alefsym;", "\u2135" },
			{ "&larr;", "\u2190" },
			{ "&uarr;", "\u2191" },
			{ "&rarr;", "\u2192" },
			{ "&darr;", "\u2193" },
			{ "&harr;", "\u2194" },
			{ "&crarr;", "\u21B5" },
			{ "&lArr;", "\u21D0" },
			{ "&uArr;", "\u21D1" },
			{ "&rArr;", "\u21D2" },
			{ "&dArr;", "\u21D3" },
			{ "&hArr;", "\u21D4" },
			{ "&forall;", "\u2200" },
			{ "&part;", "\u2202" },
			{ "&exist;", "\u2203" },
			{ "&empty;", "\u2205" },
			{ "&nabla;", "\u2207" },
			{ "&isin;", "\u2208" },
			{ "&notin;", "\u2209" },
			{ "&ni;", "\u220B" },
			{ "&prod;", "\u220F" },
			{ "&sum;", "\u2211" },
			{ "&minus;", "\u2212" },
			{ "&lowast;", "\u2217" },
			{ "&radic;", "\u221A" },
			{ "&prop;", "\u221D" },
			{ "&infin;", "\u221E" },
			{ "&ang;", "\u2220" },
			{ "&and;", "\u2227" },
			{ "&or;", "\u2228" },
			{ "&cap;", "\u2229" },
			{ "&cup;", "\u222A" },
			{ "&int;", "\u222B" },
			{ "&there4;", "\u2234" },
			{ "&sim;", "\u223C" },
			{ "&cong;", "\u2245" },
			{ "&asymp;", "\u2248" },
			{ "&ne;", "\u2260" },
			{ "&equiv;", "\u2261" },
			{ "&le;", "\u2264" },
			{ "&ge;", "\u2265" },
			{ "&sub;", "\u2282" },
			{ "&sup;", "\u2283" },
			{ "&nsub;", "\u2284" },
			{ "&sube;", "\u2286" },
			{ "&supe;", "\u2287" },
			{ "&oplus;", "\u2295" },
			{ "&otimes;", "\u2297" },
			{ "&perp;", "\u22A5" },
			{ "&sdot;", "\u22C5" },
			{ "&lceil;", "\u2308" },
			{ "&rceil;", "\u2309" },
			{ "&lfloor;", "\u230A" },
			{ "&rfloor;", "\u230B" },
			{ "&lang;", "\u2329" },
			{ "&rang;", "\u232A" },
			{ "&Alpha;", "\u0391" },
			{ "&Beta;", "\u0392" },
			{ "&Gamma;", "\u0393" },
			{ "&Delta;", "\u0394" },
			{ "&Epsilon;", "\u0395" },
			{ "&Zeta;", "\u0396" },
			{ "&Eta;", "\u0397" },
			{ "&Theta;", "\u0398" },
			{ "&Iota;", "\u0399" },
			{ "&Kappa;", "\u039A" },
			{ "&Lambda;", "\u039B" },
			{ "&Mu;", "\u039C" },
			{ "&Nu;", "\u039D" },
			{ "&Xi;", "\u039E" },
			{ "&Omicron;", "\u039F" },
			{ "&Pi;", "\u03A0" },
			{ "&Rho;", "\u03A1" },
			{ "&Sigma;", "\u03A3" },
			{ "&Tau;", "\u03A4" },
			{ "&Upsilon;", "\u03A5" },
			{ "&Phi;", "\u03A6" },
			{ "&Chi;", "\u03A7" },
			{ "&Psi;", "\u03A8" },
			{ "&Omega;", "\u03A9" },
			{ "&alpha;", "\u03B1" },
			{ "&beta;", "\u03B2" },
			{ "&gamma;", "\u03B3" },
			{ "&delta;", "\u03B4" },
			{ "&epsilon;", "\u03B5" },
			{ "&zeta;", "\u03B6" },
			{ "&eta;", "\u03B7" },
			{ "&theta;", "\u03B8" },
			{ "&iota;", "\u03B9" },
			{ "&kappa;", "\u03BA" },
			{ "&lambda;", "\u03BB" },
			{ "&mu;", "\u03BC" },
			{ "&nu;", "\u03BD" },
			{ "&xi;", "\u03BE" },
			{ "&omicron;", "\u03BF" },
			{ "&pi;", "\u03C0" },
			{ "&rho;", "\u03C1" },
			{ "&sigmaf;", "\u03C2" },
			{ "&sigma;", "\u03C3" },
			{ "&tau;", "\u03C4" },
			{ "&upsilon;", "\u03C5" },
			{ "&phi;", "\u03C6" },
			{ "&chi;", "\u03C7" },
			{ "&loz;", "\u25CA" },
			{ "&psi;", "\u03C8" },
			{ "&omega;", "\u03C9" },
			{ "&thetasym;", "\u03D1" },
			{ "&upsih;", "\u03D2" },
			{ "&piv;", "\u03D6" },
			{ "&spades;", "\u2660" },
			{ "&clubs;", "\u2663" },
			{ "&hearts;", "\u2665" },
			{ "&diams;", "\u2666" } };
	
	public static final String[][] parse2Latex = {
			{ "\"", "\\\"'" },
			{ "\\", "\\textbackslash" },
			{ "ä", "\\\"a" },
			{ "ö", "\\\"o" },
			{ "ü", "\\\"ü" },
			{ "ß", "\\ss" },
			// { "_", "\\_" },
			{ "<", "\\textless" },
			{ ">", "\\textgreater" },
			{ "§", "\\S" },
			{ "$", "\\$" },
			{ "&", "\\&" },
			{ "#", "\\#" },
			{ "{", "\\{" },
			{ "}", "\\}" },
			{ "%", "\\%" },
			{ "~", "\\textasciitilde" },
			
			{ "€", "\\texteuro" } };
	
	/**
	 * Replace occurrences of a substring.
	 * http://ostermiller.org/utils/StringHelper.html
	 * StringHelper.replace("1-2-3", "-", "|");<br>
	 * result: "1|2|3"<br>
	 * StringHelper.replace("-1--2-", "-", "|");<br>
	 * result: "|1||2|"<br>
	 * StringHelper.replace("123", "", "|");<br>
	 * result: "123"<br>
	 * StringHelper.replace("1-2---3----4", "--", "|");<br>
	 * result: "1-2|-3||4"<br>
	 * StringHelper.replace("1-2---3----4", "--", "---");<br>
	 * result: "1-2----3------4"<br>
	 * 
	 * @param s
	 *           String to be modified.
	 * @param find
	 *           String to find.
	 * @param replace
	 *           String to replace.
	 * @return a string with all the occurrences of the string to find replaced.
	 * @throws NullPointerException
	 *            if s is null.
	 */
	public static String stringReplace(String s, String find, String replace) {
		int findLength;
		// the next statement has the side effect of throwing a null pointer
		// exception if s is null.
		if (s == null)
			return s;
		int stringLength = s.length();
		if (find == null || (findLength = find.length()) == 0) {
			// If there is nothing to find, we won't try and find it.
			return s;
		}
		if (replace == null) {
			// a null string and an empty string are the same
			// for replacement purposes.
			replace = ""; //$NON-NLS-1$
		}
		int replaceLength = replace.length();
		
		// We need to figure out how long our resulting string will be.
		// This is required because without it, the possible resizing
		// and copying of memory structures could lead to an unacceptable runtime.
		// In the worst case it would have to be resized n times with each
		// resize having a O(n) copy leading to an O(n^2) algorithm.
		int length;
		if (findLength == replaceLength) {
			// special case in which we don't need to count the replacements
			// because the count falls out of the length formula.
			length = stringLength;
		} else {
			int count;
			int start;
			int end;
			
			// Scan s and count the number of times we find our target.
			count = 0;
			start = 0;
			while ((end = s.indexOf(find, start)) != -1) {
				count++;
				start = end + findLength;
			}
			if (count == 0) {
				// special case in which on first pass, we find there is nothing
				// to be replaced. No need to do a second pass or create a string
				// buffer.
				return s;
			}
			length = stringLength - (count * (findLength - replaceLength));
		}
		
		int start = 0;
		int end = s.indexOf(find, start);
		if (end == -1) {
			// nothing was found in the string to replace.
			// we can get this if the find and replace strings
			// are the same length because we didn't check before.
			// in this case, we will return the original string
			return s;
		}
		// it looks like we actually have something to replace
		// *sigh* allocate memory for it.
		StringBuffer sb = new StringBuffer(length);
		
		// Scan s and do the replacements
		while (end != -1) {
			sb.append(s.substring(start, end).toString());
			sb.append(replace.toString());
			start = end + findLength;
			end = s.indexOf(find, start);
		}
		end = stringLength;
		sb.append(s.substring(start, end).toString());
		
		return (sb.toString());
	}
	
	public static String removeHTMLtags(String textWithHtmlTags) {
		if (textWithHtmlTags == null)
			return null;
		textWithHtmlTags = stringReplace(textWithHtmlTags, "<p>", " ");
		textWithHtmlTags = stringReplace(textWithHtmlTags, "<br>", " ");
		String res = StringManipulationTools.removeTags(textWithHtmlTags, "<", ">");
		res = stringReplace(res, "&nbsp;", " ");
		res = stringReplace(res, "%20", " ");
		return res;
	}
	
	public static String removeTags(String textWithHtmlTags, String tagA, String tagB) {
		if (textWithHtmlTags == null)
			return null;
		int tagApos = textWithHtmlTags.indexOf(tagA);
		while (tagApos >= 0) {
			int tagBpos = textWithHtmlTags.indexOf(tagB, tagApos + tagB.length()) + tagB.length();
			if (tagB.length() > 0 && tagBpos > 0) {
				textWithHtmlTags = textWithHtmlTags.substring(0, tagApos) + textWithHtmlTags.substring(tagBpos);
				tagApos = textWithHtmlTags.indexOf(tagA);
			} else {
				textWithHtmlTags = textWithHtmlTags.substring(0, tagApos);
				tagApos = textWithHtmlTags.indexOf(tagA);
			}
		}
		if (tagB.length() > 0 && textWithHtmlTags.indexOf(tagB) >= 0)
			textWithHtmlTags = textWithHtmlTags.substring(textWithHtmlTags.indexOf(tagB) + tagB.length());
		return textWithHtmlTags;
	}
	
	public static StringAnnotationProcessor getAnnotationProcessor(String value) {
		return new StringAnnotationProcessor(value);
	}
	
	/**
	 * Removes the tags from a html-text and gives back the striped text.
	 * 
	 * @param textWithHtmlTags
	 *           the text with html tags
	 * @param tagA
	 *           The left tag (e.g. <a>)
	 * @param tagB
	 *           The right tag (e.g. </a>)
	 * @return The array list< string>, where get(0) is the striped text and all
	 *         other are the striped texts
	 */
	public static ArrayList<String> removeTagsGetTextAndRemovedTexts(String textWithHtmlTags, String tagA, String tagB) {
		ArrayList<String> tu = new ArrayList<String>();
		if (textWithHtmlTags == null)
			return null;
		
		tu.add(textWithHtmlTags);
		int tagApos = tu.get(0).indexOf(tagA);
		while (tagApos >= 0) {
			int tagBpos = tu.get(0).indexOf(tagB, tagApos + tagB.length()) + tagB.length();
			if (tagBpos > 0) {
				tu.add(tu.get(0).substring(tagApos + tagA.length(), tagBpos - tagB.length()));
				tu.set(0, tu.get(0).substring(0, tagApos) + tu.get(0).substring(tagBpos));
				tagApos = tu.get(0).indexOf(tagA);
			} else {
				tu.add(tu.get(0).substring(tagApos + 1));
				tu.set(0, tu.get(0).substring(0, tagApos));
				tagApos = tu.get(0).indexOf(tagA);
			}
		}
		if (tu.get(0).indexOf(tagB) >= 0)
			tu.set(0, tu.get(0).substring(tu.get(0).indexOf(tagB) + tagB.length()));
		
		return tu;
	}
	
	/**
	 * Supports html break-tags, br, li, p
	 */
	public static String getWordWrap(String desc, int width) {
		if (desc == null)
			return desc;
		StringBuilder res = new StringBuilder();
		for (String splitter : new String[] { "<li>" }) {
			int n = 0;
			for (String w : desc.split(splitter)) {
				if (n > 0)
					res.append(splitter);
				res.append(getWordWrapString(w, width));
				n++;
			}
		}
		return res.toString();
	}
	
	/**
	 * Assumes pure text string with no html tags.
	 */
	public static String getWordWrapString(String desc, int width) {
		String[] words = desc.split(" ");
		String result = "";
		int column = 0;
		for (int i = 0; i < words.length; i++) {
			if (i > 0 && column + words[i].length() > width) {
				result = result + "<br>" + words[i];
				column = words[i].length();
			} else
				if (i > 0) {
					result = result + " " + words[i];
					column += words[i].length();
				} else {
					result = words[0];
					column = words[0].length();
				}
		}
		return result;
	}
	
	public static String getWordWrap(String[] desc, int width) {
		StringBuilder sb = new StringBuilder();
		for (String s : desc) {
			sb.append(getWordWrap(s, width));
		}
		return sb.toString();
	}
	
	/**
	 * @param mapName
	 * @return
	 */
	final static String[] numbers = new String[] { "0", "1", "2", "3", "4", "5", "6", "7", "8", "9" };
	final static Character[] numberChars = new Character[] { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9' };
	
	public static String removeNumbersFromString(String s) {
		for (String r : numbers)
			s = stringReplace(s, r, "");
		return s;
	}
	
	public static String getNumbersFromString(String s) {
		StringBuilder res = new StringBuilder();
		if (s != null)
			for (Character c : s.toCharArray()) {
				for (Character n : numberChars) {
					if (n.equals(c)) {
						res.append(n);
						break;
					}
				}
			}
		return res.toString();
	}
	
	public static List<Integer> getAllNumbersFromString(String str) {
		
		if (str == null || str.length() == 0) {
			return null;
		}
		ArrayList<Integer> ints = new ArrayList<Integer>();
		
		StringBuffer strBuff = new StringBuffer();
		char c;
		
		for (int i = 0; i < str.length(); i++) {
			c = str.charAt(i);
			
			if (Character.isDigit(c))
				strBuff.append(c);
			else
				if (strBuff.length() > 0) {
					ints.add(new Integer(strBuff.toString()));
					strBuff = new StringBuffer();
				}
		}
		return ints;
	}
	
	public static String UnicodeToURLsyntax(String unicodeText) {
		if (unicodeText == null)
			return null;
		StringBuffer result = new StringBuffer();
		char[] characters = unicodeText.toCharArray();
		for (int i = 0; i < characters.length; i++) {
			char curChar = characters[i];
			if (curChar < 128 && Character.isLetterOrDigit(curChar)) {
				result.append(curChar);
			} else {
				String html = "%" + new Integer(curChar).toString();
				result.append(html);
			}
		}
		return result.toString();
	}
	
	public static String UnicodeToHtml(String unicodeText, HashSet<Character> badChars) {
		StringBuffer result = new StringBuffer();
		char[] characters = unicodeText.toCharArray();
		for (int i = 0; i < characters.length; i++) {
			char curChar = characters[i];
			if (!badChars.contains(curChar)) {
				result.append(curChar);
			} else {
				String html = "&#" + new Integer(curChar).toString() + ";";
				while (html.length() < 8)
					html = stringReplace(html, "&#", "&#0");
				result.append(html);
			}
		}
		return result.toString();
	}
	
	public static String UnicodeToHtml(String unicodeText) {
		StringBuffer result = new StringBuffer();
		char[] characters = unicodeText.toCharArray();
		for (int i = 0; i < characters.length; i++) {
			char curChar = characters[i];
			if (curChar < 128 && Character.isLetterOrDigit(curChar)) {
				result.append(curChar);
			} else
				if (curChar == 13)
					result.append("<br>");
				else {
					String html = "&#" + new Integer(curChar).toString() + ";";
					while (html.length() < 8)
						html = stringReplace(html, "&#", "&#0");
					result.append(html);
				}
		}
		return result.toString();
	}
	
	/**
	 * @param html
	 * @return
	 */
	public synchronized static String htmlToUnicode(String html) {
		String uni = html;
		uni = StringManipulationTools.stringReplace(uni, "&# ", "");
		int p = uni.indexOf("&#"); //$NON-NLS-1$
		while (p >= 0) {
			String s = uni.substring(p + 1);
			int p2 = s.indexOf(";"); //$NON-NLS-1$
			String code = null;
			try {
				code = s.substring(1, p2);
			} catch (Exception err) {
				System.err.println("Invalid String (Error Type 1): " + html);
				ErrorMsg.addErrorMessage("Invalid String (Error Type 1): " + html);
				break;
			}
			int ic;
			try {
				ic = Integer.parseInt(code);
			} catch (Exception ee) {
				System.err.println("Invalid String (Error Type 2): " + html);
				ErrorMsg.addErrorMessage("Invalid String (Error Type 2): " + html);
				break;
			}
			char c = (char) ic;
			uni = stringReplace(uni, "&#" + code + ";", c + ""); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			p = uni.indexOf("&#"); //$NON-NLS-1$
		}
		return uni;
	}
	
	/**
	 * converts all HTML named entities within a string to unicode
	 * 
	 * @param html
	 * @return unicode string
	 */
	public static String htmlNamedEntities2Unicode(String html) {
		String unicode = html;
		for (int k = 0; k < htmlNamedEntity2Unicode.length; k++)
			unicode = stringReplace(unicode, htmlNamedEntity2Unicode[k][0], htmlNamedEntity2Unicode[k][1]);
		return unicode;
	}
	
	/**
	 * converts special entities within a string to latex
	 * 
	 * @param string
	 * @return latex string
	 */
	public static String string2Latex(String inputString) {
		String latexString = inputString;
		for (int k = 0; k < parse2Latex.length; k++)
			latexString = stringReplace(latexString, parse2Latex[k][0], parse2Latex[k][1]);
		return latexString;
	}
	
	public static String getFileSystemName(String name) {
		String namenew = stringReplace(name, "*", "");
		namenew = stringReplace(namenew, "<br>", " ");
		namenew = stringReplace(namenew, ":", "_");
		namenew = stringReplace(namenew, " ", "_");
		namenew = stringReplace(namenew, "/", "_");
		namenew = stringReplace(namenew, "\\", "_");
		namenew = stringReplace(namenew, "<", "_");
		namenew = stringReplace(namenew, ">", "_");
		return namenew;
	}
	
	/**
	 * @return null, if no value is found to be added to the result. This is different
	 *         to the other getStringList method calls!
	 */
	public static String getStringList(HashMap<Integer, String> elements,
			Integer[] cols, String div) {
		if (elements == null || elements.size() <= 0)
			return null;
		else {
			StringBuilder sb = new StringBuilder();
			int i = 0;
			if (cols != null)
				for (Integer idx : cols) {
					String v = elements.get(idx);
					if (v == null)
						continue;
					sb.append(v);
					if (i < cols.length - 1)
						sb.append(div);
					i++;
				}
			return sb.toString();
		}
	}
	
	public static ArrayList<String> getStringListFromArray(String[] elements) {
		ArrayList<String> al = new ArrayList<String>();
		if (elements != null)
			for (String o : elements)
				al.add(o);
		return al;
	}
	
	/**
	 * @return An empty string, if no value is added to the result.
	 */
	public static String getStringList(Object[] elements, String div) {
		ArrayList<Object> al = new ArrayList<Object>();
		if (elements != null)
			for (Object o : elements)
				al.add(o);
		return getStringList(al, div);
	}
	
	public static String getStringList(Collection<?> elements, String div) {
		if (elements == null || elements.size() <= 0)
			return "";
		else {
			StringBuilder sb = new StringBuilder();
			int i = 0;
			for (Object e : elements) {
				sb.append(e + "");
				if (div != null)
					if (i < elements.size() - 1)
						sb.append(div);
				i++;
			}
			return sb.toString();
		}
	}
	
	public static String getStringListMerge(Collection<?> elements, String div) {
		if (elements == null || elements.size() <= 0)
			return "";
		else {
			HashMap<String, Integer> known2cnt = new HashMap<String, Integer>();
			for (Object e : elements) {
				String s = e + "";
				if (!known2cnt.containsKey(s))
					known2cnt.put(s, 1);
				else
					known2cnt.put(s, known2cnt.get(s) + 1);
			}
			HashSet<String> added = new HashSet<String>();
			StringBuilder sb = new StringBuilder();
			for (Object e : elements) {
				String s = e + "";
				if (added.contains(s))
					continue;
				else
					added.add(s);
				if (sb.length() > 0)
					sb.append(div);
				int n = known2cnt.get(s);
				if (n > 1)
					sb.append(s + " (" + n + ")");
				else
					sb.append(s);
			}
			return sb.toString();
		}
	}
	
	public static String getStringList(String pre, Collection<?> elements, String div) {
		if (elements == null || elements.size() <= 0)
			return "";
		else {
			StringBuilder sb = new StringBuilder();
			int i = 0;
			for (Object e : elements) {
				sb.append(pre + e + "");
				if (i < elements.size() - 1)
					sb.append(div);
				i++;
			}
			return sb.toString();
		}
	}
	
	public static String getStringList(String pre, Collection<?> elements, String div, int nRoll, String rollDiv) {
		if (elements == null || elements.size() <= 0)
			return "";
		else {
			StringBuilder sb = new StringBuilder();
			int i = 0;
			for (Object e : elements) {
				sb.append(pre + e + "");
				if (i < elements.size() - 1)
					sb.append(div);
				i++;
				if (i % nRoll == 0)
					sb.append(rollDiv);
			}
			return sb.toString();
		}
	}
	
	public static String[] splitSafe(String str, String delimiter) {
		String[] stringPieces;
		try {
			Vector<String> v = new Vector<String>();
			int start = 0;
			int end = str.indexOf(delimiter);
			
			while (-1 != end) {
				if (end > start) {
					v.addElement(new String(str.substring(start, end)));
				}
				
				start = end + delimiter.length();
				end = str.indexOf(delimiter, start);
			}
			v.addElement(new String(str.substring(start, str.length())));
			
			stringPieces = new String[v.size()];
			for (int i = 0; i < v.size(); ++i) {
				stringPieces[i] = v.elementAt(i).toString();
			}
		} catch (Exception e) {
			System.err.println(e.toString());
			stringPieces = null;
		}
		
		return stringPieces;
	}
	
	public static String reverse(String in) {
		StringBuilder out = new StringBuilder(in.length());
		int len = in.length();
		for (int i = (len - 1); i >= 0; i--)
			out.append(in.charAt(i));
		return out.toString();
	}
	
	/**
	 * @param d
	 *           e.g. 3.14159, see http://docs.oracle.com/javase/tutorial/i18n/format/decimalFormat.html
	 * @param pattern
	 *           e.g. #.##, e.g. 000.00
	 * @return e.g. 3.14
	 */
	public static String formatNumber(double d, String pattern) {
		return ErrorMsg.getDecimalFormat(pattern).format(d);
	}
	
	public static String formatNumber(long l) {
		Locale locale = Locale.getDefault();
		NumberFormat f = NumberFormat.getNumberInstance(locale);
		f.setMaximumFractionDigits(0);
		return f.format(l);
	}
	
	public static String formatNumber(double l, int fracDigits) {
		Locale locale = Locale.getDefault();
		NumberFormat f = NumberFormat.getNumberInstance(locale);
		f.setMaximumFractionDigits(fracDigits);
		return f.format(l);
	}
	
	public static int count(String s, String find) {
		if (s == null || s.isEmpty())
			return 0;
		else
			return (s.length() - StringManipulationTools.stringReplace(s, find, "").length()) / find.length();
	}
	
	public static String getStringList(Set<Integer> times, String div) {
		return getStringList(times.toArray(new Integer[] {}), div);
	}
	
	public static String getMergedStringItems(String list1, String list2, String split) {
		TreeSet<String> res = new TreeSet<String>();
		if (list1 != null)
			for (String s : list1.split(split))
				res.add(s);
		if (list2 != null)
			for (String s : list2.split(split))
				res.add(s);
		return getStringList(res, " " + split + " ");
	}
	
	public static boolean containsAny(String input, Collection<String> search) {
		for (String s : search)
			if (input.contains(s))
				return true;
		return false;
	}
	
	public static void addIfNotEmpty(ArrayList<String> list, String content) {
		if (list != null && content != null && !content.isEmpty())
			list.add(content);
	}
	
	/**
	 * @return Filtered result.
	 */
	public static String grep(String content, String find) {
		ArrayList<String> res = new ArrayList<String>();
		for (String l : content.split("\r\n")) {
			if (l.contains(find))
				res.add(l);
		}
		return getStringList(res, "\r\n");
	}
	
	public static String txt2html(String txt) {
		txt = stringReplace(txt, "\n", "<br>");
		return "<html><code>" + txt + "</code>";
	}
	
	public static Color getColorFromHTMLdef(String colDef) {
		return new Color(
				Integer.valueOf(colDef.substring(1, 3), 16),
				Integer.valueOf(colDef.substring(3, 5), 16),
				Integer.valueOf(colDef.substring(5, 7), 16));
	}
	
	public static String getColorHTMLdef(Color color) {
		String red = Integer.toHexString(color.getRed());
		String green = Integer.toHexString(color.getGreen());
		String blue = Integer.toHexString(color.getBlue());
		
		return "#" +
				(red.length() == 1 ? "0" + red : red) +
				(green.length() == 1 ? "0" + green : green) +
				(blue.length() == 1 ? "0" + blue : blue);
	}
	
	public static String trimString(String value, int maxLength) {
		if (value == null || maxLength < 3)
			return value;
		if (value.length() >= maxLength - 3)
			return value.substring(0, maxLength - 3) + "...";
		else
			return value;
	}
}
