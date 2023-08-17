package tv.dustypig.dustypig.api

/*
0x00000000 = Unknown
0x00000001 = Action
0x00000002 = Adventure
0x00000004 = Animation
0x00000008 = Anime
0x00000010 = Awards Show
0x00000020 = Children
0x00000040 = Comedy
0x00000080 = Crime
0x00000100 = Documentary
0x00000200 = Drama
0x00000400 = Family
0x00000800 = Fantasy
0x00001000 = Food
0x00002000 = Game Show
0x00004000 = History
0x00008000 = Home and Garden
0x00010000 = Horror
0x00020000 = Indie
0x00040000 = Martial Arts
0x00080000 = Mini Series
0x00100000 = Music
0x00200000 = Musical
0x00400000 = Mystery
0x00800000 = News
0x01000000 = Podcast
0x02000000 = Political
0x04000000 = Reality
0x08000000 = Romance
0x10000000 = Science Fiction
0x20000000 = Soap
0x40000000 = Sports
 */

enum class Genre(val value: Long) {
    Unknown(0x00000000),
    Action(0x00000001),
    Adventure(0x00000002),
    Animation(0x00000004),
    Anime(0x00000008),
    AwardsShow(0x00000010),
    Children(0x00000020),
    Comedy(0x00000040),
    Crime(0x00000080),
    Documentary(0x00000100),
    Drama(0x00000200),
    Family(0x00000400),
    Fantasy(0x00000800),
    Food(0x00001000),
    GameShow(0x00002000),
    History(0x00004000),
    HomeAndGarden(0x00008000),
    Horror(0x00010000),
    Indie(0x00020000),
    MartialArts(0x00040000),
    MiniSeries(0x00080000),
    Music(0x00100000),
    Musical(0x00200000),
    Mystery(0x00400000),
    News(0x00800000),
    Podcast(0x01000000),
    Political(0x02000000),
    Reality(0x04000000),
    Romance(0x08000000),
    ScienceFiction(0x10000000),
    Soap(0x20000000),
    Sports(0x40000000)
}

class Genres constructor() {

    constructor(fromVal: Long) : this() {
        value = fromVal
    }

    constructor(fromVal: Genre) : this() {
        value = fromVal.value
    }

    constructor(fromVal: Genres) : this() {
        value = fromVal.value
    }

    var value = Genre.Unknown.value
        private set

    fun or(genre: Genre) {
        value = value.or(genre.value)
    }

    fun and(genre: Genre) {
        value = value.and(genre.value)
    }

    fun has(genre: Genre) =
        value.and(genre.value) == genre.value

    fun del(genre: Genre) {
        value = value.and(genre.value.inv())
    }


    fun toList(): List<String> {
        val ret = ArrayList<String>()

        if(has(Genre.Action)) ret.add("Action")
        if(has(Genre.Adventure)) ret.add("Adventure")
        if(has(Genre.Animation)) ret.add("Animation")
        if(has(Genre.Anime)) ret.add("Anime")
        if(has(Genre.AwardsShow)) ret.add("Awards Show")
        if(has(Genre.Children)) ret.add("Children")
        if(has(Genre.Comedy)) ret.add("Comedy")
        if(has(Genre.Crime)) ret.add("Crime")
        if(has(Genre.Documentary)) ret.add("Documentary")
        if(has(Genre.Drama)) ret.add("Drama")
        if(has(Genre.Family)) ret.add("Family")
        if(has(Genre.Fantasy)) ret.add("Fantasy")
        if(has(Genre.Food)) ret.add("Food")
        if(has(Genre.GameShow)) ret.add("Game Show")
        if(has(Genre.History)) ret.add("History")
        if(has(Genre.HomeAndGarden)) ret.add("Home and Garden")
        if(has(Genre.Horror)) ret.add("Horror")
        if(has(Genre.Indie)) ret.add("Indie")
        if(has(Genre.MartialArts)) ret.add("Martial Arts")
        if(has(Genre.MiniSeries)) ret.add("Mini Series")
        if(has(Genre.Music)) ret.add("Music")
        if(has(Genre.Musical)) ret.add("Musical")
        if(has(Genre.Mystery)) ret.add("Mystery")
        if(has(Genre.News)) ret.add("News")
        if(has(Genre.Podcast)) ret.add("Podcast")
        if(has(Genre.Political)) ret.add("Political")
        if(has(Genre.Reality)) ret.add("Reality")
        if(has(Genre.Romance)) ret.add("Romance")
        if(has(Genre.ScienceFiction)) ret.add("Science Fiction")
        if(has(Genre.Soap)) ret.add("Soap")
        if(has(Genre.Sports)) ret.add("Sports")

        return ret.toList()
    }
}