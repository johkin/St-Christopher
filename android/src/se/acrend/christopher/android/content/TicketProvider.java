package se.acrend.christopher.android.content;

import se.acrend.christopher.android.db.TicketDatabaseHelper;
import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

public class TicketProvider extends ContentProvider {

  private static final String TAG = "TicketProvider";

  private UriMatcher uriMatcher = null;
  // private Map<String, String> projectionMap = null;

  private static final int TICKET = 1;
  private static final int TICKETS = 2;

  private TicketDatabaseHelper ticketDatabaseHelper;

  @Override
  public boolean onCreate() {

    uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    uriMatcher.addURI(ProviderTypes.AUTHORITY, ProviderTypes.PATH_SINGLE, TICKET);
    uriMatcher.addURI(ProviderTypes.AUTHORITY, ProviderTypes.PATH_MULTIPLE, TICKETS);

    return true;
  }

  @Override
  public int delete(final Uri uri, final String selection, final String[] selectionArgs) {
    Log.d(TAG, "Delete Ticket, Uri: " + uri);
    int count = 0;
    switch (uriMatcher.match(uri)) {
    case TICKET:
      String segment = uri.getPathSegments().get(1);
      String whereSql = "_id=" + segment;
      if (!TextUtils.isEmpty(selection)) {
        whereSql += "AND (" + selection + ")";
      }

      count = getDatabaseHelper().getWritableDatabase().delete("ticket", whereSql, selectionArgs);
      break;
    case TICKETS:

      count = getDatabaseHelper().getWritableDatabase().delete("ticket", selection, selectionArgs);
      break;
    default:
      throw new IllegalArgumentException("Uknonw URI: " + uri);
    }

    return count;
  }

  @Override
  public String getType(final Uri uri) {
    switch (uriMatcher.match(uri)) {
    case TICKET:
      return ProviderTypes.TICKET_ITEM_TYPE;
    case TICKETS:
      return ProviderTypes.TICKET_MULTIPLE_TYPE;
    default:
      throw new IllegalArgumentException("Uknonw URI: " + uri);
    }
  }

  @Override
  public Cursor query(final Uri uri, final String[] projection, final String selection, final String[] selectionArgs,
      final String sortOrder) {
    Log.d(TAG, "Query Ticket, Uri: " + uri);

    SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
    String orderBy = "originalDeparture ASC";

    switch (uriMatcher.match(uri)) {
    case TICKET:
      builder.setTables("ticket");

      builder.appendWhere("_id=" + uri.getPathSegments().get(1));

      break;
    case TICKETS:
      builder.setTables("ticket");

      // builder.setProjectionMap(projectionMap);
      break;
    default:
      throw new IllegalArgumentException("Uknonw URI: " + uri);
    }

    if (!TextUtils.isEmpty(sortOrder)) {
      orderBy = sortOrder;
    }
    Cursor cursor = builder.query(getDatabaseHelper().getReadableDatabase(), projection, selection, selectionArgs,
        null, null, orderBy);
    cursor.setNotificationUri(getContext().getContentResolver(), uri);

    return cursor;
  }

  @Override
  public Uri insert(final Uri uri, final ContentValues values) {
    Log.d(TAG, "Insert Ticket, Uri: " + uri);
    if (uriMatcher.match(uri) != TICKETS) {
      throw new IllegalArgumentException("Unkown Uri: " + uri + ". Should be " + ProviderTypes.CONTENT_URI);
    }

    long id = getDatabaseHelper().getWritableDatabase().insert("ticket", null, values);
    Log.d(TAG, "Insert Ticket, id: " + id);
    if (id > 0) {

      Uri result = ContentUris.withAppendedId(ProviderTypes.CONTENT_URI, id);

      Log.d(TAG, "Insert Ticket, result: " + result);

      getContext().getContentResolver().notifyChange(result, null);

      return result;
    }

    throw new SQLException("Failed to insert row into " + uri);
  }

  @Override
  public int update(final Uri uri, final ContentValues values, final String selection, final String[] selectionArgs) {
    Log.d(TAG, "Update Ticket, Uri: " + uri);
    int count = 0;
    switch (uriMatcher.match(uri)) {
    case TICKET:
      String segment = uri.getPathSegments().get(1);
      String whereSql = "_id=" + segment;
      if (!TextUtils.isEmpty(selection)) {
        whereSql += "AND (" + selection + ")";
      }

      count = getDatabaseHelper().getWritableDatabase().update("ticket", values, whereSql, selectionArgs);
      if (count > 0) {
        getContext().getContentResolver().notifyChange(uri, null);
      }
      break;
    case TICKETS:

      count = getDatabaseHelper().getWritableDatabase().update("ticket", values, selection, selectionArgs);
      if (count > 0) {
        getContext().getContentResolver().notifyChange(uri, null);
      }
      break;
    default:
      throw new IllegalArgumentException("Uknonw URI: " + uri);
    }

    return count;
  }

  TicketDatabaseHelper getDatabaseHelper() {
    if (ticketDatabaseHelper == null) {
      ticketDatabaseHelper = new TicketDatabaseHelper(getContext());
    }
    return ticketDatabaseHelper;
  }

}
