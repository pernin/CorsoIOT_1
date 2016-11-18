package it.univpm.pernini.corsoiot_1;

import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;
import java.util.UUID;

public class BTActivity extends AppCompatActivity {

    /**
     * 	TAG: per il debug
     */
    private final String TAG="BTApp Debug";

    /**
     * Edittext contenente il nome del dispositivo da cercare
     */
    EditText connDevET;

    /**
     * textview dovo mostro il valore ricevuto
     */
    TextView incomingTV;

    /**
     * Et per inserire la stringa da inviare
     */
    EditText tosendET;

    /**
     * Elementi per l'utilizzo del bluetooth
     */
    BluetoothAdapter mBluetoothAdapter;
    BluetoothSocket mmSocket;
    BluetoothDevice mmDevice;
    OutputStream mmOutputStream;
    InputStream mmInputStream;
    public String myDeviceName="HC-05N";

    /**
     * thread che si occupa della ricezione (come potete vedere sotto)
     */
    Thread workerThread;

    /**
     * valori ricevuti in forma di array di byte
     */
    byte[] readBuffer;

    /**
     * per scorrere nel buffer letto
     */
    int readBufferPosition;



    /**
     * Flag utile al thread che gestisce i dati ricevuti
     */
    volatile boolean stopWorker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bt);

        Button connectButton = (Button)findViewById(R.id.connectbutton);
        Button disconnectButton = (Button)findViewById(R.id.disconnectbutton);
        final Button sendButton = (Button)findViewById(R.id.sendbutton);

        connDevET = (EditText)findViewById(R.id.devnameet);

        incomingTV = (TextView)findViewById(R.id.rcvtexttv);
        tosendET = (EditText)findViewById(R.id.texttosendet);

        //Open Button
        connectButton.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                if (findBT()){
                    try {
                        openBT();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

            }
        });


        //Close button
        disconnectButton.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                try {
                    closeBT();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        //Send Button
        sendButton.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                try {
                    sendData();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

    }

    @Override
    protected void onPause() {
        super.onPause();
        try {
            closeBT();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * cerca il nostro dispositivo tra quelli accoppiati
     */
    boolean findBT()
    {
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if(mBluetoothAdapter == null)
        {
            showToast("No bluetooth adapter available");
            return false;
        }

        if(!mBluetoothAdapter.isEnabled())
        {
            Intent enableBluetooth = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBluetooth, 0);
        }

        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
        if(pairedDevices.size() > 0)
        {
            for(BluetoothDevice device : pairedDevices)
            {
                myDeviceName = connDevET.getText().toString();
                if(device.getName().equals(myDeviceName))
                {
                    mmDevice = device;
                    showToast("Bluetooth Device Found");
                    return true;
                }
            }
        }
        showToast("Bluetooth Device Not Found");
        return false;
    }

    /**
     * Apre la connessione bluetooth con il dispositivo
     * @throws IOException
     */
    void openBT() throws IOException
    {
        UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"); //Standard SerialPortService ID
        mmSocket = mmDevice.createRfcommSocketToServiceRecord(uuid);
        mmSocket.connect();
        mmOutputStream = mmSocket.getOutputStream();
        mmInputStream = mmSocket.getInputStream();

        beginListenForData();

        showToast("Bluetooth Opened");
    }



    /**
     * Thread che resta in ascolto di dati ricevuti
     */
    void beginListenForData()
    {
        final Handler handler = new Handler();
        final byte delimiter = 10; //This is the ASCII code for a newline character

        stopWorker = false;
        readBufferPosition = 0;
        readBuffer = new byte[1024];
        workerThread = new Thread(new Runnable()
        {
            public void run()
            {
                while(!Thread.currentThread().isInterrupted() && !stopWorker)
                {
                    try
                    {
                        int bytesAvailable = mmInputStream.available();

                        if(bytesAvailable > 0)
                        {
                            //Log.i(TAG,"bytes Available");
                            Log.i(TAG,"Bytes available: "+Integer.toString(bytesAvailable));
                            byte[] packetBytes = new byte[bytesAvailable];
                            mmInputStream.read(packetBytes);
                            for(int i=0;i<bytesAvailable;i++)
                            {
                                byte b = packetBytes[i];
                                Log.i(TAG,Byte.toString(b));
                                if(b == delimiter)
                                {
                                    Log.i(TAG,"reading");
                                    byte[] encodedBytes = new byte[readBufferPosition];


                                    System.arraycopy(readBuffer, 0, encodedBytes, 0, encodedBytes.length);

                                    final String data = new String(encodedBytes, "ISO-8859-1");
                                    Log.i(TAG,"data: "+data);

                                    readBufferPosition = 0;

                                    handler.post(new Runnable()
                                    {
                                        public void run()
                                        {
                                            showToast("Data Received: "+data);
                                            incomingTV.setText(data);
                                        }
                                    });
                                }
                                else
                                {
                                    readBuffer[readBufferPosition++] = b;
                                }
                            }
                        }
                    }
                    catch (IOException ex)
                    {
                        stopWorker = true;
                    }
                }
            }
        });

        workerThread.start();
    }

    void sendData() throws IOException
    {
        String msg = tosendET.getText().toString();
        msg += "\n";
        showToast("Sending "+msg);
        mmOutputStream.write(msg.getBytes());
        showToast("Data Sent");
    }

    /**
     * chiude la connessione bluetooth
     * @throws IOException
     */
    void closeBT() throws IOException
    {
        stopWorker = true;
        mmOutputStream.close();
        mmInputStream.close();
        mmSocket.close();
        showToast("Bluetooth Closed");



    }

    /**
     * Mostra un Toast sullo schermo
     * @param msg
     */
    public void showToast(String msg){
        Log.i(TAG,"showToast(): "+msg);
        Toast t=Toast.makeText(this, msg, Toast.LENGTH_SHORT);
        t.show();
    }

}
