package yukiito.campusmap;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.style.BulletSpan;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

import hw8.*;

public class MainActivity extends AppCompatActivity {

    MapDrawView map;
    Campus campus = null;
    BuildingsAdapter adapter = null; // Custom list view adapter
    ListView buildings1;
    ListView buildings2;
    ReentrantLock loading = new ReentrantLock(); // for multi threading

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        map = (MapDrawView) findViewById(R.id.mapImage);
        new initiateThread().start();

        Button search = (Button) findViewById(R.id.search);
        Button reset = (Button) findViewById(R.id.reset);

        search.setOnClickListener(searchPaths);
        reset.setOnClickListener(resetMarks);
    }

    /**
     * A thread that initializes campus map. It's on a separate thread because parsing the campus
     * data takes forever and it doesn't load the first screen if it's on onCreate().
     */
    private class initiateThread extends Thread {
        public void run() {
            loading.lock();
            buildings1 = (ListView) findViewById(R.id.buildingStart);
            buildings2 = (ListView) findViewById(R.id.buildingEnd);
            campus = parseData();
            adapter = new BuildingsAdapter(getApplicationContext(), campus.allBuildings().keySet());
            buildings1.setOnItemClickListener(building1click);
            buildings2.setOnItemClickListener(building2click);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    findViewById(R.id.progressBar).setVisibility(View.GONE);
                    buildings1.setAdapter(adapter);
                    buildings2.setAdapter(adapter);
                }
            });
            loading.unlock();
        }
    }

    /**
     * parses campus map
     * @return Campus object to interact with
     */
    private Campus parseData() {
        InputStream buildIS = this.getResources().openRawResource(R.raw.campus_buildings);
        InputStream pathsIS = this.getResources().openRawResource(R.raw.campus_paths);

        Campus cg;
        try {
            cg = new Campus(buildIS, pathsIS);
        } catch (MapParser.MalformedDataException e) {
            System.out.println("There was an error with the file format.");
            return null;
        }
        return cg;
    }

    /**
     * For calculating the shortest path. Again, it's on a separate thread to have a responsive UI.
     */
    private class drawThread extends Thread {
        private final String start;
        private final String end;

        public drawThread(String start, String end) {
            this.start = start;
            this.end = end;
        }
        public void run() {
            loading.lock();
            List<Coordinate> coords = new ArrayList<>();
            List<Double> weights = new ArrayList<>();
            List<Float> floats = new ArrayList<>();
            if (campus.shortestPath(start, end, coords, weights) == 0)
                return;

            final float[] points = new float[(coords.size() * 2 - 2) * 2];
            for (int i = 1; i < coords.size(); i++) {
                Coordinate c1 = coords.get(i - 1);
                Coordinate c2 = coords.get(i);

                floats.add((float) c1.x);
                floats.add((float) c1.y);
                floats.add((float) c2.x);
                floats.add((float) c2.y);
            }
            for (int i = 0; i < floats.size(); i++) {
                points[i] = floats.get(i);
            }
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    map.setPaths(points);
                }
            });
            loading.unlock();
        }
    }

    /**
     * listens to the search button.
     */
    private View.OnClickListener searchPaths = new View.OnClickListener() {
        public void onClick(View v) {
            if (!loading.isLocked()) {
                String[] startEnd = new String[2];
                if (adapter.getBuildings(startEnd)) {
                    new drawThread(startEnd[0], startEnd[1]).start();
                }
            }
        }
    };

    /**
     * listens to the reset button
     */
    private View.OnClickListener resetMarks = new View.OnClickListener() {
        public void onClick(View v) {
            if (!loading.isLocked()) {
                map.clear();
                adapter.clear();
                adapter.notifyDataSetChanged();
            }
        }
    };


    /**
     * listens to the start building selection list
     */
    private ListView.OnItemClickListener building1click = new ListView.OnItemClickListener() {
        public void onItemClick(AdapterView<?> adapter, View v, int position, long id) {
            BuildingsAdapter ba = (BuildingsAdapter) adapter.getAdapter();
            String name = ba.setStart(position);
            ba.notifyDataSetChanged();
            if (name != null) {
                Coordinate c = campus.getCoordinate(name);
                map.setStart(new float[]{(float) c.x, (float) c.y});
            } else {
                map.setStart(null);
            }
        }
    };

    /**
     * listens to the destination building selection list
     */
    private ListView.OnItemClickListener building2click = new ListView.OnItemClickListener() {
        public void onItemClick(AdapterView<?> adapter, View v, int position, long id) {
            BuildingsAdapter ba = (BuildingsAdapter) adapter.getAdapter();
            String name = ba.setEnd(position);
            ba.notifyDataSetChanged();
            if (name != null) {
                Coordinate c = campus.getCoordinate(name);
                map.setEnd(new float[]{(float) c.x, (float) c.y});
            } else {
                map.setEnd(null);
            }
        }
    };
}
