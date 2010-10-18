/**
 * SPL Meter is a Software Based Sound Pressure Level Meter that runs in android OS.
 *  Copyright (C) 2009  Hashir N A <hashir@mobware4u.com>

 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU General Public License
 *  as published by the Free Software Foundation; either version 2
 *  of the License, or (at your option) any later version.

 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
   
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.

 */

package com.splGUI;

import java.io.FileWriter;
import java.math.BigDecimal;
import java.util.Date;

import android.content.Context;
import android.content.SharedPreferences;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;

/**
 * 
 * @author Hashir N A <hashir@mobware4u.com>
 * 
 */
public class SplEngine extends Thread {
	private static final int FREQUENCY = 16000;
	private static final int CHANNEL = AudioFormat.CHANNEL_CONFIGURATION_MONO;
	private static final int ENCODING = AudioFormat.ENCODING_PCM_16BIT;
	private static final int MY_MSG = 1;
	protected static final int MAXOVER_MSG = 2;
	private int BUFFSIZE = 4096;
	private static final double P0 = 0.000002;

	private static final int CALIB_INCREMENT = 3;
	private static final int CALIB_DEFAULT = -80;
	private int mCaliberationValue = CALIB_DEFAULT;

	public volatile boolean mIsRunning = false;
	private Handler mHandle = null;

	private double mMaxValue = 0.0;
	public volatile boolean mShowMaxValue = false;

	private FileWriter splLog = null;
	private volatile boolean isLogging = false;
	private static String LOGPATH = "/sdcard/splmeter_";
	private int LOGLIMIT = 50;
	private int logCount = 0;

	private volatile String mode = "FAST";

	AudioRecord mRecordInstance = null;
	Context mContext = null;
	String PREFS_NAME = "SPLMETER";

	public SplEngine(Handler handle, Context context) {
		this.mHandle = handle;
		this.mContext = context;
		this.mCaliberationValue = readCalibValue();
		this.mode = "FAST";
		this.isLogging = false;
		this.mIsRunning = false;
		this.mMaxValue = 0.0;
		this.mShowMaxValue = false;
		//
		// android.os.Process
		// .setThreadPriority(android.os.Process.THREAD_PRIORITY_URGENT_AUDIO);
		mRecordInstance = new AudioRecord(MediaRecorder.AudioSource.MIC,
				FREQUENCY, CHANNEL, ENCODING, FREQUENCY);
	}

	/**
	 * Reset the Engine by deleting all calibration details and resetting it to
	 * a default value
	 */
	public void reset() {
		SharedPreferences settings = PreferenceManager
				.getDefaultSharedPreferences(mContext);
		SharedPreferences.Editor editor = settings.edit();
		editor.putInt("CalibSlow", 0);

		editor.putInt("CalibFast", 0);
		editor.commit();
		mCaliberationValue = CALIB_DEFAULT;
	}

	/**
	 * starts the engine.
	 */
	public void start_engine() {
		this.mIsRunning = true;
		this.start();
	}

	/**
	 * stops the engine
	 */
	public void stop_engine() {
		this.mIsRunning = false;
		mRecordInstance.stop();
	}

	/**
	 * sets mode as fast or slow
	 * 
	 * @param mode
	 */
	public void setMode(String mode) {
		this.mode = mode;
		setCalibValue(readCalibValue());

		if ("SLOW".equals(mode)) {
			BUFFSIZE = 32768;
			LOGLIMIT = 10;
		} else {
			BUFFSIZE = 4096;
			LOGLIMIT = 50;
		}
	}

	/**
	 * Returns the current calibration value
	 * 
	 * @return
	 */
	public int getCalibValue() {
		return mCaliberationValue;
	}

	/**
	 * Sets the calibration value to a value passed.
	 * 
	 * @param value
	 */
	public void setCalibValue(int value) {
		mCaliberationValue = value;
	}

	/**
	 * Reads the calibration details from the settings file. separate files used
	 * for slow and fast mode if files doesn't exits, sets calibration to a
	 * default value
	 * 
	 * @return
	 */
	public int readCalibValue() {
		
		SharedPreferences settings = mContext
										.getSharedPreferences(
												PREFS_NAME, 
												Context.MODE_WORLD_READABLE);
		int calibValue = 0;

		if ("SLOW".equals(mode)) {
			calibValue = settings.getInt("calibSlow", CALIB_DEFAULT);
		} else {
			calibValue = settings.getInt("calibFast", CALIB_DEFAULT);
		}
		return calibValue;
	}

