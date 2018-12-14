# Overview
Táto aplikácia dokáže zobrazovať verejné záchody po celej Austrálii. Scenáre, ktoré dokáže aplikácia zobraziť používateľovi
-	Keďže sa aplikácia nachádza mimo územia SR, je to zobrazený marker, ktorý simuluje aktuálnu polohu
-	Zobrazenie N najbližších verejných záchodov na základe aktuálnej polohy. (aj filtre)
-	Zobrazenie najbližších záchodov na základe zadanej vzdialenosti od aktuálnej polohy. (aj filtre)
-	Zapnutie filtrov, ak sú filtre zapojené zapoja sa do viacerých scenárov.
-	Zobrazenie území ako polygón = výber z combo boxa
-	Zobrazenie verejných záchodov v určitom polygóne (aj filtre)
-	Nakreslenie polygónu a zobrazenie verejných záchodov v ňom

#Screenshots  

- Úvodná obrazovka
![Screenshot](screenshots/screenshot01.png)

- Zobrazenie 10 najbližších verejných záchodov. Klik na záchod otvorý popup popis
![Screenshot](screenshots/screenshot02.png)

- Zobrazenie najbližsích verejných záchodov v okruhu 300km
![Screenshot](screenshots/screenshot03.png)

- Zobrazenie najbližsích verejných záchodov v okruhu 300km. Spustený filter pitná voda
![Screenshot](screenshots/screenshot04.png)

- Zobrazenie hraníc oblasti Western Austrália
![Screenshot](screenshots/screenshot05.png)

- Zobrazenie verejných záchodov v oblasti Western Austrália
![Screenshot](screenshots/screenshot06.png)

- Zobrazenie heatmapy, ktorá znázoňuje v ktorých oblastiach je najviac a naopak najmenej pitnej vody na verejných záchodoch
![Screenshot](screenshots/screenshot07.png)

- Zobrazenie heatmapy, ktorá znázoňuje v ktorých mestách (okresoch) je najviac a naopak najmenej pitnej vody na verejných záchodoch
![Screenshot](screenshots/screenshot08.png)

- Zobrazenie POI v oblasti 100km od verejného záchoda na ktorý sa klikne dvojklikom
![Screenshot](screenshots/screenshot09.png)

- Nakreslenie polygónu a zobrazenie verejných záchodov v jeho vnútri
![Screenshot](screenshots/screenshot10.png)


# Frontend
Frontend aplikácia pozostáva zo statickej html stránky. Html stránka je pridaná ako resource aplikačného servera a teda po jeho naštartovaní  je k dispozícii na zobrazenie. Index.html obsahuje skript (script.js), v ktorej je celá logika frontendu a to teda volania na backend a zobrazovanie dopytov na stránku. Na zobrazovanie sa využíva mapbox gl knižnica, ktorá je taktiež referencovaná v hlavnej stránke. Index.html do seba vkladá kaskádové štýly pre lepší dizajn aplikácie (styles.css), ďalej obsahuje menu.js, ktoré sa stará o ovládanie menu.

# Backend

Backend aplikácia je napísaná v Jave. Aplikácia je spustená na webovom server Tomcat a funguje ako REST server, ktorý počúva na dopyty a odpovedá v o forme JSON správ. Pozostáva z dvoch hlavných Java tried `Connector.java` a `Controller.java`. Connector sa stara o pripojenie k databáze a obstarávanie GET requestov. Controller sa stará o zostrojenie query a jeho vykonanie a poskladanie výslednej JSON správy.

## Data

Dáta mám z viacerých zdrojov. Hlavný zdroj z ktorého sa skladá jadro aplikácie sa nachádza na webovej lokalite pre otvorené dáta Austrálie: `https://data.gov.au/dataset/national-public-toilet-map/resource/54566d76-a809-4959-8622-61dc30b3114d`
Hranice oblastí Austrálie: `https://www.igismap.com/australia-shapefile-download/`
Ostatné dáta vo formáte osm: `http://download.geofabrik.de/australia-oceania/australia.html` - tieto dáta bolo potrebné naimportovať do databázy pomocou nástroja osm2pgsql


## Api

**Nájdenie N najbližších verejnych záchodov**

`GET /nearestcount?longitude135&latitude-25&limit10`

**Response**

```JSON
{
  "features": [
    {
      "geometry": {
        "coordinates": [
          135.2484994,
          -26.07050006
        ],
        "type": "Point"
      },
      "type": "Feature",
      "properties": {
        "font-color": "#000000",
        "isopen": "Variable",
        "marker-symbol": "toilets",
        "address": "Mount Dare Road",
        "town": "Finke",
        "icon": "unisex",
        "name": "Witjira National Park 1",
        "postcode": "0872",
        "state": "Northern Territory"
      }
    },
    ...
  ]
}
```

**Nájdenie najbližšie verejne záchody v okruhu N km**

`GET /nearest?longitude=150.82031250000546&latitude=-33.62742849477819&distance=10000`

**Response**

```JSON
{
  "features": [
    {
      "geometry": {
        "coordinates": [
          150.80352581,
          -33.55922095
        ],
        "type": "Point"
      },
      "type": "Feature",
      "properties": {
        "font-color": "#007fff",
        "isopen": "AllHours",
        "marker-symbol": "toilets",
        "address": "356 Kurmond Road",
        "town": "Freemans Reach",
        "icon": "toilet",
        "name": "Freemans Reach Park",
        "postcode": "2756",
        "state": "New South Wales"
      }
    },
    ...
  ]
}
```

**Zobrazenie hraníc oblasti**

`GET /boundary?id=2316598`

**Response**

