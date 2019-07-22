# MyWeatherApp
Android application to fetch current weather data

Ominaisuudet
* Säie, joka hakee datan Rest API:n kautta JSON muodossa
* Viewpager, johon liitetään fragmentteja kustomoidun adapterin avulla
* Erilaisia fragmentteja, jotka sisältävät mm. MapView karttanäkymän, ListView listanäkymän ja kustomoidun monista elementeistä koostuvan näkymän
* Kustomoitu MapView (teema)
* Widget, jonka päivitystiheys on asetettu kiinteäksi 30min välein
* Sovelluksen saadessa paikkatieto-oikeuden, se hakee automaattisesti säätiedot nykyisestä olinpaikasta (viimeksi saatu olinpaikka)
* Service, joka tarkistaa sään tilan nykyisestä olinpaikasta joka 60. minuutti. Mikäli olinpaikassa on sateista, service lähettää notifikaation sään tilasta.
* Asetukset (Settings View)
  - Oletussijainti voidaan asettaa etukäteen, jonka data haetaan jo sovelluksen käynnistyessä
  - Oletussijainti widgettiin voidaan asettaa, jolloin widget ei hae dataa nykyisestä olinpaikasta, vaan ennalta määritellystä sijainnista
  - Taustapalvelu, joka varoittaa nykyisen sijainnin sateisuudesta voidaan asettaa päälle/pois päältä


![alt text](https://github.com/tuomomees/MyWeatherApp/blob/master/app/src/main/res/screenshots/weather_details.jpeg "Weather Details")

![alt text](https://github.com/tuomomees/MyWeatherApp/blob/master/app/src/main/res/screenshots/weather_list.jpeg "Weather List")

![alt text](https://github.com/tuomomees/MyWeatherApp/blob/master/app/src/main/res/screenshots/weather_map.jpeg "Weather Map")

![alt text](https://github.com/tuomomees/MyWeatherApp/blob/master/app/src/main/res/screenshots/weather_widget.jpeg "Weather Widget")
