package com.example.smartvoice.ui.signup

object PasswordGenerator {
    private val englishWords = listOf(
        "abbey", "about", "above", "abuse", "abyss", "actor", "acute", "admit", "adopt", "adore",
        "adult", "after", "again", "agent", "agile", "agree", "ahead", "alarm", "album", "alert",
        "bacon", "badge", "badly", "baker", "banal", "banks", "baron", "basic", "basis", "batch",
        "beach", "beard", "beast", "began", "begin", "begin", "being", "belly", "below", "bench",
        "cable", "cadet", "caged", "cakes", "camel", "canal", "candy", "cards", "cargo", "carol",
        "cases", "catch", "cause", "caves", "cedar", "chain", "chair", "chaos", "charm", "chart",
        "daily", "dance", "dared", "dated", "dealt", "death", "debut", "decal", "decor", "decoy",
        "deity", "delay", "delta", "dense", "depth", "derby", "devil", "diary", "dined", "dingy",
        "eager", "eagle", "early", "earth", "easel", "eaten", "eater", "ebony", "edges", "edict",
        "elite", "email", "ember", "emery", "empty", "ended", "enemy", "enjoy", "enter", "entry",
        "fable", "faced", "facet", "faded", "fairy", "faith", "fallen", "fancy", "farce", "farce",
        "fatal", "fatty", "fault", "favor", "fears", "feast", "feint", "fella", "felon", "femal",
        "gaily", "gains", "gales", "games", "gangs", "gates", "gauge", "gazed", "gears", "geese",
        "genes", "genre", "gents", "ghost", "giant", "gifts", "gills", "glade", "gland", "glare",
        "habit", "hails", "hairs", "hands", "handy", "hangs", "happy", "hardy", "harms", "harps",
        "harsh", "haste", "hasty", "hated", "hater", "haven", "hawks", "heads", "heals", "heaps",
        "icier", "icing", "ideal", "ideas", "idiom", "idled", "idles", "idyll", "igloo", "image",
        "imbed", "imply", "incur", "index", "inert", "infer", "input", "insane", "inter", "intro",
        "jaded", "jails", "jeans", "jeeps", "jells", "jiffy", "jingo", "joice", "joint", "jokes",
        "joker", "jolly", "joust", "judge", "juice", "julep", "jumbo", "jumps", "jumpy", "junco",
        "kaput", "kayak", "keg", "kelp", "kench", "kendo", "kente", "kerns", "ketone", "kettle",
        "keys", "kicks", "kills", "kilns", "kilts", "kinds", "kings", "kinks", "kiosk", "kissed",
        "label", "labor", "laced", "laces", "lacks", "laded", "lades", "ladle", "lager", "lakes",
        "lamps", "lance", "lands", "lanes", "lapse", "larch", "lards", "large", "larks", "lasso",
        "macho", "macro", "madam", "made", "magic", "magma", "maids", "mails", "mains", "maize",
        "major", "maker", "males", "malts", "mamma", "maned", "mango", "manor", "maple", "marcs",
        "nacho", "nails", "naive", "naked", "named", "names", "nanny", "naped", "napes", "nappy",
        "narks", "nasty", "natal", "naval", "navel", "neaps", "nears", "neath", "necks", "needs",
        "oaken", "oared", "oases", "oasis", "oater", "oaths", "obese", "obeys", "odder", "oddly",
        "odors", "offal", "offer", "often", "oiled", "oiler", "okays", "olden", "older", "olive",
        "paced", "pacer", "paces", "packs", "pacts", "paddy", "paged", "pager", "pages", "pails",
        "pains", "paint", "pairs", "paled", "pales", "palms", "palsy", "panda", "paned", "panes",
        "quack", "quake", "qualm", "quart", "quash", "queen", "quell", "query", "quest", "queue",
        "quick", "quiet", "quiff", "quill", "quilt", "quirk", "quite", "quits", "quota", "quote",
        "rabbi", "rabid", "raced", "racer", "races", "racks", "radar", "radii", "radio", "radium",
        "radon", "rafts", "raged", "rages", "raids", "rails", "rains", "rainy", "raise", "raked",
        "saber", "sable", "safed", "safer", "sages", "saggy", "sagas", "sails", "saint", "sakes",
        "salon", "salsa", "salts", "salty", "salvo", "sands", "sandy", "sanes", "saner", "sappy",
        "table", "taboo", "tabor", "tacit", "tacks", "tacos", "tacts", "tails", "taint", "taken",
        "taker", "takes", "tales", "talks", "tamed", "tamer", "tames", "tamps", "tango", "tanks",
        "udder", "ulcer", "ultra", "umber", "umbra", "unbar", "uncap", "uncut", "under", "undue",
        "unfed", "unfit", "union", "unite", "units", "unity", "unlit", "unpeg", "unset", "until",
        "vague", "valet", "valid", "valor", "valve", "vamps", "vaned", "vanes", "vans", "vapid",
        "vapor", "varus", "vased", "vases", "vaste", "vasty", "vaulk", "vault", "vaunt", "veals",
        "waded", "wader", "wades", "wages", "wagon", "waifs", "wails", "waist", "waits", "waked",
        "waken", "waker", "wakes", "wales", "walks", "walls", "waltz", "wands", "waned", "wanes",
        "xenic", "xenon", "xerus", "xrays", "xylem", "xylol", "xeric", "xanth", "xiphi", "xysts",
        "yacht", "yacks", "yager", "yales", "yanks", "yards", "yarns", "yawed", "yawer", "yawls",
        "zaire", "zonal", "zoned", "zones", "zonal", "zoic", "zoeas", "zombi", "zonal", "zymes"
    )