	/**
	 * Stores the current calibration value to a file separate calibration for
	 * SLOW and FAST modes
	 */
	public void storeCalibvalue() {
		SharedPreferences settings = mContext
										.getSharedPreferences(
												PREFS_NAME, 
												Context.MODE_WORLD_WRITEABLE);
		SharedPreferences.Editor editor = settings.edit();

		if ("SLOW".equals(mode)) {
			editor.putInt("CalibSlow", 0);
		} else {
			editor.putInt("CalibFast", 0);
		}

		editor.commit();
	}

	/**
	 * Increase the calibration by an fixed increment
	 */
	public void calibUp() {
		mCaliberationValue = mCaliberationValue + CALIB_INCREMENT;
		if (mCaliberationValue == 0) {
			mCaliberationValue = mCaliberationValue + 1;
		}
	}

	/**
	 * Descrease the calibration by a fixed value
	 */
	public void calibDown() {
		mCaliberationValue = mCaliberationValue - CALIB_INCREMENT;
		if (mCaliberationValue == 0) {
			mCaliberationValue = mCaliberationValue - 1;
		}
	}

	/* Display max value for 2 seconds and then resume */
	public double showMaxValue() {
		mShowMaxValue = true;
		return mMaxValue;
	}

	/**
	 * Get the maximum value recorded so far
	 * 
	 * @return
	 */
	public double getMaxValue() {
		return mMaxValue;
	}

	/*
	 * Sets the max value of SPL
	 */
	public void setMaxValue(double max) {
		mMaxValue = max;
	}

	/*
	 * Start logging the values to a log file
	 */
	public void startLogging() {
		isLogging = true;
	}

	/*
	 * Stop the logging
	 */
	public void stopLogging() {
		isLogging = false;
	}

	/*
	 * If logging, then store the spl values to a log file. separate log file
	 * for each day.
	 */
	private void writeLog(double value) {
		if (isLogging) {
			if (logCount++ > LOGLIMIT) {
				try {
					Date now = new Date();

					splLog = new FileWriter(LOGPATH + now.getDate() + "_"
							+ now.getMonth() + "_" + (now.getYear() + 1900)
							+ ".xls", true);
					splLog.append(value + "\n");
					splLog.close();

				} catch (Exception e) {

				}
				logCount = 0;
			}
		}
	}

	/*
	 * The main thread. Records audio and calculates the SPL The heart of the
	 * Engine.
	 */
	public void run() {
		try {
			mRecordInstance.startRecording();

			double splValue = 0.0;
			double rmsValue = 0.0;
			int SIZE = BUFFSIZE;
			short[] tempBuffer = new short[SIZE];

			while (this.mIsRunning) {
				
				mRecordInstance.read(tempBuffer, 0, SIZE);

				for (int i = 0; i < SIZE - 1; i++) {
					rmsValue += tempBuffer[i] * tempBuffer[i];
				}
				rmsValue = rmsValue / SIZE;
				rmsValue = Math.sqrt(rmsValue);

				splValue = 20 * Math.log10(rmsValue / P0);
				splValue = splValue + mCaliberationValue;
				splValue = round(splValue, 2);

				if (mMaxValue < splValue) {
					mMaxValue = splValue;
				}

				if (!mShowMaxValue) {
					Message msg = mHandle.obtainMessage(MY_MSG, splValue);
					mHandle.sendMessage(msg);
				} else {
					Message msg = mHandle.obtainMessage(MY_MSG, mMaxValue);
					mHandle.sendMessage(msg);
					Thread.sleep(2000);
					msg = mHandle.obtainMessage(MAXOVER_MSG, mMaxValue);
					mHandle.sendMessage(msg);
					mShowMaxValue = false;
				}

				writeLog(splValue);
			}

			mRecordInstance.stop();
		} catch (Exception e) {
			mRecordInstance.stop();
			mRecordInstance.release();
			mRecordInstance = null;
		}
	}

	/*
	 * Utility function for rounding decimal values
	 */
	public double round(double d, int decimalPlace) {
		// see the Javadoc about why we use a String in the constructor
		// http://java.sun.com/j2se/1.5.0/docs/api/java/math/BigDecimal.html#BigDecimal(double)
		BigDecimal bd = new BigDecimal(Double.toString(d));
		bd = bd.setScale(decimalPlace, BigDecimal.ROUND_HALF_UP);
		return bd.doubleValue();
	}

}
