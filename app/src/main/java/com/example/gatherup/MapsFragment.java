package com.example.gatherup;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

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
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.maps.android.clustering.ClusterManager;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

public class MapsFragment extends Fragment implements OnMapReadyCallback, MainActivity.LocationCallback, SensorEventListener {

    private static final String TAG = "Teste";
    public static final int ERROR_DIALOG_REQUEST = 9001;
    public static final int PERMISSIONS_REQUEST_ENABLE_GPS = 9002;
    public static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 9003;

    private FirebaseFirestore store;
    private ArrayList<EventsModel> list;
    private ArrayList<EventsModel> shownevents;
    private ArrayList<EventsModel> filteredEvents;
    private GoogleMap mMap;
    private static final int REQUEST_CHECK_SETTINGS = 100;
    private boolean flagLocation = false;
    Marker markername;
    Button listaEventosButton;
    Button btnFiltros;
    FloatingActionButton focusMeButton;
    private Location location;
    private StorageReference storageReference;

    private ClusterManager<ClusterMarker> mClusterManager;
    private ClusterManagerRenderer mClusterManagerRenderer;
    private ArrayList<ClusterMarker> mClusterMarkers = new ArrayList<>();

    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private float mAccel;
    private float mAccelCurrent;
    private float mAccelLast;
    private long mLastShakeTimestamp;
    private static final int SHAKE_TIME_LAPSE = 8000;

    private String filter;

    private final OnMapReadyCallback callback = new OnMapReadyCallback() {
        @Override
        public void onMapReady(GoogleMap googleMap) {
            mMap = googleMap;
            if (location != null) {
                DisplayLocation();
            }
            //requestlocation();
        }
    };

    /*
    public void requestlocation() {
        checkLocationPermission();
        locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 10, 100, (LocationListener) this);

        location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        DisplayLocation(location);
    }

     */

