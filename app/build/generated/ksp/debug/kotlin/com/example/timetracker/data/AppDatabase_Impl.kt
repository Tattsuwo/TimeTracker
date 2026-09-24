package com.example.timetracker.`data`

import androidx.room.InvalidationTracker
import androidx.room.RoomOpenDelegate
import androidx.room.migration.AutoMigrationSpec
import androidx.room.migration.Migration
import androidx.room.util.TableInfo
import androidx.room.util.TableInfo.Companion.read
import androidx.room.util.dropFtsSyncTriggers
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.execSQL
import javax.`annotation`.processing.Generated
import kotlin.Lazy
import kotlin.String
import kotlin.Suppress
import kotlin.collections.List
import kotlin.collections.Map
import kotlin.collections.MutableList
import kotlin.collections.MutableMap
import kotlin.collections.MutableSet
import kotlin.collections.Set
import kotlin.collections.mutableListOf
import kotlin.collections.mutableMapOf
import kotlin.collections.mutableSetOf
import kotlin.reflect.KClass

@Generated(value = ["androidx.room.RoomProcessor"])
@Suppress(names = ["UNCHECKED_CAST", "DEPRECATION", "REDUNDANT_PROJECTION", "REMOVAL"])
public class AppDatabase_Impl : AppDatabase() {
  private val _categoryDao: Lazy<CategoryDao> = lazy {
    CategoryDao_Impl(this)
  }

  private val _sessionDao: Lazy<SessionDao> = lazy {
    SessionDao_Impl(this)
  }

  private val _activeTimerDao: Lazy<ActiveTimerDao> = lazy {
    ActiveTimerDao_Impl(this)
  }

