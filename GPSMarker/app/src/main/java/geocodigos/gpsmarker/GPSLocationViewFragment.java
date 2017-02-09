package geocodigos.gpsmarker;

import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import geocodigos.gpsmarker.Conversion.CoordinateConversion;
import geocodigos.gpsmarker.Conversion.DMSConversion;
import geocodigos.gpsmarker.Dialogs.DialogAddPoint;
import geocodigos.gpsmarker.Fragments.ViewPoints;
import geocodigos.gpsmarker.Implementation.GetDate;
import geocodigos.gpsmarker.Implementation.GetTime;
import geocodigos.gpsmarker.Models.PointModel;
import geocodigos.gpsmarker.Utils.CoordinatesArray;


/**
 * A placeholder fragment containing a simple view.
 */
public class GPSLocationViewFragment extends Fragment implements LocationListener {

    private String strFormat = "%.5f";
    private int requests = 3000;
    private int min_distance=1;
    private Location location;
    private LocationManager locationManager;
    private String provider;
    private String strLatitude, strLongitude, strPrecisao, strAltitude, strDate, strTime;
    private ImageButton ibPoints, ibMark;
    private TextView tvLatitude, tvLongitude, tvPrecisao, tvAltitude,
            tvSetor, tvNorte, tvLeste, tvData, tvLatgms, tvLongms, tvGpsStatus;

    private double latitude, longitude, altitude, precisao;

    private final String keyLatitude = "lat";
    private final String keyLongitude = "lon";
    private final String keySector = "sec";
    private final String keyNorth = "north";
    private final String keyEast = "east";
    private final String keyPrecision ="prec";
    private final String keyAltitude = "alt";
    private final String keyDate = "date";
    private final String keyLatGms = "latgms";
    private final String keyLonGms = "longms";

    private View view;

    /**
     * Create View for the fragment
     * @param inflater
     * @param container
     * @param savedInstanceState
     * @return
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_gps, container, false);
        ibPoints = (ImageButton) view.findViewById(R.id.ibViewPoints);
        ibMark = (ImageButton) view.findViewById(R.id.ibmarcar);

        tvGpsStatus = (TextView) view.findViewById(R.id.in_status);
        tvAltitude = (TextView) view.findViewById(R.id.in_altitude);
        tvLongitude = (TextView) view.findViewById(R.id.in_longitude);
        tvLatitude = (TextView) view.findViewById(R.id.in_latitude);
        tvLeste = (TextView) view.findViewById(R.id.in_leste);
        tvNorte = (TextView) view.findViewById(R.id.in_norte);
        tvPrecisao = (TextView) view.findViewById(R.id.in_precisao);
        tvSetor = (TextView) view.findViewById(R.id.in_quadrante);
        tvLatgms = (TextView) view.findViewById(R.id.in_lat_gms);
        tvLongms = (TextView) view.findViewById(R.id.in_lon_gms);
        tvData  = (TextView) view.findViewById(R.id.tv_data);

        ibPoints.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ViewPoints ver_pontos = new ViewPoints();
                FragmentTransaction transaction = getFragmentManager().beginTransaction();
                transaction.replace(R.id.gps, ver_pontos);
                transaction.commit();
            }
        });

        ibMark.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (!tvLatitude.getText().toString().isEmpty()) {

                    if (!tvLongitude.getText().toString().isEmpty()) {
                        PointModel pm = new PointModel();
                        pm.setLatidude(tvLatitude.getText().toString());
                        pm.setLongitude(tvLongitude.getText().toString());
                        pm.setAltitude(tvAltitude.getText().toString());
                        pm.setPrecision(tvPrecisao.getText().toString());
                        pm.setNorth(tvNorte.getText().toString());
                        pm.setEast(tvLeste.getText().toString());
                        pm.setSector(tvSetor.getText().toString());
                        GetTime time = new GetTime();
                        String strTime = time.returnTime();
                        GetDate date = new GetDate();
                        String strDate = date.returnDate();
                        pm.setData(strDate);
                        pm.setTime(strTime);


                        DialogAddPoint dialog = new DialogAddPoint(getActivity(), pm);
                        AlertDialog.Builder alert = dialog.createAlertAdd(view);
                        alert.show();

                    } else {
                        Toast.makeText(getActivity(), R.string.nao_loc, Toast.LENGTH_SHORT).show();
                    }

                } else {
                    Toast.makeText(getActivity(), R.string.nao_loc, Toast.LENGTH_SHORT).show();
                }
            }

        });
        if(savedInstanceState!=null){
            if(savedInstanceState.containsKey(keyLatitude)){
                String str = savedInstanceState.getString(keyLatitude);
                tvLatitude.setText(str);
            }
            if(savedInstanceState.containsKey(keyLongitude)){
                String str = savedInstanceState.getString(keyLongitude);
                tvLongitude.setText(str);
            }
            if(savedInstanceState.containsKey(keySector)){
                String str = savedInstanceState.getString(keySector);
                tvSetor.setText(str);
            }
            if(savedInstanceState.containsKey(keyNorth)){
                String str = savedInstanceState.getString(keyNorth);
                tvNorte.setText(str);
            }
            if(savedInstanceState.containsKey(keyEast)){
                String str = savedInstanceState.getString(keyEast);
                tvLeste.setText(str);
            }
            if(savedInstanceState.containsKey(keyDate)){
                String str = savedInstanceState.getString(keyDate);
                tvData.setText(str);
            }
            if(savedInstanceState.containsKey(keyAltitude)){
                String str = savedInstanceState.getString(keyAltitude);
                tvAltitude.setText(str);
            }
            if(savedInstanceState.containsKey(keyPrecision)){
                String str = savedInstanceState.getString(keyPrecision);
                tvPrecisao.setText(str);
            }
            if(savedInstanceState.containsKey(keyLatGms)){
                String str = savedInstanceState.getString(keyLatGms);
                tvLatgms.setText(str);
            }
            if(savedInstanceState.containsKey(keyLonGms)){
                String str = savedInstanceState.getString(keyLonGms);
                tvLongms.setText(str);
            }

        }
        gpsStatus();
        return view;
    }

    /**
     * Get the GPS status and show to the user
     */
    public void gpsStatus() {
        locationManager = (LocationManager) getActivity().
                getSystemService(Context.LOCATION_SERVICE);

        provider = locationManager.GPS_PROVIDER;
        //locationManager.removeUpdates(this);
        location = locationManager.getLastKnownLocation(provider);
        if (location != null) {
            onLocationChanged(location);
        }
        if(locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            tvGpsStatus.setText(R.string.req_loc);
            tvGpsStatus.setTextColor(getResources().getColor(R.color.verde));
        } else {
            tvGpsStatus.setText(R.string.gps_desativado);
            tvGpsStatus.setTextColor(getResources().getColor(R.color.vermelho));
        }
        locationManager.requestLocationUpdates(provider, requests, min_distance, this);
    }

