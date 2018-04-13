package prexition.plugin.voip;

import org.apache.cordova.*;
import org.json.JSONArray;
import org.json.JSONException;

import com.sinch.android.rtc.SinchError;
import com.sinch.android.rtc.calling.Call;
import com.sinch.android.rtc.PushPair;
import com.sinch.android.rtc.calling.CallDetails;
import com.sinch.android.rtc.calling.CallEndCause;
import com.sinch.android.rtc.calling.CallListener;

import android.util.Log;
import android.Manifest;
import android.content.pm.PackageManager;

import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import org.json.JSONObject;

import android.media.AudioManager;

import java.util.List;

public class CallActivity extends CordovaPlugin implements SinchService.StartFailedListener {

    static final String TAG = CallActivity.class.getSimpleName();
    private SinchService callService;
    private Call callInProgress;

    private boolean isPermitted;

    public static final String[] permissions = {Manifest.permission.RECORD_AUDIO, Manifest.permission.MODIFY_AUDIO_SETTINGS, Manifest.permission.READ_PHONE_STATE};

    //callbacks
    private CallbackContext onClientStartedCallbackContext;
    private CallbackContext onCallStartedCallbackContext;
    private CallbackContext onCallEndedCallbackContext;
    private CallbackContext onCallEstablishedCallbackContext;
    private CallbackContext onCallInProgressCallbackContext;


    private AudioPlayer mAudioPlayer;
    private Timer mTimer;
    private UpdateCallDurationTask mDurationTask;

    private Integer mCallDuration;
    private String mCallState;
    private String mCallerName;

    private String phoneNumber;
    private String customerName;
    private Integer bookingId;
    private String userId;

    private String mCallId;


    private class UpdateCallDurationTask extends TimerTask {

