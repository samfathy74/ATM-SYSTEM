package com.example.firebasetutorial.classes;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class ShPreferences {
    public static void storeDataInListPreferences(Context context, ArrayList<String> arrayList,final String KEY,final String SHARED_PREFERENCE_NAME) {
        Gson gson = new Gson();
        String toGson = gson.toJson(arrayList);
        SharedPreferences preferences = context.getSharedPreferences(SHARED_PREFERENCE_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(KEY, toGson);
        editor.apply();

    }

    public static List<String> readDataInListPreferences(Context context,final String KEY,final String SHARED_PREFERENCE_NAME) {
        Gson gson = new Gson();
        SharedPreferences preferences = context.getSharedPreferences(SHARED_PREFERENCE_NAME, Context.MODE_PRIVATE);
        String fromGson = preferences.getString(KEY, "");
        Type type = new TypeToken<ArrayList<String>>() {}.getType();
        return gson.fromJson(fromGson, type);
    }

    public static void clearDataInListPreferences(Context context,final String KEY,final String SHARED_PREFERENCE_NAME) {
        SharedPreferences preferences = context.getSharedPreferences(SHARED_PREFERENCE_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.remove(KEY);
        editor.apply();
    }

}
