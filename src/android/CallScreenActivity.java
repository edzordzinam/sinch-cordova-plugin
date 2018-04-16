package prexition.plugin.voip;

import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import com.sinch.android.rtc.PushPair;
import com.sinch.android.rtc.SinchError;
import com.sinch.android.rtc.calling.Call;
import com.sinch.android.rtc.calling.CallDetails;
import com.sinch.android.rtc.calling.CallEndCause;
import com.sinch.android.rtc.calling.CallListener;

import org.json.JSONException;
import org.json.JSONObject;


public class CallScreenActivity extends Activity implements SinchService.StartFailedListener {


    static final String TAG = CallScreenActivity.class.getSimpleName();
    private SinchService callService;
    private Call callInProgress;

    private String mCallId;
    private String phoneNumber;
    private String userId;
    private String customerName;
    private Integer bookingId;
    private String customerAddress;


    private TextView mCallDuration;
    private TextView mCallState;
    private TextView mCallerName;
    private TextView mAddress;
    private Boolean isLoudSpeaker = false;
    private Button bSpeaker;


    private AudioPlayer mAudioPlayer;
    private Timer mTimer;
    private UpdateCallDurationTask mDurationTask;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(getResources().getIdentifier("activity_call_screen", "layout", getPackageName()));

        //set the UI elements
        mCallDuration = (TextView) findViewById(getResources().getIdentifier("duration", "id", getPackageName()));
        mCallerName = (TextView) findViewById(getResources().getIdentifier("username", "id", getPackageName()));
        mCallState = (TextView) findViewById(getResources().getIdentifier("status", "id", getPackageName()));
        mAddress = (TextView) findViewById(getResources().getIdentifier("address", "id", getPackageName()));

        bSpeaker = (Button) findViewById(getResources().getIdentifier("loudspeaker", "id", getPackageName()));


        phoneNumber = getIntent().getExtras().getString("PHONE_NUMBER");
        userId = getIntent().getExtras().getString("USER_ID");
        customerName = getIntent().getExtras().getString("CUSTOMER_NAME");
        bookingId = getIntent().getExtras().getInt("BOOKING_ID");
        customerAddress = getIntent().getExtras().getString("CUSTOMER_ADDRESS");

        mCallerName.setText(customerName);
        mCallState.setText("Starting Service");
        mAddress.setText(customerAddress);


        //start sinch service if its not already started.
        callService = new SinchService();

