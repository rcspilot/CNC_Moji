package com.example.android.cnc_moji;

import android.app.Dialog;
import android.os.Environment;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.os.AsyncTask;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.UUID;

import android.content.ActivityNotFoundException;
import android.net.Uri;
import android.view.View.OnClickListener;

import com.example.android.led_moji.R;


public class MainActivity extends ActionBarActivity {

    Button BTclearSerial, BTstripes, BTdisconnect, BTsend, BTshiftUp;
    Button mHome, mUp, mDown, mLeft, mRight, mMachineStart, mStartAxis, mdelaySet;
    Button myOpenFileButton;
    SeekBar brightness;
    TextView lumn;
    EditText BeeDelay;
    String pathFileTemp;

    private static final String TAG = "MainActivity";
    private static final int REQUEST_CODE = 6384; // onActivityResult request
    // code


    String address = null;
    Integer delayValFinal = 500;
    private ProgressDialog progress;
    BluetoothAdapter myBluetooth = null;
    BluetoothSocket btSocket = null;
    private boolean isBtConnected = false;
    //SPP UUID. Look for it
    static final UUID myUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        Intent newint = getIntent();
        address = newint.getStringExtra(DeviceList.EXTRA_ADDRESS); //receive the address of the bluetooth device

        //view of the ledControl
        setContentView(R.layout.activity_main);

        //========================================BEE EDIT ====================================

        //call the widgtes
        BTclearSerial = (Button)findViewById(R.id.button2);
        BTstripes = (Button)findViewById(R.id.button3);
        BTdisconnect = (Button)findViewById(R.id.DisconnectBT);
        brightness = (SeekBar)findViewById(R.id.seekBar);
        lumn = (TextView)findViewById(R.id.lumn);
        BTsend = (Button)findViewById(R.id.BTsend);
        BTshiftUp = (Button)findViewById(R.id.button5);

        final Switch toggle = (Switch) findViewById(R.id.sswitch1);
        final Switch manualOv = (Switch) findViewById(R.id.sswitch2);
        final Switch delayOv = (Switch) findViewById(R.id.sswitch3);

        // call manual override button
        mHome = (Button) findViewById(R.id.mHome);
        mUp = (Button) findViewById(R.id.mUp);
        mDown = (Button) findViewById(R.id.mDown);
        mLeft = (Button) findViewById(R.id.mLeft);
        mRight = (Button) findViewById(R.id.mRight);
        mMachineStart = (Button) findViewById(R.id.mMachineStart);
        mStartAxis = (Button) findViewById(R.id.mStartAxis);
        mdelaySet = (Button) findViewById(R.id.msetDelay);
        BeeDelay = (EditText)findViewById(R.id.myInputDelay);
        myOpenFileButton = (Button) findViewById(R.id.openFile) ;


        toggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    BTclearSerial.setVisibility(View.VISIBLE);
                    BTstripes.setVisibility(View.VISIBLE);
                    BTshiftUp.setVisibility(View.VISIBLE);
                    delayOv.setVisibility(View.VISIBLE);
                    manualOv.setChecked(false);
                } else {
                    BTclearSerial.setVisibility(View.INVISIBLE);
                    BTstripes.setVisibility(View.INVISIBLE);
                    BTshiftUp.setVisibility(View.INVISIBLE);
                    delayOv.setChecked(false);
                    delayOv.setVisibility(View.INVISIBLE);
                }
            }
        });

        manualOv.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    mHome.setVisibility(View.VISIBLE);
                    mUp.setVisibility(View.VISIBLE);
                    mDown.setVisibility(View.VISIBLE);
                    mLeft.setVisibility(View.VISIBLE);
                    mRight.setVisibility(View.VISIBLE);
                    mMachineStart.setVisibility(View.VISIBLE);
                    mStartAxis.setVisibility(View.VISIBLE);
                    toggle.setChecked(false);
                } else {
                    mHome.setVisibility(View.INVISIBLE);
                    mUp.setVisibility(View.INVISIBLE);
                    mDown.setVisibility(View.INVISIBLE);
                    mLeft.setVisibility(View.INVISIBLE);
                    mRight.setVisibility(View.INVISIBLE);
                    mMachineStart.setVisibility(View.INVISIBLE);
                    mStartAxis.setVisibility(View.INVISIBLE);
                }
            }
        });
        delayOv.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {

                    mdelaySet.setVisibility(View.VISIBLE);
                    BeeDelay.setVisibility(View.VISIBLE);

                } else {
                    mdelaySet.setVisibility(View.INVISIBLE);
                    BeeDelay.setVisibility(View.INVISIBLE);
                }
            }
        });

    //========================================BEE EDIT ====================================
    new ConnectBT().execute(); //Call the class to connect
    //commands to be sent to bluetooth

        // ==============================  LISTENER START ==============================

       myOpenFileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               showChooser();      //method to turn on
            }
        });
       BeeDelay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BTclearSerial.setVisibility(View.GONE);
                BTstripes.setVisibility(View.GONE);
                BTshiftUp.setVisibility(View.GONE);
            }
        });
        BTclearSerial.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clearSerialScreen();      //method to turn on
            }
        });
        BTstripes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                manualCmd("STRIPES");   //method to turn off
            }
        });

        BTshiftUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                manualCmd("SHIFTUP");   //method to turn off
            }
        });
        Button btnDialog = (Button)findViewById(R.id.BTsend);
        BTsend.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                final Dialog dialog = new Dialog(MainActivity.this);
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialog.setContentView(R.layout.customdialog);
                dialog.setCancelable(true);

                Button button1 = (Button)dialog.findViewById(R.id.button1);
                button1.setOnClickListener(new OnClickListener() {
                    public void onClick(View v) {
                        Toast.makeText(getApplicationContext()
                                , "Close dialog", Toast.LENGTH_SHORT);
                        dialog.cancel();
                    }
                });

                TextView textView1 = (TextView)dialog.findViewById(R.id.textView1);
                textView1.setText("BEEE");
                TextView textView2 = (TextView)dialog.findViewById(R.id.textView2);
                textView2.setText("Testing 1 2 3");

                dialog.show();
                // ============ end popup view V.2 =============
                TextView tv2 =  (TextView)findViewById(R.id.mojiTV2);
                tv2.setText("");
                sendBTData();   //method to turn off
            }
        });
        BTdisconnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Disconnect(); //close connection
            }
        });

        mHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                manualCmd("HOME");      //method to turn on
            }
        });
        mUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                manualCmd("UP");      //method to turn on
            }
        });
        mDown.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                manualCmd("DOWN");      //method to turn on
            }
        });
        mLeft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                manualCmd("LEFT");      //method to turn on
            }
        });
        mRight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                manualCmd("RIGHT");      //method to turn on
            }
        });
        mMachineStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                manualCmd("MACHINESTART");      //method to turn on
            }
        });
        mStartAxis.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                manualCmd("STARTAXIS");      //method to turn on
            }
        });

        mdelaySet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                delaySet();      //method to turn on
            }
        });


        //============================== LISTENER END ==============================
    }

    private void delaySet()
    {
        EditText inputDelay = (EditText)findViewById(R.id.myInputDelay);
        Integer delayInputVal = Integer.parseInt(inputDelay.getText().toString());
        if (TextUtils.isEmpty(inputDelay.getText().toString())) {
            Toast.makeText(this, "You did not enter a username", Toast.LENGTH_SHORT).show();
            return;
        }
        else {
            Toast.makeText(getBaseContext(),
                    "Delay set: " + delayInputVal + "ms", Toast.LENGTH_LONG).show();

            delayValFinal = delayInputVal;
            Log.e("BEEEE", delayValFinal.toString());
        }
    }

    private void Disconnect()
    {
        if (btSocket!=null) //If the btSocket is busy
        {
            try
            {
                btSocket.close(); //close connection
            }
            catch (IOException e)
            { msg("Error");}
        }
        finish(); //return to the first layout
    }

    private void BTsendText(String y){
        if (btSocket!=null) {
            try {
                btSocket.getOutputStream().write(y.toString().getBytes());
            }
            catch (IOException e)
            { msg("Error");}
        }
    }
    private void manualCmd(String x)
    {
        switch (x){
            case "HOME":
                BTsendText("G0 X0 Y0 Z0\n");
                BTsendText("G0\n");
                break;
            case "UP":
                BTsendText("G91 Y5\n");
                BTsendText("G0\n");
                break;
            case "DOWN":
                BTsendText("G91 Y-5\n");
                BTsendText("G0\n");
                break;
            case "LEFT":
                BTsendText("G91 X-5\n");
                BTsendText("G0\n");
                break;
            case "RIGHT":
                BTsendText("G91 X5\n");
                BTsendText("G0\n");
                break;
            case "STRIPES":
                BTsendText(" ================================ \n");
                break;
            case "SHIFTUP":
                BTsendText("  \n");
                break;
            case "MACHINESTART":
                BTsendText("$G\n");
                BTsendText("$$\n");
                break;
            case "STARTAXIS":
                BTsendText("$X\n");
                break;
        }
    }

    public void timeDelay(long t) {
        try {
            Thread.sleep(t);
            final Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    //Do something after 100ms
                    TextView tv2 =  (TextView)findViewById(R.id.mojiTV2);
                    //tv2.append("OK\n");
                }
            }, t);
        } catch (InterruptedException e) {}

