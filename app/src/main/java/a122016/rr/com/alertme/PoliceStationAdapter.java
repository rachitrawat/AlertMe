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

public class PoliceStationAdapter extends ArrayAdapter<PoliceStation> {

    /**
     * Create a new {@link PoliceStationAdapter} object.
     *
     * @param context is the current context (i.e. Activity) that the adapter is being created in.
     * @param PoliceStations   is the list of {@link PoliceStation}s to be displayed.
     */
    public PoliceStationAdapter(Context context, ArrayList<PoliceStation> PoliceStations) {
        super(context, 0, PoliceStations);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Check if an existing view is being reused, otherwise inflate the view
        View listItemView = convertView;
        if (listItemView == null) {
            listItemView = LayoutInflater.from(getContext()).inflate(
                    R.layout.listlayout2, parent, false);
        }

        // Get the {@link PoliceStation} object located at this position in the list
        PoliceStation currentPoliceStation = getItem(position);

        // Find the TextView in the list_item.xml layout with the ID miwok_text_view.
        TextView nameTextView = (TextView) listItemView.findViewById(R.id.nameps_text_view);
        // Get the Miwok translation from the currentPoliceStation object and set this text on
        // the Miwok TextView.
        nameTextView.setText(currentPoliceStation.getmName());

        // Find the TextView in the list_item.xml layout with the ID default_text_view.
        TextView numberTextView = (TextView) listItemView.findViewById(R.id.numberps_text_view);
        // Get the default translation from the currentPoliceStation object and set this text on
        // the default TextView.
        numberTextView.setText(currentPoliceStation.getmNumber());

        // Return the whole list item layout (containing 2 TextViews) so that it can be shown in
        // the ListView.
        return listItemView;
    }

}
