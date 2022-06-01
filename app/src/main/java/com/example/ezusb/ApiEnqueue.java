package com.example.ezusb;

import android.util.Log;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class ApiEnqueue {
    private String TAG = ApiEnqueue.class.getSimpleName() + "(TAG)";

    public interface resultListener {
        void onSuccess(final String message);

        void onFailure(final String message);
    }

    private ApiEnqueue.resultListener listener;

    public static String runTask = "";


    public final String TASK_CONSUL_TATION = "TASK_CONSUL_TATION";
    public final String TASK_REGIST_RATION = "TASK_REGIST_RATION";


    // 目前看診號查詢
    public void consultation(String PID, String CCode, resultListener listen) {

        runTask = TASK_CONSUL_TATION;

        listener = listen;

        String url = ApiUrl.API_URL + ApiUrl.consultation_inquiries;
        Log.d(TAG, "url: " + url);

        FormBody formBody = new FormBody.Builder()
                .add("PID", PID)
                .add("CCode", CCode)
                .build();
        Log.d(TAG, "PID: " + PID);
        Log.d(TAG, "CCode: " + CCode);

        runOkHttp(url, formBody);
    }

    // 預約掛號單筆報到
    public void registration(resultListener listen) {

//        runTask = TASK_REGIST_RATION;


        listener = listen;

        String url = ApiUrl.API_URL + ApiUrl.make＿appointment;
        Log.d(TAG, "url: " + url);

        FormBody formBody = new FormBody.Builder()
                .add("PID", "PartnerTest")
                .add("CCode", "3543091231")
                .add("ID", MemberBeen.ID)
                .add("BDate", MemberBeen.BDate)
                .add("rid", MemberBeen.rid)
                .build();
        Log.d(TAG, "PID: " + "PartnerTest");
        Log.d(TAG, "CCode: " + "3543091231");
        Log.d(TAG, "ID: " + MemberBeen.ID);
        Log.d(TAG, "BDate: " + MemberBeen.BDate);
        Log.d(TAG, "rid: " + MemberBeen.rid);

        runOkHttpGet(url);
    }

    private void runOkHttpGet(String url) {

        Request request = new Request.Builder().url(url).build();

        new OkHttpClient().newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                listener.onFailure("連線失敗");
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {


            }
        });

    }

    private void runOkHttp(String url, FormBody formBody) {

        Request request = new Request.Builder().url(url).post(formBody).build();

        new OkHttpClient().newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                listener.onFailure("連線失敗");
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {

                ResponseBody responseBody = response.body();

                if (responseBody != null) {
                    String body = responseBody.string();
                    Log.d(TAG, "body: " + body);

                    processBody(body);
                } else {
                    listener.onFailure("無回傳資料");
                }
            }

        });
    }

    private void processBody(String body) {

        switch (runTask) {
            case TASK_CONSUL_TATION:
                taskConsulTation(body, TASK_CONSUL_TATION);
                break;

            case TASK_REGIST_RATION:
//                taskRegistration(body, TASK_REGIST_RATION);
                break;
        }
    }

    private void taskConsulTation(String body, String task) {
        try {
            JSONObject jsonObject = new JSONObject(body);
            String back = jsonObject.getString("目前看診號清單");
            Log.d(TAG, "back: " + back);
            listener.onSuccess(back);

        } catch (JSONException e) {
            Log.d(TAG, task + " 剖析失敗：欄位不存在");
            e.printStackTrace();
        }

    }

//    private void taskRegistration(String body, String task) {
//        try {
//            JSONObject jsonObject = new JSONObject(body);
//            String back = jsonObject.getString("回傳資訊");
//            Log.d(TAG, "back: " + back);
//            String message = jsonObject.getString("message");
//            Log.d(TAG, "message: " + message);
//            listener.onSuccess(back);
//
//        } catch (JSONException e) {
//            Log.d(TAG, task + " 剖析失敗：欄位不存在");
//            e.printStackTrace();
//        }
//    }
}
