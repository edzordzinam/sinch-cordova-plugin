package prexition.plugin.voip;

import com.sinch.android.rtc.ClientRegistration;
import com.sinch.android.rtc.Sinch;
import com.sinch.android.rtc.SinchClient;
import com.sinch.android.rtc.SinchClientListener;
import com.sinch.android.rtc.SinchError;
import com.sinch.android.rtc.calling.Call;

import android.content.Context;
import android.util.Log;

import java.util.Map;

public class SinchService {

    private static final String APP_KEY = "XXXXXXXXXXXXx";
    private static final String APP_SECRET = "YYYYYYYYYYYYY";
    private static final String ENVIRONMENT = "clientapi.sinch.com";

    public static final String CALL_ID = "CALL_ID";
    static final String TAG = SinchService.class.getSimpleName();

    private SinchClient mSinchClient;
    private String mUserId;
    private Context applicationContext;

    private StartFailedListener mListener;


    public void onDestroy() {
        if (mSinchClient != null && mSinchClient.isStarted()) {
            mSinchClient.terminate();
        }
    }

    private void start(String userName) {
        if (mSinchClient == null) {

            mUserId = userName;
            mSinchClient = Sinch.getSinchClientBuilder().context(applicationContext).userId(userName)
                    .applicationKey(APP_KEY)
                    .applicationSecret(APP_SECRET)
                    .environmentHost(ENVIRONMENT).build();

            mSinchClient.setSupportCalling(true);

            mSinchClient.addSinchClientListener(new MySinchClientListener());
            mSinchClient.start();
        }
    }

    private void stop() {
        if (mSinchClient != null) {
            mSinchClient.terminate();
            mSinchClient = null;
        }
    }

    private boolean isClientStarted() {
        return (mSinchClient != null && mSinchClient.isStarted());
    }


    public Call callPhoneNumber(String phoneNumber, Map headers) {
        return mSinchClient.getCallClient().callPhoneNumber(phoneNumber, headers);
    }

    public Call callUser(String userId) {
        return mSinchClient.getCallClient().callUser(userId);
    }

    public String getUserName() {
        return mUserId;
    }

    public boolean isStarted() {
        return this.isClientStarted();
    }

    public void startClient(String userName, Context context) {
        applicationContext = context;
        start(userName);
    }

    public SinchClient getClient(){
        return this.mSinchClient;
    }

    public void stopClient() {
        stop();
    }

    public void setStartListener(StartFailedListener listener) {
        mListener = listener;
    }

    public Call getCall(String callId) {
        return mSinchClient.getCallClient().getCall(callId);
    }

    public interface StartFailedListener {

        void onStartFailed(SinchError error);

        void onStarted();
    }

    private class MySinchClientListener implements SinchClientListener {

        @Override
        public void onClientFailed(SinchClient client, SinchError error) {
            if (mListener != null) {
                mListener.onStartFailed(error);
            }
            mSinchClient.terminate();
            mSinchClient = null;
        }

        @Override
        public void onClientStarted(SinchClient client) {
            Log.d(TAG, "SinchClient client started");
            if (mListener != null) {
                mListener.onStarted();
            } else {
                Log.d(TAG, "mListener is null started");
            }
        }

        @Override
        public void onClientStopped(SinchClient client) {
            Log.d(TAG, "SinchClient stopped");
        }

        @Override
        public void onLogMessage(int level, String area, String message) {
            switch (level) {
                case Log.DEBUG:
                    Log.d(area, message);
                    break;
                case Log.ERROR:
                    Log.e(area, message);
                    break;
                case Log.INFO:
                    Log.i(area, message);
                    break;
                case Log.VERBOSE:
                    Log.v(area, message);
                    break;
                case Log.WARN:
                    Log.w(area, message);
                    break;
            }
        }

        @Override
        public void onRegistrationCredentialsRequired(SinchClient client,
                                                      ClientRegistration clientRegistration) {
        }
    }


}