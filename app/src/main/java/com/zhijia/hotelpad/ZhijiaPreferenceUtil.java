package com.zhijia.hotelpad;

import android.content.Context;


/**
 * Created by rxnh8 on 2018/3/26.
 */

public class ZhijiaPreferenceUtil extends PreferenceUtil {
    public static final String Zhijia_OAUTH_CONFIG = "zhijia_oauth_config";
    public static final String HUB_NAME = "messagehub";

  public static class ZhijiaPrefenenceKey {
    // 持久化token信息的各种监制
    public static final String SP_ACCESS_TOKEN = "zhijia_oauth_config_prop_access_token";
    public static final String SP_CREATE_TIME = "zhijia_oauth_config_prop_create_time";
    public static final String SP_EXPIRE_SECONDS = "zhijia_oauth_config_prop_expire_secends";
  }

    /**
     * 保存数据的方法，拿到保存数据的具体类型，然后根据类型调用不同的保存方法
     *
     * @param context 上下文
     * @param key     key
     * @param object  value
     */
    public static void put(Context context, String key, Object object) {
        put(context, Zhijia_OAUTH_CONFIG, key, object);
    }

    /**
     * 得到保存数据的方法，
     * 根据默认值得到保存的数据的具体类型，
     * 然后调用相对于的方法获取值
     *
     * @param context       上下文
     * @param key           key
     * @param defaultObject default-value
     */
    public static Object get(Context context, String key, Object defaultObject) {
        return get(context, Zhijia_OAUTH_CONFIG, key, defaultObject);
    }

    public static void clear(Context context) {
        clear(context, Zhijia_OAUTH_CONFIG);
    }

    public static void setAccessToken(Context context, String value) {
        put(context, ZhijiaPrefenenceKey.SP_ACCESS_TOKEN, value);
    }

    public static String getAccessToken(Context context) {
        return (String) get(context, ZhijiaPrefenenceKey.SP_ACCESS_TOKEN, "");
    }

    public static void setExpires(Context context, long value) {
        put(context, ZhijiaPrefenenceKey.SP_EXPIRE_SECONDS, value);
    }

    public static long getExpires(Context context) {
        return (long) get(context, ZhijiaPrefenenceKey.SP_EXPIRE_SECONDS, 0L);
    }

    public static void setCreateTime(Context context, long value) {
        put(context, ZhijiaPrefenenceKey.SP_CREATE_TIME, value);
    }

    public static long getCreateTime(Context context) {
        return (long) get(context, ZhijiaPrefenenceKey.SP_CREATE_TIME, 0L);
    }

    public static void clearAllOauth(Context context) {
        clear(context);
    }
}
