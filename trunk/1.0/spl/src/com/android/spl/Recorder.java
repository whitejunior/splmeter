/*********************************************
 * ANDROID SOUND PRESSURE METER APPLICATION
 * DESC   : Recording Thread that calculates SPL.  
 * WEBSRC : Recording : http://www.anddev.org/viewtopic.php?p=22820
 * AUTHOR : hashir.mail@gmail.com
 * DATE   : 19 JUNE 2009
 * CHANGES: - Changed the recording logic
 * 			- Added logic to pass recorded buffer to FFT
 * 			- Added logic to calculate SPL.
 *********************************************/

package com.android.spl;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Handler;
import android.os.Message;

public class Recorder implements Runnable {
    private int frequency;
    private int channelConfiguration;
    private File fileName;
    private volatile boolean isRecording = false;;

    short[] tempBuffer;
    Handler handle;
	private static final  int MY_MSG = 1;
	

    // Changing the sample resolution changes sample type. byte vs. short.
    private static final int audioEncoding = AudioFormat.ENCODING_PCM_16BIT;

    /**
     * Handler is passed to pass messages to main screen
     * Recording is done 8000Hz MONO 16 bit
     */
    public Recorder(Handler h) {
         super();
         this.setFrequency(8000);
         this.setChannelConfiguration(AudioFormat.CHANNEL_CONFIGURATION_MONO);
         this.handle = h;
    }

    /* Recording THREAD */
    public void run() {
         // Wait until we're recording...
    	 AudioRecord recordInstance = null;
    	 BufferedOutputStream bufferedStreamInstance = null;
    	   
    	     
	         // We're important...
	         android.os.Process
	                   .setThreadPriority(android.os.Process.THREAD_PRIORITY_URGENT_AUDIO);
	
	         // Allocate Recorder and Start Recording...
	         int bufferRead = 0;
	         //int bufferSize = AudioRecord.getMinBufferSize(this.getFrequency(),
	         //          this.getChannelConfiguration(), this.getAudioEncoding());
	         int bufferSize = 4096;//2048;
	         
	         recordInstance = new AudioRecord(
	                   MediaRecorder.AudioSource.MIC, this.getFrequency(), this
	                             .getChannelConfiguration(), this.getAudioEncoding(),
	                   bufferSize);
	         
	         tempBuffer = new short[bufferSize];
	         recordInstance.startRecording();
	   
	      // Continue till STOP button is pressed.   
	      while (this.isRecording) {
	    	 
	    	 // Re Create file each time to save space. 
	    	 if (fileName.exists()) {
	              fileName.delete();
	         }
	         try {
	              fileName.createNewFile();
	         } catch (IOException e) {
	              throw new IllegalStateException("Cannot create file: " + fileName.toString());
	         }
	         
               
	         // Open output stream...
	         if (this.fileName == null) {
	              throw new IllegalStateException("fileName is null");
	         }
	     
	        
	         try {
	              bufferedStreamInstance = new BufferedOutputStream(
	                        new FileOutputStream(this.fileName));
	         } catch (FileNotFoundException e) {
	              throw new IllegalStateException("Cannot Open File", e);
	         }
	         DataOutputStream dataOutputStreamInstance =
	              new DataOutputStream(bufferedStreamInstance);
	         
	         
	         bufferRead = recordInstance.read(tempBuffer, 0, bufferSize);
	         
              if (bufferRead == AudioRecord.ERROR_INVALID_OPERATION) {
                   throw new IllegalStateException(
                             "read() returned AudioRecord.ERROR_INVALID_OPERATION");
              } else if (bufferRead == AudioRecord.ERROR_BAD_VALUE) {
                   throw new IllegalStateException(
                             "read() returned AudioRecord.ERROR_BAD_VALUE");
              } else if (bufferRead == AudioRecord.ERROR_INVALID_OPERATION) {
                   throw new IllegalStateException(
                             "read() returned AudioRecord.ERROR_INVALID_OPERATION");
              }
              try {
                   for (int idxBuffer = 0; idxBuffer < bufferRead; ++idxBuffer) {
                        dataOutputStreamInstance.writeShort(tempBuffer[idxBuffer]);
                   }
                  measure(bufferSize);  // calucalte SPL
              } catch (IOException e) {
                   throw new IllegalStateException(
                        "dataOutputStreamInstance.writeShort(curVal)");
              }
              
          }
       
         // STOP BUTTON WAS PRESSED.
         // Close resources...
         recordInstance.stop();
         try {
              bufferedStreamInstance.close();
         } catch (IOException e) {
              throw new IllegalStateException("Cannot close buffered writer.");
         }
    }
    
