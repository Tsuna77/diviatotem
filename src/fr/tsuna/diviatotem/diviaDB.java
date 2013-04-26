package fr.tsuna.diviatotem;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

public class diviaDB{
	   
	protected SQLiteDatabase mDb = null;
	protected DatabaseHandler mHandler = null;
	protected boolean opened = false;
	   
	public diviaDB(Context pContext) {
		this.mHandler = new DatabaseHandler(pContext);
	}
	   
	public SQLiteDatabase open() {
		mDb = mHandler.getWritableDatabase();
		opened = true;
		return mDb;
	}
	   
	public void close() {
		mDb.close();
		opened = false;
	}
	   
	public void insert_ligne(String code, String nom, String sens, String vers){
		if (!opened){
			open();
		}
	}
	
	public SQLiteDatabase getDb() {
		return mDb;
}
}
