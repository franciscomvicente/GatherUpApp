package com.example.gatherup;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.gatherup.Utils.ClusterManagerRenderer;
import com.example.gatherup.Utils.ClusterMarker;
import com.example.gatherup.Utils.EventsModel;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterManager;

import java.util.ArrayList;

public class MapsFragment extends Fragment implements LocationListener, OnMapReadyCallback {

    private static final String TAG = "Teste";
    public static final int ERROR_DIALOG_REQUEST = 9001;
    public static final int PERMISSIONS_REQUEST_ENABLE_GPS = 9002;
    public static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 9003;

    private FirebaseFirestore store;
    private ArrayList<EventsModel> list= new ArrayList<>();

    private GoogleMap mMap;
    LocationManager locationManager;
    private static final int REQUEST_CHECK_SETTINGS = 100;
    public double latitude;
    public double longitude;
    Marker markername;
    Button listaEventosButton;
    private Location location;
    Marker currentLocationMarker;

    private ClusterManager mClusterManager;
    private ClusterManagerRenderer mClusterManagerRenderer;
    private ArrayList<ClusterMarker> mClusterMarkers = new ArrayList<>();

    private ArrayList<EventsModel> mEventLocations = new ArrayList<>();

    private final OnMapReadyCallback callback = new OnMapReadyCallback() {
        @Override
        public void onMapReady(GoogleMap googleMap) {
            mMap = googleMap;
            requestlocation();
            //LatLng sydney = new LatLng(38.756829, -9.155290);
        }
    };

    //NAO VERIFICA PERMISSOES, TIRAR DEPOIS
    public void requestlocation(){
        checkLocationPermission();
        locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 10, 100, this);

