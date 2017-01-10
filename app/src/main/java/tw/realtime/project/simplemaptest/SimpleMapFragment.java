package tw.realtime.project.simplemaptest;

import android.location.Location;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.model.MarkerOptions;

/**
 * Created by vexonelite on 2017/1/6.
 */

public class SimpleMapFragment extends BaseMapFragment {

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.base_fragment_simple_map, container, false);
        setUpMapFragment(R.id.google_map_container);

        View view = rootView.findViewById(R.id.myPosition);
        if (null != view) {
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    view.setEnabled(false);

                    //getCameraToSpecifiedLocation( locationToLatLng(getCurrentLocation() ) );
                    taggleMapTraffic();

                    view.setEnabled(true);
                }
            });
        }
        return rootView;
    }

//    @Override
//    protected void onUserLocationAvailable(Location userLocation) {
//        addMarkerByPosition(new MarkerOptionsBuilder()
//                .setMarkerPosition(locationToLatLng(userLocation))
//                .build() );
//    }
}
