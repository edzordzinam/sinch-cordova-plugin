package prexition.plugin.voip;

import org.apache.cordova.*;
import org.json.JSONArray;
import org.json.JSONException;


import android.app.PendingIntent;
import android.telephony.SmsManager;
import android.util.Log;
import android.Manifest;
import android.content.pm.PackageManager;
import android.content.Intent;
import android.content.Context;

public class CallPlugin extends CordovaPlugin {

    static final String TAG = CallPlugin.class.getSimpleName();

    private boolean isPermitted;

    public static final String[] permissions = {Manifest.permission.RECORD_AUDIO, Manifest.permission.MODIFY_AUDIO_SETTINGS, Manifest.permission.READ_PHONE_STATE};
    public static final String[] sms_permissions = {Manifest.permission.SEND_SMS, Manifest.permission.READ_PHONE_STATE};


    private String phoneNumber;
    private String customerName;
    private String customerAddress;
    private Integer bookingId;
    private String userId;
    private String message;

    private String mCallId;


    private void startVOIPCall(Context context) {
        Intent intent = new Intent(context, CallScreenActivity.class);
        intent.putExtra("PHONE_NUMBER", phoneNumber);
        intent.putExtra("CUSTOMER_NAME", customerName);
        intent.putExtra("CUSTOMER_ADDRESS", customerAddress);
        intent.putExtra("BOOKING_ID", bookingId);
        intent.putExtra("USER_ID", userId);

        this.cordova.getActivity().startActivity(intent);
    }


    private void sendSMS(Context context) {
        if (this.cordova.getActivity().getPackageManager().hasSystemFeature("android.hardware.telephony")) {
            int n;
            PendingIntent sentIntent = PendingIntent.getBroadcast((Context) this.cordova.getActivity(), (int) 0, (Intent) new Intent("SENDING_SMS"), (int) 0);
            SmsManager sms = SmsManager.getDefault();
            sms.sendTextMessage(this.phoneNumber, null, this.message, sentIntent, (PendingIntent) null);
        }
    }

    @Override
    public boolean execute(String action, JSONArray data, CallbackContext callbackContext) throws JSONException {

        Context context = cordova.getActivity().getApplicationContext();

        if (action.equals("initSinchClient")) {

            return true;

        } else if (action.equals("makeVOIPCall")) {

            //check if isStarted before attempting to make call

            userId = data.getString(0);
            phoneNumber = data.getString(1);
            customerName = data.getString(2);
            bookingId = data.getInt(3);
            customerAddress = data.getString(4);

            if (!hasPermissions()) {
                Log.d(TAG, "Requesting permissions from user");
                callbackContext.error("Permission not granted");
                PermissionHelper.requestPermissions(this, 0, permissions);
                return false;
            }

            this.startVOIPCall(context);

            callbackContext.success();

            return true;


        } else if (action.equals("sendSMS")) {
            phoneNumber = data.getString(0);
            message = data.getString(1);

            if (!hasSMSPermissions()) {
                Log.d(TAG, "Requesting permissions from user");
                callbackContext.error("Permission not granted");
                PermissionHelper.requestPermissions(this, 1, sms_permissions);
                return false;
            }

            this.sendSMS(context);
        }


        return false;
    }

    public boolean hasPermissions() {
        for (String p : permissions) {
            if (!PermissionHelper.hasPermission(this, p)) {
                return false;
            }
        }
        return true;
    }

    public boolean hasSMSPermissions(){
        for (String p : sms_permissions) {
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

                return;
            }
        }
        switch (requestCode) {
            case 0:
                //continue and start..
                this.startVOIPCall(this.cordova.getActivity().getApplicationContext());
                break;

            case 1:
                this.sendSMS(this.cordova.getActivity().getApplicationContext());
                break;
        }
    }


}
