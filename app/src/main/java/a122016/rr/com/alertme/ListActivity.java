package a122016.rr.com.alertme;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ListView;

public class ListActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);
         PlaceAdapter adapter = new PlaceAdapter(this, MainActivity.getArrayList());
         ListView listView = (ListView) findViewById(R.id.list);
         listView.setAdapter(adapter);

    }


}
