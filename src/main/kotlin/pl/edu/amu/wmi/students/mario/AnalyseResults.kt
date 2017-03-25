package pl.edu.amu.wmi.students.mario

import java.io.File

fun main(args: Array<String>) {
    val vowpalFile: File = File("result")

//    val databaseUrl = "jdbc:h2:./mailsdb"
//    val connectionSource = JdbcConnectionSource(databaseUrl)
//    val mailDao = DaoManager.createDao(connectionSource, Mail::class.java)

    var good = 0
    var bad = 0

    vowpalFile.readLines()
            .map { it.split(" ") }
            .forEach {
//                val mail = mailDao.queryForEq("id", it[1].toLong()).first()
                val predicted = it[0].toInt()
                val isSpam = if (it[1] == "spam") 1 else -1
                if (predicted == isSpam) good++ else bad++
            }
//    connectionSource.close()

    println("good: $good, bad: $bad")
    val accuracy = "%.2f".format(100 * (good.toDouble() / (good + bad).toDouble()))
    println("accuracy: $accuracy%")
}