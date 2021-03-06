package com.deb.notific;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.media.AudioManager;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.ContactsContract;
import android.telephony.PhoneStateListener;
import android.telephony.SmsManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.android.internal.telephony.ITelephony;
import com.deb.notific.helper.BusStation;
import com.deb.notific.helper.Check;
import com.deb.notific.helper.message;
import com.deb.notific.helper.pnumber;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.Api;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.Builder;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.maps.android.PolyUtil;
import com.snatik.polygon.Point;
import com.snatik.polygon.Polygon;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class MyService extends Service implements GoogleApiClient.ConnectionCallbacks,GoogleApiClient.OnConnectionFailedListener, LocationListener {
    private static final String TAG = MyService.class.getSimpleName();
    DatabaseReference root = FirebaseDatabase.getInstance().getReference();
    LatLng mLng,mLatLng;
    List<LatLng>mLatLngs = new ArrayList<>();
    List<LatLng>mLatLngList = new ArrayList<>();
    Double latitude,longitude;
    AudioManager mAudioManager;
    GoogleApiClient mLocationClient;
    Boolean flag =false;
    String phoneNr;
    String result;
    String number;
    BroadcastReceiver receiver;
    Context mContext;
    ITelephony  telephonyService;
    LocationRequest mLocationRequest = new LocationRequest();
    public static final String CHANNEL_ID = "ForegroundServiceChannel";
    private static final int MY_PERMISSIONS_REQUEST_SEND_SMS =0 ;
    public static final String ACTION_LOCATION_BROADCAST = MyService.class.getName() + "LocationBroadcast";
    public static final String EXTRA_LATITUDE = "extra_latitude";
    public static final String EXTRA_LONGITUDE = "extra_longitude";
    Phone broad;

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = this;
       broad = new Phone();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
//        if(intent.hasExtra())
            if(intent.hasExtra("ACTION")){
                if(intent.getStringExtra("ACTION").equals("STOP"))
                {
                    Intent intent1 = new Intent(this,MyService.class);
                    stopService(intent1);
                }
            }

        getData();
        createNotificationChannel();

        mAudioManager = (AudioManager) getSystemService(AUDIO_SERVICE);


        Intent notificationIntent = new Intent(this, Main2Activity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,
                0, notificationIntent, 0);
//        Intent stopService = new Intent(this,MyService.class);
//        stopService.setAction("STOP");
//        PendingIntent stop = PendingIntent.getService(this,0,stopService,PendingIntent.FLAG_CANCEL_CURRENT);
        CharSequence input = "Checking";
        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Foreground Service")
                .setContentText(input)
                .setContentIntent(pendingIntent)
//                .addAction(R.drawable.address,"Stop",stop)
                .build();

        startForeground(1, notification);
        mLocationClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API).build();

        mLocationRequest.setInterval(3000);
        mLocationRequest.setFastestInterval(2000);
