package de.awp.scanner;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import de.awp.scanner.util.SystemUiHider;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentFilter.MalformedMimeTypeException;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.NfcF;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 *
 * @see SystemUiHider
 */
public class Scan extends Activity {
   
	private NfcAdapter mAdapter;
    private PendingIntent mPendingIntent;
    Intent intent;
    private IntentFilter[] intentFiltersArray;
    private String[][] techListsArray;
    private TextView mText;
    private int mCount = 0;
    String rfidid;
	
	/**
     * Whether or not the system UI should be auto-hidden after
     * {@link #AUTO_HIDE_DELAY_MILLIS} milliseconds.
     */
    private static final boolean AUTO_HIDE = true;

    /**
     * If {@link #AUTO_HIDE} is set, the number of milliseconds to wait after
     * user interaction before hiding the system UI.
     */
    private static final int AUTO_HIDE_DELAY_MILLIS = 3000;

    /**
     * If set, will toggle the system UI visibility upon interaction. Otherwise,
     * will show the system UI visibility upon interaction.
     */
    private static final boolean TOGGLE_ON_CLICK = true;

    /**
     * The flags to pass to {@link SystemUiHider#getInstance}.
     */
    private static final int HIDER_FLAGS = SystemUiHider.FLAG_HIDE_NAVIGATION;

    /**
     * The instance of the {@link SystemUiHider} for this activity.
     */
    private SystemUiHider mSystemUiHider;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_scan);
        
        Button button = (Button)findViewById(R.id.dummy_button);
        
        button.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				System.out.println("test");
				
				/*HttpClient httpClient = new DefaultHttpClient();
	            HttpPost post = new HttpPost("http://192.168.1.155:8080/planspielBPMN/rest/create");
	            post.setHeader("content-type", "application/json");

	            JSONObject dato = new JSONObject();



	            try {
	            	dato.put("id", "scanner1");
	            	dato.put("rfidid", "test");
	            	
	            	StringEntity entity = new StringEntity(dato.toString());
	                post.setEntity(entity);

	                HttpResponse resp = httpClient.execute(post);
	                String respStr = EntityUtils.toString(resp.getEntity());
	                
	                System.out.println(respStr);
	            }
	            catch (Exception ex) {
	            	ex.printStackTrace();
	            }*/
				Thread thread = new Thread(new Runnable(){
				    @Override
				    public void run() {
				    	try {
							URL url = new URL("http://192.168.1.155:8080/planspielBPMN/rest/create");
							HttpURLConnection connection = (HttpURLConnection) url.openConnection();
							connection.setDoOutput(true);
							connection.setRequestMethod("POST");
							connection.setRequestProperty("Content-Type", "application/json");
							
							String input = String.format("{\"id\":\"%s\",\"rfidid\":\"%s\"}", "scanner0", "8F49488");
							
							OutputStream output = connection.getOutputStream();
							output.write(input.getBytes());
							output.flush();
							
							System.out.println(connection.getResponseCode());
							System.out.println("ended");
							connection.disconnect();
						} catch(MalformedURLException mE) {
							mE.printStackTrace();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
				    }
				});

				thread.start(); 	
					
			};
				
			
		});

        final View controlsView = findViewById(R.id.fullscreen_content_controls);
        final View contentView = findViewById(R.id.fullscreen_content);

        // Set up an instance of SystemUiHider to control the system UI for
        // this activity.
        mSystemUiHider = SystemUiHider.getInstance(this, contentView, HIDER_FLAGS);
        mSystemUiHider.setup();
        mSystemUiHider
                .setOnVisibilityChangeListener(new SystemUiHider.OnVisibilityChangeListener() {
                    // Cached values.
                    int mControlsHeight;
                    int mShortAnimTime;

                    @Override
                    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
                    public void onVisibilityChange(boolean visible) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
                            // If the ViewPropertyAnimator API is available
                            // (Honeycomb MR2 and later), use it to animate the
                            // in-layout UI controls at the bottom of the
                            // screen.
                            if (mControlsHeight == 0) {
                                mControlsHeight = controlsView.getHeight();
                            }
                            if (mShortAnimTime == 0) {
                                mShortAnimTime = getResources().getInteger(
                                        android.R.integer.config_shortAnimTime);
                            }
                            controlsView.animate()
                                    .translationY(visible ? 0 : mControlsHeight)
                                    .setDuration(mShortAnimTime);
                        } else {
                            // If the ViewPropertyAnimator APIs aren't
                            // available, simply show or hide the in-layout UI
                            // controls.
                            controlsView.setVisibility(visible ? View.VISIBLE : View.GONE);
                        }

                        if (visible && AUTO_HIDE) {
                            // Schedule a hide().
                            delayedHide(AUTO_HIDE_DELAY_MILLIS);
                        }
                    }
                });

        // Set up the user interaction to manually show or hide the system UI.
        contentView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (TOGGLE_ON_CLICK) {
                    mSystemUiHider.toggle();
                } else {
                    mSystemUiHider.show();
                }
            }
        });
        
        ////////////////////////////////////////////////////////////////
       
        mAdapter = NfcAdapter.getDefaultAdapter(this);
        mPendingIntent = PendingIntent.getActivity(
        	    this, 0, new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
        
//        IntentFilter ndef = new IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED);
        IntentFilter tagDetected = new IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED);
