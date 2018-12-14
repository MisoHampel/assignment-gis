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

Backend aplikácia je napísaná v Jave. Aplikácia je spustená na webovom server Tomcat a funguje ako REST server, ktorý počúva na dopyty a odpovedá v o forme JSON správ.

## Data

Dáta mám z viacerých zdrojov. Hlavný zdroj z ktorého sa skladá jadro aplikácie sa nachádza na webovej lokalite pre otvorené dáta Austrálie: `https://data.gov.au/dataset/national-public-toilet-map/resource/54566d76-a809-4959-8622-61dc30b3114d`
Hranice oblastí Austrálie: `https://www.igismap.com/australia-shapefile-download/`
Ostatné dáta vo formáte osm: `http://download.geofabrik.de/australia-oceania/australia.html` - tieto dáta bolo potrebné naimportovať do databázy pomocou nástroja osm2pgsql

## Api

**Find hotels in proximity to coordinates**

`GET /search?lat=25346&long=46346123`

**Find hotels by name, sorted by proximity and quality**

`GET /search?name=hviezda&lat=25346&long=46346123`

### Response

API calls return json responses with 2 top-level keys, `hotels` and `geojson`. `hotels` contains an array of hotel data for the sidebar, one entry per matched hotel. Hotel attributes are (mostly self-evident):
```
{
  "name": "Modra hviezda",
  "style": "modern", # cuisine style
  "stars": 3,
  "address": "Panska 31"
  "image_url": "/assets/hotels/652.png"
}
```
`geojson` contains a geojson with locations of all matched hotels and style definitions.
