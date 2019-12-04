package com.sdk.ltgame.ltgoogle;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import com.sdk.ltgame.core.common.LTGameOptions;
import com.sdk.ltgame.core.common.LTGameSdk;
import com.sdk.ltgame.core.common.Target;
import com.sdk.ltgame.core.impl.OnLoginStateListener;
import com.sdk.ltgame.core.impl.OnRechargeListener;
import com.sdk.ltgame.core.model.LoginObject;
import com.sdk.ltgame.core.model.RechargeObject;
import com.sdk.ltgame.core.platform.AbsPlatform;
import com.sdk.ltgame.core.platform.IPlatform;
import com.sdk.ltgame.core.platform.PlatformFactory;
import com.sdk.ltgame.core.uikit.BaseActionActivity;
import com.sdk.ltgame.core.util.LTGameUtil;
import com.sdk.ltgame.ltgoogle.uikit.GoogleLoginActivity;

public class GooglePlatform extends AbsPlatform {

    private GoogleLoginHelper mGoogleHelper;

    private GooglePlatform(Context context,boolean isServerTest,  String appId, String appKey, String clientID,
                           String adID, String packageID, int selfRequestCode, int target) {
        super(context, isServerTest, appId, appKey, clientID, adID, packageID, selfRequestCode, target);
    }

    /**
     * 工厂
     */
    public static class Factory implements PlatformFactory {


        @Override
        public IPlatform create(Context context, int target) {
            IPlatform platform = null;
            LTGameOptions options = LTGameSdk.options();
            if (!LTGameUtil.isAnyEmpty(options.getGoogleClientID(), options.getAdID(),
                    options.getLtAppId(), options.getLtAppKey(), options.getPackageID())
                    && options.getSelfRequestCode() != -1) {
                platform = new GooglePlatform(context,options.getISServerTest(), options.getLtAppId(),
                        options.getLtAppKey(), options.getGoogleClientID(), options.getAdID(),
                        options.getPackageID(), options.getSelfRequestCode(), target);
            }
            return platform;
        }

        @Override
        public int getPlatformTarget() {
            return Target.PLATFORM_GOOGLE;
        }

        @Override
        public boolean checkLoginPlatformTarget(int target) {
            return target == Target.LOGIN_GOOGLE;
        }

        @Override
        public boolean checkRechargePlatformTarget(int target) {
            return false;
        }
    }

    @Override
    public Class getUIKitClazz() {
        return GoogleLoginActivity.class;
    }

    @Override
    public void onActivityResult(BaseActionActivity activity, int requestCode, int resultCode, Intent data) {
        super.onActivityResult(activity, requestCode, resultCode, data);
        mGoogleHelper.onActivityResult(requestCode, data, mGoogleHelper.selfRequestCode);
    }


    @Override
    public void login(Activity activity, int target, LoginObject object, OnLoginStateListener listener) {
        mGoogleHelper = new GoogleLoginHelper(activity, object.getmGoogleClient(), object.getmAdID(),
                object.getSelfRequestCode(), listener);
        if (object.isLoginOut()) {
            mGoogleHelper.loginOut(activity, object.getmGoogleClient());
        } else {
            mGoogleHelper.login();
        }


    }

    @Override
    public void recharge(Activity activity, int target, RechargeObject object, OnRechargeListener listener) {

    }
}