  protected override fun createOpenDelegate(): RoomOpenDelegate {
    val _openDelegate: RoomOpenDelegate = object : RoomOpenDelegate(1, "70328f5d67647484c3ec25c469ef0d33", "45503b30c2ffa25cb9582eccf345e662") {
      public override fun createAllTables(connection: SQLiteConnection) {
        connection.execSQL("CREATE TABLE IF NOT EXISTS `categories` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT NOT NULL)")
        connection.execSQL("CREATE TABLE IF NOT EXISTS `sessions` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT NOT NULL, `description` TEXT NOT NULL, `categoryId` INTEGER NOT NULL, `startTime` INTEGER NOT NULL, `endTime` INTEGER NOT NULL, FOREIGN KEY(`categoryId`) REFERENCES `categories`(`id`) ON UPDATE NO ACTION ON DELETE RESTRICT )")
        connection.execSQL("CREATE INDEX IF NOT EXISTS `index_sessions_categoryId` ON `sessions` (`categoryId`)")
        connection.execSQL("CREATE TABLE IF NOT EXISTS `active_timer` (`id` INTEGER NOT NULL, `startTime` INTEGER NOT NULL, PRIMARY KEY(`id`))")
        connection.execSQL("CREATE TABLE IF NOT EXISTS room_master_table (id INTEGER PRIMARY KEY,identity_hash TEXT)")
        connection.execSQL("INSERT OR REPLACE INTO room_master_table (id,identity_hash) VALUES(42, '70328f5d67647484c3ec25c469ef0d33')")
      }

      public override fun dropAllTables(connection: SQLiteConnection) {
        connection.execSQL("DROP TABLE IF EXISTS `categories`")
        connection.execSQL("DROP TABLE IF EXISTS `sessions`")
        connection.execSQL("DROP TABLE IF EXISTS `active_timer`")
      }

      public override fun onCreate(connection: SQLiteConnection) {
      }

      public override fun onOpen(connection: SQLiteConnection) {
        connection.execSQL("PRAGMA foreign_keys = ON")
        internalInitInvalidationTracker(connection)
      }

      public override fun onPreMigrate(connection: SQLiteConnection) {
        dropFtsSyncTriggers(connection)
      }

      public override fun onPostMigrate(connection: SQLiteConnection) {
      }

      public override fun onValidateSchema(connection: SQLiteConnection): RoomOpenDelegate.ValidationResult {
        val _columnsCategories: MutableMap<String, TableInfo.Column> = mutableMapOf()
        _columnsCategories.put("id", TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsCategories.put("name", TableInfo.Column("name", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysCategories: MutableSet<TableInfo.ForeignKey> = mutableSetOf()
        val _indicesCategories: MutableSet<TableInfo.Index> = mutableSetOf()
        val _infoCategories: TableInfo = TableInfo("categories", _columnsCategories, _foreignKeysCategories, _indicesCategories)
        val _existingCategories: TableInfo = read(connection, "categories")
        if (!_infoCategories.equals(_existingCategories)) {
          return RoomOpenDelegate.ValidationResult(false, """
              |categories(com.example.timetracker.data.Category).
              | Expected:
              |""".trimMargin() + _infoCategories + """
              |
              | Found:
              |""".trimMargin() + _existingCategories)
        }
        val _columnsSessions: MutableMap<String, TableInfo.Column> = mutableMapOf()
        _columnsSessions.put("id", TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsSessions.put("name", TableInfo.Column("name", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsSessions.put("description", TableInfo.Column("description", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsSessions.put("categoryId", TableInfo.Column("categoryId", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsSessions.put("startTime", TableInfo.Column("startTime", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsSessions.put("endTime", TableInfo.Column("endTime", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysSessions: MutableSet<TableInfo.ForeignKey> = mutableSetOf()
        _foreignKeysSessions.add(TableInfo.ForeignKey("categories", "RESTRICT", "NO ACTION", listOf("categoryId"), listOf("id")))
        val _indicesSessions: MutableSet<TableInfo.Index> = mutableSetOf()
        _indicesSessions.add(TableInfo.Index("index_sessions_categoryId", false, listOf("categoryId"), listOf("ASC")))
        val _infoSessions: TableInfo = TableInfo("sessions", _columnsSessions, _foreignKeysSessions, _indicesSessions)
        val _existingSessions: TableInfo = read(connection, "sessions")
        if (!_infoSessions.equals(_existingSessions)) {
          return RoomOpenDelegate.ValidationResult(false, """
              |sessions(com.example.timetracker.data.Session).
              | Expected:
              |""".trimMargin() + _infoSessions + """
              |
              | Found:
              |""".trimMargin() + _existingSessions)
        }
        val _columnsActiveTimer: MutableMap<String, TableInfo.Column> = mutableMapOf()
        _columnsActiveTimer.put("id", TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsActiveTimer.put("startTime", TableInfo.Column("startTime", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysActiveTimer: MutableSet<TableInfo.ForeignKey> = mutableSetOf()
        val _indicesActiveTimer: MutableSet<TableInfo.Index> = mutableSetOf()
        val _infoActiveTimer: TableInfo = TableInfo("active_timer", _columnsActiveTimer, _foreignKeysActiveTimer, _indicesActiveTimer)
        val _existingActiveTimer: TableInfo = read(connection, "active_timer")
        if (!_infoActiveTimer.equals(_existingActiveTimer)) {
          return RoomOpenDelegate.ValidationResult(false, """
              |active_timer(com.example.timetracker.data.ActiveTimer).
              | Expected:
              |""".trimMargin() + _infoActiveTimer + """
              |
              | Found:
              |""".trimMargin() + _existingActiveTimer)
        }
        return RoomOpenDelegate.ValidationResult(true, null)
      }
    }
    return _openDelegate
  }

  protected override fun createInvalidationTracker(): InvalidationTracker {
    val _shadowTablesMap: MutableMap<String, String> = mutableMapOf()
    val _viewTables: MutableMap<String, Set<String>> = mutableMapOf()
    return InvalidationTracker(this, _shadowTablesMap, _viewTables, "categories", "sessions", "active_timer")
  }

  public override fun clearAllTables() {
    super.performClear(true, "sessions", "categories", "active_timer")
  }

  protected override fun getRequiredTypeConverterClasses(): Map<KClass<*>, List<KClass<*>>> {
    val _typeConvertersMap: MutableMap<KClass<*>, List<KClass<*>>> = mutableMapOf()
    _typeConvertersMap.put(CategoryDao::class, CategoryDao_Impl.getRequiredConverters())
    _typeConvertersMap.put(SessionDao::class, SessionDao_Impl.getRequiredConverters())
    _typeConvertersMap.put(ActiveTimerDao::class, ActiveTimerDao_Impl.getRequiredConverters())
    return _typeConvertersMap
  }

  public override fun getRequiredAutoMigrationSpecClasses(): Set<KClass<out AutoMigrationSpec>> {
    val _autoMigrationSpecsSet: MutableSet<KClass<out AutoMigrationSpec>> = mutableSetOf()
    return _autoMigrationSpecsSet
  }

  public override fun createAutoMigrations(autoMigrationSpecs: Map<KClass<out AutoMigrationSpec>, AutoMigrationSpec>): List<Migration> {
    val _autoMigrations: MutableList<Migration> = mutableListOf()
    return _autoMigrations
  }

  public override fun categoryDao(): CategoryDao = _categoryDao.value

  public override fun sessionDao(): SessionDao = _sessionDao.value

  public override fun activeTimerDao(): ActiveTimerDao = _activeTimerDao.value
}
