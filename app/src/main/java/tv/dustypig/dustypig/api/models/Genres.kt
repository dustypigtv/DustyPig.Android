package tv.dustypig.dustypig.api.models

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

data class GenrePair(
    val genre: Genre,
    val text: String
) {
    companion object {
        fun fromGenre(genre: Genre): GenrePair {
            if (genre == Genre.Action) return GenrePair(genre = Genre.Action, text = "Action")
            if (genre == Genre.Adventure) return GenrePair(
                genre = Genre.Adventure,
                text = "Adventure"
            )
            if (genre == Genre.Animation) return GenrePair(
                genre = Genre.Animation,
                text = "Animation"
            )
            if (genre == Genre.Anime) return GenrePair(genre = Genre.Anime, text = "Anime")
            if (genre == Genre.AwardsShow) return GenrePair(
                genre = Genre.AwardsShow,
                text = "AwardsShow"
            )
            if (genre == Genre.Children) return GenrePair(genre = Genre.Children, text = "Children")
            if (genre == Genre.Comedy) return GenrePair(genre = Genre.Comedy, text = "Comedy")
            if (genre == Genre.Crime) return GenrePair(genre = Genre.Crime, text = "Crime")
            if (genre == Genre.Documentary) return GenrePair(
                genre = Genre.Documentary,
                text = "Documentary"
            )
            if (genre == Genre.Drama) return GenrePair(genre = Genre.Drama, text = "Drama")
            if (genre == Genre.Family) return GenrePair(genre = Genre.Family, text = "Family")
            if (genre == Genre.Fantasy) return GenrePair(genre = Genre.Fantasy, text = "Fantasy")
            if (genre == Genre.Food) return GenrePair(genre = Genre.Food, text = "Food")
            if (genre == Genre.GameShow) return GenrePair(
                genre = Genre.GameShow,
                text = "Game Show"
            )
            if (genre == Genre.History) return GenrePair(genre = Genre.History, text = "History")
            if (genre == Genre.HomeAndGarden) return GenrePair(
                genre = Genre.HomeAndGarden,
                text = "Home And Garden"
            )
            if (genre == Genre.Horror) return GenrePair(genre = Genre.Horror, text = "Horror")
            if (genre == Genre.Indie) return GenrePair(genre = Genre.Indie, text = "Indie")
            if (genre == Genre.MartialArts) return GenrePair(
                genre = Genre.MartialArts,
                text = "Martial Arts"
            )
            if (genre == Genre.MiniSeries) return GenrePair(
                genre = Genre.MiniSeries,
                text = "Mini Series"
            )
            if (genre == Genre.Music) return GenrePair(genre = Genre.Music, text = "Music")
            if (genre == Genre.Musical) return GenrePair(genre = Genre.Musical, text = "Musical")
            if (genre == Genre.Mystery) return GenrePair(genre = Genre.Mystery, text = "Mystery")
            if (genre == Genre.News) return GenrePair(genre = Genre.News, text = "News")
            if (genre == Genre.Podcast) return GenrePair(genre = Genre.Podcast, text = "Podcast")
            if (genre == Genre.Political) return GenrePair(
                genre = Genre.Political,
                text = "Political"
            )
            if (genre == Genre.Reality) return GenrePair(genre = Genre.Reality, text = "Reality")
            if (genre == Genre.Romance) return GenrePair(genre = Genre.Romance, text = "Romance")
            if (genre == Genre.ScienceFiction) return GenrePair(
                genre = Genre.ScienceFiction,
                text = "Science Fiction"
            )
            if (genre == Genre.Soap) return GenrePair(genre = Genre.Soap, text = "Soap")
            if (genre == Genre.Sports) return GenrePair(genre = Genre.Sports, text = "Sports")

            return GenrePair(genre = Genre.Unknown, text = "Unknown")
        }
    }
}

class Genres() {

    constructor(fromVal: Long) : this() {
        value = fromVal
    }

    var value = Genre.Unknown.value
        private set

    fun or(genre: Genre) {
        value = value.or(genre.value)
    }

    fun and(genre: Genre) {
        value = value.and(genre.value)
    }

    private fun has(genre: Genre) =
        value.and(genre.value) == genre.value


    fun toList(): List<GenrePair> {
        val ret = ArrayList<GenrePair>()

        if (has(Genre.Action)) ret.add(GenrePair.fromGenre(Genre.Action))
        if (has(Genre.Adventure)) ret.add(GenrePair.fromGenre(Genre.Adventure))
        if (has(Genre.Animation)) ret.add(GenrePair.fromGenre(Genre.Animation))
        if (has(Genre.Anime)) ret.add(GenrePair.fromGenre(Genre.Anime))
        if (has(Genre.AwardsShow)) ret.add(GenrePair.fromGenre(Genre.AwardsShow))
        if (has(Genre.Children)) ret.add(GenrePair.fromGenre(Genre.Children))
        if (has(Genre.Comedy)) ret.add(GenrePair.fromGenre(Genre.Comedy))
        if (has(Genre.Crime)) ret.add(GenrePair.fromGenre(Genre.Crime))
        if (has(Genre.Documentary)) ret.add(GenrePair.fromGenre(Genre.Documentary))
        if (has(Genre.Drama)) ret.add(GenrePair.fromGenre(Genre.Drama))
        if (has(Genre.Family)) ret.add(GenrePair.fromGenre(Genre.Family))
        if (has(Genre.Fantasy)) ret.add(GenrePair.fromGenre(Genre.Fantasy))
        if (has(Genre.Food)) ret.add(GenrePair.fromGenre(Genre.Food))
        if (has(Genre.GameShow)) ret.add(GenrePair.fromGenre(Genre.GameShow))
        if (has(Genre.History)) ret.add(GenrePair.fromGenre(Genre.History))
        if (has(Genre.HomeAndGarden)) ret.add(GenrePair.fromGenre(Genre.HomeAndGarden))
        if (has(Genre.Horror)) ret.add(GenrePair.fromGenre(Genre.Horror))
        if (has(Genre.Indie)) ret.add(GenrePair.fromGenre(Genre.Indie))
        if (has(Genre.MartialArts)) ret.add(GenrePair.fromGenre(Genre.MartialArts))
        if (has(Genre.MiniSeries)) ret.add(GenrePair.fromGenre(Genre.MiniSeries))
        if (has(Genre.Music)) ret.add(GenrePair.fromGenre(Genre.Music))
        if (has(Genre.Musical)) ret.add(GenrePair.fromGenre(Genre.Musical))
        if (has(Genre.Mystery)) ret.add(GenrePair.fromGenre(Genre.Mystery))
        if (has(Genre.News)) ret.add(GenrePair.fromGenre(Genre.News))
        if (has(Genre.Podcast)) ret.add(GenrePair.fromGenre(Genre.Podcast))
        if (has(Genre.Political)) ret.add(GenrePair.fromGenre(Genre.Political))
        if (has(Genre.Reality)) ret.add(GenrePair.fromGenre(Genre.Reality))
        if (has(Genre.Romance)) ret.add(GenrePair.fromGenre(Genre.Romance))
        if (has(Genre.ScienceFiction)) ret.add(GenrePair.fromGenre(Genre.ScienceFiction))
        if (has(Genre.Soap)) ret.add(GenrePair.fromGenre(Genre.Soap))
        if (has(Genre.Sports)) ret.add(GenrePair.fromGenre(Genre.Sports))

        return ret.toList()
    }
}