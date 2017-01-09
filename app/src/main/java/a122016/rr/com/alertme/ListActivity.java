package a122016.rr.com.alertme;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ListView;

public class ListActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);
        Intent mIntent = getIntent();
        int intValue = mIntent.getIntExtra("intVariableName", 0);

        if (intValue == 1) {
            PlaceAdapter adapter = new PlaceAdapter(this, MainActivity.getArrayList());
            ListView listView = (ListView) findViewById(R.id.list);
            listView.setAdapter(adapter);
        } else {
            PoliceStationAdapter adapter = new PoliceStationAdapter(this, MainActivity.getArrayListPS());
            ListView listView = (ListView) findViewById(R.id.list);
            listView.setAdapter(adapter);
        }
    }
}