    private val spanishWords = listOf(
        "abaco", "bacon", "cabra", "dador", "echar", "falso", "gafas", "haber", "ideas", "jabon",
        "karma", "labio", "macho", "nabos", "obras", "pacto", "quema", "radio", "sabio", "tabla",
        "vacas", "xenon", "yacer", "zorro", "acero", "broma", "cruce", "drama",
        "selva", "firme", "gente", "huevo", "igual", "juego", "kepis", "largo", "mueca", "nieve",
        "oeste", "punto", "queso", "rango", "suelo", "tren", "unido", "vuelo", "llama", "xileno",
        "yerno", "zurdo", "aquel", "barco", "campo", "ducho", "enano", "fiero", "gordo", "helio",
        "isla", "joven", "kilo", "lindo", "miedo", "nuevo", "orino", "pardo", "quijo", "roble",
        "senda", "tinto", "usura", "vello", "tenaz", "yogur", "zafar", "andar", "besar"
    )

    private val italianWords = listOf(
        "abate", "bacio", "casa", "dado", "erba", "falso", "gatto", "hotel", "indio", "jazz",
        "kafka", "lago", "mare", "naso", "otto", "pacco", "quadro", "radio", "sacco", "tacco",
        "uva", "vacca", "watt", "xenon", "yacht", "zaino", "accia", "brace", "cacio", "dazio",
        "etere", "fiume", "gazza", "hobby", "inno", "jolly", "krapfen", "lutto", "mazzo", "notte",
        "orzo", "pizzo", "quota", "razzo", "setta", "tazza", "umore", "vizio", "weekend", "xilofono",
        "yogurt", "zuppa", "alato", "buono", "cotto", "dotto", "esame", "fatto", "gallo", "heimat",
        "ira", "jazzista", "kantiano", "lotta", "motto", "noto", "ozono", "pasto", "quarto", "rallo",
        "sasso", "tatto", "unico", "voto", "water", "xerico", "yoga", "zeta", "altra", "borsa"
    )

    fun generatePassword(): String {
        val passwordParts = listOf(
            englishWords.random(),
            spanishWords.random(),
            italianWords.random()

        ).shuffled()

        return passwordParts.joinToString("-")
    }
}