//        final Handler handler = new Handler();
//        handler.postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                // Do something after 5s = 5000ms
//                //buttons[inew][jnew].setBackgroundColor(Color.Red);
//            }
//        }, t);
    }

    private void sendBTData() {
        if (btSocket!=null) {
            try {
                File sdcard = Environment.getExternalStorageDirectory();
                Log.e("FILE LOCATIO",sdcard.toString());
                Log.e("BEEEEEEEEE",Environment.getExternalStorageDirectory().toString());

                //Get the text file
                File file = new File(sdcard,"moji.txt");

                //Read text from file
                StringBuilder text = new StringBuilder();

                    BufferedReader br = new BufferedReader(new FileReader(pathFileTemp));
                    String line;
                    Log.e("myfileTest",file.toString());
                    Log.e("myBrTest",br.toString());
                    text.append("moji.txt");

                    while ((line = br.readLine()) != null) {
                        text.append(line);
                        btSocket.getOutputStream().write(line.toString().getBytes());
                        Log.e("BEEEEEEE",line);
                        text.append('\n');
                        BTsendText("\n");

                       // new myAsyncUpdate().execute(); //Execute myAsyncUpdate
                       // btSocket.getOutputStream().write("\n".toString().getBytes());
                       // timeDelay(delayValFinal);
                        TextView tv2 =  (TextView)findViewById(R.id.mojiTV2);
                        tv2.append("OK\n");
                        Log.e("I AM HERE","PASSING ME FUNC");


                    }

                    br.close();
            }
            catch (IOException e) {
                msg("Error");
            }
        }
    }

    private void clearSerialScreen() {
        if (btSocket!=null) {
            try {
                TextView tv2 =  (TextView)findViewById(R.id.mojiTV2);
                tv2.setText("");
                for (int i=0; i<40; i++){

                    btSocket.getOutputStream().write("\n".toString().getBytes());

                }
            }
            catch (IOException e) {
                msg("Error");
            }
        }
    }

    private void showChooser() {
        // Use the GET_CONTENT intent from the utility class
        Intent target = FileUtils.createGetContentIntent();
        // Create the chooser Intent
        Intent intent = Intent.createChooser(
                target, getString(R.string.chooser_title));
        try {
            startActivityForResult(intent, REQUEST_CODE);
        } catch (ActivityNotFoundException e) {
            // The reason for the existence of aFileChooser
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_CODE:
                // If the file selection was successful
                if (resultCode == RESULT_OK) {
                    if (data != null) {
                        // Get the URI of the selected file
                        final Uri uri = data.getData();
                        Log.i(TAG, "Uri = " + uri.toString());
                        try {
                            // Get the file path from the URI
                            final String path = FileUtils.getPath(this, uri);
                            Toast.makeText(MainActivity.this,
                                    "File Selected: " + path, Toast.LENGTH_LONG).show();
                            Log.e("BEEEEE",path);
                            pathFileTemp = path;

                          //=================================== BEE EDIT ===================================
                            File sdcard = Environment.getExternalStorageDirectory();
                            Log.e("FILE LOCATIO",sdcard.toString());
                            Log.e("BEEEEEEEEE",Environment.getExternalStorageDirectory().toString());

                            //Get the text file
                            File file = new File(sdcard,"moji.txt");

                            //Read text from file
                            StringBuilder text = new StringBuilder();

                            try {
                                BufferedReader br = new BufferedReader(new FileReader(path));
                                String line;

                                while ((line = br.readLine()) != null) {
                                    text.append(line);
                                    text.append('\n');
                                }
                               // timeDelay(delayValFinal);
                                br.close();
                            }
                            catch (IOException e) {
                                //You'll need to add proper error handling here
                            }
                            //Find the view by its id
                            TextView tv = (TextView)findViewById(R.id.mojiTV1);
                            tv.setMovementMethod(new ScrollingMovementMethod()); // Activate mojiTV1 TextView ScrollBar

                            //Set the text
                            tv.setText(text.toString());
                            BTsend.setVisibility(View.VISIBLE); /** Show BTsend button when selected a file **/
                            //=================================== BEE EDIT ===================================

                        } catch (Exception e) {
                            Log.e("FileSelectorTest", "File select error", e);

                        }
                    }
                }
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void msg(String s)
    {
        Toast.makeText(getApplicationContext(),s,Toast.LENGTH_LONG).show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_led_control, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private class ConnectBT extends AsyncTask<Void, Void, Void>  // UI thread
    {
        private boolean ConnectSuccess = true; //if it's here, it's almost connected

        @Override
        protected void onPreExecute()
        {
            progress = ProgressDialog.show(MainActivity.this, "Connecting...", "Please wait!!!");  //show a progress dialog
        }

        @Override
        protected Void doInBackground(Void... devices) //while the progress dialog is shown, the connection is done in background
        {
            try
            {
                if (btSocket == null || !isBtConnected)
                {
                    myBluetooth = BluetoothAdapter.getDefaultAdapter();//get the mobile bluetooth device
                    BluetoothDevice dispositivo = myBluetooth.getRemoteDevice(address);//connects to the device's address and checks if it's available
                    btSocket = dispositivo.createInsecureRfcommSocketToServiceRecord(myUUID);//create a RFCOMM (SPP) connection
                    BluetoothAdapter.getDefaultAdapter().cancelDiscovery();
                    btSocket.connect();//start connection
                }
            }
            catch (IOException e)
            {
                ConnectSuccess = false;//if the try failed, you can check the exception here
            }
            return null;
        }
        @Override
        protected void onPostExecute(Void result) //after the doInBackground, it checks if everything went fine
        {
            super.onPostExecute(result);

            if (!ConnectSuccess)
            {
                msg("Connection Failed. Is it a SPP Bluetooth? Try again.");
                finish();
            }
            else
            {
                msg("Connected.");
                isBtConnected = true;
            }
            progress.dismiss();
        }
    }
}