//        mLocationRequest.setSmallestDisplacement(10f);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationClient.connect();

        return START_REDELIVER_INTENT;
    }
    private void getData() {
        root.child("Marked Location").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot dataSnapshot1:dataSnapshot.getChildren())
                {
                    for(DataSnapshot dataSnapshot2:dataSnapshot1.getChildren())
                    {

                        for(DataSnapshot dataSnapshot3: dataSnapshot2.getChildren())
                        {
                            if(dataSnapshot3.getKey().equals("latitude"))
                            {
                                latitude = Double.parseDouble(dataSnapshot3.getValue().toString()) ;
                            }
                            else
                                longitude =  Double.parseDouble(dataSnapshot3.getValue().toString()) ;
                        }
                        mLng = new LatLng(latitude,longitude);
                        Log.d("MyService",mLng.toString());
                        mLatLngs.add(mLng);
                        Log.d("MyService","Got data");
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "== Error On onConnected() Permission not granted");
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(mLocationClient,mLocationRequest,this);
        Log.d(TAG,"Connected to Google API");

    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.d(TAG, "Connection suspended");

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.d(TAG, "Failed to connect to Google API");
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.d(TAG, "Location changed");
        if(location != null)
        {
            send(location);
//            check(location);
            mLatLng = new LatLng(location.getLatitude(), location.getLongitude());
            Log.d("MyService",location.getLatitude()+ " " + location.getLongitude() +" A");
            getchecked();
            mLatLngs.clear();
            getData();
            sound(flag);
        }
    }

    private void getchecked() {
        for(int i=0;i<mLatLngs.size()/5;i++)
        {
            Log.d("MyService", String.valueOf(i));
            for(int j =0;j<5;j++)
            {
                Log.d("MyService", String.valueOf(j)+"point");
                mLatLngList.add(mLatLngs.get(j));
                Log.d("MyService",mLatLngList.get(j).toString());
            }
            mLatLngList.add(mLatLngs.get(0));
            flag = PolyUtil.containsLocation(mLatLng,mLatLngList,true);
            Log.d("MyService",flag.toString());

            sound(flag);
            if(flag)
            {
                startBroadcast();
            }
            else
            {

                try {
                    stopBroadcast();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            mLatLngList.clear();
        }
    }

    private void stopBroadcast() {
        unregisterReceiver(broad);
    }

    private void startBroadcast() {

        IntentFilter filter = new IntentFilter();
        filter.addAction("android.intent.action.PHONE_STATE");
        filter.addAction("android.intent.action.NEW_OUTGOING_CALL");
        filter.addAction("android.media.RINGER_MODE_CHANGED");
        registerReceiver(broad, filter);
    }

    private void sound(Boolean flag) {
        if(flag)
        {

            if(mAudioManager.getRingerMode() != AudioManager.RINGER_MODE_VIBRATE)
            {
                if(mAudioManager.getRingerMode() != AudioManager.RINGER_MODE_SILENT)
                {
                    mAudioManager.setRingerMode(AudioManager.RINGER_MODE_VIBRATE);
                }

            }
        }
        else
            if(mAudioManager.getRingerMode() != AudioManager.RINGER_MODE_NORMAL)
            {
                mAudioManager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
            }


    }

    private void send(Location location) {
        Geocoder geocoder = new Geocoder(this);
        try {
            List<Address> addresses =
                    geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
            result = addresses.get(0).getLocality() + ":";
            result += addresses.get(0).getCountryName();
        } catch (Exception e) {
            e.printStackTrace();
        }
        sendnotific(result);
        sendMessageToUI(String.valueOf(location.getLatitude()),String.valueOf(location.getLongitude()),result,flag);
    }


    private void sendnotific(String result) {
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,
                0, notificationIntent, 0);
        Intent stopService = new Intent(this,MyService.class);
        stopService.putExtra("ACTION","STOP");
        PendingIntent stop = PendingIntent.getService(this,0,stopService,PendingIntent.FLAG_CANCEL_CURRENT);
        Notification notification =  new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("The locaton service is running")
                .setContentText(result)
                .setSmallIcon(R.drawable.address)
                .setContentIntent(pendingIntent)
                .addAction(R.drawable.address,"Stop",stop)
                .build();

        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(1, notification);
    }




    private void sendMessageToUI(String lat ,String lng,String nm, Boolean state) {
        Intent intent = new Intent(ACTION_LOCATION_BROADCAST);
        intent.putExtra(EXTRA_LATITUDE,lat);
        intent.putExtra(EXTRA_LONGITUDE,lng);
        intent.putExtra("Name",nm);
        intent.putExtra("STATE",state.toString());
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "Foreground Service Channel",
                    NotificationManager.IMPORTANCE_LOW
            );

            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(serviceChannel);
        }
    }

//    public class mybroad extends BroadcastReceiver{
//    Context mContext;
//        @Override
//        public void onReceive(Context context, Intent intent) {
//            mContext = context;
//        }
//
//        public Context getContext() {
//            return mContext;
//        }
//    }
}
