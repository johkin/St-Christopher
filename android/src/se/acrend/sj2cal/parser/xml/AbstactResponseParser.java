package se.acrend.sj2cal.parser.xml;

import java.util.Calendar;

import se.acrend.christopher.shared.model.AbstractResponse;
import se.acrend.christopher.shared.model.ErrorCode;
import se.acrend.christopher.shared.model.ReturnCode;
import se.acrend.sj2cal.util.DateUtil;
import android.sax.EndTextElementListener;
import android.sax.RootElement;

public abstract class AbstactResponseParser {

  private static final String TAG = "AbstactResponseParser";

  protected void handleResponse(final RootElement root, final AbstractResponse response) {

    root.getChild("errorCode").setEndTextElementListener(new EndTextElementListener() {
      @Override
      public void end(final String body) {
        response.setErrorCode(ErrorCode.valueOf(body));
      }
    });
    root.getChild("returnCode").setEndTextElementListener(new EndTextElementListener() {
      @Override
      public void end(final String body) {
        response.setReturnCode(ReturnCode.valueOf(body));
      }
    });

  }

  protected Calendar parseTime(final String time) {
    if (time == null) {
      return null;
    }
    if (time.length() == 0) {
      return null;
    }
    return DateUtil.parseTime(time);
  }
}