    /**
     * Add the GPS information to TextView on fragment layout
     */
    public void fillProperties(){
        if(latitude!=0 && longitude!=0) {
            DMSConversion dms = new DMSConversion();
            String sLat = dms.convertFromDegrees(latitude);
            String sLon = dms.convertFromDegrees(longitude);
            String coordLat[] = sLat.split(" ");
            String coordLon[] = sLon.split(" ");
            String norte = "N ";
            String leste = "E ";
            if (latitude < 0) {
                norte = "S ";
            }
            if (longitude < 0) {
                leste = "W ";
            }
            CoordinatesArray formater = new CoordinatesArray();
            strLatitude = formater.formatCoordinateToDMS(norte, coordLat[0], coordLat[1],coordLat[2]);
            strLongitude =formater.formatCoordinateToDMS(leste, coordLat[3], coordLat[4],coordLat[5]);
            strPrecisao = String.format("%.1f", precisao);
            strAltitude = String.format("%.1f", altitude);
            tvLatitude.setText(String.format(strFormat, latitude));
            tvLongitude.setText(String.format(strFormat, longitude));
            tvLatgms.setText(strLatitude);
            tvLongms.setText(strLongitude);
            tvPrecisao.setText(strPrecisao+" m");
            tvAltitude.setText(strAltitude+" m");
            tvData.setText(strTime + "     -     " + strDate);

            CoordinateConversion cc = new CoordinateConversion();
            String latlon = cc.latLon2UTM(latitude, longitude);
            String coord[] = latlon.split(" ");
            tvSetor.setText(coord[0] + " " + coord[1]);
            tvLeste.setText(coord[2]);
            tvNorte.setText(coord[3]);
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        latitude =  (location.getLatitude());
        longitude = (location.getLongitude());
        altitude =  (location.getAltitude());
        precisao = (double) (location.getAccuracy());
        GetTime time = new GetTime();
        strTime = time.returnTime();
        GetDate date = new GetDate();
        strDate = date.returnDate();
        fillProperties();
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onPause(){
        super.onPause();
        locationManager.removeUpdates(this);
    }

    @Override
    public void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
        locationManager.requestLocationUpdates(provider, requests, min_distance, this);
        fillProperties();
        gpsStatus();
    }

    @Override
    public void onProviderEnabled(String s) {
        tvGpsStatus.setText(R.string.req_loc);
        tvGpsStatus.setTextColor(getResources().getColor(R.color.verde));
    }

    @Override
    public void onProviderDisabled(String s) {
        tvGpsStatus.setText(R.string.gps_desativado);
        tvGpsStatus.setTextColor(getResources().getColor(R.color.vermelho));
    }
}