```JSON
{
  "features": [
    {
      "name": "Western Australia",
      "geometry": {
        "coordinates": [
          [
            [
              117.8714232,
              -35.1918585
            ],
            [
              117.8695438,
              -35.191744
            ],
            ...
          ]
        ],
        "type": "Polygon"
      },
      "id": "2316598",
      "type": "Feature"
    },
    ...
  ]
}
```

**Zobrazenie verejnych záchodov v oblasti**

`GET /boundarypoints?id=2316598`

**Response**

```JSON
{
  "features": [
    {
      "geometry": {
        "coordinates": [
          115.52884845,
          -32.02241248
        ],
        "type": "Point"
      },
      "type": "Feature",
      "properties": {
        "font-color": "#007fff",
        "isopen": "AllHours",
        "marker-symbol": "toilets",
        "address": "Parker Point Road",
        "town": "Rottnest Island",
        "icon": "toilet",
        "name": "Parker Point",
        "postcode": "6161",
        "state": "Western Australia"
      }
    },
    ...
  ]
}
```


**Zobrazenie heatmapy oblasti**

`GET /heatmapcountries`

**Response**

```JSON
[
  {
    "name": "New South Wales",
    "count": 554
  },
  {
    "name": "Queensland",
    "count": 429
  },
  ...
]
```


**Zobrazenie heatmapy okresu**

`GET /heatmapdistricts`

**Response**

```JSON
[
  {
    "name": "Sutherland Shire Council",
    "count": 83
  },
  {
    "name": "Northern Beaches Council",
    "count": 66
  },
  {
  ...
]
```


**Nakreslenie polygónu a zobrazenie verejných záchodov vo vnútri**

`GET /polygon?geojson={"type":"FeatureCollection","features":[{"id":"ce13360faeebb97b4c82886e84e7cced","type":"Feature","properties":{},"geometry":{"coordinates":[[[148.7109374999971,-20.85265332853281],[152.5341796874908,-19.367123496979104],[156.7089843749859,-29.663236089948896],[143.6132812500055,-25.85329304169605],[148.7109374999971,-20.85265332853281]]],"type":"Polygon"}}]}`

**Response**

```JSON
{
  "features": [
    {
      "geometry": {
        "coordinates": [
          148.04524183,
          -22.01444418
        ],
        "type": "Point"
      },
      "type": "Feature",
      "properties": {
        "font-color": "#007fff",
        "isopen": "AllHours",
        "marker-symbol": "toilets",
        "address": "Moranbah Access Road",
        "town": "Moranbah",
        "icon": "toilet",
        "name": "Apex Park",
        "postcode": "4744",
        "state": "Queensland"
      }
    },
  ...
  ],
}
```


## Optimalisation

- Keďže hlavné dáta ktoré sa využívajú najviac neobsahovali stĺpec geometry, tak prvá optimalizácia bolo vytvorenie tohoto stĺpca na základe longituda a latitude a následnom vytvorení indexu na geometry stĺpci pretože sa používa pri dopytoch ci sa verejný záchod nachádza alebo nenachádza v nejakej oblasti.
```SQL
SELECT AddGeometryColumn('toilet', 'geom', 4326, 'POINT', 2);
UPDATE toilet SET geom = ST_SetSRID(ST_MakePoint(longitude, latitude), 4326);  
CREATE INDEX idx_toilet ON toilet USING gist(geom); 
```

Prvú optimalizáciu som následne využil na dohľadanie verených záchodov v okruhu. (Total cost sa zmenšil na 1/3)
```SQL
SELECT *, ST_AsGeoJSON(geom) as geometry
FROM toilet
WHERE ST_DWithin(geom, ST_MakePoint(135,-25)::geography, 200000); 
```

- Využitie <--> operátora - vráti najbližích susedov na zákalde centra geometrie. Nájdenie najbližších verejných záchodov
```SQL
SELECT *, ST_AsGeoJSON(geom) as geometry  
FROM toilet 
ORDER BY geom 
<-> 
st_setsrid(st_makepoint(135,-25),4326) 
LIMIT 10;
```

- Keďže dáta o hraniciach austrálskych oblastí mám veľmi presné, vznikol tu problem že rozne oblasti sa skladali z MULTIPOLYGONU ktorý obsahoval napríklad aj 6000 menších polygónov. Pri vyhľadávaní záchodov v tejto oblasti sa prehľadávali aj tei ktoré žiadny záchod neobsahovali a tak som vtvoril novú tabuľku bez týchto prázdnych polygónov.
```SQL
--multipolygon to polygons
create table australia_boundaries_polygons as 
SELECT id,(ST_Dump(geom)).geom as geometry 
FROM public.australiaboundaries

--polygony ktore obsahuju z8chody
create table australia_boundaries_contains_geometry as 
SELECT UNNEST("area_list") geom FROM (
SELECT array_agg(ar.geometry) as area_list
FROM australia_boundaries_polygons ar, toilet pl 
WHERE ST_Contains(ST_SETSRID(ar.geometry,4326), pl.geom) 
GROUP BY pl.toiletid
ORDER BY pl.toiletid) s group by geom; 

--sparovanie mena a geometrie
create table australia_boundaries_result as 
select abp.* from australia_boundaries_polygons abp 
join australia_boundaries_contains_geometry abcg on abp.geometry=abcg.geom order by id

```
Zmena z ![Screenshot](screenshots/opt2.png) na ![Screenshot](screenshots/opt1.png)

- Optimalizácie pre zobrazenie POI
```SQL
create table poi AS (select ST_AsGeoJSON(ST_Transform(way, 4326)) AS geojson, amenity, way 
FROM planet_osm_point 
where (amenity LIKE 'fuel' OR amenity LIKE 'bar' OR amenity LIKE 'cafe' OR amenity LIKE 'restaurant' )
order by amenity)

CREATE INDEX idx_poi ON poi(amenity);
```