        if (!callService.isStarted()) {
            callService.setStartListener(this);
            callService.startClient(userId, this);
            Log.d(TAG, "Sinch Service Started");
        }

    }


    private class UpdateCallDurationTask extends TimerTask {

        @Override
        public void run() {
            CallScreenActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    updateCallDuration();
                }
            });
        }
    }

    public void hangUp(View view) {
        //this is the onclick handler for hanging up the call
        Toast.makeText(this, "Hangup call", Toast.LENGTH_SHORT).show();
        mCallState.setText("Ending Call");

        this.endCall();
        this.finish();
    }

    public void toggleSpeaker(View view) {
        if (!this.isLoudSpeaker) {
            callService.getClient().getAudioController().enableSpeaker();
            bSpeaker.setCompoundDrawablesWithIntrinsicBounds(getResources().getIdentifier("loudspeaker_on", "mipmap", getPackageName()), 0, 0, 0);
            bSpeaker.setText("Speaker On");
        } else {
            callService.getClient().getAudioController().disableSpeaker();
            bSpeaker.setCompoundDrawablesWithIntrinsicBounds(getResources().getIdentifier("loudspeaker", "mipmap", getPackageName()), 0, 0, 0);
            bSpeaker.setText("Speaker Off");
        }

        this.isLoudSpeaker = !this.isLoudSpeaker;
    }


    @Override
    public void onStarted() {
        Log.d(TAG, "Sinch Service successfully started");

        Log.d(TAG, "Starting VOIP Call");
        mCallState.setText("Initiating Call");

        mAudioPlayer = new AudioPlayer(this, getResources().getIdentifier("progress_tone", "raw", getPackageName()));

        //add customer data
        Map<String,String> headers = new HashMap<String, String>();
        headers.put("customer", this.customerName);
        headers.put("address", this.customerAddress);
        headers.put("bookingId", this.bookingId.toString());
        headers.put("number", this.phoneNumber);

        callInProgress = callService.callPhoneNumber(phoneNumber, headers);
        callInProgress.addCallListener(new SinchCallListener());
    }



    @Override
    public void onStartFailed(SinchError error) {
        Log.d(TAG, "Sinch Service FAILED to start");

        mCallState.setText("Connection failed, retrying...");

        //attempting to restart
        if (!callService.isStarted()) {
            callService.setStartListener(this);
            callService.startClient(userId, this);
            Log.d(TAG, "Sinch Service Started");
        }
    }

    private void endCall() {

        callService.getClient().getAudioController().disableSpeaker();
        callService.getClient().getAudioController().unmute();

        mAudioPlayer.stopProgressTone();
        if (callInProgress != null) {
            //stop timer

            if (mDurationTask != null) {
                mDurationTask.cancel();
                mTimer.cancel();
            }

            callInProgress.hangup();
        }

    }


    @Override
    public void onBackPressed() {
        Toast.makeText(this, "End call to go back!", Toast.LENGTH_LONG).show();
    }


    @Override
    public void onPause() {
        super.onPause();
        mDurationTask.cancel();
        mTimer.cancel();
    }

    @Override
    public void onResume() {
        super.onResume();
        mTimer = new Timer();
        mDurationTask = new UpdateCallDurationTask();
        mTimer.schedule(mDurationTask, 0, 500);
    }

    private String formatTimespan(int totalSeconds) {
        long minutes = totalSeconds / 60;
        long seconds = totalSeconds % 60;
        return String.format(Locale.US, "%02d:%02d", minutes, seconds);
    }

    private void updateCallDuration() {
        if (callInProgress != null) {
            mCallDuration.setText(formatTimespan(callInProgress.getDetails().getDuration()));
        }
    }


    private class SinchCallListener implements CallListener {

        @Override
        public void onCallEnded(Call call) {
            CallEndCause cause = call.getDetails().getEndCause();
            Log.d(TAG, "Call ended. Reason: " + cause.toString());

            mCallState.setText("Call Ended");

            mAudioPlayer.stopProgressTone();
            setVolumeControlStream(AudioManager.USE_DEFAULT_STREAM_TYPE);

            CallDetails details = call.getDetails();

            JSONObject callDetails = new JSONObject();
            try {
                callDetails.put("startedTime", details.getStartedTime());
                callDetails.put("endedTime", details.getEndedTime());
                callDetails.put("establishedTime", details.getEstablishedTime());
                callDetails.put("phonenumber", phoneNumber);
                callDetails.put("customer_name", customerName);
                callDetails.put("booking_id", bookingId);

            } catch (JSONException e) {

            }

            endCall();
            finish();
        }

        @Override
        public void onCallEstablished(Call call) {
            Log.d(TAG, "Call established");
            mCallState.setText("Call Connected");

            mAudioPlayer.stopProgressTone();
            setVolumeControlStream(AudioManager.STREAM_VOICE_CALL);

            //start timer
            mTimer = new Timer();
            mDurationTask = new CallScreenActivity.UpdateCallDurationTask();
            mTimer.schedule(mDurationTask, 0, 500);


        }

        @Override
        public void onCallProgressing(Call call) {
            Log.d(TAG, "Call progressing");
            mCallState.setText("Calling");

            mAudioPlayer.playProgressTone();
        }

        public void onShouldSendPushNotification(Call call, List<PushPair> pushPairs) {
            // Send a push through your push provider here, e.g. GCM
        }

    }

}



