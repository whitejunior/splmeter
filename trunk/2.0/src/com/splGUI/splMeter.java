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

import java.util.Date;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

/**
 * 
 * @author Hashir N A <hashir@mobware4u.com>
 * 
 */
public class splMeter extends Activity
{
   /** Called when the activity is first created. */
   
   TextView splModeTV;
   TextView splDataTV;
   
   Button splMaxButton;
   Button splOnOffButton;
   
   Button splModeButton;
   Button splCalibButton;
   
   Button splLogButton;
   ImageButton splCalibUpButton;
   ImageButton splCalibDownButton;
   
   Boolean mode = false; // false -> fast , true -> slow
   Boolean calib = false;
   Boolean log = false;
   Boolean max = false;
   
   static final int MY_MSG = 1;
   static final int MAXOVER_MSG = 2;
   static int PREFERENCES_GROUP_ID = 0;
   
   static final int RESET_OPTION = 1;
   static final int ABOUT_OPTION = 2;
   static final int EXIT_OPTION = 3;
   
   protected splEngine engine;
   
   @Override
   public void onCreate(Bundle savedInstanceState)
   {
      
      super.onCreate(savedInstanceState);
      setContentView(R.layout.main);
      
      this.setTitle("SPL Meter");
      
      splModeTV = (TextView) findViewById(R.id.splModeTV);
      splModeTV.setBackgroundColor(Color.GRAY);
      splModeTV.setText("");
      
      splDataTV = (TextView) findViewById(R.id.splTV);
      splDataTV.setBackgroundColor(Color.GRAY);
      splDataTV.setText("");
      
      splOnOffButton = (Button) findViewById(R.id.splOnOffB);
      splOnOffButton.setOnClickListener(start_button_handle);
      
      splMaxButton = (Button) findViewById(R.id.splMaxB);
      splMaxButton.setOnClickListener(max_button_handle);
      
      splModeButton = (Button) findViewById(R.id.splModeB);
      splModeButton.setOnClickListener(mode_button_handle);
      
      splCalibButton = (Button) findViewById(R.id.splCalibB);
      splCalibButton.setOnClickListener(calib_button_handle);
      
      splLogButton = (Button) findViewById(R.id.splLogB);
      splLogButton.setOnClickListener(log_button_handle);
      
      splCalibUpButton = (ImageButton) findViewById(R.id.splCalibUpB);
      splCalibUpButton.setOnClickListener(calibup_button_handle);
      
      splCalibDownButton = (ImageButton) findViewById(R.id.splCalibDownB);
      splCalibDownButton.setOnClickListener(calibdown_button_handle);
      
      // start meter in ON state
      
      splOnOffButton.setText("OFF");
      splOnOffButton.setTextColor(Color.BLACK);
      splModeTV.setText("FAST");
      splModeTV.setBackgroundResource(R.drawable.lcd4);
      splDataTV.setText("");
      splDataTV.setBackgroundResource(R.drawable.lcd2);
      splCalibButton.setVisibility(View.VISIBLE);
      splLogButton.setVisibility(View.VISIBLE);
      splMaxButton.setVisibility(View.VISIBLE);
      splModeButton.setVisibility(View.VISIBLE);
      
      engine = new splEngine(mhandle);
      start_meter();
      
   }
   
   /**
    * Handles start/stop Logging
    * 
    * @param set
    */
   public void handle_log(boolean set)
   {
      if (set)
      {
         engine.startLogging();
      }
      else
      {
         engine.stopLogging();
         Date today = new Date();
         Toast.makeText(
               splMeter.this,
               "Log saved to /sdcard/splmeter_" + today.getDate() + "_"
                     + today.getMonth() + "_" + (today.getYear() + 1900)
                     + ".xls", Toast.LENGTH_SHORT).show();
      }
   }
   
   /**
    * Displays the maximum SPL Value
    */
   public void display_max()
   {
      max = true;
      handle_mode_display();
      engine.showMaxValue();
      
   }
   
   /**
    * Display appropriate mode in the mode Text View
    */
   private void handle_mode_display()
   {
      String modeString = "";
      
      if (mode)
      {
         modeString = "SLOW";
      }
      else
      {
         modeString = "FAST";
         
      }
      
      if (calib)
      {
         modeString += "    CALIB";
      }
      else
      {
         modeString += "              "; // 1 char = 2 spaces ; 1 space = 1
         // space
      }
      
      if (log)
      {
         modeString += "    LOG";
      }
      else
      {
         modeString += "          ";
      }
      
      if (max)
      {
         modeString = "MAX";
      }
      
      splModeTV.setText(modeString);
      
   }
   
