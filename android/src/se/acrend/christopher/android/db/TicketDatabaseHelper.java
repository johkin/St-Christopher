package se.acrend.christopher.android.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class TicketDatabaseHelper extends SQLiteOpenHelper {

  private static final String TAG = "TicketDatabaseHelper";

  private static final int DATABASE_VERSION = 14;
  public static final String DATABASE_NAME = "christopher.db";

  public TicketDatabaseHelper(final Context context) {
    super(context, DATABASE_NAME, null, DATABASE_VERSION);
  }

  @Override
  public void onCreate(final SQLiteDatabase db) {
    db.execSQL("CREATE TABLE ticket (" + "_id INTEGER PRIMARY KEY AUTOINCREMENT," + "ticketCode TEXT NOT NULL ,"
        + "ticketText TEXT NOT NULL ,trainNo TEXT, car TEXT, seat TEXT,"
        + "originalDeparture TEXT NOT NULL, estimatedDeparture TEXT, guessedDeparture TEXT, actualDeparture TEXT, "
        + "originalArrival TEXT NOT NULL, estimatedArrival TEXT, guessedArrival TEXT, actualArrival TEXT, "
        + "fromStation TEXT NOT NULL, toStation TEXT NOT NULL, "
        + "notify INTEGER NOT NULL DEFAULT 1, registered INTEGER NOT NULL DEFAULT 0, "
        + "departureTrack TEXT, arrivalTrack TEXT, departureInfo TEXT, arrivalInfo TEXT, "
        + "arrivalCancelled INTEGER NOT NULL DEFAULT 0, departureCancelled INTEGER NOT NULL DEFAULT 0);");
  }

  @Override
  public void onUpgrade(final SQLiteDatabase db, int oldVersion, final int newVersion) {
    Log.d(TAG, "Uppgraderar");
    try {
      if (oldVersion < 14) {
        upgradeTo14(db);
        oldVersion = 14;
      }
    } catch (SQLiteException e) {
      Log.e(TAG, "onUpgrade: SQLiteException, recreating db. " + e);
      dropTables(db);
      onCreate(db);
      return; // this was lossy
    }
  }

  private void dropTables(final SQLiteDatabase db) {
    db.execSQL("DROP TABLE ticket");
  }

  private void upgradeTo14(final SQLiteDatabase db) {
    db.execSQL("ALTER TABLE ticket ADD COLUMN arrivalCancelled INTEGER NOT NULL DEFAULT 0");
    db.execSQL("ALTER TABLE ticket ADD COLUMN departureCancelled INTEGER NOT NULL DEFAULT 0");
    db.execSQL("ALTER TABLE ticket DROP COLUMN calendarEventUri");
  }

  @Override
  public void onOpen(final SQLiteDatabase db) {
    super.onOpen(db);
  }
}