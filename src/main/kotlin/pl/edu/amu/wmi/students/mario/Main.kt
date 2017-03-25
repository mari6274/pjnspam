package pl.edu.amu.wmi.students.mario

import com.j256.ormlite.dao.DaoManager
import com.j256.ormlite.jdbc.JdbcConnectionSource
import com.j256.ormlite.stmt.SelectArg
import com.j256.ormlite.table.TableUtils
import org.apache.james.mime4j.MimeException
import org.apache.james.mime4j.stream.EntityState
import org.apache.james.mime4j.stream.MimeTokenStream
import java.io.File


fun main(args: Array<String>) {
//    loadSpam()
//    loadHam()

//    loadTesting()

//    generatePopularWords(true)
    generatePopularWords(false)
}

fun generatePopularWords(isSpam: Boolean) {
    val databaseUrl = "jdbc:h2:./mailsdb"
    val connectionSource = JdbcConnectionSource(databaseUrl)
    TableUtils.createTableIfNotExists(connectionSource, PopularWord::class.java)
    val mailDao = DaoManager.createDao(connectionSource, Mail::class.java)
    val popularWordDao = DaoManager.createDao(connectionSource, PopularWord::class.java)

    mailDao.queryForEq("spam", isSpam).map { generateWords(it.content!!) }.forEach {
        it.forEach {
            val selectArg = SelectArg(it)
            val popularWord = popularWordDao.queryForFirst(popularWordDao.queryBuilder()
                    .where()
                    .like("word", selectArg)
                    .and()
                    .like("spam", if (isSpam) 1 else 0)
                    .prepare())
            if (popularWord != null) {
                popularWord.count++
                popularWordDao.update(popularWord)
            } else {
                popularWordDao.create(PopularWord(it, 1, isSpam))
            }
        }
    }
    connectionSource.close()
}

fun loadTesting() {
    val databaseUrl = "jdbc:h2:./mailsdb"
    val connectionSource = JdbcConnectionSource(databaseUrl)
    TableUtils.createTableIfNotExists(connectionSource, TestingMail::class.java)
    val testingMailDao = DaoManager.createDao(connectionSource, TestingMail::class.java)
    val spamMap = File("testing.label").readLines()
            .map { it.split(" ") }
            .associateBy({ it[1] }, { it[0] != "1" })
    val testingDirectory: File = File("testing")
    for (file in testingDirectory.listFiles()) {
        try {
            val fullMail = file.readText()
            val stream: MimeTokenStream = MimeTokenStream()
            stream.parse(fullMail.byteInputStream())
            var content: String = ""
            while (stream.state != EntityState.T_END_OF_STREAM) {
                val state = stream.next()
                if (state == EntityState.T_BODY) {
                    content += stream.inputStream.reader().readText()
                }
            }
            val mail = TestingMail(fullMail, content, spamMap[file.name]!!)
            testingMailDao.create(mail)
        } catch (e: MimeException) {
        }
    }
    connectionSource.close()
}

private fun populate(directory: String, isSpam: Boolean) {
    val databaseUrl = "jdbc:h2:./mailsdb"
    val connectionSource = JdbcConnectionSource(databaseUrl)
    TableUtils.createTableIfNotExists(connectionSource, Mail::class.java)
    val mailDao = DaoManager.createDao(connectionSource, Mail::class.java)
    val spamDirectory: File = File(directory)
    for (file in spamDirectory.listFiles()) {
        try {
            val fullMail = file.readText()
            val stream: MimeTokenStream = MimeTokenStream()
            stream.parse(fullMail.byteInputStream())
            var content: String = ""
            while (stream.state != EntityState.T_END_OF_STREAM) {
                val state = stream.next()
                if (state == EntityState.T_BODY) {
                    content += stream.inputStream.reader().readText()
                }
            }
            val mail = Mail(fullMail, content, isSpam)
            mailDao.create(mail)
        } catch (e: MimeException) {
        }
    }
    connectionSource.close()
}

private fun loadSpam() {
    populate("spam", true)
}

private fun loadHam() {
    populate("ham", false)
}

