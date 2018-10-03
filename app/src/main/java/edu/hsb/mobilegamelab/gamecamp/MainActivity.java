package edu.hsb.mobilegamelab.gamecamp;

import android.app.ActionBar;
import android.app.Activity;
import android.graphics.drawable.ClipDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.things.pio.Gpio;
import com.google.android.things.pio.PeripheralManager;
import com.google.android.things.pio.SpiDevice;

import java.io.IOException;
import java.nio.Buffer;
import java.util.List;

import static java.lang.StrictMath.abs;

public class MainActivity extends Activity {
    private static final String SPI_DEVICE_NAME = "SPI0.0";
    private static final String TAG = "bla";
    private MCP3008 mMCP3008;
    private Handler mHandler;

    private ImageView batterie;
    private ClipDrawable clip_batterie;
    private ImageView termo;
    private ClipDrawable clip_termo;

    private Runnable mReadAdcRunnable = new Runnable() {

        private static final long DELAY_MS = 3000L; // 3 seconds

        @Override
        public void run() {
            if (mMCP3008 == null) {
                return;
            }
            int batterieWert = 1000;
            int termoWert = 1000;
            try {
                batterieWert = mMCP3008.readAdc(0x00)*10;
                termoWert = mMCP3008.readAdc(0x01)*10;
            } catch( IOException e ) {
                Log.e("MCP3008", "Something went wrong while reading from the ADC: " + e.getMessage());
            }

            clip_batterie.setLevel(batterieWert);
            clip_termo.setLevel(termoWert);
            mHandler.postDelayed(this, 10);
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);
        hideBar();

        batterie = findViewById(R.id.batterie_wert);
        clip_batterie = (ClipDrawable) batterie.getDrawable();
        termo = findViewById(R.id.termofill);
        clip_termo = (ClipDrawable) termo.getDrawable();


        try {
            mMCP3008 = new MCP3008("BCM12", "BCM21", "BCM16", "BCM20");
            mMCP3008.register();
        } catch( IOException e ) {
            Log.e("MCP3008", "MCP initialization exception occurred: " + e.getMessage());
        }

        mHandler = new Handler();
        mHandler.post(mReadAdcRunnable);

        PeripheralManager manager = PeripheralManager.getInstance();
        List<String> deviceList = manager.getSpiBusList();
        if (deviceList.isEmpty()) {
            Log.i(TAG, "No SPI bus available on this device.");
        } else {
            Log.i(TAG, "List of available devices: " + deviceList);
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if( mMCP3008 != null ) {
            mMCP3008.unregister();
        }

        if( mHandler != null ) {
            mHandler.removeCallbacks(mReadAdcRunnable);
        }
    }



    private void hideBar(){
        View decorView = getWindow().getDecorView();
// Hide the status bar.
        int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);
// Remember that you should never show the action bar if the
// status bar is hidden, so hide that too if necessary.
        ActionBar actionBar = getActionBar();
        actionBar.hide();
    }
}
