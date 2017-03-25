package pl.edu.amu.wmi.students.mario

import com.j256.ormlite.dao.DaoManager
import com.j256.ormlite.jdbc.JdbcConnectionSource
import com.j256.ormlite.stmt.SelectArg
import com.j256.ormlite.support.ConnectionSource
import java.io.File
import java.util.concurrent.atomic.AtomicInteger

fun main(args: Array<String>) {
    val databaseUrl = "jdbc:h2:./mailsdb"
    val connectionSource = JdbcConnectionSource(databaseUrl)

    val vowpalFileTraining: File = File("vowpal_training")
    val mailDao = DaoManager.createDao(connectionSource, Mail::class.java)

    mailDao.queryForAll().forEach {
        val vowpalString = "${if (it.spam!!) 1 else -1} ${if (it.spam!!) "spam" else "ham"}|${generateFeatures7(connectionSource, it?.content!!)}\n"
        println(vowpalString)
        vowpalFileTraining.appendText(vowpalString)
    }

    val vowpalFileTesting: File = File("vowpal_testing")

    val testingMailDao = DaoManager.createDao(connectionSource, TestingMail::class.java)

    testingMailDao.queryForAll().forEach {
        val vowpalString = "${if (it.spam!!) "spam" else "ham"}|${generateFeatures7(connectionSource, it?.content!!)}\n"
        println(vowpalString)
        vowpalFileTesting.appendText(vowpalString)
    }

    connectionSource.close()
}

// cechy: lematy dla każdego słowa
// 76,00%    - dla 1500:1500 - 76,20%
fun generateFeatures(content: String): String {
    return " ${generateWords(content).joinToString(" ")}"
}

// cechy: lematy z krotnością
// 73,40%
fun generateFeatures4(content: String): String {
    val words = mutableMapOf<String, AtomicInteger>()
    generateWords(content).forEach {
        words.getOrPut(it, { AtomicInteger(0) }).incrementAndGet()
    }
    return " ${words.map { "${it.key}:${it.value.get()}" }.joinToString(" ")}"
}

// cechy: słowo ze słownika : liczba wystąpień * liczba wystąpień ze słownika
// 72,40%
fun generateFeatures2(connectionSource: ConnectionSource, content: String): String {
    val popularWordDao = DaoManager.createDao(connectionSource, PopularWord::class.java)

    val sb = StringBuilder()

    val words = mutableMapOf<String, Int>()

    generateWords(content).forEach {
        words.put(it, words.getOrElse(it, { 1 }))
    }

    words.forEach {
        val selectArg = SelectArg(it.key)
        val word = popularWordDao.queryBuilder().where().like("word", selectArg).queryForFirst()
        if (word != null) {
            sb.append(" ${it.key}:${it.value * word.count}")
        }
    }

    return sb.toString()

}

// cechy: słowo ze słownika : liczba wystąpień
// 1500:1500 - 71,60%
fun generateFeatures5(content: String): String {
    val sb = StringBuilder()

    val words = mutableMapOf<String, Int>()

    generateWords(content).forEach {
        words.put(it, words.getOrElse(it, { 1 }))
    }

    words.forEach {
        sb.append(" ${it.key}:${it.value}")
    }

    return sb.toString()

}

// cechy: suffixy
// 1500:1500 - 65,80%
fun generateFeatures6(content: String): String {
    val words = content.split(" ").map(String::trim).filterNot(String::isEmpty)
    val suffixes = listOf("e", "s", "d", "t", "he", "n", "a", "of", "the", "y", "r", "to", "in", "f", "o", "ed", "nd", "is", "on", "l", "g", "and", "ng", "er", "as", "ing", "h", "at", "es", "or", "re", "it", "an", "m", "i", "ly", "ion", "en", "al", "?", "nt", "be", "hat", "st", "his", "th", "ll", "le", "ce", "by", "ts", "me", "ve", "'", "se", "ut", "was", "for", "ent", "ch", "k", "w", "ld", "rs", "ted", "ere", "her", "ne", "ns", "ith", "ad", "ry", ")", "(", "te", "ay", "ty", "ot", "p", "nce", "'s", "ter", "om", "ss", "we", "are", "c", "ers", "uld", "had", "so", "ey")
    val suffixesMap = suffixes.associateBy({ it }, {
        val suffix = it
        words.count { it.endsWith(suffix) }
    })
    return " " + suffixesMap.map { "suf" + it.key + ":${it.value}" }.joinToString(" ")
}

// bigrams
// 67,00%
fun generateFeatures3(content: String): String {
    return " ${findBigrams(content).joinToString(" ")}"
}

// spam, ham words counters
// 1500:1500 65,80%
fun generateFeatures7(connectionSource: ConnectionSource, content: String): String {
    val popularWordDao = DaoManager.createDao(connectionSource, PopularWord::class.java)

    val words = mutableMapOf<String, Int>()

    generateWords(content).forEach {
        words.put(it, words.getOrElse(it, { 1 }))
    }

    var spamWords = 0
    var hamWords = 0

    words.forEach {
        val selectArg = SelectArg(it.key)
        val popularWordQuery = popularWordDao
                .queryBuilder()
                .where()
                .like("word", selectArg)
                .prepare()
        val popularWord = popularWordDao.query(popularWordQuery)
        if (popularWord.size > 1) {
            spamWords += it.value
            hamWords += it.value
        } else if (popularWord.size == 1) {
            if (popularWord[0].spam) {
                spamWords += it.value
            } else {
                hamWords += it.value
            }
        }
    }

    return " spamWords:$spamWords hamWords:$hamWords"
}

fun generateWords(text: String): List<String> {
    val onlyWordRegex = Regex("^\\w[-'\\w]*$")
    val onlyNumbers = Regex("^\\d+$")
    val lemmatizer = StanfordLemmatizer()
    val lemmatize = lemmatizer.lemmatize(text)
    return lemmatize.map(String::trim).filter { it.matches(onlyWordRegex) }.filterNot { it.matches(onlyNumbers) }
}
