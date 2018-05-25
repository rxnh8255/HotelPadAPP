package com.zhijia.hotelpad;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.app.Activity;
import android.os.AsyncTask;

import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.dcs.okhttp3.Call;
import com.baidu.dcs.okhttp3.Callback;
import com.baidu.dcs.okhttp3.Response;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static android.Manifest.permission.READ_CONTACTS;

/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends Activity{


    private  final String TAG = "LoginActivity";
    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */
    private UserLoginTask mAuthTask = null;

    // UI references.
    private EditText mEmailView;
    private EditText mPasswordView;
    private View mProgressView;
    private View mLoginFormView;
    private Spinner spDown;
    private List<SpinnerItem> list;
    private ArrayAdapter<SpinnerItem> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        // Set up the login form.
        mEmailView =  findViewById(R.id.email);
        spDown=findViewById(R.id.spDwon);
        mPasswordView =  findViewById(R.id.password);
        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == EditorInfo.IME_ACTION_DONE || id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });

        Button mEmailSignInButton = findViewById(R.id.email_sign_in_button);
        mEmailSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });
        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);
        initOauth();
        initSelect();
    }
    private void initOauth(){

        Log.i(TAG, "initOauth: "+ZhijiaPreferenceUtil.getAccessToken(LoginActivity.this));
            HttpUtil.httpPost(LoginActivity.this, ZhiJiaUrl.Account, "{}", new Callback() {
                @Override
                public void onFailure(Call call, final IOException e) {
                    runOnUiThread(new Runnable() {
                        public void run() {
                            Toast.makeText(LoginActivity.this,
                                    "验证失败,重新登陆",
                                    Toast.LENGTH_SHORT)
                                    .show();
                        }
                    });
                }

                @Override
                public void onResponse(Call call, final Response response) throws IOException {
                    if (response.isSuccessful()) {
                        try {
                            String data = response.body().string();
                            final JSONObject object = new JSONObject(data);
                            if (object.getInt("error_code") != 0) {

                                runOnUiThread(new Runnable() {
                                    public void run() {
                                        try {
                                            Toast.makeText(LoginActivity.this,
                                                    object.getString("error_message"),
                                                    Toast.LENGTH_SHORT)
                                                    .show();
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                });

                            } else {
                                //FamilyUtil.saveFamily(object);
                                final Handler mainHandler = new Handler(Looper.getMainLooper());
                                mainHandler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                        startActivity(intent);
                                        finish();
                                    }
                                });
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }else
                    {
                        runOnUiThread(new Runnable() {
                            public void run() {
                                try {
                                    Toast.makeText(LoginActivity.this,
                                            "newme错误response:"+response.body().string(),
                                            Toast.LENGTH_SHORT)
                                            .show();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                    }
                }
            });
    }


    private void initSelect(){
        HttpUtil.httpPost(LoginActivity.this, ZhiJiaUrl.EnterpriseFind, "{}", new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try {
                    list=new ArrayList<SpinnerItem>();
                    JSONArray ja = new JSONArray(response.body().string());
                    if(ja.length()>0){
                        for(int i=0;i<ja.length();i++){
                            // 遍历 jsonarray 数组，把每一个对象转成 json 对象
                            JSONObject job = ja.getJSONObject(i);

                            SpinnerItem si = new SpinnerItem(job.getString("id"),job.getString("name"));
                            list.add(si);
                        }
                        /*新建适配器*/
                        adapter=new ArrayAdapter<SpinnerItem>(LoginActivity.this,android.R.layout.simple_spinner_item,list);
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_item);

                        runOnUiThread(new Runnable() {
                            public void run() {
                                spDown.setAdapter(adapter);
                            }
                        });
                        spDown.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){
                            @Override
                            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                                Log.i(TAG, "onItemSelected: "+ adapter.getItem(i).GetID());
                            }
                            @Override
                            public void onNothingSelected(AdapterView<?> adapterView) {

                            }
                        });
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void attemptLogin() {
        if (mAuthTask != null) {
            return;
        }

        // Reset errors.
        mEmailView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        String email = mEmailView.getText().toString();
        String password = mPasswordView.getText().toString();
        String enterpriceId = ((SpinnerItem)spDown.getSelectedItem()).GetID();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(email)) {
            mEmailView.setError(getString(R.string.error_field_required));
            focusView = mEmailView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            showProgress(true);
            mAuthTask = new UserLoginTask(email, password,enterpriceId);
            mAuthTask.execute((Void) null);
        }
    }


    private boolean isPasswordValid(String password) {
        return password.length() > 4;
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mLoginFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    public class UserLoginTask extends AsyncTask<Void, Void, Boolean> {

        private final String mEmail;
        private final String mPassword;
        private final String mEnterpriceId;

        UserLoginTask(String email, String password,String enterpriceId) {
            mEmail = email;
            mPassword = password;
            mEnterpriceId = enterpriceId;
        }

        @Override
        protected Boolean doInBackground(Void... params) {

            JSONObject jo = new JSONObject();
            try {
                jo.put("mobile",mEmail);
                jo.put("password",mPassword);
                jo.put("enterpriseId",mEnterpriceId);

            } catch (JSONException e) {
                e.printStackTrace();
            }

            HttpUtil.httpPost(LoginActivity.this, ZhiJiaUrl.Login, jo.toString(), new Callback() {
                @Override
                public void onFailure(Call call, final IOException e) {

                    final Handler mainHandler = new Handler(Looper.getMainLooper());
                    mainHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(LoginActivity.this,
                                    "验证失败,重新登陆",
                                    Toast.LENGTH_SHORT)
                                    .show();
                        }
                    });

                    Log.i("kwwl", "onFailure: " + e.getMessage());
                }

                @Override
                public void onResponse(Call call, final Response response) throws IOException {
                    if (response.isSuccessful()) {
                        try {
                            String data = response.body().string();
                            final JSONObject object = new JSONObject(data);
                            if (object.getInt("error_code") != 0) {
                                final Handler mainHandler = new Handler(Looper.getMainLooper());
                                mainHandler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        try {
                                            mPasswordView.setError(object.getString("error_message"));
                                            Toast.makeText(LoginActivity.this,
                                                    object.getString("error_message"),
                                                    Toast.LENGTH_SHORT)
                                                    .show();
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                });
                            } else {
                                ZhijiaPreferenceUtil.setAccessToken(LoginActivity.this,object.getJSONObject("data").getString("access_token"));
                                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                startActivity(intent);
                                finish();
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }else
                    {
                        final Handler mainHandler = new Handler(Looper.getMainLooper());
                        mainHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    Toast.makeText(LoginActivity.this,
                                            "new错误response:"+response.body().string(),
                                            Toast.LENGTH_SHORT)
                                            .show();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                    }
                }
            });

            return true;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mAuthTask = null;
            showProgress(false);

            if (success) {
                Log.i("blankhotel", "onPostExecute: ");
                //finish();
            } else {
                mPasswordView.setError(getString(R.string.error_incorrect_password));
                mPasswordView.requestFocus();
            }
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
            showProgress(false);
        }
    }
}

