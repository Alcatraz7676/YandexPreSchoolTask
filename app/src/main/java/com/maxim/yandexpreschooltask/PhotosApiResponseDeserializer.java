package com.maxim.yandexpreschooltask;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.maxim.yandexpreschooltask.entities.GalleryItem;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class PhotosApiResponseDeserializer
        implements JsonDeserializer<List<GalleryItem>> {



    @Override
    public List<GalleryItem> deserialize(JsonElement json, Type type,
                                         JsonDeserializationContext context) throws JsonParseException {

        List<GalleryItem> items = new ArrayList<>();

        JsonArray jArray = json.getAsJsonObject().getAsJsonObject("photos").getAsJsonArray("photo");

        for(int i = 0; i < jArray.size(); i++) {
            JsonObject jObject = (JsonObject) jArray.get(i);
            GalleryItem item = new GalleryItem();

            if (jObject.get("url_n") != null)
                item.setUrl(jObject.get("url_n").getAsString());

            if (jObject.get("url_o") != null)
                item.setBigImageUrl(jObject.get("url_o").getAsString());
            else if (jObject.get("url_n") != null)
                item.setBigImageUrl(jObject.get("url_n").getAsString());

            items.add(item);
        }

        return items;
    }
}