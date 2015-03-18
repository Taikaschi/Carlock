package schmallcoders.carlock;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.IOException;
import java.io.OutputStream;
import java.util.UUID;

public class MainActivity extends Activity {
    private static final String TAG = "Carlock";

    Button car_open_btn, door_open_btn, door_close_btn;
    Button car_close_btn, back_open_btn, back_close_btn;

    private static final int REQUEST_ENABLE_BT = 1;
    private BluetoothAdapter btAdapter = null;
    private BluetoothSocket btSocket = null;
    private OutputStream outStream = null;

    // Well known SPP UUID
    private static final UUID MY_UUID =
            UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    // Bluetooth MAC Addresse meines Smartphones
    private static String address = "00:14:01:21:31:29";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "In onCreate()");

        setContentView(R.layout.activity_main);
        // Buttons initialisieren
        car_open_btn = (Button) findViewById(R.id.car_open_button);     // Auto oeffnen
        car_close_btn = (Button) findViewById(R.id.car_close_button);   // Auto schliessen
        door_open_btn = (Button) findViewById(R.id.door_open_button);   // Tueren oeffnen
        door_close_btn = (Button) findViewById(R.id.door_close_button); // Tueren schliessen
        back_open_btn = (Button) findViewById(R.id.back_open_button);   // Kofferraum oeffnen
        back_close_btn = (Button) findViewById(R.id.back_close_button); // Kofferaum schliessen


        btAdapter = BluetoothAdapter.getDefaultAdapter();
        checkBTState(); // Prüfung ob Bluetooth unterstützt wird und ob eingeschaltet
        // OnClickListener für jeden Button anlegen
        car_open_btn.setOnClickListener(new View.OnClickListener() {    // Auto oeffnen
            public void onClick(View v) {
                sendData("1");
                }
        });
        door_open_btn.setOnClickListener(new View.OnClickListener() {   // Tueren oeffnen
            public void onClick(View v) {
                sendData("2");
                }
        });
        door_close_btn.setOnClickListener(new View.OnClickListener() {  // Tueren schliessen
            public void onClick(View v) {
                sendData("3");
                }
        });
        car_close_btn.setOnClickListener(new View.OnClickListener() {   // Auto schliessen
            public void onClick(View v) {
                sendData("4");
            }
        });
        back_open_btn.setOnClickListener(new View.OnClickListener() {   // Kofferraum oeffnen
            public void onClick(View v) {
                sendData("5");
                }
        });
        back_close_btn.setOnClickListener(new View.OnClickListener() {  // Kofferaum schliessen
            public void onClick(View v) {
                sendData("6");
                }
        });

    }

    @Override
    public void onResume() {
        super.onResume();

        Log.d(TAG, "...In onResume - Client versucht zu verbinden...");

        // Bluetooth Device anlegen mit Hilfe seiner Adresse und den Bluetooth Adapter
        BluetoothDevice device = btAdapter.getRemoteDevice(address);

        try {
            btSocket = device.createRfcommSocketToServiceRecord(MY_UUID);
        } catch (IOException e) {
            errorExit("Fatal Error", "In onResume() erstellen des Sockets fehlgeschlagen: " + e.getMessage() + ".");
        }

        // Suche nach Bluetoothgeräten abbrechen bevor verbunden wird
        btAdapter.cancelDiscovery();

        // Verbindung herstellen
        Log.d(TAG, "...Verbinde...");
        try {
            btSocket.connect();
            Log.d(TAG, "...Verbindung hergestellt und Datenverbindung geöffnet...");
        } catch (IOException e) {
            try {
                btSocket.close();
            } catch (IOException e2) {
                errorExit("Fatal Error", "In onResume() Socket konnte während Verbindungsaufbau nicht geschlossen werden" + e2.getMessage() + ".");
            }
        }

        // Datenstream herstellen.
        Log.d(TAG, "...Creating Socket...");

        try {
            outStream = btSocket.getOutputStream();
        } catch (IOException e) {
            errorExit("Fatal Error", "In onResume() herstellen des Outputstream fehgeschlagen:" + e.getMessage() + ".");
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        Log.d(TAG, "...In onPause()...");

        if (outStream != null) {
            try {
                outStream.flush();
            } catch (IOException e) {
                errorExit("Fatal Error", "In onPause() Outputstream konnte nicht geleert werden: " + e.getMessage() + ".");
            }
        }

        try     {
            btSocket.close();
        } catch (IOException e2) {
            errorExit("Fatal Error", "In onPause() Socket konnte nicht geschlossen werden." + e2.getMessage() + ".");
        }
    }
    private void checkBTState() {
        // Prüfung ob Bluetooth unterstützt wird und ob eingeschaltet
        if(btAdapter==null) {
            errorExit("Fatal Error", "Bluetooth Not supported. Aborting.");
        } else {
            if (btAdapter.isEnabled()) {
                Log.d(TAG, "...Bluetooth is enabled...");
            } else {
                // Benutzer auffordern Bluetooth einzuschalten
                Intent enableBtIntent = new Intent(btAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            }
        }
    }
    private void errorExit(String title, String message){
        Toast msg = Toast.makeText(getBaseContext(),
                title + " - " + message, Toast.LENGTH_SHORT);
        msg.show();
        finish();
    }

    private void sendData(String message) {
        byte[] msgBuffer = message.getBytes();

        Log.d(TAG, "...Sending data: " + message + "...");

        try {
            outStream.write(msgBuffer);
        } catch (IOException e) {
            String msg = "In onResume() ist eine Ausnahme während des Schreibens aufgetreten: " + e.getMessage();
            if (address.equals("00:00:00:00:00:00"))
                msg = msg + ".\n\n";
            msg = msg +  ".\n\n Existiert die SPP UUID: " + MY_UUID.toString() + " auf dem Server?\n\n";

            errorExit("Fatal Error", msg);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
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
}
