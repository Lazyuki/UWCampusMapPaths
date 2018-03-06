package yukiito.campusmap;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;


import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by yukiito on 12/8/17.
 */


// Custom adapter fot the building list views
public class BuildingsAdapter extends BaseAdapter {
    Context context; // App context
    LayoutInflater layoutInflater = null; // Used for filling the layout
    List<Building> buildings; // List of buildings
    Building start = null; // Starting building
    Building end = null; // Destination building
    ReentrantLock lock = new ReentrantLock(); // For multithreading

    /**
     * Constructor
     * @param context application context
     * @param buildings Set of Strings, as obtained by Campus.allBuildings.keySet()
     */
    public BuildingsAdapter(Context context, Set<String> buildings) {
        this.context = context;
        this.layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.buildings = new ArrayList<>();
        for (String b : buildings) {
            this.buildings.add(new Building(b));
        }
    }

    /**
     * sets the starting building
     * @param i position of the list item
     * @return Name of the new starting building if it exists. Else, null.
     */
    public String setStart(int i) {
        Building b = buildings.get(i);
        lock.lock();
        if (end != null && end.equals(b)) {
            lock.unlock();
            return start == null? null : start.name;
        } else if (start == null) {
            start = b;
            b.setStart();
            lock.unlock();
            return b.name;
        } else if (start.equals(b)) {
            start = null;
            b.setStart();
            lock.unlock();
            return null;
        } else {
            start.setStart();
            b.setStart();
            start = b;
            lock.unlock();
            return b.name;
        }
    }

    /**
     * sets the destination building
     * @param i position of the list item
     * @return Name of the new destination building if it exists. Else, null.
     */
    public String setEnd(int i) {
        Building b =  buildings.get(i);
        lock.lock();
        if (start != null && start.equals(b)) {
            lock.unlock();
            return end == null ? null : end.name;
        } else if (end == null) {
            end = b;
            b.setEnd();
            lock.unlock();
            return b.name;
        } else if (end.equals(b)) {
            end = null;
            b.setEnd();
            lock.unlock();
            return null;
        } else {
            end.setEnd();
            b.setEnd();
            end = b;
            lock.unlock();
            return b.name;
        }
    }

    /**
     *
     * @param startEnd for returning start and destination buildings names
     * @modifies startEnd
     * @effect startEnd[0] is the starting name, startEnd[1] is the destination name iff
     *         both exist.
     * @return whether or not there exists both start and end buildings.
     */
    public boolean getBuildings(String[] startEnd) {
        lock.lock();
        if (start != null && end != null) {
            startEnd[0] = start.name;
            startEnd[1] = end.name;
            lock.unlock();
            return true;
        }
        lock.unlock();
        return false;
    }

    /**
     * clears the selection
     */
    public void clear() {
        lock.lock();
        if (start != null) {
            start.setStart();
            start = null;
        }
        if (end != null) {
            end.setEnd();
            end = null;
        }
        lock.unlock();
    }

    @Override
    public int getCount() {
        return buildings.size();
    }

    @Override
    public Object getItem(int i) {
        return buildings.get(i);
    }

    @Override
    public long getItemId(int i) {
        return buildings.get(i).hashCode();
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        Building b = buildings.get(i);
        view = layoutInflater.inflate(R.layout.building, viewGroup, false);
        ((TextView) view.findViewById(R.id.buildingItem)).setText(b.name);
        if (b.start) {
            view.findViewById(R.id.buildingItem).setBackgroundColor(Color.rgb(153, 153, 255));
        } else if (b.end) {
            view.findViewById(R.id.buildingItem).setBackgroundColor(Color.rgb(255, 101, 101));
        } else {
            view.findViewById(R.id.buildingItem).setBackgroundColor(Color.rgb(255, 255, 255));
        }
        return view;
    }

    /**
     * class representing the building
     */
    private class Building {
        public final String name;
        public boolean start;
        public boolean end;

        /**
         * initialize with a name
         * @param shortname short name for the building
         */
        public Building(String shortname) {
            name = shortname;
            start = false;
            end = false;
        }

        /**
         * toggles the start boolean
         */
        public void setStart() {
            start = !start;
        }

        /**
         * toggles the end boolean
         */
        public void setEnd() {
            end = !end;
        }

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof Building)) {
                return false;
            }
            Building b = (Building) o;
            return b.name.equals(this.name);
        }
    }
}
