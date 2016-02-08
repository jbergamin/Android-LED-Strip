package com.jakebergmain.ledstrip;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.SeekBar;

import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link MainFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link MainFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MainFragment extends Fragment implements DiscoverTask.DiscoverCallback, SeekBar.OnSeekBarChangeListener {

    final String LOG_TAG = MainFragment.class.getSimpleName();

    private OnFragmentInteractionListener mListener;

    SeekBar redBar, greenBar, blueBar;


    public MainFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment MainFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static MainFragment newInstance() {
        MainFragment fragment = new MainFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {

        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        // seekbars
        redBar = (SeekBar) rootView.findViewById(R.id.seekBarRed);
        greenBar = (SeekBar) rootView.findViewById(R.id.seekBarGreen);
        blueBar = (SeekBar) rootView.findViewById(R.id.seekBarBlue);
        redBar.setOnSeekBarChangeListener(this);
        blueBar.setOnSeekBarChangeListener(this);
        greenBar.setOnSeekBarChangeListener(this);

        // test
        searchForDevices();

        return rootView;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }



    // methods for seek bar listener

    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser){
        // colors changed so send packet to led strip
        changeColor();
    }

    public void onStartTrackingTouch(SeekBar seekBar){

    }

    public void onStopTrackingTouch(SeekBar seekBar){

    }



    /**
     * method for Discover task callback
     */
    public void onFoundDevice(){
        // we found a LED strip!
        // what do we do now?

        // for debug only
        SharedPreferences preferences = getContext().getSharedPreferences(Constants.SHARED_PREFERENCES_NAME, 0);
        String ipString = preferences.getString(Constants.PREFERENCES_IP_ADDR, "");
        Log.v(LOG_TAG, "onDeviceFound() ipAddr: " + ipString);
    }


    /**
     * Start DiscoverTask to search for devices on local network.
     */
    public void searchForDevices(){
        Log.v(LOG_TAG, "starting DiscoverTask");
        new DiscoverTask(getContext(), this).execute(null, null);
    }

    /**
     * Gets color data from seekbars and starts ChangeColorTask
     * to send a packet and change the color of the LEDs
     */
    public void changeColor(){
        int r = redBar.getProgress();
        int g = greenBar.getProgress();
        int b = blueBar.getProgress();
        new ChangeColorTask(getContext()).execute(r, g, b);
    }


    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