   /**
    * Sets the SPL Meter Mode
    */
   public void setMeterMode(String mode)
   {
      double maxValue = engine.getMaxValue();
      stop_meter();
      engine = new splEngine(mhandle);
      engine.setMode(mode);
      engine.setMaxValue(maxValue);
      engine.start_engine();
      
   }
   
   /**
    * Starts the SPL Meter
    */
   public void start_meter()
   {
      calib = false;
      max = false;
      log = false;
      mode = false;
      
      engine = new splEngine(mhandle);
      engine.start_engine();
   }
   
   /**
    * Stops the SPL Meter
    */
   public void stop_meter()
   {
      engine.stop_engine();
   }
   
   /**
    * Calibration Down Button Handler
    */
   private OnClickListener calibdown_button_handle = new OnClickListener()
   {
      
      public void onClick(View v)
      {
         AnimationSet set = new AnimationSet(true);
         
         Animation animation = new TranslateAnimation(
               Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF,
               0.01f, Animation.RELATIVE_TO_SELF, 0.0f,
               Animation.RELATIVE_TO_SELF, 0.09f);
         animation.setDuration(100);
         set.addAnimation(animation);
         
         splCalibDownButton.startAnimation(animation);
         
         engine.calibDown();
         
      }
   };
   
   /**
    * Calibration Up Button Handler
    */
   private OnClickListener calibup_button_handle = new OnClickListener()
   {
      
      public void onClick(View v)
      {
         AnimationSet set = new AnimationSet(true);
         
         Animation animation = new TranslateAnimation(
               Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF,
               0.01f, Animation.RELATIVE_TO_SELF, 0.0f,
               Animation.RELATIVE_TO_SELF, -0.09f);
         animation.setDuration(100);
         set.addAnimation(animation);
         
         splCalibUpButton.startAnimation(animation);
         engine.calibUp();
         
      }
   };
   
   /**
    * Log Button Handler
    */
   private OnClickListener log_button_handle = new OnClickListener()
   {
      
      public void onClick(View v)
      {
         if (log)
         {
            splLogButton.setTextColor(Color.parseColor("#6D7B8D"));
            log = false;
            handle_log(false);
            
         }
         else
         {
            splLogButton.setTextColor(Color.BLACK);
            log = true;
            handle_log(true);
         }
         
         handle_mode_display();
         
      }
   };
   
   /**
    * Calibration Button Handler
    */
   private OnClickListener calib_button_handle = new OnClickListener()
   {
      
      public void onClick(View v)
      {
         if (calib)
         {
            splCalibButton.setTextColor(Color.parseColor("#6D7B8D"));
            splCalibUpButton.setVisibility(View.INVISIBLE);
            splCalibDownButton.setVisibility(View.INVISIBLE);
            splMaxButton.setVisibility(View.VISIBLE);
            splLogButton.setVisibility(View.VISIBLE);
            calib = false;
            engine.storeCalibvalue();
            Toast.makeText(splMeter.this, "Calibration Saved.",
                  Toast.LENGTH_SHORT).show();
         }
         else
         {
            splCalibButton.setTextColor(Color.BLACK);
            splCalibUpButton.setVisibility(View.VISIBLE);
            splCalibDownButton.setVisibility(View.VISIBLE);
            splMaxButton.setVisibility(View.INVISIBLE);
            splLogButton.setVisibility(View.INVISIBLE);
            calib = true;
            
         }
         handle_mode_display();
         
      }
   };
   
   /**
    * Mode Button Handler
    */
   private OnClickListener mode_button_handle = new OnClickListener()
   {
      
      public void onClick(View v)
      {
         if (mode)
         {
            splModeButton.setText("SLOW");
            mode = false;
            setMeterMode("FAST");
         }
         else
         {
            splModeButton.setText("FAST");
            mode = true;
            setMeterMode("SLOW");
         }
         handle_mode_display();
      }
   };
   
