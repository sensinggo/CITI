package com.example.citti;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface BSDao {
    @Insert
    void insertAll(List<BS> BSs);

    @Query("SELECT * FROM BS LIMIT 5")
    List<BS> getTopBSs();

    @Query("SELECT * FROM BS WHERE cell_id = :cellId AND pci = :pci AND tac = :tac AND mnc = :mnc AND nt = :nt LIMIT 1")
    BS getBS(int cellId, int pci, int tac, int mnc, String nt);

    @Query("DELETE FROM BS")
    void deleteAll();
}
