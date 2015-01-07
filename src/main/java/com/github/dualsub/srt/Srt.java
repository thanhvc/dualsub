/*
 * (C) Copyright 2014 Boni Garcia (http://bonigarcia.github.io/)
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.github.dualsub.srt;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.github.dualsub.translate.Translator;
import com.github.dualsub.util.Charset;
import com.github.dualsub.util.Log;

/**
 * Srt.
 * 
 * @author Boni Garcia (boni.gg@gmail.com)
 * @since 1.0.0
 */
public class Srt {

	private Map<String, Entry> subtitles;
	private String fileName;
	private String charset;

	public Srt(String fileName) throws IOException {
		this.fileName = fileName;
		this.subtitles = new TreeMap<String, Entry>();
		this.readSrt(fileName);
	}

  public Srt(Srt inputSrt, String fromLang, String toLang, String charset) throws IOException {
    this.subtitles = new TreeMap<String, Entry>();
    Map<String, Entry> subtitlesToTranslate = inputSrt.getSubtitles();
    String lineToTranslate;
    String translatedLine;
    Entry translatedEntry;
    for (String time : subtitlesToTranslate.keySet()) {
      lineToTranslate = "";
      for (String line : subtitlesToTranslate.get(time).getSubtitleLines()) {
        lineToTranslate += line + " ";
      }
      translatedEntry = new Entry();
      translatedLine = Translator.getInstance().translate(lineToTranslate,
                                                          fromLang,
                                                          toLang,
                                                          charset);
      Log.debug("** Translate " + lineToTranslate + " ** FROM " + fromLang + " TO " + toLang + " ** " + translatedLine);
      translatedEntry.add(translatedLine);
      subtitles.put(time, translatedEntry);
    }
  }

	/**
	 * Converts a subtitle file (SRT) into a Map, in which key in the timing of
	 * each subtitle entry, and the value is a List of String with the content
	 * of the entry.
	 * 
	 * @param file
	 * @throws IOException
	 */
	private void readSrt(String file) throws IOException {
		Entry entry = new Entry();
		int i = 0;
		String time = "";
		InputStream isForDetection = readSrtInputStream(file);
		InputStream isForReading = readSrtInputStream(file);
		charset = Charset.detect(isForDetection);
		Log.info(file + " detected charset " + charset);

		BufferedReader br = new BufferedReader(new InputStreamReader(isForReading, charset));
		try {
			String line = br.readLine();
			while (line != null) {
				if (line.isEmpty()) {
					if (entry.size() > 0) {
						subtitles.put(time, entry);
						entry = new Entry();
						i = 0;
						time = "";
					}
				} else {
					if (i == 1) {
						time = line;
					}
					if (i >= 2) {
						// Not adding first two lines of subtitles (index and
						// time)
						if (line.indexOf(SrtUtils.TAG_INIT) != -1
								&& line.indexOf(SrtUtils.TAG_END) != -1) {
							line = removeTags(line);
						}
						entry.add(line);
						Log.debug(line);
					}
					i++;
				}
				line = br.readLine();
			}
		} finally {
			br.close();
			isForReading.close();
		}
	}

	public InputStream readSrtInputStream(String file)
			throws FileNotFoundException {
		InputStream inputStream = Thread.currentThread()
				.getContextClassLoader().getResourceAsStream(file);
		if (inputStream == null) {
			inputStream = new BufferedInputStream(new FileInputStream(file));
		}
		return inputStream;
	}

	public static String removeTags(String line) {
		Pattern pattern = Pattern.compile("<([^>]+)>");
		Matcher matcher = pattern.matcher(line);
		return matcher.replaceAll("");
	}

	public void log() {
		Entry list;
		for (String time : subtitles.keySet()) {
			list = subtitles.get(time);
			for (int i = 0; i < list.size(); i++) {
				Log.info(time + " " + list.get(i) + " ");
			}
			Log.info("");
		}
	}

	public Map<String, Entry> getSubtitles() {
		return subtitles;
	}

	public void resetSubtitles() {
		subtitles.clear();
	}

	public String getFileName() {
		return fileName;
	}

	public String getCharset() {
		return charset;
	}

}
