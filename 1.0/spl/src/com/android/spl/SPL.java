/*********************************************
 * ANDROID SOUND PRESSURE METER APPLICATION
 * DESC   : Main Activity File.
 * AUTHOR : hashir.mail@gmail.com
 * DATE   : 19 JUNE 2009
 *********************************************/

package com.android.spl;

import java.io.File;

import android.app.Activity;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class SPL extends Activity {
    /** Called when the activity is first created. */
	String filename;
	private Button start_button;
	private Button stop_button;
	Recorder recorderInstance;
	Thread th;
	private static final  int MY_MSG = 1;
	TextView tv;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        start_button = (Button) findViewById(R.id.Button01);      // start button
        start_button.setOnClickListener(start_button_handle);
        stop_button = (Button) findViewById(R.id.Button02);       // stop button
        stop_button.setOnClickListener(stop_button_handle);
        tv = (TextView) findViewById(R.id.TextView01);            // text area for displaying result
        tv.setTextSize(1, 30);
        tv.setText(" db SPL");
        

        start_button.setEnabled(false);
        stop_button.setEnabled(true);
        
        start_scan();  // start the measuring automatically when application starts
       
    }
    
    private void start_scan(){
    	
    	recorderInstance = new Recorder(mhandle);
    	recorderInstance.setRecording(true);
        th = new Thread(recorderInstance);
        // writing to /sdcard/test.raw. Rewritten each time 
        recorderInstance.setFileName(new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/test.raw"));
        th.start();
        start_button.setEnabled(false);  // start automatically on start
        stop_button.setEnabled(true);
        
    }
    
    /* Start Button Handler */
    private OnClickListener start_button_handle = new OnClickListener()
    {

		@Override
		public void onClick(View arg0) {
			start_scan();

	       
		}
    };
    
    /* Stop Button Handler */
    private OnClickListener stop_button_handle = new OnClickListener()
    {

		@Override
		public void onClick(View v) {
			
			
			recorderInstance.setRecording(false);		
	        //th.join();			
	        start_button.setEnabled(true);
	        stop_button.setEnabled(false); 
	        
			
		}
    };
    
    /* Handler for displaying messages from recording thread */     
    public Handler mhandle = new Handler()
     {
  	   @Override
      	   public void handleMessage(Message msg) {
  	   		switch(msg.what)
      			{
	       		case MY_MSG:
	      			tv.setText(""+msg.obj);
	       			break;
	       		default:
	       			super.handleMessage(msg);
	       		    break;
      			}
  	   		}
  	   
     };

    
}