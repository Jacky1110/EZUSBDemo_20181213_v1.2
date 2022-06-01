package com.example.ezusb;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;

import android.app.Activity;
import android.content.Context;
import android.hardware.usb.UsbConstants;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends Activity {
    private String TAG = getClass().getSimpleName() + "(TAG)";

    private int slot = 0;
    private int sequence = 0;
    private ApiEnqueue apiEnqueue;
    //    private static final String TAG = "EZUSB";   //記錄標識
    private Button btsend;      //發送按鈕
    private Button btclear;
    private UsbManager manager;   //USB管理器
    private UsbDevice mUsbDevice;  //找到的USB設備
    private UsbInterface mInterface;
    private UsbDeviceConnection mDeviceConnection;
    private static final int MAX_LINES = 20;
    private TextView mResponseTextView, dtName, flName, mNem, mNowMem, mtitle;
    private byte[] Receiveytes;  //接收資料
    private TextView cardID, Name, Identity, Birthday, Sex;
    private RecyclerView recyNow, recyOve;

    ArrayList<RegistrationModel> data = new ArrayList<>();
    private RegistrationAdapter adapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_main);

        init();
        handleinit();
        btsend.setOnClickListener(btsendListener);
        // Initialize response text view
        mResponseTextView.setMovementMethod(new ScrollingMovementMethod());
        mResponseTextView.setMaxLines(MAX_LINES);
        mResponseTextView.setText("");


        btclear.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View arg0) {
                cardID.setText("");
                Name.setText("");
                Identity.setText("");
                Birthday.setText("");
                Sex.setText("");
                mResponseTextView.setText("");
            }
        });


        // 獲取USB設備
        manager = (UsbManager) getSystemService(Context.USB_SERVICE);
        if (manager == null) {
            return;
        } else {
            Log.i(TAG, "usb manager: " + String.valueOf(manager.toString()));
        }

        HashMap<String, UsbDevice> deviceList = manager.getDeviceList();
        Iterator<UsbDevice> deviceIterator = deviceList.values().iterator();
        while (deviceIterator.hasNext()) {
            UsbDevice device = deviceIterator.next();
            // For EZUSB device
            if (device.getVendorId() == 3238 && device.getProductId() == 16) {
                mUsbDevice = device;
                logMsg("Reader Found !");
            }
        }
        findIntfAndEpt();
        handleRequest();
    }

    private void init() {
        apiEnqueue = new ApiEnqueue();
        btclear = (Button) findViewById(R.id.btclear);
        btsend = (Button) findViewById(R.id.btsend);
        mResponseTextView = (TextView) findViewById(R.id.main_text_view_response);
        cardID = (TextView) findViewById(R.id.info_cardID);
        Name = (TextView) findViewById(R.id.info_Name);
        Identity = (TextView) findViewById(R.id.info_Identity);
        Birthday = (TextView) findViewById(R.id.info_Birthday);
        Sex = (TextView) findViewById(R.id.info_Sex);
        dtName = (TextView) findViewById(R.id.dtName);
        flName = (TextView) findViewById(R.id.flName);
        mNem = (TextView) findViewById(R.id.mNem);
        mNowMem = (TextView) findViewById(R.id.mNowMem);
        mtitle = (TextView) findViewById(R.id.mtitle);
        recyNow = (RecyclerView) findViewById(R.id.rev_Now);
        recyOve = (RecyclerView) findViewById(R.id.rev_Over);


    }

    private void handleinit() {
        recyNow.setLayoutManager(new GridLayoutManager(this, 2));

    }

    private void handleRequest() {

        String PID = "PartnerTest";
        String CCode = "3543091231";

        apiEnqueue.consultation(PID, CCode, new ApiEnqueue.resultListener() {
            @Override
            public void onSuccess(String message) {
                runOnUiThread(() -> {
                    try {
                        JSONArray jsonArray = new JSONArray(message);
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject jsonObject = (JSONObject) jsonArray.get(i);
                            MemberBeen.doctor_name = jsonObject.getString("醫師名");
                            Log.d(TAG, "MemberBeen.doctor_name: " + MemberBeen.doctor_name);
                            MemberBeen.visit_number = jsonObject.getString("目前看診號");
                            Log.d(TAG, "MemberBeen.visit_number: " + MemberBeen.visit_number);
                            MemberBeen.people_waiting = jsonObject.getString("等待人數");
                            Log.d(TAG, "MemberBeen.people_waiting: " + MemberBeen.people_waiting);
                            MemberBeen.Section = jsonObject.getString("科別");
                            MemberBeen.diagnosis = jsonObject.getString("診別");
                            String Details = jsonObject.getString("掛號明細");


                            dtName.setText(MemberBeen.doctor_name);
                            mNem.setText(MemberBeen.people_waiting);
                            mNowMem.setText(MemberBeen.visit_number);
                            mtitle.setText(MemberBeen.Section);
                            flName.setText(MemberBeen.diagnosis);

                            JSONArray jsonArray1 = new JSONArray(Details);
                            for (int j = 0; j < jsonArray1.length(); j++) {
                                RegistrationModel model = new RegistrationModel();
                                JSONObject jsonObject1 = (JSONObject) jsonArray1.get(j);
                                model.patient_name = jsonObject1.getString("患者姓名");
                                model.look_number = jsonObject1.getString("看診號");
                                model.ID = jsonObject1.getString("iD");
                                model.birthday = jsonObject1.getString("bDate");
                                model.Rid = jsonObject1.getString("rid");
                                data.add(model);

                                MemberBeen.patient_name = model.patient_name;
                                Log.d(TAG, "MemberBeen.patient_name: " + MemberBeen.patient_name);
                                MemberBeen.clinic_number = model.look_number;
                                Log.d(TAG, "MemberBeen.clinic_number: " + MemberBeen.clinic_number);
                                MemberBeen.ID = model.ID;
                                Log.d(TAG, "MemberBeen.ID: " + MemberBeen.ID);
                                MemberBeen.BDate = model.birthday;
                                Log.d(TAG, "MemberBeen.BDate: " + MemberBeen.BDate);
                                MemberBeen.rid = model.Rid;
                                Log.d(TAG, "MemberBeen.rid: " + MemberBeen.rid);
                            }

                        }

                        runOnUiThread(() -> {
                            adapter = new RegistrationAdapter();
                            adapter.setData(data);
                            recyNow.setAdapter(adapter);
                        });
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                });

            }

            @Override
            public void onFailure(String message) {

            }
        });
    }

    void cmdPowerON() {
        int ret = -100;
        byte[] PowerOnCmd = new byte[]{(byte) 0x62, 0x00, 0x00, 0x00, 0x00, (byte) slot, (byte) sequence, 0x00, 0x00, 0x00};

        if (mDeviceConnection == null) {
            logMsg("No Reader Found !");
            return;
        }
        //-------------------------
        ret = mDeviceConnection.bulkTransfer(epOut, PowerOnCmd, PowerOnCmd.length, 5000);
        logMsg("Power On:" + String.valueOf(ret));
        if (ret == PowerOnCmd.length) {
            logBuffer(PowerOnCmd, PowerOnCmd.length);
        }
        //-------------------------


        // 2. 接收Reader回傳資料
        Receiveytes = new byte[265];
        ret = -1;
        ret = mDeviceConnection.bulkTransfer(epIn, Receiveytes, Receiveytes.length, 10000);
        logMsg("Len=" + String.valueOf(ret));
        if (ret < 0) {
            DisplayToast("接收回傳值" + String.valueOf(ret));
            return;
        } else {
            //回傳資料
            logBuffer(Receiveytes, ret);

            sequence = (sequence + 1) % 0xFF;

            if (Receiveytes[0] == (byte) 0x80 && Receiveytes[7] == 0x00) {
                byte[] bATR = new byte[Receiveytes[4] - 1];
                logMsg("ART " + String.valueOf(Receiveytes[4] - 1) + " bytes :");
                System.arraycopy(Receiveytes, 11, bATR, 0, Receiveytes[4] - 1);
                logBuffer(bATR, bATR.length);

                if (bATR[0] != (byte) 0x3B && bATR[0] != (byte) 0x3F) {
                    logMsg("It's memory card , don't send APDU !");
                }
            } else if (Receiveytes[0] == (byte) 0x80 && Receiveytes[7] == 0x42)
                logMsg("No Card !");
            else if (Receiveytes[0] == (byte) 0x80 && Receiveytes[7] == 0x41)
                logMsg("Connect Card Fail !");
            else
                logMsg("Connect Card Fail2 !");
        }
    }

    ;

    void cmdPowerOFF() {
        int ret = -100;
        byte[] PowerOffCmd = new byte[]{(byte) 0x63, 0x00, 0x00, 0x00, 0x00, (byte) slot, (byte) sequence, 0x00, 0x00, 0x00};

        if (mDeviceConnection == null) {
            logMsg("No Reader Found !");
            return;
        }
        //-------------------------
        ret = mDeviceConnection.bulkTransfer(epOut, PowerOffCmd, PowerOffCmd.length, 5000);
        logMsg("Power Off:" + String.valueOf(ret));
        if (ret == PowerOffCmd.length) {
            logBuffer(PowerOffCmd, PowerOffCmd.length);
        }
        //-------------------------

        // 2. 接收Reader回傳資料
        Receiveytes = new byte[265];
        ret = -1;
        ret = mDeviceConnection.bulkTransfer(epIn, Receiveytes, Receiveytes.length, 10000);
        logMsg("Len=" + String.valueOf(ret));
        if (ret < 0) {
            DisplayToast("接收回傳值" + String.valueOf(ret));
            return;
        } else {
            //回傳資料
            logBuffer(Receiveytes, ret);

            sequence = (sequence + 1) % 0xFF;

            //-------------------------
            if (Receiveytes[0] == (byte) 0x81 && Receiveytes[7] == 0x01) {
                logMsg("Disconnect Card OK !");
            } else if (Receiveytes[0] == (byte) 0x81 && Receiveytes[7] == 0x02)
                logMsg("No Card !");
            else
                logMsg("Disconnect Card Fail2 !");
            //-------------------------
        }
    }

    ;

    public static String big5ToUnicode(String str) {
        // 請在此寫出本方法的程式碼
        String strResult = "";
        try {
            strResult = new String(str.getBytes("ISO8859_1"), "Big5");
        } catch (Exception e) {
        }
        return strResult;
    }

    private OnClickListener btsendListener = new OnClickListener() {
        int ret = -100;

        @Override
        public void onClick(View v) {
            byte[] GetHealthIDCardCmd1 = new byte[]{0x00, (byte) 0xA4, 0x04, 0x00, (byte) 0x10, (byte) 0xD1, 0x58, 0x00, 0x00,
                    0x01, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x11, 0x00};
            byte[] GetHealthIDCardCmd2 = new byte[]{0x00, (byte) 0xCA, 0x11, 0x00, 0x02, 0x00, 0x00};

            cardID.setText("");
            Name.setText("");
            Identity.setText("");
            Birthday.setText("");
            Sex.setText("");

            if (DetectionCard() == false)
                return;

            cmdPowerON();

            if (mDeviceConnection == null) {
                logMsg("No Reader Found !");
                cmdPowerOFF();
                return;
            }

            byte[] spGetHealthIDCardCmd1 = new byte[GetHealthIDCardCmd1.length + 10];
            spGetHealthIDCardCmd1[0] = 0x57;//type
            spGetHealthIDCardCmd1[1] = 0x00;//we don't support long length
            spGetHealthIDCardCmd1[2] = 0x00; //we don't support long length
            spGetHealthIDCardCmd1[3] = 0x00;//we don't support long length
            spGetHealthIDCardCmd1[4] = (byte) GetHealthIDCardCmd1.length;  // 長度
            spGetHealthIDCardCmd1[5] = (byte) slot;
            spGetHealthIDCardCmd1[6] = (byte) sequence;
            spGetHealthIDCardCmd1[7] = 0x00;
            spGetHealthIDCardCmd1[8] = 0x00;
            spGetHealthIDCardCmd1[9] = 0x00;
            System.arraycopy(GetHealthIDCardCmd1, 0, spGetHealthIDCardCmd1, 10, GetHealthIDCardCmd1.length);


            ret = mDeviceConnection.bulkTransfer(epOut, spGetHealthIDCardCmd1, spGetHealthIDCardCmd1.length, 5000);
            logMsg("Get HealthID Card Cmd1 Type:" + String.valueOf(ret));
            logBuffer(GetHealthIDCardCmd1, GetHealthIDCardCmd1.length);

            // 2. 接收Reader回傳資料
            Receiveytes = new byte[265];
            ret = -1;
            ret = mDeviceConnection.bulkTransfer(epIn, Receiveytes, Receiveytes.length, 10000);
            if (ret < 0) {
                DisplayToast("接收回傳值" + String.valueOf(ret));
                cmdPowerOFF();
                return;
            } else {
                //回傳資料
                sequence = (sequence + 1) % 0xFF;

                logMsg("Len=" + String.valueOf(ret));
                logBuffer(Receiveytes, ret);


                if (Receiveytes[ret - 2] == (byte) 0x90 && Receiveytes[ret - 1] == 0x00) {
                    byte[] spGetHealthIDCardCmd2 = new byte[GetHealthIDCardCmd2.length + 10];
                    spGetHealthIDCardCmd2[0] = 0x57;//type
                    spGetHealthIDCardCmd2[1] = 0x00;//we don't support long length
                    spGetHealthIDCardCmd2[2] = 0x00; //we don't support long length
                    spGetHealthIDCardCmd2[3] = 0x00;//we don't support long length
                    spGetHealthIDCardCmd2[4] = (byte) GetHealthIDCardCmd2.length;  // 長度
                    spGetHealthIDCardCmd2[5] = (byte) slot;
                    spGetHealthIDCardCmd2[6] = (byte) sequence;
                    spGetHealthIDCardCmd2[7] = 0x00;
                    spGetHealthIDCardCmd2[8] = 0x00;
                    spGetHealthIDCardCmd2[9] = 0x00;
                    System.arraycopy(GetHealthIDCardCmd2, 0, spGetHealthIDCardCmd2, 10, GetHealthIDCardCmd2.length);

                    ret = mDeviceConnection.bulkTransfer(epOut, spGetHealthIDCardCmd2, spGetHealthIDCardCmd2.length, 5000);
                    logMsg("Get HealthID Card Cmd2 Type:" + String.valueOf(ret));
                    logBuffer(spGetHealthIDCardCmd2, spGetHealthIDCardCmd2.length);
                    // 3. 接收Reader回傳資料
                    Receiveytes = new byte[265];
                    ret = -1;
                    ret = mDeviceConnection.bulkTransfer(epIn, Receiveytes, Receiveytes.length, 10000);
                    if (ret < 0) {
                        DisplayToast("接收回傳值" + String.valueOf(ret));
                        cmdPowerOFF();
                        return;
                    } else {
                        sequence = (sequence + 1) % 0xFF;
                        logMsg("Len=" + String.valueOf(ret));
                        logBuffer(Receiveytes, ret);

                        //mResponseTextView.append("=====================");
                        String temp = "";
                        int offset;
                        offset = 10;
                        for (int temp_i = 0; temp_i < 12; temp_i++)
                            temp += String.format("%C", Receiveytes[temp_i + offset]);
                        //logMsg("Card ID = " + temp);
                        cardID.setText("Card ID = " + temp);

                        offset += 12;
                        temp = "";
                        for (int temp_i = 0; temp_i < 20; temp_i += 2) {
                            if (Receiveytes[temp_i + offset] != 0 && Receiveytes[temp_i + offset + 1] != 0) {
                                String temp2 = "";
                                if (Receiveytes[temp_i + offset] < 0) {
                                    temp2 += "" + (char) (256 + Receiveytes[temp_i + offset]); //轉char
                                } else {
                                    temp2 += "" + (char) Receiveytes[temp_i + offset];
                                }

                                if (Receiveytes[temp_i + offset + 1] < 0) {
                                    temp2 += "" + (char) (256 + Receiveytes[temp_i + offset + 1]); //轉char
                                } else {
                                    temp2 += "" + (char) Receiveytes[temp_i + offset + 1];
                                }
                                temp += big5ToUnicode(temp2);
                            }
                        }
                        //logMsg("Name = " + temp);
                        Name.setText("Name = " + temp);

                        offset += 20;
                        temp = "";
                        for (int temp_i = 0; temp_i < 10; temp_i++)
                            temp += String.format("%C", Receiveytes[temp_i + offset]);
                        //logMsg("Identity Card = " + temp);
                        Identity.setText("Identity Card = " + temp);
                        if (temp != MemberBeen.ID) {
                            Toast.makeText(MainActivity.this, "查無今日預約掛號待報到資料!", Toast.LENGTH_SHORT).show();
                        }


                        offset += 10;
                        temp = "";
                        for (int temp_i = 0; temp_i < 7; temp_i++)
                            temp += String.format("%C", Receiveytes[temp_i + offset]);
                        //logMsg("Birthday = " + temp);
                        Birthday.setText("Birthday = " + temp);

                        offset += 7;
                        temp = "";
                        if (Receiveytes[offset] == 0x4D) {
                            //logMsg("Sex =  男");
                            Sex.setText("Sex =  男");
                        } else {
                            //logMsg("Sex =  女");
                            Sex.setText("Sex =  女");
                        }

                        //mResponseTextView.append("\n=====================");
                    }
                }
            }
            cmdPowerOFF();
            handleID();
        }

    };

    private void handleID() {

        apiEnqueue.registration(new ApiEnqueue.resultListener() {
            @Override
            public void onSuccess(String message) {

            }

            @Override
            public void onFailure(String message) {

            }
        });
    }


    private boolean DetectionCard() {
        int ret = -100;

        byte[] GetHealthIDCardCmd1 = new byte[]{0x65};


        if (mDeviceConnection == null) {
            logMsg("No Reader Found !");
            return false;
        }

        byte[] spGetHealthIDCardCmd1 = new byte[GetHealthIDCardCmd1.length + 10];
        spGetHealthIDCardCmd1[0] = 0x57;
        spGetHealthIDCardCmd1[1] = 0x00;
        spGetHealthIDCardCmd1[2] = 0x00;
        spGetHealthIDCardCmd1[3] = 0x00;
        spGetHealthIDCardCmd1[4] = (byte) GetHealthIDCardCmd1.length;
        spGetHealthIDCardCmd1[5] = (byte) slot;
        spGetHealthIDCardCmd1[6] = (byte) sequence;
        spGetHealthIDCardCmd1[7] = 0x00;
        spGetHealthIDCardCmd1[8] = 0x00;
        spGetHealthIDCardCmd1[9] = 0x00;
        System.arraycopy(GetHealthIDCardCmd1, 0, spGetHealthIDCardCmd1, 10, GetHealthIDCardCmd1.length);


        ret = mDeviceConnection.bulkTransfer(epOut, spGetHealthIDCardCmd1, spGetHealthIDCardCmd1.length, 5000);
        logMsg("Get HealthID Card Cmd1 Type:" + String.valueOf(ret));
        logBuffer(GetHealthIDCardCmd1, GetHealthIDCardCmd1.length);


        Receiveytes = new byte[265];
        ret = -1;
        ret = mDeviceConnection.bulkTransfer(epIn, Receiveytes, Receiveytes.length, 10000);
        if (ret < 0) {
            DisplayToast("接收回傳值" + String.valueOf(ret));
            return false;
        } else {
            //回傳資料
            sequence = (sequence + 1) % 0xFF;

            logMsg("Len=" + String.valueOf(ret));
            logBuffer(Receiveytes, ret);
        }

        System.out.println("Receiveytes.length is " + Receiveytes.length);
        System.out.print("Receiveytes: ");
        for (int i = 0; i < ret; i++)
            System.out.print(String.format("%02X ", Receiveytes[i]));
        System.out.println();


        if (ret == 10 && Receiveytes[7] == 0x42) {
            logMsg("沒有插卡!");
            return false;
        } else {
            logMsg("偵測到卡!");
            return true;
        }
    }


    // 顯示提示的函數
    public void DisplayToast(CharSequence str) {
        Toast toast = Toast.makeText(this, str, Toast.LENGTH_LONG);
        // 設置Toast顯示的位置
        toast.setGravity(Gravity.TOP, 0, 200);
        // 顯示Toast
        toast.show();
    }

    // 尋找介面和終端接點
    private void findIntfAndEpt() {
        if (mUsbDevice == null) {
            Log.i(TAG, "Device not found");
            return;
        }
        for (int i = 0; i < mUsbDevice.getInterfaceCount(); ) {
            UsbInterface intf = mUsbDevice.getInterface(i);
            Log.d(TAG, i + " " + intf);
            mInterface = intf;
            break;
        }
        if (mInterface != null) {
            UsbDeviceConnection connection = null;
            // 判斷是否有權限
            if (manager.hasPermission(mUsbDevice)) {
                // 打開設備，獲取 UsbDeviceConnection 對象，連接設備，用於後面的通訊
                connection = manager.openDevice(mUsbDevice);
                if (connection == null) {
                    return;
                }

                if (connection.claimInterface(mInterface, true)) {
                    Log.i(TAG, "Interface found");
                    mDeviceConnection = connection;
                    //用UsbDeviceConnection 與 UsbInterface 進行端點設置和通訊
                    getEndpoint(mDeviceConnection, mInterface);
                } else {
                    connection.close();
                }

            } else {
                Log.i(TAG, "No permission");
            }
        } else {
            Log.i(TAG, "Interface not found");
        }
    }

    private UsbEndpoint epOut;
    private UsbEndpoint epIn;

    //用UsbDeviceConnection 與 UsbInterface 進行端點設置和通訊
    private void getEndpoint(UsbDeviceConnection connection, UsbInterface intf) {
        //Get the interfaces
        for (int i = 0; i < intf.getEndpointCount(); i++) {
            UsbEndpoint usbEp = intf.getEndpoint(i);
            if (usbEp.getDirection() == UsbConstants.USB_DIR_OUT && usbEp.getType() == UsbConstants.USB_ENDPOINT_XFER_BULK && usbEp.getAttributes() == 0x02) {
                epOut = usbEp;
            }
            if (usbEp.getDirection() == UsbConstants.USB_DIR_IN && usbEp.getType() == UsbConstants.USB_ENDPOINT_XFER_BULK && usbEp.getAttributes() == 0x02) {
                epIn = usbEp;
            }
        }
    }

    /**
     * Logs the message.
     *
     * @param msg the message.
     */
    private void logMsg(String msg) {

        DateFormat dateFormat = new SimpleDateFormat("[HH:mm:ss]: ");
        Date date = new Date();
        String oldMsg = mResponseTextView.getText().toString();

        mResponseTextView.setText(oldMsg + "\n" + dateFormat.format(date) + msg);

        if (mResponseTextView.getLineCount() > MAX_LINES) {
            mResponseTextView.scrollTo(0,
                    (mResponseTextView.getLineCount() - MAX_LINES)
                            * mResponseTextView.getLineHeight());
        }
    }

    /**
     * Logs the contents of buffer.
     *
     * @param buffer       the buffer.
     * @param bufferLength the buffer length.
     */
    private void logBuffer(byte[] buffer, int bufferLength) {

        String bufferString = "";

        for (int i = 0; i < bufferLength; i++) {
            String hexChar = Integer.toHexString(buffer[i] & 0xFF);
            if (hexChar.length() == 1) {
                hexChar = "0" + hexChar;
            }
            if (i % 12 == 0) {
                if (bufferString != "") {
                    logMsg(bufferString);
                    bufferString = "";
                }
            }
            bufferString += hexChar.toUpperCase() + " ";
        }

        if (bufferString != "") {
            logMsg(bufferString);
        }
    }

    public void logStr(byte[] bytes, int bufferLength) {
        String bufferString = "";

        char[] buffer = new char[bufferLength + 1];
        for (int i = 0; i < bufferLength; i++) {
            int bpos = i;
            char c = (char) (bytes[bpos] & 0xFF);
            buffer[i] = c;
        }

        bufferString = new String(buffer);

        if (bufferString != "") {
            logMsg(bufferString);
        }

    }

}