    /**
     * Calculate SPL
     *  P = square root ( 2*Z*I ) - > Pressure
     *  Z = Acoustic Impedance = 406.2 for air at 30 degree celsius
     *  I = Intensity = 2*Z*pi square*frequency square*Amplitude square
     *  @param bsize - the size of FFT required.
     */
    public void measure(int bsize){
    	int i = 0;
    	double frequency = 0;
    	double amplitude = 0;
    	double max = 0.0;
    	int max_index = 0;
    	double w = 0.0;
    	
    	 
    	double Z   = 406.2;
    	double I   = 0.0;
    	double P   = 0.0;
    	double P0  = 2*0.00001;  //is constant
    	double Istar = 0.0;  // SPL
    	
    	Complex[] x = new Complex[bsize];
    	
    	for( i = 0; i < bsize; i++ ){
    		x[i] = new Complex(tempBuffer[i],0);
    	}
    	
    	Complex[] xf = new Complex[bsize];
    	xf = FFT.fft(x);
    	
    	for( i = 0; i < bsize/2; i++ ){
    		w = xf[i].abs();
    		if ( w > max ){
    			max_index = i;
    			max = w;
    		}
    	}
    	// Frequency and Amp of fundamental frequency
    	frequency = max_index;
    	amplitude = xf[max_index].abs()*2/bsize;
    	    	
    	I  = Z*2*2*Math.PI*Math.PI*frequency*frequency*amplitude*amplitude;
    	P  = Math.sqrt(Z*I);
        if ( P != 0 ) 
        	Istar = round(20*Math.log10(P/P0)/10,3);  // divide by 10 to correct the calculation
        
    	
    	Message msg = handle.obtainMessage(MY_MSG,"\n\n"+Istar+ " db SPL");
		handle.sendMessage(msg);
    }
    
    /**
     * Utility Function to round a double value
     * @param d  - The decimal value
     * @param decimalPlace - how many places required
     * @return double - the rounded value
     */
    public double round(double d, int decimalPlace){
        // see the Javadoc about why we use a String in the constructor
        // http://java.sun.com/j2se/1.5.0/docs/api/java/math/BigDecimal.html#BigDecimal(double)
        BigDecimal bd = new BigDecimal(Double.toString(d));
        bd = bd.setScale(decimalPlace,BigDecimal.ROUND_HALF_UP);
        return bd.doubleValue();
      }

    public void setFileName(File fileName) {
         this.fileName = fileName;
    }

    public File getFileName() {
         return fileName;
    }

    /**
     * @param isRecording
     *            the isRecording to set
     */
    public void setRecording(boolean isRecording) {
        
              this.isRecording = isRecording;
     
    }

    /**
     * @return the isRecording
     */
    public boolean isRecording() {
        
              return isRecording;
         
    }

    /**
     * @param frequency
     *            the frequency to set
     */
    public void setFrequency(int frequency) {
         this.frequency = frequency;
    }

    /**
     * @return the frequency
     */
    public int getFrequency() {
         return frequency;
    }

    /**
     * @param channelConfiguration
     *            the channelConfiguration to set
     */
    public void setChannelConfiguration(int channelConfiguration) {
         this.channelConfiguration = channelConfiguration;
    }

    /**
     * @return the channelConfiguration
     */
    public int getChannelConfiguration() {
         return channelConfiguration;
    }

    /**
     * @return the audioEncoding
     */
    public int getAudioEncoding() {
         return audioEncoding;
    }


} 