        location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        DisplayLocation(location);
    }

    public void checkLocationPermission() { //TESTE verificar o que faz
        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 1001);
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_maps, container, false);
        setHasOptionsMenu(true);

        listaEventosButton = view.findViewById(R.id.ListButton);
        store = FirebaseFirestore.getInstance();

        listaEventosButton.setOnClickListener(view1 -> {
            Fragment eventListFragment = new EventListFragment();
            FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.MainFragment, eventListFragment).addToBackStack(null).commit();
        });

        /*
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Events");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot datasnapshot) {
                list.clear();
                System.out.println("1");
                for(DataSnapshot snapshot: datasnapshot.getChildren()){
                    EventsModel eventsModel = snapshot.getValue(EventsModel.class);
                    list.add(eventsModel);
                    System.out.println("2");
                    addMapMarkers();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

         */

        store.collection("Events").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    System.out.println("TRY");
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        Log.d(TAG, document.getId() + " => " + document.getData());
                        EventsModel eventsModel = document.toObject(EventsModel.class);
                        list.add(eventsModel);
                    }
                } else {
                    Log.d(TAG, "Error getting documents: ", task.getException());
                }
                addMapMarkers();
            }
        });


        /*
        Query query = store.collection("Events");

        PagingConfig config = new PagingConfig(3);//MODIFICAR QUANTO NECESSÁRIO

        //PAGING OPTIONS
        FirestorePagingOptions<EventsModel> options = new FirestorePagingOptions.Builder<EventsModel>().setLifecycleOwner(this).setQuery(query, config, new SnapshotParser<EventsModel>() {
            @NonNull
            @Override
            public EventsModel parseSnapshot(@NonNull DocumentSnapshot snapshot) {
                EventsModel eventsModel = snapshot.toObject(EventsModel.class);
                System.out.println("1");
                String eventID = snapshot.getId();
                eventsModel.setEventID(eventID);
                return eventsModel;
            }
        }).build();

        adapter = new FirestoreAdapter(options, this);

         */
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.mapFragment);
        if (mapFragment != null) {
            mapFragment.getMapAsync(callback);
        }
    }

    @Override
    public void onLocationChanged(@NonNull Location location) {
        markername.remove();
        latitude = location.getLatitude();
        longitude = location.getLongitude();
        markername.remove();
        DisplayLocation(location);
    }

    @SuppressLint("MissingPermission")
    public void DisplayLocation(Location location) {

        if (location != null) {
            latitude = location.getLatitude();
            longitude = location.getLongitude();

        }

        LatLng loc = new LatLng(latitude, longitude);
        if (markername != null) {
            markername.remove();
        }
        markername = mMap.addMarker(new MarkerOptions().position(loc).title("This is Me").icon(bitmapDescriptorFromVector(getActivity(), R.drawable.people)));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latitude,longitude),13));


        if (location == null) {
            markername.remove();
        }


    }

    private BitmapDescriptor bitmapDescriptorFromVector(Context context, int vectorResId) {
        Drawable vectorDrawable = ContextCompat.getDrawable(context, vectorResId);
        vectorDrawable.setBounds(0, 0, vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight());
        Bitmap bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        vectorDrawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }


    private void addMapMarkers(){
        if(mMap != null){
            if(mClusterManager == null){
                mClusterManager = new ClusterManager<ClusterMarker>(getContext(), mMap);
            }
            if(mClusterManagerRenderer == null){
                mClusterManagerRenderer = new ClusterManagerRenderer(
                        getActivity(),
                        mMap,
                        mClusterManager
                );
                mClusterManager.setRenderer(mClusterManagerRenderer);
            }
            for(EventsModel eventLocation: list){
                try{
                    String snippet = "";
                    snippet = eventLocation.getDescription() + "-" + eventLocation.getDate() + " at " + eventLocation.getHours();
                    int avatar = R.drawable.ic_baseline_3p_24;
                    try {
                        //avatar = Integer.parseInt(eventLocation.getUser().getAvatar());
                    }catch (NumberFormatException e){
                        //Log.d(TAG, "addMapMarkers; no avatar for: " + eventLocation.getUser().getUsername() + ", setting default");
                    }


                    ClusterMarker newClusterMarker = new ClusterMarker(
                            new LatLng(eventLocation.getLocal().getLatitude(), eventLocation.getLocal().getLongitude() ),
                            eventLocation.getTitle(),
                            snippet,
                            avatar
                    );





                    mClusterManager.addItem(newClusterMarker);
                    mClusterMarkers.add(newClusterMarker);



                }catch (NullPointerException e){
                    Log.e(TAG, "addMapMarkers: NullPointerException: " + e.getMessage());
                }
            }

            mClusterManager.cluster();

        }
    }


    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        requestlocation();
        //LatLng sydney = new LatLng(38.756829, -9.155290);
    }

    /*
    private boolean checkMapServices() {
        if (isServicesOK()) {
            if (isMapsEnabled()) {
                return true;
            }
        }
        return false;
    }

    private void buildAlertMessageNoGps() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("This application requires GPS to work properly, do you want to enable it?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        Intent enableGpsIntent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        someActivityResultLauncher.launch(enableGpsIntent);
                    }
                });
        final AlertDialog alert = builder.create();
        alert.show();
    }

    public boolean isMapsEnabled() {
        final LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            buildAlertMessageNoGps();
            return false;
        }
        return true;
    }

    private void getLocationPermission() {
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mLocationPermissionGranted = true;
            //getChatrooms();
            //getUserDetails();
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
    }

    public boolean isServicesOK() {
        Log.d(TAG, "isServicesOK: checking google services version");

        int available = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(MainActivity.this);

        if (available == ConnectionResult.SUCCESS) {
            //everything is fine and the user can make map requests
            Log.d(TAG, "isServicesOK: Google Play Services is working");
            return true;
        } else if (GoogleApiAvailability.getInstance().isUserResolvableError(available)) {
            //an error occured but we can resolve it
            Log.d(TAG, "isServicesOK: an error occured but we can fix it");
            Dialog dialog = GoogleApiAvailability.getInstance().getErrorDialog(MainActivity.this, available, ERROR_DIALOG_REQUEST);
            dialog.show();
        } else {
            Toast.makeText(this, "You can't make map requests", Toast.LENGTH_SHORT).show();
        }
        return false;
    }


    ActivityResultLauncher<Intent> someActivityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
        if (result.getResultCode() == Activity.RESULT_OK) {
            Log.d(TAG, "onActivityResult: called.");
            switch (result.getResultCode()) {
                case PERMISSIONS_REQUEST_ENABLE_GPS: {
                    if (mLocationPermissionGranted) {
                        //getChatrooms();
                        //getUserDetails();
                    } else {
                        getLocationPermission();
                    }
                }
            }
        }
    });

     */
}