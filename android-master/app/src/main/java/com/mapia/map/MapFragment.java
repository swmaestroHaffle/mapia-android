package com.mapia.map;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.gson.JsonObject;
import com.mapia.R;
import com.mapia.network.RestRequestHelper;

import java.util.ArrayList;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class MapFragment extends Fragment implements OnClickListener, LocationListener, OnMapClickListener, GoogleMap.OnCameraChangeListener{


	/*protected LatLng currentLatlng;
	protected LatLng cameraLatlng;
	protected float cameraZoom = 15;*/
	ImageButton btnLocCurrent;
	protected int type_num;
	private String[] type_string = {"","Private","Public","Follow","Group"};
	protected ArrayList<MarkerData> markerDatas = new ArrayList<MarkerData>();
	protected GoogleMap backgroundMap;
	protected LocationManager locationManager;
	protected static final long MIN_TIME = 400;
	protected static final float MIN_DISTANCE = 1000;
	protected boolean cameraMoveWhenCreate = false;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_map, container, false);
		final SupportMapFragment smf = (SupportMapFragment)getChildFragmentManager().findFragmentById(R.id.map_background);

		backgroundMap = smf.getMap();
		backgroundMap.setOnMapClickListener(this);
		backgroundMap.setOnCameraChangeListener(this);

		locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
		locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, MIN_TIME, MIN_DISTANCE, this);

		btnLocCurrent = (ImageButton)view.findViewById(R.id.btnLocCurrent);
		btnLocCurrent.setOnClickListener(this);
		return view;
	}

	@Override
	public void onResume() {
		if(MapActivity.currentLatlng!=null) this.backgroundMap.moveCamera((CameraUpdate)CameraUpdateFactory.newLatLngZoom(MapActivity.cameraLatlng, MapActivity.cameraZoom));
		drawMarker(markerDatas);
		super.onResume();
	}

	protected void drawMarker(ArrayList<MarkerData> markerDatas){
		for(int i=0;i<markerDatas.size();i++){
			markerDatas.get(i).marker = this.backgroundMap.addMarker(new MarkerOptions().position(markerDatas.get(i).location).title(type_string[type_num]));
		}
	}
	@Override
	public void onClick(View v) {
		switch(v.getId()){
			case R.id.btnLocCurrent:
				if(MapActivity.currentLatlng!=null) this.backgroundMap.moveCamera((CameraUpdate)CameraUpdateFactory.newLatLngZoom(MapActivity.currentLatlng, MapActivity.cameraZoom));
				break;
		}
		// TODO Auto-generated method stub
	}
	@Override
	public void onLocationChanged(Location location) {
		MapActivity.currentLatlng = new LatLng(location.getLatitude(), location.getLongitude());
		if (cameraMoveWhenCreate == false) {
			if (MapActivity.cameraLatlng != null)
				this.backgroundMap.animateCamera((CameraUpdate) CameraUpdateFactory.newLatLngZoom(MapActivity.currentLatlng, 15));
			cameraMoveWhenCreate = true;
		}
		//locationManager.removeUpdates(this);

	}
	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void onProviderEnabled(String provider) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void onProviderDisabled(String provider) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onMapClick(LatLng point) {
		Intent intent = new Intent(getActivity(), WritePostActivity.class);
		intent.putExtra("latlng",point);
		startActivityForResult(intent, 0);
		// TODO Auto-generated method stub
	}

	@Override
	public void onCameraChange(CameraPosition cameraPosition) {
		MapActivity.cameraLatlng = cameraPosition.target;
		MapActivity.cameraZoom = cameraPosition.zoom;
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, final Intent data) {
		switch(requestCode){
			case 0:
				if(resultCode== Activity.RESULT_OK) {
					RestRequestHelper requestHelper = RestRequestHelper.newInstance();
					final LatLng postLatlng = (LatLng)data.getParcelableExtra("latlng");
					final String postComment = (String)data.getStringExtra("comment");
                    final ArrayList<String> fileList;
                    if((ArrayList<String>)data.getStringArrayListExtra("filelist") != null) {
                        fileList = (ArrayList<String>) data.getStringArrayListExtra("filelist");
                        requestHelper.posts(
                                postComment, postLatlng, fileList, new Callback<JsonObject>() {
                                    @Override
                                    public void success(JsonObject jsonObject, Response response) {
                                        markerDatas.add(new MarkerData(postLatlng, postComment));
                                        markerDatas.get(markerDatas.size() - 1).marker = backgroundMap.addMarker(new MarkerOptions().position(markerDatas.get(markerDatas.size() - 1).location).title(markerDatas.get(markerDatas.size() - 1).letter));

                                        String filename = ((ArrayList<String>)data.getStringArrayListExtra("filelist")).get(0);
                                        String keyPrefix = "us-east-1:6638a26e-2810-4e4b-8d50-11cce837133f/";
                                        String[] keyArr = new String[1];
                                        keyArr[0] = keyPrefix+filename;
                                        //File Download Module
//                                        TransferController.download(WritePostActivity.this, keyArr);

                                        Toast.makeText(getActivity(), "Post 등록 성공".toString(), Toast.LENGTH_LONG).show();
                                    }

                                    @Override
                                    public void failure(RetrofitError error) {
                                        Log.i("Post 등록 실패", error.getMessage().toString());
                                        Toast.makeText(getActivity(), "Post 등록 실패".toString(), Toast.LENGTH_LONG).show();
                                    }
                                });


                    }
                    else{
                            requestHelper.posts(
                                    postComment, postLatlng, new Callback<JsonObject>() {
                                        @Override
                                        public void success(JsonObject jsonObject, Response response) {
                                            markerDatas.add(new MarkerData(postLatlng, postComment));
                                            markerDatas.get(markerDatas.size() - 1).marker = backgroundMap.addMarker(new MarkerOptions().position(markerDatas.get(markerDatas.size() - 1).location).title(markerDatas.get(markerDatas.size() - 1).letter));
                                            Toast.makeText(getActivity(), "Post 등록 성공".toString(), Toast.LENGTH_LONG).show();
                                        }

                                        @Override
                                        public void failure(RetrofitError error) {
                                            Log.i("Post 등록 실패", error.getMessage().toString());
                                            Toast.makeText(getActivity(), "Post 등록 실패".toString(), Toast.LENGTH_LONG).show();
                                        }
                                    });
                        }
				}
				else{

				}
				break;
		}
	}
}
