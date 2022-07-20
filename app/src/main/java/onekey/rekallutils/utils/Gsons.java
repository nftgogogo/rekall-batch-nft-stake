package onekey.rekallutils.utils;


import static com.google.gson.FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES;

import android.content.Context;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;

import java.io.Reader;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


/**
 *
 * <p/>
 * Created by tony on 8/21/14.
 */
public abstract class Gsons {

    private static Gson GSON = createGson(true);
    private static Gson GSON_NO_NULLS = createGson(false);

    /**
     * gson builder
     *
     * @param builder
     */
    public static void createGson(GsonBuilder builder) {
        GSON = builder.create();
        GSON_NO_NULLS = builder.create();
    }

    /**
     * Create the standard {@link Gson} configuration
     *
     * @return created gson, never null
     */
    public static final Gson createGson() {
        return createGson(true);
    }

    /**
     * Create the standard {@link Gson} configurationØ
     *
     * @param serializeNulls whether nulls should be serialized
     * @return created gson, never null
     */
    public static final Gson createGson(final boolean serializeNulls) {
        final GsonBuilder builder = new GsonBuilder();


        // token_auth -> tokenAuth
        builder.setFieldNamingPolicy(LOWER_CASE_WITH_UNDERSCORES);
        builder.disableHtmlEscaping();
        builder.setLenient();
        // { token:null }
        if (serializeNulls) {
            builder.serializeNulls();
        }
        return builder.create();
    }

    /**
     * Get reusable pre-configured {@link Gson} instance
     *
     * @return Gson instance
     */
    public static final Gson getGson() {
        return GSON;
    }

    /**
     * Get reusable pre-configured {@link Gson} instance
     *
     * @return Gson instance
     */
    public static final Gson getGson(final boolean serializeNulls) {
        return serializeNulls ? GSON : GSON_NO_NULLS;
    }

    /**
     * Convert object to json
     *
     * @return json string
     */
    public static final String toJson(final Object object) {
        return toJson(object, true);
    }

    /**
     * Convert object to json
     *
     * @return json string
     */
    public static final String toJson(final Object object, final boolean includeNulls) {
        return includeNulls ? GSON.toJson(object) : GSON_NO_NULLS.toJson(object);
    }

    /**
     * Convert string to given type
     *
     * @return instance of type
     */
    public static final <V> V fromJson(String json, Class<V> type) {
        return GSON.fromJson(json, type);
    }


    public static final <V> V fromJson(String json, Type type) {
        return GSON.fromJson(json, type);
    }


    public static final <V> ArrayList<V> fromJsonList(String json) {
        TypeToken<ArrayList<V>> typeToken = new TypeToken<ArrayList<V>>() {
        };
        return GSON.fromJson(json, typeToken.getType());
    }


    public static <T> List<T> getDataList(String json, Class<T> clazz) {
        List<T> datalist = new ArrayList<>();
        if (null == json) {
            return datalist;
        }
        Gson gson = new Gson();
        //        datalist = gson.fromJson(strJson, new TypeToken<List<T>>() {}.getType());
        Type listType = com.google.gson.internal.$Gson$Types.newParameterizedTypeWithOwner(null, ArrayList.class, clazz);
        datalist = gson.fromJson(json, listType);
        return datalist;
    }

    public static final <T> List<T> parseString2List(String json, Class clazz) {
        Type type = new ParameterizedTypeImpl(clazz);
        List<T> list =  new Gson().fromJson(json, type);
        return list;
    }

    private static class ParameterizedTypeImpl implements ParameterizedType {
        Class clazz;

        public ParameterizedTypeImpl(Class clz) {
            clazz = clz;
        }

        @Override
        public Type[] getActualTypeArguments() {
            return new Type[]{clazz};
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


    public static final <V> V fromJson(Reader reader, Class<V> type) {
        return GSON.fromJson(reader, type);
    }

    public static final <V> V fromJson(Reader reader, Type type) {
        return GSON.fromJson(reader, type);
    }


    public static String fromKeyString(String json, String key) {
        if (json.isEmpty()) {
            return "";
        }
        JsonElement element = JsonParser.parseString(json);

        JsonObject root = element.getAsJsonObject();

        return root.get(key).getAsString();
    }
}