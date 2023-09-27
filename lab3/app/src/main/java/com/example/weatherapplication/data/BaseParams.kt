package com.example.weatherapplication.data

import android.content.Context
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.example.weatherapplication.R
import org.json.JSONArray
import org.json.JSONObject

object BaseParams{
    val radius = 2000000
    val interestingPlacesApiKey = "5ae2e3f221c38a28845f05b6d04f0380533116febadd3271aeeb1200"
    val weatherApiKey = "bf8a55567e974f43bd2203507221710"
    val geoCodeApiKey = "e7b7068f-e047-4b7f-9b6b-adce35db4024"
    val placesList = mutableStateOf(listOf<Place>())
    val interestingPlacesList = mutableStateOf(listOf<PlaceBaseInfo>())
    val placeInfo = mutableStateOf(PlaceFullInfo())
    lateinit var place: Place
    var city: String = "Saint Petersburg"
    var countOfBoxes: Int = 7
    var mainBg: Int = R.drawable.spb_bg

    fun updatePlace(context: Context, place: Place) {
        this.place = place
        city = place.city

        mainBg = when(city) {
            "Saint Petersburg" -> R.drawable.spb_bg
            "Novosibirsk" -> R.drawable.nsk_bg
            else -> R.drawable.nsk_bg
        }

        getInterestingPlacesList(context)
    }

    fun getListOfPlaces(context: Context, placeName: String) {
        val url = "https://graphhopper.com/api/1/geocode" +
                "?q=$placeName" +
                "&key=$geoCodeApiKey"
        val queue = Volley.newRequestQueue(context)
        val sRequest = StringRequest(
            Request.Method.GET,
            url,
            { response ->
                Log.d("MyLog", "Response: $response")
                placesList.value = getListOfPlacesFromJSON(response)
            },
            {
                Log.d("MyLog", "VolleyError: $it")
            }
        )

        queue.add(sRequest)
    }

    private fun getListOfPlacesFromJSON(response: String): List<Place> {
        if(response.isEmpty()) return listOf()
        val list = ArrayList<Place>()
        val mainObject = JSONObject(response)
        val places = mainObject.getJSONArray("hits")

        for(i in 0 until places.length()) {
            val item = places[i] as JSONObject
            Log.d("MyLog", "Item: $item")

            if(item.has("city")) {
                list.add(
                    Place(
                        item.getString("osm_value"),
                        item.getString("name"),
                        item.getString("country"),
                        item.getString("city"),
                        item.getJSONObject("point").getDouble("lng"),
                        item.getJSONObject("point").getDouble("lat")
                    )
                )
            } else if(item.has("state")) {
                list.add(
                    Place(
                        item.getString("osm_value"),
                        item.getString("state"),
                        item.getString("country"),
                        item.getString("name"),
                        item.getJSONObject("point").getDouble("lng"),
                        item.getJSONObject("point").getDouble("lat")
                    )
                )
            }
        }

        Log.d("MyLog", "Items finished")

        return list
    }

    fun getInterestingPlacesList(context: Context){
        val url = "https://api.opentripmap.com/0.1/" +
                "ru/places/radius" +
                "?radius=$radius" +
                "&lon=${place.lng}" +
                "&lat=${place.lat}" +
                "&src_geom=wikidata" +
                "&src_attr=wikidata" +
                "&kinds=interesting_places" +
                "&rate=1" +
                "&format=json" +
                "&limit=20" +
                "&apikey=$interestingPlacesApiKey"

        val queue = Volley.newRequestQueue(context)
        val sRequest = StringRequest(
            Request.Method.GET,
            url,
            { response ->
                Log.d("MyLog", "Response: $response")
                interestingPlacesList.value = getListOfInterestingPlacesFromJSON(response)
            },
            {
                Log.d("MyLog", "VolleyError on opentripmap: $it")
            }
        )

        queue.add(sRequest)
    }

    private fun getListOfInterestingPlacesFromJSON(response: String): List<PlaceBaseInfo> {
        if(response.isEmpty()) return listOf()
        val list = ArrayList<PlaceBaseInfo>()
        val places = JSONArray(response)

        for(i in 0 until places.length()) {
            val item = places[i] as JSONObject
            Log.d("MyLog", "Item: $item")

            list.add(
                PlaceBaseInfo(
                    item.getString("xid"),
                    item.getString("name"),
                    item.getDouble("dist"),
                    item.getString("rate"),
                    item.getString("wikidata"),
                    item.getString("kinds"),
                    item.getJSONObject("point").getDouble("lon"),
                    item.getJSONObject("point").getDouble("lat")
                )
            )
        }

        Log.d("MyLog", "Items finished")

        return list
    }

    fun updateInterestingPlace(context: Context, xid: String) {
        val url = "https://api.opentripmap.com/0.1/" +
                "ru/places/xid/$xid" +
                "?apikey=$interestingPlacesApiKey"

        val queue = Volley.newRequestQueue(context)
        val sRequest = StringRequest(
            Request.Method.GET,
            url,
            { response ->
                Log.d("MyLog", "Response: $response")
                placeInfo.value = getPlaceInfo(response)
            },
            {
                Log.d("MyLog", "VolleyError on opentripmap: $it, $xid")
            }
        )

        queue.add(sRequest)
    }

    private fun getPlaceInfo(response: String): PlaceFullInfo {
        if (response.isEmpty()) return PlaceFullInfo()

        return PlaceFullInfo(
            if(JSONObject(response).has("wikipedia")) JSONObject(response).getString("wikipedia") else "",
            if(JSONObject(response).has("info")) JSONObject(response).getJSONObject("info").getString("descr") else "",
            if(JSONObject(response).has("preview")) JSONObject(response).getJSONObject("preview").getString("source") else "",
            if(JSONObject(response).has("preview")) JSONObject(response).getJSONObject("preview").getInt("height") else 0,
            if(JSONObject(response).has("preview")) JSONObject(response).getJSONObject("preview").getInt("width") else 0,
            if(JSONObject(response).has("address")) parseAddress(JSONObject(response).getString("address")) else ""
        )
    }

    fun parseAddress(string: String): String {
        var newString = string
        newString = newString.replace(Regex("\"country_code\":\"\\w*\","), "дом ")
        newString = newString.replace(Regex("\"\\w*\":"), "")
        newString = newString.replace(Regex("[{}]"), "")
        newString = newString.replace("\"", "")
        newString = newString.replace(",", ", ")
        newString = newString.replace("\\/", "/")
        return newString
    }
}
