/*
  NTL_Bluetooth_Car - Reference MultiColorLamp's Source code to build
  Copyright (c) 2012 Nathaniel_Chen 

  Ver 3.0_beta

  Not allow commercial use.
  E-Mail: s99115247@stu.edu.tw 

  thanks MIT research help me so that I can do this project ;) 


  [Next to do:add level meter control, menu select function.]

 version history
 3.0_beta--New UI Construe, use RelativeLayout. 
  		 --Add English Language support. 
  		 --Add rotation function, but not use right code, will re Activity when every rotation. 
  		 (need to fix...I spend 2 hours to fix but too noob to fail lol!)
  		 --Improve code, button use ClickListener, clear some unnecessary comment but keep some important.
  		 @2012/08/13
  		 
 2.1_beta--Fix Program Crash when execute Amarino.connect(due to ADT update reason). @ 2012/08/09
 1.5_beta--Improve UI Experience.
 1.0_beta--First re-modify control program.
 =========================================================================

  MultiColorLamp - Example to use with Amarino
  Copyright (c) 2009 Bonifaz Kaufmann. 

  This library is free software; you can redistribute it and/or
  modify it under the terms of the GNU Lesser General Public
  License as published by the Free Software Foundation; either
  version 2.1 of the License, or (at your option) any later version.

  This library is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  Lesser General Public License for more details.

  You should have received a copy of the GNU Lesser General Public
  License along with this library; if not, write to the Free Software
  Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA

 */
package org.guims.robot.RobotControl;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import at.abraxas.amarino.Amarino;

import com.integratingstuff.android.webserver.DataHolder;
import com.integratingstuff.android.webserver.WebServer;

