package com.utility;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Phong on 10/20/2017.
 */

public class DataUtils {

    public static <T> T parserObject(String json, Class<T> typeClass) {
        try {
            if (json == null) {
                return null;
            }
            Gson gson = new Gson();
            return gson.fromJson(json, new ObjectOfJson<T>(typeClass));
        } catch (JsonSyntaxException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static class ObjectOfJson<T> implements ParameterizedType {
        private Class<T> wrapped;

        public ObjectOfJson(Class<T> wrapper) {
            this.wrapped = wrapper;
        }

        @Override
        public Type[] getActualTypeArguments() {
            return new Type[]{wrapped};
        }

        @Override
        public Type getRawType() {
            return wrapped;
        }

        @Override
        public Type getOwnerType() {
            return null;
        }
    }

    public static <T> List<T> getListData(String jsonData, Class<T> typeClass) {
        List<T> list = new Gson().fromJson(jsonData, new ListOfJson(typeClass));
        if (list == null) {
            return new ArrayList<>();
        }
        return list;
    }

    public static class ListOfJson<T> implements ParameterizedType {
        private Class<?> wrapped;

        public ListOfJson(Class<T> wrapper) {
            this.wrapped = wrapper;
        }

        @Override
        public Type[] getActualTypeArguments() {
            return new Type[]{wrapped};
        }

        @Override
        public Type getRawType() {
            return List.class;
        }

        @Override
        public Type getOwnerType() {
            return null;
        }
    }
}
