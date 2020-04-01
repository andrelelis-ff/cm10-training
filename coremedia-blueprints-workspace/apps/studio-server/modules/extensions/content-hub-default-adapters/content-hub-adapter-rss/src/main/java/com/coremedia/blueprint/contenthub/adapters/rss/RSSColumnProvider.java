package com.coremedia.blueprint.contenthub.adapters.rss;

import com.coremedia.contenthub.api.ContentHubObject;
import com.coremedia.contenthub.api.Folder;
import com.coremedia.contenthub.api.column.Column;
import com.coremedia.contenthub.api.column.ColumnValue;
import com.coremedia.contenthub.api.column.DefaultColumnProvider;
import com.rometools.rome.feed.synd.SyndEntry;
import edu.umd.cs.findbugs.annotations.NonNull;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Adds a custom column for the lifecycle status of a content.
 */
class RSSColumnProvider extends DefaultColumnProvider {

  @NonNull
  @Override
  public List<Column> getColumns(Folder folder) {
    List<Column> columns = new ArrayList<>(super.getColumns(folder));
    columns.add(new Column("author", "author", 100, -1));
    columns.add(new Column("lastModified", "lastModified", 100, -1));
    return columns;
  }

  @NonNull
  @Override
  public List<ColumnValue> getColumnValues(ContentHubObject hubObject) {
    String author = null;
    Calendar lastModified = null;
    if (hubObject instanceof RSSItem) {
      RSSItem item = (RSSItem) hubObject;
      author = item.getRssEntry().getAuthor();
      SyndEntry rssEntry = item.getRssEntry();

      lastModified = getCalendarFromDate(rssEntry.getUpdatedDate() != null ? rssEntry.getUpdatedDate() : rssEntry.getPublishedDate());


    } else if (hubObject instanceof RSSFolder) {
      RSSFolder folder = (RSSFolder) hubObject;
      lastModified = getCalendarFromDate(folder.getFeed().getPublishedDate());
    }

    List<ColumnValue> columnValues = new ArrayList<>(super.getColumnValues(hubObject));
    columnValues.add(new ColumnValue("author", author, null, author));
    columnValues.add(new ColumnValue("lastModified", lastModified, null, author));
    return columnValues;
  }

  private Calendar getCalendarFromDate(Date date) {
    Calendar calendar = Calendar.getInstance();
    calendar.setTime(date);
    return calendar;
  }
}
