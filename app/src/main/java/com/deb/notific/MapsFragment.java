package com.deb.notific;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.deb.notific.helper.polylocation;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class MapsFragment extends Fragment {
List<LatLng>mLatLngs = new ArrayList<>();
LatLng mLatLng;
DatabaseReference local;
Double lat,lon;
Polygon mPolygon;
    GoogleMap googleMap;
Handler mHandler = new Handler();
    private OnMapReadyCallback callback = new OnMapReadyCallback() {


        @Override
        public void onMapReady(final GoogleMap googleMap) {
            googleMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
            googleMap.setBuildingsEnabled(true);
            googleMap.setMinZoomPreference(1.0f);
            googleMap.setMaxZoomPreference(25.0f);
            googleMap.setMyLocationEnabled(true);
            googleMap.getUiSettings().setZoomControlsEnabled(true);
            googleMap.getUiSettings().setCompassEnabled(true);
            googleMap.getUiSettings().setMyLocationButtonEnabled(true);
            CameraUpdateFactory.scrollBy(6, 6);
//           networkop runnable = new networkop(googleMap);
//           new Thread(runnable).start();
            local = FirebaseDatabase.getInstance().getReference();
            local.child("Marked Location").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    for(DataSnapshot dataSnapshot1:dataSnapshot.getChildren())
                    {
                        for(DataSnapshot dataSnapshot2:dataSnapshot1.getChildren())
                        {
                            for(DataSnapshot dataSnapshot3:dataSnapshot2.getChildren())
                            {
                                if(dataSnapshot3.getKey().equals("latitude")){
                                    lat = Double.parseDouble(dataSnapshot3.getValue().toString()) ;
                                    Log.d("Map",lat+"");
                                }
                                else if(dataSnapshot3.getKey().equals("longitude"))
                                {
                                    lon = Double.parseDouble(dataSnapshot3.getValue().toString()) ;
                                    Log.d("Map",lon+"");
                                }

                            }
                            mLatLng = new LatLng(lat, lon);
                            mLatLngs.add(mLatLng);
                        }
                        PolygonOptions polygonOptions = new PolygonOptions().addAll(mLatLngs).clickable(true);
                        mPolygon = googleMap.addPolygon(polygonOptions);
                        mPolygon.setTag("First Location");
                        mPolygon.setStrokeColor(Color.BLACK);
                        mPolygon.setFillColor(Color.BLACK);
                        mLatLngs.clear();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });


        }
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_maps, container, false);
        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        SupportMapFragment mapFragment =
                (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(callback);
        }


    }
    public class networkop implements Runnable{
    DatabaseReference root;
    GoogleMap mMap;

        public networkop(GoogleMap map) {
            mMap = map;
        }

        @Override
        public void run() {
            Log.d("Map","Thread Started");
//            root = FirebaseDatabase.getInstance().getReference();
//            root.child("Marked location").addValueEventListener(new ValueEventListener() {
//                @Override
//                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                    for(DataSnapshot dataSnapshot1:dataSnapshot.getChildren())
//                    {
//                        for(DataSnapshot dataSnapshot2:dataSnapshot1.getChildren())
//                        {
////                            for(DataSnapshot dataSnapshot3:dataSnapshot2.getChildren())
////                            {
////                                if(dataSnapshot3.getKey().equals("latlang"))
////                                {
////                                    lat = (Double) dataSnapshot3.getValue();
////                                    Log.d("Map",lat+"");
////                                }else
////                                    lon = (Double) dataSnapshot3.getValue();
////                            }
//                            polylocation polylocation = dataSnapshot2.getValue(com.deb.notific.helper.polylocation.class);
//                            Log.d("Map",polylocation+"");
//                            mLatLng = new LatLng(polylocation.getLatitude(), polylocation.getLongitude());
//                            mLatLngs.add(mLatLng);
//                        }
//                        mHandler.post(new Runnable() {
//                            @Override
//                            public void run() {
//                                Log.d("Map","Runnable");
//                                PolygonOptions polygonOptions = new PolygonOptions().addAll(mLatLngs).clickable(true);
//                                mPolygon = mMap.addPolygon(polygonOptions);
//                                mPolygon.setTag("First Location");
//                                mPolygon.setStrokeColor(Color.BLACK);
//                                mPolygon.setFillColor(Color.BLACK);
//                            }
//                        });
//                        mLatLngs.clear();
//                    }
//                }
//
//                @Override
//                public void onCancelled(@NonNull DatabaseError databaseError) {
//
//                }
//            });
        }
    }
}
