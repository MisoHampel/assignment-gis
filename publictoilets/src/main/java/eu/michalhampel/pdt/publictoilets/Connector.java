package eu.michalhampel.pdt.publictoilets;

import org.json.JSONObject;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class Connector {

    private Connection conn = null;

    public Connector() {
        try {
            Class.forName("org.postgresql.Driver");
            String url = "jdbc:postgresql://localhost:5432/publictoilets";
            conn = DriverManager.getConnection(url, "postgres", "Halabala123");
        }catch( Exception e ) {
            e.printStackTrace();
        }
    }

    public Statement createStatement(){
        try {
            return conn.createStatement();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }


    public List<JSONObject> getAll(Integer limit) throws SQLException {
        List<JSONObject> geoJsons = new ArrayList<>();

        ResultSet result = createStatement().executeQuery("SELECT ST_AsGeoJSON(ST_Point( \"longitude\",\"latitude\"),4326) AS result, \"Name\" FROM \"toilet\"  ORDER BY \"Name\" LIMIT "+limit);
        while(result.next()) {
            JSONObject json = new JSONObject();
            json.put("type", "Feature");
            json.put("geometry", new JSONObject(result.getString("result")));
            JSONObject properties = new JSONObject();
            properties.put("title", result.getString("name"));
//            properties.put("marker-color", "#fc4353");
//            properties.put("marker-size", "large");
//            properties.put("marker-symbol", "fuel");
            json.put("properties", properties);
            geoJsons.add(json);
        }

        return geoJsons;
    }
    //select name,  ST_AsGeoJSON(ST_Union(geom)) from australia_countries where name like 'Western Australia' group by name
    public List<JSONObject> getBoundaryCountryByName(String name) throws SQLException {
        List<JSONObject> boundaries = new ArrayList<>();
        ResultSet result = createStatement().executeQuery("select name,  ST_AsGeoJSON(geom) as geometry from australia_countries where name like '"+name+"'");
        while (result.next()){
            JSONObject json = new JSONObject();
            json.put("name",result.getString("name"));
            json.put("type", "Feature");
            json.put("geometry",new JSONObject( result.getString("geometry")));
            boundaries.add(json);
        }
        return boundaries;
    }

    public List<JSONObject> getBoundaryDistrictsByName(String name) throws SQLException {
        List<JSONObject> boundaries = new ArrayList<>();
        ResultSet result = createStatement().executeQuery("select name,  ST_AsGeoJSON(geom) as geometry from australia_districts where name like '"+name+"'");
        while (result.next()){
            JSONObject json = new JSONObject();
            json.put("name",result.getString("name"));
            json.put("type", "Feature");
            json.put("geometry",new JSONObject( result.getString("geometry")));
            boundaries.add(json);
        }
        return boundaries;
    }


    public List<JSONObject> getBoundaryById(Integer id) throws SQLException {
        List<JSONObject> boundaries = new ArrayList<>();
        ResultSet result = createStatement().executeQuery("SELECT id, name,ST_AsGeoJSON((ST_Dump(geom)).geom) as geometry FROM public.australiaboundaries WHERE id = "+id);
        while (result.next()){
            JSONObject json = new JSONObject();
            json.put("name",result.getString("name"));
            json.put("id",result.getString("id"));
            json.put("type", "Feature");
            json.put("geometry",new JSONObject( result.getString("geometry")));
            boundaries.add(json);
        }
        return boundaries;
    }

    public List<JSONObject> getPointsInsideBoundaryById(Integer id, List<String> filter) throws SQLException {
        List<JSONObject> geoJsons = new ArrayList<>();
        String sqlWhere = getFilterQuery(filter).equals("")?"":(" and "+getFilterQuery(filter));
//         starsi pristup
//        ResultSet result = createStatement().executeQuery("SELECT  *, ST_AsGeoJSON(geom) as geometry  FROM toilet WHERe st_contains(ST_Transform(ST_SetSRID((select geom from australiaboundaries where id ="+id+" ),4326),4326),  geom)=true" +sqlWhere);
//        po optimalizovani
        ResultSet result = createStatement().executeQuery("SELECT t.*,ST_AsGeoJSON(t.geom) as geometry  FROM  toilet AS t,  australia_boundaries_result AS r WHERE r.id="+id+" and ST_CONTAINS(r.geom, t.geom) " +sqlWhere);
        parseResultFromToilets(geoJsons, result);
        return geoJsons;
    }

    public List<JSONObject> getHeatMapCountries() throws SQLException {
        List<JSONObject> geoJsons = new ArrayList<>();
//        String sqlWhere = getFilterQuery(filter).equals("")?"":(" and "+getFilterQuery(filter));
        ResultSet result = createStatement().executeQuery("SELECT a.name, count(a.name) as count  FROM  toilet AS t,  australia_countries AS a WHERE t.drinkingwater like 'True' and ST_CONTAINS(a.geom, t.geom) group by a.name order by count desc");
        while(result.next()) {
            JSONObject boundary = new JSONObject();
            boundary.put("name",result.getString("name"));
            boundary.put("count",result.getInt("count"));
            geoJsons.add(boundary);
        }
        return geoJsons;
    }


    public List<JSONObject> getHeatMapDistricts() throws SQLException {
        List<JSONObject> geoJsons = new ArrayList<>();
//        String sqlWhere = getFilterQuery(filter).equals("")?"":(" and "+getFilterQuery(filter));
        ResultSet result = createStatement().executeQuery("SELECT a.name, count(a.name) as count  FROM  toilet AS t,  australia_districts AS a WHERE t.drinkingwater like 'True' and ST_CONTAINS(a.geom, t.geom) group by a.name order by count desc");
        while(result.next()) {
            JSONObject boundary = new JSONObject();
            boundary.put("name",result.getString("name"));
            boundary.put("count",result.getInt("count"));
            geoJsons.add(boundary);
        }
        return geoJsons;
    }

    public List<JSONObject> getPointsInsidePolygon(String json, List<String> filter) throws SQLException {
        List<JSONObject> geoJsons = new ArrayList<>();
        String sqlWhere = getFilterQuery(filter).equals("")?"":(" and "+getFilterQuery(filter));
        ResultSet result = createStatement().executeQuery("SELECT  *, ST_AsGeoJSON(geom) as geometry  FROM toilet WHERe st_contains(ST_Transform(ST_SetSRID(((SELECT ST_GeomFromGeoJSON(feat->>'geometry') as geom FROM (  SELECT json_array_elements('"+json+"'::json->'features') AS feat) AS f)),4326),4326),  geom)=true"+sqlWhere);
        parseResultFromToilets(geoJsons, result);
        return geoJsons;
    }



    public List<JSONObject> getAustralianBoundariesList() throws SQLException {
        List<JSONObject> boundaries = new ArrayList<>();
        ResultSet result = createStatement().executeQuery("SELECT id,name FROM public.australiaboundaries");
        while (result.next()){
            JSONObject boundary = new JSONObject();
            boundary.put("name",result.getString("name"));
            boundary.put("id",result.getString("id"));
            boundaries.add(boundary);
        }
        return boundaries;
    }

    public List<JSONObject> getNearest(Double longitude, Double latitude, Integer distance, List<String> filter) throws SQLException {
        List<JSONObject> geoJsons = new ArrayList<>();
//        Po optimalizacii som upravil query
//        String sqlWhere = getFilterQuery(filter).equals("")?("WHERE \"distance\" < "+ distance):(("WHERE \"distance\" < "+ distance)+" "+getFilterQuery(filter));
//        ResultSet result1 = createStatement().executeQuery("SELECT * FROM (SELECT ST_Distance_Sphere(ST_Point("+longitude+","+latitude+"), ST_Point(\"longitude\",\"latitude\")) AS \"distance\", ST_AsGeoJSON(ST_Point(\"longitude\",\"latitude\"),4326) AS \"result\", \"name\", * FROM \"toilet\"  ORDER BY \"distance\" ) as \"res\" "+sqlWhere);
        String sqlWhere = getFilterQuery(filter).equals("")?"":(" and ("+getFilterQuery(filter)+")");
        ResultSet result = createStatement().executeQuery("SELECT *, ST_AsGeoJSON(geom) as geometry  FROM toilet WHERE ST_DWithin(geom, ST_MakePoint("+longitude+","+latitude+")::geography, "+distance+")" + sqlWhere);
        parseResultFromToilets(geoJsons, result);


        return geoJsons;
    }

    public List<JSONObject> getNearestCount(Double longitude, Double latitude, Integer limit, List<String> filter) throws SQLException {
        List<JSONObject> geoJsons = new ArrayList<>();
        String sqlWhere = getFilterQuery(filter).equals("")?"":(" WHERE "+getFilterQuery(filter));
//        ResultSet result = createStatement().executeQuery("SELECT *, ST_AsGeoJSON(geom) as geometry FROM (SELECT ST_Distance_Sphere(ST_Point("+longitude+","+latitude+"), ST_Point(\"longitude\",\"latitude\")) AS \"distance\", ST_AsGeoJSON(ST_Point(\"longitude\",\"latitude\"),4326) AS \"result\", \"name\", * FROM \"toilet\"  ORDER BY \"distance\" ) as \"res\" "+sqlWhere +" "+sqlLimit);
        ResultSet result = createStatement().executeQuery("SELECT *, ST_AsGeoJSON(geom) as geometry  FROM toilet "+sqlWhere+" ORDER BY geom <-> st_setsrid(st_makepoint("+longitude+","+latitude+"),4326) LIMIT "+limit);
        parseResultFromToilets(geoJsons, result);

        return geoJsons;
    }

    private void parseResultFromToilets(List<JSONObject> geoJsons, ResultSet result) throws SQLException {
        while(result.next()) {
            JSONObject json = new JSONObject();
            json.put("type", "Feature");
//            json.put("geometry", new JSONObject(result.getString("result")));
            json.put("geometry", new JSONObject(result.getString("geometry")));
            json.put("properties", getProperties(result));
            geoJsons.add(json);
        }
    }

    public List<JSONObject> getNearestPoi(Double longitude, Double latitude, Integer distance) throws SQLException {
        List<JSONObject> geoJsons = new ArrayList<>();
        ResultSet result = createStatement().executeQuery("SELECT * FROM poi WHERE ST_DWithin(ST_Transform(way, 4326), ST_MakePoint(" + longitude + " , " + latitude + ")::geography," + distance + ")");
        while(result.next()) {
            JSONObject json = new JSONObject();
            json.put("type", "Feature");
//            json.put("geometry", new JSONObject(result.getString("result")));
            json.put("geometry", new JSONObject(result.getString("geojson")));
            geoJsons.add(json);
        }
        return geoJsons;
    }

    private JSONObject getProperties(ResultSet resultSet){
        JSONObject properties = new JSONObject();
        try {
            if(resultSet.getString("unisex").equalsIgnoreCase("true")){
                properties.put("icon","unisex");
                properties.put("font-color","#000000");
            }else if(resultSet.getString("male").equalsIgnoreCase("true")
                    && resultSet.getString("female").equalsIgnoreCase("true")){
                properties.put("icon","toilet");
                properties.put("font-color","#007fff");
            }else if(resultSet.getString("male").equalsIgnoreCase("true")){
                properties.put("icon","male");
                properties.put("font-color","#007fff");
            }else if(resultSet.getString("female").equalsIgnoreCase("true")){
                properties.put("icon","female");
                properties.put("font-color","#ff5656");
            }
            properties.put("name", resultSet.getString("name"));
            properties.put("address", resultSet.getString("address1"));
            properties.put("town", resultSet.getString("town"));
            properties.put("state", resultSet.getString("state"));
            properties.put("postcode", resultSet.getString("postcode"));
            properties.put("isopen", resultSet.getString("isopen"));
            properties.put("marker-symbol","toilets");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return properties;
    }

    public String getFilterQuery(List<String>filters){
        if(filters.size()!=0) {
            StringBuilder sb = new StringBuilder();
            sb.append(" ");
            for (String filter : filters) {
                String[] split = filter.split(":");
                if(split[1].equalsIgnoreCase("True")) {
                    sb.append("\"" + split[0] + "\"");
                    sb.append(" like ");
                    split[1]=split[1].substring(0, 1).toUpperCase() + split[1].substring(1);
                    sb.append("\'" + split[1] + "\'");
                    sb.append(" ");
                    sb.append("or ");
                }
            }
            return sb.toString().equals(" ")?"":sb.substring(0,sb.length()-3);
        }
        return "";
    }
    //nearest in meters SELECT ST_Distance_Sphere(ST_Point(150,30), ST_Point("longitude","latitude")) AS distance, ST_Point("longitude","latitude") AS result, "name" FROM "Toilet"  ORDER BY distance
}
