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
package com.github.dualsub.test;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.util.Date;
import java.util.Properties;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.github.dualsub.srt.DualSrt;
import com.github.dualsub.srt.Merger;
import com.github.dualsub.srt.Srt;
import com.github.dualsub.srt.SrtUtils;
import com.github.dualsub.util.Charset;
import com.github.dualsub.util.Log;


public class TestFriendsSrt {

	private Srt srtEn;
	private Srt srtVn;
	private Merger merger;

	@Before
  public void setup() throws IOException {
    String srtEnFile = "Friends.S05E01_en.srt";
    String srtVnFile = "Friends.S05E01_vn.srt";

    SrtUtils.init("450", "Tahoma", 14, true, false, ".", 50);
    srtEn = new Srt(srtEnFile);
    srtVn = new Srt(srtVnFile);
    Log.info(srtEn.getFileName() + " " + Charset.detect(srtEnFile));
    Log.info(srtVn.getFileName() + " " + Charset.detect(srtVnFile));
    Properties properties = new Properties();
    InputStream inputStream = Thread.currentThread()
                                    .getContextClassLoader()
                                    .getResourceAsStream("dualsub.properties");
    properties.load(inputStream);
    merger = new Merger(".", true, 1000, true, properties, Charset.UTF8, 0, false, false);
  }

	@Test
	public void testReadSrt() throws IOException {
		Log.info("srtEnFile size=" + srtEn.getSubtitles().size());
		Log.info("srtEsFile size=" + srtVn.getSubtitles().size());

		//srtEn.log();
		//srtVn.log();
		Assert.assertEquals(srtEn.getSubtitles().size(), srtVn.getSubtitles().size());
	}

	@Test
	public void testTime() throws ParseException {
		String line1 = "01:27:40,480 --> 01:26:56,260";
		String line2 = "01:27:40 --> 01:27:49,500";

		Date init1 = SrtUtils.getInitTime(line1);
		Date init2 = SrtUtils.getInitTime(line2);

		Date end1 = SrtUtils.getEndTime(line1);
		Date end2 = SrtUtils.getEndTime(line2);

		Log.info(init1 + " " + init1.getTime());
		Log.info(init2 + " " + init2.getTime());
		Log.info("---");
		Log.info(end1 + " " + end1.getTime());
		Log.info(end2 + " " + end2.getTime());

		Assert.assertTrue(init1.getTime() > init2.getTime());
	}

	@Test
	public void testMergedFileName() {
		String mergedFileName = merger.getMergedFileName(srtVn, srtEn);
		Log.info(mergedFileName);
		Assert.assertEquals("." + File.separator + "Friends S05E01.srt", mergedFileName);
	}

	@Test
	public void testCompleteSrtISO88591() throws ParseException, IOException {
	  DualSrt dualSrt = merger.mergeSubs(srtEn, srtVn);
		String mergedFileName = merger.getMergedFileName(srtEn,  srtVn);
		dualSrt.writeSrt(mergedFileName, Charset.ISO88591, false, true);
		Log.info(mergedFileName + " " + Charset.detect(mergedFileName));
		//new File(mergedFileName).delete();
		
	}

	@Test
	public void testCompleteSrtUTF8() throws ParseException, IOException {
		DualSrt dualSrt = merger.mergeSubs(srtEn, srtVn);
		String mergedFileName = merger.getMergedFileName(srtVn, srtEn);
		dualSrt.writeSrt(mergedFileName, Charset.UTF8, false, true);
		Log.info(mergedFileName + " " + Charset.detect(mergedFileName));
		//new File(mergedFileName).delete();
	}

}
