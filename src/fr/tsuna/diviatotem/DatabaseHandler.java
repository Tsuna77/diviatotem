package fr.tsuna.diviatotem;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHandler extends SQLiteOpenHelper{
    public static final int DATABASE_VERSION = 1;

    public static final String DATABASE_NAME = "diviaTotem.db";
    
    public static final String TABLE_LIGNES = "lignes";
    public static final String LIGNES_ID="ID";
    public static final String LIGNES_KEY="PK_ID";
    public static final String LIGNES_CODE="code";
    public static final String LIGNES_NOM="nom";
    public static final String LIGNES_SENS="sens";
    public static final String LIGNES_VERS="vers";
    
    public static final String TABLE_FAVORIS = "favoris";
    public static final String FAVORIS_ARRET_ID = "ARRET_ID";
    
	
    public DatabaseHandler(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		String CREATE_LIGNE_TABLE = "CREATE TABLE " + TABLE_LIGNES + "("
                + LIGNES_ID + " INTEGER PRIMARY KEY,"
                + LIGNES_CODE + " TEXT,"
                + LIGNES_NOM + " TEXT,"
                + LIGNES_SENS +" TEXT,"
                + LIGNES_VERS +" TEXT)";

        String CREATE_FAVORIS_TABLE = "CREATE TABLE "+ TABLE_FAVORIS +"("
        		+ FAVORIS_ARRET_ID + "INTEGER PRIMARY KEY)";
		
		
		
        db.execSQL(CREATE_LIGNE_TABLE);
        db.execSQL(CREATE_FAVORIS_TABLE);
        
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub
		
	}

}
