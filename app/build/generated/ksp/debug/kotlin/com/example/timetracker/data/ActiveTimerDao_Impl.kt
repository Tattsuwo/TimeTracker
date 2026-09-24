package com.example.timetracker.`data`

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
import kotlin.reflect.KClass
import kotlinx.coroutines.flow.Flow

@Generated(value = ["androidx.room.RoomProcessor"])
@Suppress(names = ["UNCHECKED_CAST", "DEPRECATION", "REDUNDANT_PROJECTION", "REMOVAL"])
public class ActiveTimerDao_Impl(
  __db: RoomDatabase,
) : ActiveTimerDao {
  private val __db: RoomDatabase

  private val __insertAdapterOfActiveTimer: EntityInsertAdapter<ActiveTimer>

  private val __converters: Converters = Converters()
  init {
    this.__db = __db
    this.__insertAdapterOfActiveTimer = object : EntityInsertAdapter<ActiveTimer>() {
      protected override fun createQuery(): String = "INSERT OR ABORT INTO `active_timer` (`id`,`startTime`) VALUES (?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: ActiveTimer) {
        statement.bindLong(1, entity.id.toLong())
        val _tmp: Long? = __converters.instantToEpochMillis(entity.startTime)
        if (_tmp == null) {
          statement.bindNull(2)
        } else {
          statement.bindLong(2, _tmp)
        }
      }
    }
  }

  public override suspend fun start(activeTimer: ActiveTimer): Unit = performSuspending(__db, false, true) { _connection ->
    __insertAdapterOfActiveTimer.insert(_connection, activeTimer)
  }

  public override fun observe(): Flow<ActiveTimer?> {
    val _sql: String = "SELECT * FROM active_timer WHERE id = 0 LIMIT 1"
    return createFlow(__db, false, arrayOf("active_timer")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfStartTime: Int = getColumnIndexOrThrow(_stmt, "startTime")
        val _result: ActiveTimer?
        if (_stmt.step()) {
          val _tmpId: Int
          _tmpId = _stmt.getLong(_columnIndexOfId).toInt()
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
          _result = ActiveTimer(_tmpId,_tmpStartTime)
        } else {
          _result = null
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getOnce(): ActiveTimer? {
    val _sql: String = "SELECT * FROM active_timer WHERE id = 0 LIMIT 1"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfStartTime: Int = getColumnIndexOrThrow(_stmt, "startTime")
        val _result: ActiveTimer?
        if (_stmt.step()) {
          val _tmpId: Int
          _tmpId = _stmt.getLong(_columnIndexOfId).toInt()
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
          _result = ActiveTimer(_tmpId,_tmpStartTime)
        } else {
          _result = null
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun clear() {
    val _sql: String = "DELETE FROM active_timer WHERE id = 0"
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        _stmt.step()
      } finally {
        _stmt.close()
      }
    }
  }

  public companion object {
    public fun getRequiredConverters(): List<KClass<*>> = emptyList()
  }
}
