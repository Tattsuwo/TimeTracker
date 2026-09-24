package com.example.timetracker.`data`

import androidx.room.EntityDeleteOrUpdateAdapter
import androidx.room.EntityInsertAdapter
import androidx.room.RoomDatabase
import androidx.room.coroutines.createFlow
import androidx.room.util.getColumnIndexOrThrow
import androidx.room.util.performSuspending
import androidx.sqlite.SQLiteStatement
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
public class CategoryDao_Impl(
  __db: RoomDatabase,
) : CategoryDao {
  private val __db: RoomDatabase

  private val __insertAdapterOfCategory: EntityInsertAdapter<Category>

  private val __deleteAdapterOfCategory: EntityDeleteOrUpdateAdapter<Category>

  private val __updateAdapterOfCategory: EntityDeleteOrUpdateAdapter<Category>
  init {
    this.__db = __db
    this.__insertAdapterOfCategory = object : EntityInsertAdapter<Category>() {
      protected override fun createQuery(): String = "INSERT OR ABORT INTO `categories` (`id`,`name`) VALUES (nullif(?, 0),?)"

      protected override fun bind(statement: SQLiteStatement, entity: Category) {
        statement.bindLong(1, entity.id)
        statement.bindText(2, entity.name)
      }
    }
    this.__deleteAdapterOfCategory = object : EntityDeleteOrUpdateAdapter<Category>() {
      protected override fun createQuery(): String = "DELETE FROM `categories` WHERE `id` = ?"

      protected override fun bind(statement: SQLiteStatement, entity: Category) {
        statement.bindLong(1, entity.id)
      }
    }
    this.__updateAdapterOfCategory = object : EntityDeleteOrUpdateAdapter<Category>() {
      protected override fun createQuery(): String = "UPDATE OR ABORT `categories` SET `id` = ?,`name` = ? WHERE `id` = ?"

      protected override fun bind(statement: SQLiteStatement, entity: Category) {
        statement.bindLong(1, entity.id)
        statement.bindText(2, entity.name)
        statement.bindLong(3, entity.id)
      }
    }
  }

  public override suspend fun insert(category: Category): Long = performSuspending(__db, false, true) { _connection ->
    val _result: Long = __insertAdapterOfCategory.insertAndReturnId(_connection, category)
    _result
  }

  public override suspend fun delete(category: Category): Unit = performSuspending(__db, false, true) { _connection ->
    __deleteAdapterOfCategory.handle(_connection, category)
  }

  public override suspend fun update(category: Category): Unit = performSuspending(__db, false, true) { _connection ->
    __updateAdapterOfCategory.handle(_connection, category)
  }

  public override fun observeAll(): Flow<List<Category>> {
    val _sql: String = "SELECT * FROM categories ORDER BY name ASC"
    return createFlow(__db, false, arrayOf("categories")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfName: Int = getColumnIndexOrThrow(_stmt, "name")
        val _result: MutableList<Category> = mutableListOf()
        while (_stmt.step()) {
          val _item: Category
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpName: String
          _tmpName = _stmt.getText(_columnIndexOfName)
          _item = Category(_tmpId,_tmpName)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun countByName(name: String): Int {
    val _sql: String = "SELECT COUNT(*) FROM categories WHERE name = ?"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, name)
        val _result: Int
        if (_stmt.step()) {
          val _tmp: Int
          _tmp = _stmt.getLong(0).toInt()
          _result = _tmp
        } else {
          _result = 0
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getByName(name: String): Category? {
    val _sql: String = "SELECT * FROM categories WHERE name = ? LIMIT 1"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, name)
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfName: Int = getColumnIndexOrThrow(_stmt, "name")
        val _result: Category?
        if (_stmt.step()) {
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpName: String
          _tmpName = _stmt.getText(_columnIndexOfName)
          _result = Category(_tmpId,_tmpName)
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
