package a122016.rr.com.alertme;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by rachitrawat on 1/5/2017.
 */

public class PlaceAdapter extends ArrayAdapter<Place> {

    /**
     * Create a new {@link PlaceAdapter} object.
     *
     * @param context is the current context (i.e. Activity) that the adapter is being created in.
     * @param Places   is the list of {@link Place}s to be displayed.
     */
    public PlaceAdapter(Context context, ArrayList<Place> Places) {
        super(context, 0, Places);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Check if an existing view is being reused, otherwise inflate the view
        View listItemView = convertView;
        if (listItemView == null) {
            listItemView = LayoutInflater.from(getContext()).inflate(
                    R.layout.listlayout, parent, false);
        }

        // Get the {@link Place} object located at this position in the list
        Place currentPlace = getItem(position);

        // Find the TextView in the list_item.xml layout with the ID miwok_text_view.
        TextView locationTextView = (TextView) listItemView.findViewById(R.id.location_text_view);
        // Get the Miwok translation from the currentPlace object and set this text on
        // the Miwok TextView.
        locationTextView.setText(currentPlace.getPlaceOfAccident());

        // Find the TextView in the list_item.xml layout with the ID default_text_view.
        TextView highwayTextView = (TextView) listItemView.findViewById(R.id.highway_text_view);
        // Get the default translation from the currentPlace object and set this text on
        // the default TextView.
        highwayTextView.setText(currentPlace.getHighwayNumber());

        // Find the TextView in the list_item.xml layout with the ID default_text_view.
        TextView fatalityTextView = (TextView) listItemView.findViewById(R.id.fatalities2016_text_view);
        // Get the default translation from the currentPlace object and set this text on
        // the default TextView.
        fatalityTextView.setText(currentPlace.getFatalties2016()+ " fatalities till July 2016");

//        TextView gmapTextView = (TextView) listItemView.findViewById(R.id.gmap_text_view);
//        // Get the default translation from the currentPlace object and set this text on
//        // the default TextView.
//        gmapTextView.setText(currentPlace.getLatitude()+ "/" + currentPlace.getLongitude());

        // Return the whole list item layout (containing 3 TextViews) so that it can be shown in
        // the ListView.
        return listItemView;
    }

}
