package com.sdk.ltgame.ltgoogle;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.sdk.ltgame.core.common.Target;
import com.sdk.ltgame.core.exception.LTGameError;
import com.sdk.ltgame.core.impl.OnLoginStateListener;
import com.sdk.ltgame.core.model.LoginResult;
import com.sdk.ltgame.net.manager.LoginRealizeManager;

import java.lang.ref.WeakReference;

import androidx.annotation.NonNull;


public class GoogleLoginHelper {
    private int mLoginTarget;
    private WeakReference<Activity> mActivityRef;
    private OnLoginStateListener mListener;
    private String clientID;
    public int selfRequestCode;
    private String adID;

    GoogleLoginHelper(Activity activity, String clientID, String adID,
                      int selfRequestCode, OnLoginStateListener listener) {
        this.mActivityRef = new WeakReference<>(activity);
        this.clientID = clientID;
        this.adID = adID;
        this.selfRequestCode = selfRequestCode;
        this.mListener = listener;
        this.mLoginTarget = Target.LOGIN_GOOGLE;
    }


    /**
     * 登录
     */
    void login() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(clientID)
                .requestEmail()
                .build();
        GoogleSignInClient mGoogleSignInClient = GoogleSignIn.getClient(mActivityRef.get(), gso);
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        mActivityRef.get().startActivityForResult(signInIntent, selfRequestCode);
    }

    /**
     * 登录回调
     */
    void onActivityResult(int requestCode, Intent data, int selfRequestCode) {
        if (requestCode == selfRequestCode) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            if (!TextUtils.isEmpty(adID)) {
                handleSignInResult(task);
            }
        }
    }


    private void handleSignInResult(@NonNull Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            if (account != null) {
                String idToken = account.getIdToken();
                if (!TextUtils.isEmpty(idToken)) {
                    LoginRealizeManager.googleLogin(mActivityRef.get(), idToken, mListener);
                } else {
                    mListener.onState(mActivityRef.get(), LoginResult.failOf(LTGameError.make("Google user token is empty")));
                }
            } else {
                mListener.onState(mActivityRef.get(), LoginResult.failOf(LTGameError.make("Google account is empty")));
            }

        } catch (ApiException e) {
            e.printStackTrace();
        }
    }

    /**
     * 退出登录
     */
    void loginOut(Context context, String clientID) {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(clientID)
                .requestEmail()
                .build();
        GoogleSignInClient mGoogleSignInClient = GoogleSignIn.getClient(context, gso);
        mGoogleSignInClient.signOut().addOnCompleteListener((Activity) context, new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                mListener.onState(mActivityRef.get(), LoginResult.loginOut(LTGameError.make("Google loginOut")));
                mActivityRef.get().finish();
            }
        });
    }

    /**
     * 获取token
     */
    public static void getToken(String clientID, int selfRequestCode, Activity activity) {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(clientID)
                .requestEmail()
                .build();
        GoogleSignInClient mGoogleSignInClient = GoogleSignIn.getClient(activity, gso);
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        activity.startActivityForResult(signInIntent, selfRequestCode);
    }


    /**
     * 获取token
     */
    public static String getGuestToken(int requestCode, Intent data, int selfRequestCode) {
        String idToken = "";
        if (requestCode == selfRequestCode) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                if (account != null) {
                    idToken = account.getIdToken();
                    return idToken;
                }
            } catch (ApiException e) {
                e.printStackTrace();
                return idToken;
            }
        }
        return idToken;
    }

}
