package pl.edu.amu.wmi.students.mario

import com.j256.ormlite.field.DataType
import com.j256.ormlite.field.DatabaseField
import com.j256.ormlite.table.DatabaseTable

/**
 * Created by Mariusz on 2017-01-28.
 */
@DatabaseTable(tableName = "testingmails")
class TestingMail() {

    constructor(fullMail: String, content: String, isSpam: Boolean) : this() {
        this.fullMail = fullMail
        this.content = content
        this.spam = isSpam
    }

    @DatabaseField(generatedId = true, allowGeneratedIdInsert = true)
    var id: Long? = null
    @DatabaseField(dataType = DataType.LONG_STRING)
    var fullMail: String? = null
    @DatabaseField(dataType = DataType.LONG_STRING)
    var content: String? = null
    @DatabaseField
    var spam: Boolean? = null
}