//        try {
//            ndef.addDataType("*/*");    /* Handles all MIME based dispatches.
//                                           You should specify only the ones that you need. */
//        }
//        catch (MalformedMimeTypeException e) {
//            throw new RuntimeException("fail", e);
//        }
        intentFiltersArray = new IntentFilter[] {tagDetected, };
        
        techListsArray = new String[][] { new String[] { NfcF.class.getName() } };
        
        
        
        ///////////////////////////////////////////////////////////////
        // Upon interacting with UI controls, delay any scheduled hide()
        // operations to prevent the jarring behavior of controls going away
        // while interacting with the UI.
        findViewById(R.id.dummy_button).setOnTouchListener(mDelayHideTouchListener);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        // Trigger the initial hide() shortly after the activity has been
        // created, to briefly hint to the user that UI controls
        // are available.
        delayedHide(100);
    }


    /**
     * Touch listener to use for in-layout UI controls to delay hiding the
     * system UI. This is to prevent the jarring behavior of controls going away
     * while interacting with activity UI.
     */
    View.OnTouchListener mDelayHideTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            if (AUTO_HIDE) {
                delayedHide(AUTO_HIDE_DELAY_MILLIS);
            }
            return false;
        }
    };

    Handler mHideHandler = new Handler();
    Runnable mHideRunnable = new Runnable() {
        @Override
        public void run() {
            mSystemUiHider.hide();
        }
    };
    
    //////////////////////////////////////////////////////////////////
    @Override
    public void onResume() {
    	super.onResume();
    	System.out.println("in onResume");
    	if (mAdapter != null) mAdapter.enableForegroundDispatch(this, mPendingIntent, intentFiltersArray,
                techListsArray);
    	System.out.println(mPendingIntent);
//    	Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
    }
    
    public void onNewIntent(Intent intent) {
        Tag tagFromIntent = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        System.out.println("in onNewIntent");
        for (String string : tagFromIntent.getTechList()) {
        	System.out.println(string);
        }
        rfidid = ByteArrayToHexString(tagFromIntent.getId());
        Thread thread = new Thread(new Runnable(){
		    @Override
		    public void run() {
		    	try {
					URL url = new URL("http://192.168.1.155:8080/planspielBPMN/rest/create");
					HttpURLConnection connection = (HttpURLConnection) url.openConnection();
					connection.setDoOutput(true);
					connection.setRequestMethod("POST");
					connection.setRequestProperty("Content-Type", "application/json");
					//8F49488
					String input = String.format("{\"id\":\"%s\",\"rfidid\":\"%s\"}", "scanner0", rfidid);
					
					OutputStream output = connection.getOutputStream();
					output.write(input.getBytes());
					output.flush();
					
					System.out.println(connection.getResponseCode());
					System.out.println("ended");
					connection.disconnect();
				} catch(MalformedURLException mE) {
					mE.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		    }
		});

		thread.start();
        
        //do something with tagFromIntent
        
    }
    
    String ByteArrayToHexString(byte [] inarray) 
    {
    int i, j, in;
    String [] hex = {"0","1","2","3","4","5","6","7","8","9","A","B","C","D","E","F"};
    String out= "";

    for(j = 0 ; j < inarray.length ; ++j) 
        {
        in = (int) inarray[j] & 0xff;
        i = (in >> 4) & 0x0f;
        out += hex[i];
        i = in & 0x0f;
        out += hex[i];
        }
    return out;
}
   
    
    public void onPause() {
        super.onPause();
        if (mAdapter != null) mAdapter.disableForegroundDispatch(this);
        System.out.println("in onPause");
    }

   
    //////////////////////////////////////////////////////////////////

    /**
     * Schedules a call to hide() in [delay] milliseconds, canceling any
     * previously scheduled calls.
     */
    private void delayedHide(int delayMillis) {
        mHideHandler.removeCallbacks(mHideRunnable);
        mHideHandler.postDelayed(mHideRunnable, delayMillis);
    }
}