   /**
    * ON/OFF Button Handler
    */
   private OnClickListener start_button_handle = new OnClickListener()
   {
      
      public void onClick(View v)
      {
         if (splOnOffButton.getText().equals("ON"))
         {
            splOnOffButton.setText("OFF");
            splOnOffButton.setTextColor(Color.BLACK);
            splModeTV.setText("FAST");
            splModeTV.setBackgroundResource(R.drawable.lcd4);
            splDataTV.setText("");
            splDataTV.setTextColor(Color.WHITE);
            splDataTV.setBackgroundResource(R.drawable.lcd2);
            splCalibButton.setVisibility(View.VISIBLE);
            splLogButton.setVisibility(View.VISIBLE);
            splMaxButton.setVisibility(View.VISIBLE);
            splModeButton.setVisibility(View.VISIBLE);
            splModeButton.setText("SLOW");
            
            start_meter();
            
         }
         else
         {
            stop_meter();
            splOnOffButton.setText("ON");
            splOnOffButton.setTextColor(Color.parseColor("#6D7B8D"));
            splModeTV.setText("");
            splModeTV.setBackgroundColor(Color.GRAY);
            splDataTV.setText("");
            splDataTV.setTextColor(Color.GRAY);
            splDataTV.setBackgroundColor(Color.GRAY);
            splCalibButton.setVisibility(View.INVISIBLE);
            splLogButton.setVisibility(View.INVISIBLE);
            splMaxButton.setVisibility(View.INVISIBLE);
            splModeButton.setVisibility(View.INVISIBLE);
            splCalibUpButton.setVisibility(View.INVISIBLE);
            splCalibDownButton.setVisibility(View.INVISIBLE);
            
         }
      }
   };
   
   /**
    * MAX Button Handler
    */
   private OnClickListener max_button_handle = new OnClickListener()
   {
      
      public void onClick(View v)
      {
         splMaxButton.setTextColor(Color.BLACK);
         display_max();
      }
   };
   
   /**
    * Handler for displaying messages
    */
   public Handler mhandle = new Handler()
   {
      @Override
      public void handleMessage(Message msg)
      {
         switch (msg.what)
         {
         case MY_MSG:
            splDataTV.setText(" " + msg.obj);
            break;
         case MAXOVER_MSG:
            max = false;
            handle_mode_display();
            splMaxButton.setTextColor(Color.parseColor("#6D7B8D"));
            break;
         default:
            super.handleMessage(msg);
            break;
         }
      }
      
   };
   
   /**
    * Create the option Menu's
    * 
    * @see android.app.Activity#onCreateOptionsMenu(android.view.Menu)
    */
   public boolean onCreateOptionsMenu(Menu menu)
   {
      
      super.onCreateOptionsMenu(menu);
      
      menu.add(PREFERENCES_GROUP_ID, RESET_OPTION, 0, "RESET").setIcon(
            android.R.drawable.ic_menu_revert);
      menu.add(PREFERENCES_GROUP_ID, ABOUT_OPTION, 0, "ABOUT").setIcon(
            android.R.drawable.ic_menu_help);
      menu.add(PREFERENCES_GROUP_ID, EXIT_OPTION, 0, "EXIT").setIcon(
            android.R.drawable.ic_menu_close_clear_cancel);
      
      return true;
   }
   
   /**
    * Reset the SPL Meter
    */
   public void reset_meter()
   {
      AlertDialog alertDialog = new AlertDialog.Builder(this).create();
      alertDialog.setTitle("Reset SPL Meter");
      alertDialog.setMessage("Do you want to reset the SPL Meter?");
      alertDialog.setButton("OK", new DialogInterface.OnClickListener()
      {
         public void onClick(DialogInterface dialog, int which)
         {
            engine.stop_engine();
            engine.reset();
            engine = new splEngine(mhandle);
            engine.start_engine();
            return;
         }
      });
      
      alertDialog.setButton2("Cancel", new DialogInterface.OnClickListener()
      {
         public void onClick(DialogInterface dialog, int which)
         {
            return;
         }
      });
      alertDialog.show();
   }
   
   /**
    * Display the ABOUT details
    */
   public void show_about()
   {
      AlertDialog alertDialog = new AlertDialog.Builder(this).create();
      alertDialog.setTitle("SPL Meter");
      alertDialog
            .setMessage("Developed By: \n\tHashir N A\n\thashir@mobware4u.com");
      // alertDialog.setIcon(R.drawable.logo);
      alertDialog.setButton("OK", new DialogInterface.OnClickListener()
      {
         public void onClick(DialogInterface dialog, int which)
         {
            
         }
      });
      
      alertDialog.show();
      
   }
   
   /**
    * Call back function when an menu option is selected.
    */
   public boolean onOptionsItemSelected(MenuItem item)
   {
      switch (item.getItemId())
      {
      case RESET_OPTION:
         reset_meter();
         break;
      case ABOUT_OPTION:
         show_about();
         break;
      case EXIT_OPTION:
         stop_meter();
         super.onDestroy();
         this.finish();
         break;
      
      }
      return true;
   }
}