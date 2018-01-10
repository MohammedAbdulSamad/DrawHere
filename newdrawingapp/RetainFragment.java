package sam.newdrawingapp;

import android.app.Fragment;
import android.os.Bundle;

/**
 * Created by Sam on 12/21/2017.
 */

public class RetainFragment extends Fragment {
    private DrawingView dV;

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    public void setdV(DrawingView drawData) {
        this.dV = drawData;
    }

    public DrawingView getdV() {
        return dV;
    }
}
