package com.example.meusafazeres.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface TaskDao {
    @Query("SELECT * FROM local_tasks WHERE removido = 0 ORDER BY dataCriacao DESC")
    suspend fun getAllLocalTasks(): List<TaskEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLocalTask(task: TaskEntity)

    @Query("UPDATE local_tasks SET removido = 1 WHERE id = :id")
    suspend fun logicalDeleteLocalTask(id: String)
}
