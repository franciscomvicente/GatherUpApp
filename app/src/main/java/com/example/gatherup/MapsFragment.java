package com.example.gatherup;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.net.Uri;
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
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.maps.android.clustering.ClusterManager;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

public class MapsFragment extends Fragment implements /*LocationListener, */ OnMapReadyCallback, MainActivity.LocationCallback {

    private static final String TAG = "Teste";
    public static final int ERROR_DIALOG_REQUEST = 9001;
    public static final int PERMISSIONS_REQUEST_ENABLE_GPS = 9002;
    public static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 9003;

    private FirebaseFirestore store;
    private ArrayList<EventsModel> list = new ArrayList<>();

    private GoogleMap mMap;
    private static final int REQUEST_CHECK_SETTINGS = 100;
    public Double latitude;
    public Double longitude;
    private boolean flagLocation = false;
    Marker markername;
    Button listaEventosButton;
    FloatingActionButton focusMeButton;
    private Location location;
    private StorageReference storageReference;

    private ClusterManager<ClusterMarker> mClusterManager;
    private ClusterManagerRenderer mClusterManagerRenderer;
    private ArrayList<ClusterMarker> mClusterMarkers = new ArrayList<>();

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

        listaEventosButton = view.findViewById(R.id.ListButton);
        focusMeButton = view.findViewById(R.id.FocusMeButton);
        store = FirebaseFirestore.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference();

        listaEventosButton.setOnClickListener(view1 -> {
            Fragment eventListFragment = new EventListFragment();

            Bundle bundle = new Bundle();
            bundle.putParcelable("last_known_location", location);
            eventListFragment.setArguments(bundle);

            FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.MainFragment, eventListFragment).addToBackStack(null).commit();
        });

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
                        String eventID = document.getId();
                        eventsModel.setEventID(eventID);
                        list.add(eventsModel);
                    }
                } else {
                    Log.d(TAG, "Error getting documents: ", task.getException());
                }
                addMapMarkers();
            }
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
        if (location != null) {
            latitude = location.getLatitude();
            longitude = location.getLongitude();
            Log.d("MapFragment", "Latitude: " + latitude + " Longitude: " + longitude);
        } else {
            Log.d("MapFragment", "Location not available");
        }
        if (mMap != null) {
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
        LatLng loc = new LatLng(latitude, longitude);
        if (markername != null) {
            markername.remove();
        }

        if (latitude != null && longitude != null) {
            try {
                markername = mMap.addMarker(new MarkerOptions().position(loc).title("This is Me").icon(bitmapDescriptorFromVector(getActivity(), R.drawable.people)));
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
            for (EventsModel eventLocation : list) {
                try {
                    String snippet = "";
                    Uri avatar = null;
                    String date = toDate(eventLocation);
                    snippet = eventLocation.getDescription() + " - " + date;

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
        if (mMap != null && latitude != null && longitude != null) {
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latitude, longitude), 13));
        } else {

        }
    }
}