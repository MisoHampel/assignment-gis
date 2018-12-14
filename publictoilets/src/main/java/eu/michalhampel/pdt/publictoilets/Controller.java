package eu.michalhampel.pdt.publictoilets;

import org.json.JSONArray;
import org.json.JSONObject;

import javax.ws.rs.*;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.MediaType;
import java.sql.SQLException;
import java.util.List;

@ApplicationPath("")
@Path("")
public class Controller extends Application {

    Connector connector = new Connector();

    @GET
    @Path("/all/{limit}")
    @Produces(MediaType.APPLICATION_JSON)
    public String getAll(@PathParam("limit") Integer limit) {
        JSONArray array = new JSONArray();
        List<JSONObject> result = null;
        JSONObject tores = new JSONObject();
        try {
            result = connector.getAll(limit);

            for (JSONObject json : result) {
                array.put(json);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        tores.put("type","FeatureCollection");
        tores.put("features",array);
        return tores.toString();
    }

    @GET
    @Path("/allboundaries")
    @Produces(MediaType.APPLICATION_JSON)
    public String getListBoundaries() {
        JSONArray array = new JSONArray();
        List<JSONObject> result = null;
        try {
            result = connector.getAustralianBoundariesList();
            for (JSONObject json : result) {
                array.put(json);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return array.toString();
    }

    @GET
    @Path("/heatmapcountries")
    @Produces(MediaType.APPLICATION_JSON)
    public String getHeatMapCountries() {
        JSONArray array = new JSONArray();
        List<JSONObject> result = null;
        try {
            result = connector.getHeatMapCountries();
            for (JSONObject json : result) {
                array.put(json);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return array.toString();
    }

    @GET
    @Path("/heatmapdistricts")
    @Produces(MediaType.APPLICATION_JSON)
    public String getHeatMapDistricts() {
        JSONArray array = new JSONArray();
        List<JSONObject> result = null;
        try {
            result = connector.getHeatMapDistricts();
            for (JSONObject json : result) {
                array.put(json);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return array.toString();
    }

    @GET
    @Path("/boundary")
    @Produces(MediaType.APPLICATION_JSON)
    public String getBoundaryById(@QueryParam("id") Integer id) {
        JSONArray array = new JSONArray();
        List<JSONObject> result = null;
        JSONObject tores = new JSONObject();
        try {
            result = connector.getBoundaryById(id);

            for (JSONObject json : result) {
                array.put(json);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        tores.put("type","FeatureCollection");
        tores.put("features",array);
        return tores.toString();
    }

    @GET
    @Path("/boundarycountriesname")
    @Produces(MediaType.APPLICATION_JSON)
    public String getBoundaryCountryByName(@QueryParam("name") String name,@QueryParam("count") String count) {
        JSONArray array = new JSONArray();
        List<JSONObject> result = null;
        JSONObject tores = new JSONObject();
        try {
            result = connector.getBoundaryCountryByName(name);

            for (JSONObject json : result) {
                array.put(json);
                tores.put("name",json.get("name"));
                tores.put("count",count);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        tores.put("type","FeatureCollection");
        tores.put("features",array);
        return tores.toString();
    }

    @GET
    @Path("/boundarydistrictsname")
    @Produces(MediaType.APPLICATION_JSON)
    public String getBoundaryDistrictsByName(@QueryParam("name") String name,@QueryParam("count") String count) {
        JSONArray array = new JSONArray();
        List<JSONObject> result = null;
        JSONObject tores = new JSONObject();
        try {
            result = connector.getBoundaryDistrictsByName(name);

            for (JSONObject json : result) {
                array.put(json);
                tores.put("name",json.get("name"));
                tores.put("count",count);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        tores.put("type","FeatureCollection");
        tores.put("features",array);
        return tores.toString();
    }

    @GET
    @Path("/boundarypoints")
    @Produces(MediaType.APPLICATION_JSON)
    public String getPointsInsideBoundaryById(@QueryParam("id") Integer id,@QueryParam("filter") List<String> filter) {
        JSONArray array = new JSONArray();
        List<JSONObject> result = null;
        JSONObject tores = new JSONObject();
        try {
            result = connector.getPointsInsideBoundaryById(id,filter);

            for (JSONObject json : result) {
                array.put(json);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        tores.put("type","FeatureCollection");
        tores.put("features",array);
        return tores.toString();
    }

    @GET
    @Path("/polygon")
    @Produces(MediaType.APPLICATION_JSON)
    public String getPointsInsidePolygon(@QueryParam("geojson") String geojson,@QueryParam("filter") List<String> filter) {
        JSONArray array = new JSONArray();
        List<JSONObject> result = null;
        JSONObject tores = new JSONObject();
        try {
            result = connector.getPointsInsidePolygon(geojson,filter);

            for (JSONObject json : result) {
                array.put(json);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        tores.put("type","FeatureCollection");
        tores.put("features",array);
        return tores.toString();
    }

    @GET
    @Path("/nearest")
    @Produces(MediaType.APPLICATION_JSON)
    public String getNearest(@QueryParam("longitude") Double longitude, @QueryParam("latitude") Double latitude, @QueryParam("distance") Integer distance,@QueryParam("filter") List<String> filter) {
        JSONArray array = new JSONArray();
        List<JSONObject> result = null;
        JSONObject tores = new JSONObject();
        try {
            result = connector.getNearest(longitude,latitude,distance,filter);

            for (JSONObject json : result) {
                array.put(json);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        tores.put("type","FeatureCollection");
        tores.put("features",array);
        return tores.toString();
    }

    @GET
    @Path("/nearestpoi")
    @Produces(MediaType.APPLICATION_JSON)
    public String getNearestPoi(@QueryParam("longitude") Double longitude, @QueryParam("latitude") Double latitude, @QueryParam("distance") Integer distance) {
        JSONArray array = new JSONArray();
        List<JSONObject> result = null;
        JSONObject tores = new JSONObject();
        try {
            result = connector.getNearestPoi(longitude,latitude,distance);

            for (JSONObject json : result) {
                array.put(json);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        tores.put("type","FeatureCollection");
        tores.put("features",array);
        return tores.toString();
    }

    @GET
    @Path("/nearestcount")
    @Produces(MediaType.APPLICATION_JSON)
    public String getNearestCount(@QueryParam("longitude") Double longitude, @QueryParam("latitude") Double latitude, @QueryParam("limit") Integer limit,@QueryParam("filter")List<String> filter) {
        JSONArray array = new JSONArray();
        List<JSONObject> result = null;
        JSONObject tores = new JSONObject();
        try {
            result = connector.getNearestCount(longitude,latitude,limit,filter);

            for (JSONObject json : result) {
                array.put(json);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        tores.put("type","FeatureCollection");
        tores.put("features",array);
        return tores.toString();
    }


//    @GET
//    @Path("")
//    @Produces(MediaType.TEXT_HTML)
//    public InputStream getIndex() {;
//            return getClass().getClassLoader().getResourceAsStream("index.html");
//
//    }

}
