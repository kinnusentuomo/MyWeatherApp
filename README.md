# MyWeatherApp
Android application to fetch current weather data

Ominaisuudet
* Säie, joka hakee datan Rest API:n kautta JSON muodossa
* Viewpager, johon liitetään fragmentteja kustomoidun adapterin avulla
* Erilaisia fragmentteja, jotka sisältävät mm. MapView karttanäkymän, ListView listanäkymän ja kustomoidun monista elementeistä koostuvan näkymän
* Widget, jonka päivitystiheys on asetettu kiinteäksi 30min välein
* Sovelluksen saadessa paikkatieto-oikeuden, se hakee automaattisesti säätiedot nykyisestä olinpaikasta (viimeksi saatu olinpaikka)
* Service, joka tarkistaa sään tilan nykyisestä olinpaikasta joka 60. minuutti. Mikäli olinpaikassa on sateista, service lähettää notifikaation sään tilasta.
