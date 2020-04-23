package com.faw.hqzl3.myencryptionapplication;

import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;

public class MainActivity extends AppCompatActivity {
    byte[] encrypt;
    String decrypt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.button1:
//                try {
//                    encrypt = HQActivationCertGenerateUtil.encrypt("cjm123..", "This is test");
//                    Log.w("CJM", "AESEncrypt:" + new String(encrypt));
//                } catch (Throwable throwable) {
//                    throwable.printStackTrace();
//                }
                HQActivationCertGenerateUtil.test();
                break;
            case R.id.button2:
                HQActivationCertDecryptUtil.Status status = HQActivationCertDecryptUtil.getInstance().validation(Environment.getExternalStorageDirectory().getPath() + "/", "");
                Log.w("CJM", "HQActivationCertDecryptUtil:" + status);
                break;
            case R.id.button3:
                try {
                    encrypt = HQActivationCertGenerateUtil.encrypt("cjm123..", "What the Fuck!");
                    HQActivationCertGenerateUtil.writeToFile(encrypt);
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                } catch (GeneralSecurityException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                break;

            case R.id.button4:
//                try {
//                    byte[] fuck = HQActivationCertDecryptUtil.getInstance().readFromFile(Environment.getExternalStorageDirectory().getPath() + File.separator);
//
//                    Log.w("CJM", "FUCK:" + encrypt.length + "--" + fuck.length);
//
//                    decrypt = HQActivationCertDecryptUtil.getInstance().decrypt("cjm123..", encrypt);
//                    Log.w("CJM", "decrypt1:" + decrypt);
//                    decrypt = HQActivationCertDecryptUtil.getInstance().decrypt("cjm123..", fuck);
//                    Log.w("CJM", "decrypt2:" + decrypt);
//                } catch (UnsupportedEncodingException e) {
//                    e.printStackTrace();
//                } catch (GeneralSecurityException e) {
//                    e.printStackTrace();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
                break;
        }
    }


}