@TargetApi(Build.VERSION_CODES.FROYO)
public class RobotBluetoothControl extends Activity implements
		OnSeekBarChangeListener {

	private static final String TAG = "RobotBluetoothControl";
	
	private String DEVICE_ADDRESS = "00:11:05:09:00:84";

	final int DELAY = 150;
	int power_Val;
	long lastChange;
	SeekBar power_SB;
	TextView powerval_Text;
	EditText setmac_ET;
    private Camera mCamera;
    private CameraPreview mPreview;
	DataHolder _dataHolder = new DataHolder();
	RobotBluetoothControl _self;
	
	Handler _handler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message inputMessage) {
            String msg = (String) inputMessage.obj;
            if(msg.equals("IMAGE"))
        		mCamera.takePicture(null, null, mPicture);
            else {
        		Amarino.sendDataToArduino(_self, DEVICE_ADDRESS, msg.charAt(0), power_Val);
                TextView tvMsg = (TextView) findViewById(R.id.tvMsg);
                tvMsg.setText(msg);
            }
            
            super.handleMessage(inputMessage);
        }
    };
    
    private PictureCallback mPicture = new PictureCallback() {

        @Override
        public void onPictureTaken(byte[] data, Camera camera) {

            File pictureFile = getOutputMediaFile();
            if (pictureFile == null){
                Log.d(TAG, "Error creating media file, check storage permissions: ");
                return;
            }

            try {
                FileOutputStream fos = new FileOutputStream(pictureFile);
                fos.write(data);
                fos.close();
            } catch (FileNotFoundException e) {
                Log.d(TAG, "File not found: " + e.getMessage());
            } catch (IOException e) {
                Log.d(TAG, "Error accessing file: " + e.getMessage());
            }
            
			synchronized (_dataHolder) {
	            _dataHolder.data = data;
				_dataHolder.notify();
			}
        }
    };

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		_self = this;
		setContentView(R.layout.main);

		Amarino.connect(this, DEVICE_ADDRESS);

		power_SB = (SeekBar) findViewById(R.id.power_seekBar);
		power_SB.setOnSeekBarChangeListener(this);

		powerval_Text = (TextView) findViewById(R.id.powerval_LargeText);
		setmac_ET = (EditText) findViewById(R.id.setmac_EditText);

		Button btnSetmac = (Button) findViewById(R.id.setmac_Button);
		btnSetmac.setTag(0);
		btnSetmac.setOnClickListener(btnOnClick);

		Button btnForward = (Button) findViewById(R.id.forward_Button);
		btnForward.setTag(1);
		btnForward.setOnClickListener(btnOnClick);

		Button btnBack = (Button) findViewById(R.id.back_Button);
		btnBack.setTag(2);
		btnBack.setOnClickListener(btnOnClick);

		Button btnLeft = (Button) findViewById(R.id.left_Button);
		btnLeft.setTag(3);
		btnLeft.setOnClickListener(btnOnClick);

		Button btnRight = (Button) findViewById(R.id.right_Button);
		btnRight.setTag(4);
		btnRight.setOnClickListener(btnOnClick);

		Button btnStop = (Button) findViewById(R.id.stop_Button);
		btnStop.setTag(5);
		btnStop.setOnClickListener(btnOnClick);
		
		final Context context = this;
		final String htmlFile = readHtmlFile();
		
		Thread thread = new Thread()
		{
		    @Override
		    public void run() {
		        try {
					WebServer ws = new WebServer(context, _handler, htmlFile, _dataHolder);
					ws.startServer();
		        } catch (Exception e) {
		            e.printStackTrace();
		        }
		    }
		};
		thread.start();
		
		//WebServerService wss = new WebServerService( _handler );
		
        // Create an instance of Camera
        mCamera = Camera.open(); 

        // Create our Preview view and set it as the content of our activity.
        mPreview = new CameraPreview(this, mCamera);
        FrameLayout fl = (FrameLayout) findViewById(R.id.flCamera);
        fl.addView(mPreview);
	}
	
	

	private Button.OnClickListener btnOnClick = new Button.OnClickListener() {

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub

			switch ((Integer) v.getTag()) {
			case 0:
				SetMacOnClick();
				break;
			case 1:
				update_Forward();
				break;
			case 2:
				update_Back();
				break;
			case 3:
				update_Left();
				break;
			case 4:
				update_Right();
				break;
			case 5:
				update_Stop();
				break;
			}

		}
	};

	public void SetMacOnClick() { 
		Amarino.disconnect(this, DEVICE_ADDRESS);
		DEVICE_ADDRESS = setmac_ET.toString();
		setmac_ET.setHint(DEVICE_ADDRESS);
		Amarino.connect(this, DEVICE_ADDRESS);
	}

	

	@Override
	protected void onStart() {
		super.onStart();

		// load last state
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(this);
		power_Val = prefs.getInt("power_Val", 0);

		// set seekbars and feedback color according to last state
		/*
		 * redSB.setProgress(red); greenSB.setProgress(green);
		 * blueSB.setProgress(blue);
		 */

		power_SB.setProgress(power_Val);

		// colorIndicator.setBackgroundColor(Color.rgb(red, green, blue));

		new Thread() {
			@Override
			public void run() {
				try {
					Thread.sleep(6000);
				} catch (InterruptedException e) {
				}
				Log.d(TAG, "update data");
				updateAlldata();
			}
		}.start();

	}

	@Override
	protected void onStop() {
		super.onStop();
		// save state
		PreferenceManager.getDefaultSharedPreferences(this).edit()
		/*
		 * .putInt("red", red) .putInt("green", green) .putInt("blue", blue)
		 */
		.putInt("power_Val", power_Val).commit();

		// stop Amarino's background service, we don't need it any more
		Amarino.disconnect(this, DEVICE_ADDRESS);
	}

	@Override
	public void onProgressChanged(SeekBar seekBar, int progress,
			boolean fromUser) {
		// do not send to many updates, Arduino can't handle so much
		if (System.currentTimeMillis() - lastChange > DELAY) {
			updateState(seekBar);
			lastChange = System.currentTimeMillis();
		}
	}

	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {
		lastChange = System.currentTimeMillis();
	}

	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {
		updateState(seekBar);
	}

	private void updateState(final SeekBar seekBar) {

		switch (seekBar.getId()) {
		/*
		 * case R.id.SeekBarRed: red = seekBar.getProgress(); updateRed();
		 * break;
		 */
		case R.id.power_seekBar:
			power_Val = seekBar.getProgress();

			powerval_Text.setText(Integer.toString(power_Val)); // 將power_Val轉型塞到textbox裡面

			update_PowerVal();

			break;
		/*
		 * case R.id.SeekBarBlue: blue = seekBar.getProgress(); updateBlue();
		 * break;
		 */
		}

		update_PowerVal();
		// provide user feedback
		// colorIndicator.setBackgroundColor(Color.rgb(red, green, blue));
	}

	private void updateAlldata() { // 這行用來送出指令到arduino
		// send state to Arduino
		// updateRed();
		update_PowerVal();

		// updateBlue();
	}

	private void update_PowerVal() {
		Amarino.sendDataToArduino(this, DEVICE_ADDRESS, 'P', power_Val);
	}

	private void update_Forward() {
		Amarino.sendDataToArduino(this, DEVICE_ADDRESS, 'F', power_Val);
	}

	private void update_Back() {
		Amarino.sendDataToArduino(this, DEVICE_ADDRESS, 'B', power_Val);
	}

	private void update_Left() {
		Amarino.sendDataToArduino(this, DEVICE_ADDRESS, 'L', power_Val);
	}

	private void update_Right() {
		Amarino.sendDataToArduino(this, DEVICE_ADDRESS, 'R', power_Val);
	}

	private void update_Stop() {
		//mCamera.takePicture(null, null, mPicture);
		Amarino.sendDataToArduino(this, DEVICE_ADDRESS, 'S', 0);
	}
	
	String readHtmlFile() {
		String result = "";
		// The InputStream opens the resourceId and sends it to the buffer
		InputStream is = this.getResources().openRawResource(R.raw.home);
		BufferedReader br = new BufferedReader(new InputStreamReader(is));
		String readLine = null;

		try {
			// While the BufferedReader readLine is not null
			while ((readLine = br.readLine()) != null) {
				result += readLine + "\n";
			}

			// Close the InputStream and BufferedReader
			is.close();
			br.close();

		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return result;
	}
	
	
	/** Create a File for saving an image or video */
	private static File getOutputMediaFile(){
	    // To be safe, you should check that the SDCard is mounted
	    // using Environment.getExternalStorageState() before doing this.

	    File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory( Environment.DIRECTORY_PICTURES), "MyCameraApp");
	    // This location works best if you want the created images to be shared
	    // between applications and persist after your app has been uninstalled.

	    // Create the storage directory if it does not exist
	    if (! mediaStorageDir.exists()){
	        if (! mediaStorageDir.mkdirs()){
	            Log.d("MyCameraApp", "failed to create directory");
	            return null;
	        }
	    }

	    // Create a media file name
	    String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
	    File mediaFile;

	    mediaFile = new File(mediaStorageDir.getPath() + File.separator + "IMG_"+ timeStamp + ".jpg");

	    return mediaFile;
	}

}