package pl.edu.amu.wmi.students.mario

import com.j256.ormlite.field.DatabaseField
import com.j256.ormlite.table.DatabaseTable

@DatabaseTable(tableName = "popularwords")
class PopularWord() {

    constructor(word: String, count: Int, isSpam: Boolean) : this() {
        this.word = word
        this.count = count
        this.spam = isSpam
    }

    @DatabaseField(generatedId = true, allowGeneratedIdInsert = true)
    var id: Long? = null
    @DatabaseField
    var word: String? = null
    @DatabaseField
    var count: Int = 1
    @DatabaseField
    var spam: Boolean = false
}