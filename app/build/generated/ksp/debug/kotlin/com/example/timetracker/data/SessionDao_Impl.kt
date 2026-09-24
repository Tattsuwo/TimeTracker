package com.example.timetracker.`data`

import androidx.room.EntityDeleteOrUpdateAdapter
import androidx.room.EntityInsertAdapter
import androidx.room.RoomDatabase
import androidx.room.coroutines.createFlow
import androidx.room.util.getColumnIndexOrThrow
import androidx.room.util.performSuspending
import androidx.sqlite.SQLiteStatement
import java.time.Instant
import javax.`annotation`.processing.Generated
import kotlin.Int
import kotlin.Long
import kotlin.String
import kotlin.Suppress
import kotlin.Unit
import kotlin.collections.List
import kotlin.collections.MutableList
import kotlin.collections.mutableListOf
import kotlin.reflect.KClass
import kotlinx.coroutines.flow.Flow

@Generated(value = ["androidx.room.RoomProcessor"])
@Suppress(names = ["UNCHECKED_CAST", "DEPRECATION", "REDUNDANT_PROJECTION", "REMOVAL"])
public class SessionDao_Impl(
  __db: RoomDatabase,
) : SessionDao {
  private val __db: RoomDatabase

  private val __insertAdapterOfSession: EntityInsertAdapter<Session>

  private val __converters: Converters = Converters()

  private val __deleteAdapterOfSession: EntityDeleteOrUpdateAdapter<Session>

  private val __updateAdapterOfSession: EntityDeleteOrUpdateAdapter<Session>
  init {
    this.__db = __db
    this.__insertAdapterOfSession = object : EntityInsertAdapter<Session>() {
      protected override fun createQuery(): String = "INSERT OR ABORT INTO `sessions` (`id`,`name`,`description`,`categoryId`,`startTime`,`endTime`) VALUES (nullif(?, 0),?,?,?,?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: Session) {
        statement.bindLong(1, entity.id)
        statement.bindText(2, entity.name)
        statement.bindText(3, entity.description)
        statement.bindLong(4, entity.categoryId)
        val _tmp: Long? = __converters.instantToEpochMillis(entity.startTime)
        if (_tmp == null) {
          statement.bindNull(5)
        } else {
          statement.bindLong(5, _tmp)
        }
        val _tmp_1: Long? = __converters.instantToEpochMillis(entity.endTime)
        if (_tmp_1 == null) {
          statement.bindNull(6)
        } else {
          statement.bindLong(6, _tmp_1)
        }
      }
    }
    this.__deleteAdapterOfSession = object : EntityDeleteOrUpdateAdapter<Session>() {
      protected override fun createQuery(): String = "DELETE FROM `sessions` WHERE `id` = ?"

      protected override fun bind(statement: SQLiteStatement, entity: Session) {
        statement.bindLong(1, entity.id)
      }
    }
    this.__updateAdapterOfSession = object : EntityDeleteOrUpdateAdapter<Session>() {
      protected override fun createQuery(): String = "UPDATE OR ABORT `sessions` SET `id` = ?,`name` = ?,`description` = ?,`categoryId` = ?,`startTime` = ?,`endTime` = ? WHERE `id` = ?"

      protected override fun bind(statement: SQLiteStatement, entity: Session) {
        statement.bindLong(1, entity.id)
        statement.bindText(2, entity.name)
        statement.bindText(3, entity.description)
        statement.bindLong(4, entity.categoryId)
        val _tmp: Long? = __converters.instantToEpochMillis(entity.startTime)
        if (_tmp == null) {
          statement.bindNull(5)
        } else {
          statement.bindLong(5, _tmp)
        }
        val _tmp_1: Long? = __converters.instantToEpochMillis(entity.endTime)
        if (_tmp_1 == null) {
          statement.bindNull(6)
        } else {
          statement.bindLong(6, _tmp_1)
        }
        statement.bindLong(7, entity.id)
      }
    }
  }

  public override suspend fun insert(session: Session): Long = performSuspending(__db, false, true) { _connection ->
    val _result: Long = __insertAdapterOfSession.insertAndReturnId(_connection, session)
    _result
  }

  public override suspend fun delete(session: Session): Unit = performSuspending(__db, false, true) { _connection ->
    __deleteAdapterOfSession.handle(_connection, session)
  }

  public override suspend fun update(session: Session): Unit = performSuspending(__db, false, true) { _connection ->
    __updateAdapterOfSession.handle(_connection, session)
  }

  public override fun observeAllWithCategory(): Flow<List<SessionWithCategory>> {
    val _sql: String = """
        |
        |        SELECT sessions.*, categories.name AS categoryName
        |        FROM sessions
        |        INNER JOIN categories ON categories.id = sessions.categoryId
        |        ORDER BY sessions.startTime DESC
        |        
        """.trimMargin()
    return createFlow(__db, false, arrayOf("sessions", "categories")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfName: Int = getColumnIndexOrThrow(_stmt, "name")
        val _columnIndexOfDescription: Int = getColumnIndexOrThrow(_stmt, "description")
        val _columnIndexOfCategoryId: Int = getColumnIndexOrThrow(_stmt, "categoryId")
        val _columnIndexOfStartTime: Int = getColumnIndexOrThrow(_stmt, "startTime")
        val _columnIndexOfEndTime: Int = getColumnIndexOrThrow(_stmt, "endTime")
        val _columnIndexOfCategoryName: Int = getColumnIndexOrThrow(_stmt, "categoryName")
        val _result: MutableList<SessionWithCategory> = mutableListOf()
        while (_stmt.step()) {
          val _item: SessionWithCategory
          val _tmpCategoryName: String
          _tmpCategoryName = _stmt.getText(_columnIndexOfCategoryName)
          val _tmpSession: Session
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpName: String
          _tmpName = _stmt.getText(_columnIndexOfName)
          val _tmpDescription: String
          _tmpDescription = _stmt.getText(_columnIndexOfDescription)
          val _tmpCategoryId: Long
          _tmpCategoryId = _stmt.getLong(_columnIndexOfCategoryId)
          val _tmpStartTime: Instant
          val _tmp: Long?
          if (_stmt.isNull(_columnIndexOfStartTime)) {
            _tmp = null
          } else {
            _tmp = _stmt.getLong(_columnIndexOfStartTime)
          }
          val _tmp_1: Instant? = __converters.fromEpochMillis(_tmp)
          if (_tmp_1 == null) {
            error("Expected NON-NULL 'java.time.Instant', but it was NULL.")
          } else {
            _tmpStartTime = _tmp_1
          }
          val _tmpEndTime: Instant
          val _tmp_2: Long?
          if (_stmt.isNull(_columnIndexOfEndTime)) {
            _tmp_2 = null
          } else {
            _tmp_2 = _stmt.getLong(_columnIndexOfEndTime)
          }
          val _tmp_3: Instant? = __converters.fromEpochMillis(_tmp_2)
          if (_tmp_3 == null) {
            error("Expected NON-NULL 'java.time.Instant', but it was NULL.")
          } else {
            _tmpEndTime = _tmp_3
          }
          _tmpSession = Session(_tmpId,_tmpName,_tmpDescription,_tmpCategoryId,_tmpStartTime,_tmpEndTime)
          _item = SessionWithCategory(_tmpSession,_tmpCategoryName)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getById(id: Long): Session? {
    val _sql: String = "SELECT * FROM sessions WHERE id = ?"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, id)
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfName: Int = getColumnIndexOrThrow(_stmt, "name")
        val _columnIndexOfDescription: Int = getColumnIndexOrThrow(_stmt, "description")
        val _columnIndexOfCategoryId: Int = getColumnIndexOrThrow(_stmt, "categoryId")
        val _columnIndexOfStartTime: Int = getColumnIndexOrThrow(_stmt, "startTime")
        val _columnIndexOfEndTime: Int = getColumnIndexOrThrow(_stmt, "endTime")
        val _result: Session?
        if (_stmt.step()) {
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpName: String
          _tmpName = _stmt.getText(_columnIndexOfName)
          val _tmpDescription: String
          _tmpDescription = _stmt.getText(_columnIndexOfDescription)
          val _tmpCategoryId: Long
          _tmpCategoryId = _stmt.getLong(_columnIndexOfCategoryId)
          val _tmpStartTime: Instant
          val _tmp: Long?
          if (_stmt.isNull(_columnIndexOfStartTime)) {
            _tmp = null
          } else {
            _tmp = _stmt.getLong(_columnIndexOfStartTime)
          }
          val _tmp_1: Instant? = __converters.fromEpochMillis(_tmp)
          if (_tmp_1 == null) {
            error("Expected NON-NULL 'java.time.Instant', but it was NULL.")
          } else {
            _tmpStartTime = _tmp_1
          }
          val _tmpEndTime: Instant
          val _tmp_2: Long?
          if (_stmt.isNull(_columnIndexOfEndTime)) {
            _tmp_2 = null
          } else {
            _tmp_2 = _stmt.getLong(_columnIndexOfEndTime)
          }
          val _tmp_3: Instant? = __converters.fromEpochMillis(_tmp_2)
          if (_tmp_3 == null) {
            error("Expected NON-NULL 'java.time.Instant', but it was NULL.")
          } else {
            _tmpEndTime = _tmp_3
          }
          _result = Session(_tmpId,_tmpName,_tmpDescription,_tmpCategoryId,_tmpStartTime,_tmpEndTime)
        } else {
          _result = null
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public companion object {
    public fun getRequiredConverters(): List<KClass<*>> = emptyList()
  }
}