        @Override
        public void run() {
            cordova.getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    updateCallDuration();
                }
            });
        }
    }

    @Override
    public boolean execute(String action, JSONArray data, CallbackContext callbackContext) throws JSONException {

        if (action.equals("initSinchClient")) {

            Log.d(TAG, "SinchClient started native");

            userId = data.getString(0);

            if (!hasPermissions()) {
                Log.d(TAG, "Requesting permissions from user");
                callbackContext.error("Permission not granted");
                PermissionHelper.requestPermissions(this, 0, permissions);
                return false;
            }

            callService = new SinchService();
            callService.setStartListener(this);
            callService.startClient(userId, cordova.getActivity().getApplicationContext());

            callbackContext.success(userId);

            return true;

        } else if (action.equals("makePhoneCall")) {

            //check if isStarted before attempting to make call
            phoneNumber = data.getString(0);
            customerName = data.getString(1);
            bookingId = data.getInt(2);

            //check for permission first

            //get progress tone id

            int identifier = cordova.getActivity().getResources().getIdentifier("progress_tone", "raw", cordova.getActivity().getPackageName());

            mAudioPlayer = new AudioPlayer(cordova.getActivity().getApplicationContext(), identifier);
            if (callService.isStarted()) {
                callInProgress = callService.callPhoneNumber(phoneNumber);
                callInProgress.addCallListener(new SinchCallListener());
                Log.d(TAG, "Call initiated");
                callbackContext.success("call initiated");
                return true;
            } else {
                Log.d(TAG, "Call Service not started");
                return true;
            }

        } else if (action.equals("hangUp")) {

            Log.d(TAG, "Hangup");
            endCall();

        } else if (action.equals("onCallStarted")) {
            if (onCallStartedCallbackContext == null)
                onCallStartedCallbackContext = callbackContext;
            return true;
        } else if (action.equals("onCallEnded")) {
            if (onCallEndedCallbackContext == null)
                onCallEndedCallbackContext = callbackContext;

            return true;
        } else if (action.equals("onCallEstablished")) {
            if (onCallEstablishedCallbackContext == null)
                onCallEstablishedCallbackContext = callbackContext;
            return true;
        } else if (action.equals("onCallInProgress")) {
            if (onCallInProgressCallbackContext == null)
                onCallInProgressCallbackContext = callbackContext;
            return true;
        } else if (action.equals("onCallClientStarted")) {
            if (onClientStartedCallbackContext == null)
                onClientStartedCallbackContext = callbackContext;
            return true;
        } else if (action.equals("isClientStarted")) {
            if (callService.isStarted()) {
                callbackContext.success();
            } else {
                callbackContext.error(0);
            }
        }

        return false;
    }

    @Override
    public void onStartFailed(SinchError error) {

    }

    public boolean hasPermissions() {
        for (String p : permissions) {
            if (!PermissionHelper.hasPermission(this, p)) {
                return false;
            }
        }
        return true;
    }

    public void onRequestPermissionResult(int requestCode, String[] permissions,
                                          int[] grantResults) throws JSONException {
        for (int r : grantResults) {
            if (r == PackageManager.PERMISSION_DENIED) {
                Log.d(TAG, "PERMISSION DENIED");
                PluginResult result;
                result = new PluginResult(PluginResult.Status.ERROR, 0);
                result.setKeepCallback(true);
                onCallStartedCallbackContext.sendPluginResult(result);
                return;
            }
        }
        switch (requestCode) {
            case 0:

                //continue and start..
                callService = new SinchService();
                callService.setStartListener(this);
                callService.startClient(userId, cordova.getActivity().getApplicationContext());

                PluginResult result;
                result = new PluginResult(PluginResult.Status.ERROR, 0);
                result.setKeepCallback(true);
                onCallStartedCallbackContext.sendPluginResult(result);


                break;
        }
    }

    @Override
    public void onStarted() {
        PluginResult result;
        result = new PluginResult(PluginResult.Status.OK, 1);
        result.setKeepCallback(true);
        onClientStartedCallbackContext.sendPluginResult(result);
    }

    private void endCall() {
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

    private String formatTimespan(int totalSeconds) {
        long minutes = totalSeconds / 60;
        long seconds = totalSeconds % 60;
        return String.format(Locale.US, "%02d:%02d", minutes, seconds);
    }

    private void updateCallDuration() {
        if (callInProgress != null) {
            mCallDuration = callInProgress.getDetails().getDuration();

            PluginResult result;
            result = new PluginResult(PluginResult.Status.OK, mCallDuration);
            result.setKeepCallback(true);
            onCallInProgressCallbackContext.sendPluginResult(result);
        }
    }

    private class SinchCallListener implements CallListener {

        @Override
        public void onCallEnded(Call call) {
            CallEndCause cause = call.getDetails().getEndCause();
            Log.d(TAG, "Call ended. Reason: " + cause.toString());
            mAudioPlayer.stopProgressTone();
            cordova.getActivity().setVolumeControlStream(AudioManager.USE_DEFAULT_STREAM_TYPE);
            String endMsg = "Call ended: " + call.getDetails().toString();

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

            PluginResult result;
            result = new PluginResult(PluginResult.Status.OK, callDetails);
            result.setKeepCallback(true);
            onCallEndedCallbackContext.sendPluginResult(result);

            endCall();
        }

        @Override
        public void onCallEstablished(Call call) {
            Log.d(TAG, "Call established");
            mAudioPlayer.stopProgressTone();
            cordova.getActivity().setVolumeControlStream(AudioManager.STREAM_VOICE_CALL);

            //start timer
            mTimer = new Timer();
            mDurationTask = new UpdateCallDurationTask();
            mTimer.schedule(mDurationTask, 0, 500);

            PluginResult result;
            result = new PluginResult(PluginResult.Status.OK, 1);
            result.setKeepCallback(true);
            onCallEstablishedCallbackContext.sendPluginResult(result);
        }

        @Override
        public void onCallProgressing(Call call) {
            Log.d(TAG, "Call progressing");
            mAudioPlayer.playProgressTone();
        }

        public void onShouldSendPushNotification(Call call, List<PushPair> pushPairs) {
            // Send a push through your push provider here, e.g. GCM
        }

    }
}
