package fr.tsuna.diviatotem;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

public abstract class diviaDB{
	   
	protected SQLiteDatabase mDb = null;
	protected DatabaseHandler mHandler = null;
	   
	public diviaDB(Context pContext) {
		this.mHandler = new DatabaseHandler(pContext);
	}
	   
	public SQLiteDatabase open() {
		// Pas besoin de fermer la dernière base puisque getWritableDatabase s'en charge
		mDb = mHandler.getWritableDatabase();
		return mDb;
	}
	   
	public void close() {
		mDb.close();
	}
	   
	public SQLiteDatabase getDb() {
		return mDb;
}
}