    public void checkLocationPermission() { //TESTE verificar o que faz
        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 1001);
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_maps, container, false);
        setHasOptionsMenu(true);

        MainActivity activity = (MainActivity) getActivity();
        activity.setCallback(this);

        if(getArguments() != null){
            filter = getArguments().getString("filter");
        }
        //System.out.println("FILTRO: " + filter);

        mSensorManager = (SensorManager) getActivity().getSystemService(Context.SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        if (mMap != null) {
            mMap.clear();
        }
        list = new ArrayList<>();

        listaEventosButton = view.findViewById(R.id.ListButton);
        focusMeButton = view.findViewById(R.id.FocusMeButton);
        store = FirebaseFirestore.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference();

        listaEventosButton.setOnClickListener(view1 -> {
            Fragment eventListFragment = new EventListFragment();
            FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.MainFragment, eventListFragment).addToBackStack(null).commit();
        });

        try {
            location = activity.getCurrentLocation();
        }catch (Exception e){
        }

        focusMeButton.setOnClickListener(v -> {
            FocusMe();
        });

        Timestamp currentTime = Timestamp.now();

        store.collection("Events").whereGreaterThan("Date", currentTime).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        EventsModel eventsModel = document.toObject(EventsModel.class);
                        if (eventsModel.getPrivate()==false){
                            String eventID = document.getId();
                            eventsModel.setEventID(eventID);
                            list.add(eventsModel);
                        }
                    }
                } else {
                    Log.d(TAG, "Error getting documents: ", task.getException());
                }

                filteredEvents = new ArrayList<>();
                if(filter != null){
                    System.out.println(filter);
                    for(int i = 0; i < list.size(); i++){
                        if(filter.equals(list.get(i).getTheme())){
                            System.out.println(list.get(i).getTitle());
                            filteredEvents.add(list.get(i));
                        }
                    }
                }
                else{
                    filteredEvents = list;
                }
                addMapMarkers();
            }
        });


        btnFiltros = view.findViewById(R.id.FiltersButton);
        btnFiltros.setOnClickListener(view1 -> {
            FiltersFragment filtersFragment = new FiltersFragment();
            FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.MainFragment, filtersFragment).commit();
        });

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
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
    }

    @Override
    public void onLocationChanged(@NonNull Location currentLocation) {
        location = currentLocation;
        UpdateDistance();

        if (mMap != null && location != null) {
            DisplayLocation();
        }
    }

    /*
    @Override
    public void onLocationChanged(@NonNull Location location) {
        markername.remove();
        latitude = location.getLatitude();
        longitude = location.getLongitude();
        markername.remove();
        DisplayLocation(location);
    }

     */

    public void DisplayLocation() {
        LatLng loc = new LatLng(location.getLatitude(), location.getLongitude());
        if (markername != null) {
            markername.remove();
        }

        if (location != null) {
            try {
                markername = mMap.addMarker(new MarkerOptions().position(loc).title("This is Me").icon(bitmapDescriptorFromVector(getActivity(), R.drawable.people)).zIndex(1));
                if (!flagLocation) {
                    FocusMe();
                    flagLocation = true;
                }
            } catch (Exception e) {
                System.out.println("Errei " + e); //ERRO
            }
        }
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

    private void addMapMarkers() {
        if (mMap != null) {
            if (mClusterManager == null) {
                mClusterManager = new ClusterManager<ClusterMarker>(requireContext(), mMap); //getContext() <- requireContext()
            }
            if (mClusterManagerRenderer == null) {
                mClusterManagerRenderer = new ClusterManagerRenderer(
                        getActivity(),
                        mMap,
                        mClusterManager
                );
                mClusterManager.setRenderer(mClusterManagerRenderer);
            }
            for (EventsModel eventLocation : filteredEvents) {
                try {
                    String snippet = "";
                    int avatar = 0;
                    String date = toDate(eventLocation);
                    snippet = eventLocation.getDescription() + " - " + date;
                    if (eventLocation.getTheme().equals("Festa")) {
                        avatar = R.drawable.party;
                    } else if (eventLocation.getTheme().equals("Refeição")) {
                        avatar = R.drawable.food;
                    } else if (eventLocation.getTheme().equals("Desporto")) {
                        avatar = R.drawable.sports;
                    } else if (eventLocation.getTheme().equals("Convívio")) {
                        avatar = R.drawable.conviviality;
                    } else if (eventLocation.getTheme().equals("Outros")) {
                        avatar = R.drawable.other;
                    }
                    
                    //System.out.println(eventLocation.getTitle() + "---" + eventLocation.getDescription() + "---" + eventLocation.getEventID());
                    try {
                        //avatar = Integer.parseInt(eventLocation.getUser().getAvatar());
                    } catch (NumberFormatException e) {
                        //Log.d(TAG, "addMapMarkers; no avatar for: " + eventLocation.getUser().getUsername() + ", setting default");
                    }
                    ClusterMarker newClusterMarker = new ClusterMarker(
                            new LatLng(eventLocation.getLocal().getLatitude(), eventLocation.getLocal().getLongitude()),
                            eventLocation.getTitle(),
                            snippet,
                            avatar,
                            eventLocation.getEventID()
                    );

                    mClusterManager.addItem(newClusterMarker);
                    mClusterMarkers.add(newClusterMarker);

                } catch (NullPointerException e) {
                    Log.e(TAG, "addMapMarkers: NullPointerException: " + e.getMessage());
                }
            }

            mClusterManager.setOnClusterItemInfoWindowClickListener(new ClusterManager.OnClusterItemInfoWindowClickListener<ClusterMarker>() {
                @Override
                public void onClusterItemInfoWindowClick(ClusterMarker clusterMarker) {
                    String id = clusterMarker.getEventID();
                    EventSpecsFragment eventSpecsFragment = new EventSpecsFragment();
                    Bundle b = new Bundle();

                    b.putString("key", id);

                    eventSpecsFragment.setArguments(b);
                    FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
                    ft.replace(R.id.MainFragment, eventSpecsFragment).addToBackStack("teste").commit();
                }
            });
            mClusterManager.cluster();
        }
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

    private String toDate(EventsModel value) {
        Timestamp timestamp = value.getDate();
        SimpleDateFormat sfd = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        String time = sfd.format(timestamp.toDate());

        return (String) time;
    }

    private void FocusMe() {
        if (mMap != null && location != null) {
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), 13));
        } else {

        }
    }

    private String distance(Location location, GeoPoint local) {
        String distancia;
        float dist;
        android.location.Location location1 = new android.location.Location("provider");
        location1.setLatitude(location.getLatitude());
        location1.setLongitude(location.getLongitude());

        android.location.Location location2 = new android.location.Location("provider");
        location2.setLatitude(local.getLatitude());
        location2.setLongitude(local.getLongitude());
        dist = location1.distanceTo(location2);
        dist = Math.round(dist * 10) / 10.0f;
        distancia = dist + "M";
        if (dist > 999) {
            dist = dist / 1000;
            dist = Math.round(dist * 10) / 10.0f;
            distancia = dist + "Km";
        }
        return distancia;
    }

    private void UpdateDistance() {
        for (EventsModel eventsModel : list) {
            eventsModel.setDistance(distance(location, eventsModel.getLocal()));
        }
    }

    private void deletemarkers() {
        mMap.clear();
        DisplayLocation();
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        float x = event.values[0];
        float y = event.values[1];
        float z = event.values[2];
        mAccelLast = mAccelCurrent;
        mAccelCurrent = (float) Math.sqrt((double) (x * x + y * y + z * z));
        float delta = mAccelCurrent - mAccelLast;
        mAccel = mAccel * 0.9f + delta;
        if (mAccel > 18) {
            long currentTimestamp = System.currentTimeMillis();
            if (currentTimestamp - mLastShakeTimestamp > SHAKE_TIME_LAPSE) {
                mLastShakeTimestamp = currentTimestamp;
                Toast.makeText(getContext(), "Events within 3Km", Toast.LENGTH_SHORT).show();
                EventsShow();
            } else {
                Toast.makeText(getContext(), "Wait a few until shake again", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    public void onResume() {
        super.onResume();
        mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_UI);
    }

    @Override
    public void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(this);
    }

    private void EventsShow() {
        UpdateDistance();
        shownevents = new ArrayList<>();
        for (EventsModel eventsModel : filteredEvents) {
            String distance = eventsModel.getDistance();
            String[] parts = distance.split("[a-zA-Z]+");
            double distanceNumber = Double.parseDouble(parts[0]);
            String letter = distance.replace(parts[0], "").trim();
            if (letter.equals("Km")) {
                if (distanceNumber < 3) {
                    shownevents.add(eventsModel);
                }
            } else if (letter.equals("M")) {
                if (distanceNumber < 3000) {
                    shownevents.add(eventsModel);
                }
            }
        }
        deletemarkers();
        addMapMarkers();
        FocusMe();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

}