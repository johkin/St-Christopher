package se.acrend.christopher.server.control;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.util.Calendar;

import se.acrend.christopher.shared.model.TrainInfo;

public interface TrafikverketController {

  TrainInfo getTagInfo(final String trainNo, final Calendar cal) throws MalformedURLException, IOException,
      UnsupportedEncodingException;

}