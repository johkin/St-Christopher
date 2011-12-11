package se.acrend.christopher.server.parser;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import javax.xml.bind.DatatypeConverter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import se.acrend.christopher.server.parser.TrafikVerketJsonParser.Response.LpvTrafiklagen.Trafiklage;
import se.acrend.christopher.server.util.DateUtil;
import se.acrend.christopher.shared.model.StationInfo;
import se.acrend.christopher.shared.model.TimeInfo;
import se.acrend.christopher.shared.model.TimeInfo.Status;
import se.acrend.christopher.shared.model.TrainInfo;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

@Component
public class TrafikVerketJsonParser {

  private final Logger log = LoggerFactory.getLogger(getClass());

  public TrainInfo parse(final String content, final String date, final String trainNo) {

    TrainInfo trainInfo = new TrainInfo();
    trainInfo.setDate(date);
    trainInfo.setTrainNo(trainNo);

    Gson gson = createJsonParser();

    Response response = gson.fromJson(content, Response.class);

    Trafiklage firstLage = null;
    for (Trafiklage lage : response.getLpvTrafiklagen().getTrafiklage()) {
      if (firstLage == null) {
        firstLage = lage;
      }
      StationInfo info = new StationInfo();
      if (lage.isArAvgangTag()) {
        TimeInfo time = new TimeInfo();
        time.setActual(lage.getVerkligTidpunktAvgang());
        time.setEstimated(lage.getBeraknadTidpunktAvgang());
        time.setOriginal(lage.getAnnonseradTidpunktAvgang());
        if (lage.isInstalldAvgang()) {
          time.setStatus(Status.Cancelled);
        } else {
          if (isDelayed(time)) {
            time.setStatus(Status.Delayed);
          } else {
            time.setStatus(Status.Ok);
          }
        }
        time.setTrack(lage.getSparangivelseAvgang());
        info.setDeparture(time);
      }
      if (lage.isArAnkomstTag()) {
        TimeInfo time = new TimeInfo();
        time.setActual(lage.getVerkligTidpunktAnkomst());
        time.setEstimated(lage.getBeraknadTidpunktAnkomst());
        time.setOriginal(lage.getAnnonseradTidpunktAnkomst());
        if (lage.isInstalldAnkomst()) {
          time.setStatus(Status.Cancelled);
        } else {
          if (isDelayed(time)) {
            time.setStatus(Status.Delayed);
          } else {
            time.setStatus(Status.Ok);
          }
        }
        time.setTrack(lage.getSparangivelseAnkomst());
        info.setArrival(time);
      }
      info.setName(lage.getTrafikplatsNamn());
      // TODO Spår för både avgång och ankomst

      trainInfo.getStations().add(info);
    }
    trainInfo.setLastKnownPosition(firstLage.getSenasteTidrapportTrafikplats());
    trainInfo.setLastKnownTime(DateUtil.formatTime(firstLage.getSenasteTidrapportTidpunkt()));

    return trainInfo;
  }

  Gson createJsonParser() {
    GsonBuilder builder = new GsonBuilder();
    builder.setFieldNamingPolicy(FieldNamingPolicy.UPPER_CAMEL_CASE);
    builder.registerTypeAdapter(Calendar.class, new JsonDeserializer<Calendar>() {

      @Override
      public Calendar deserialize(final JsonElement json, final Type typeOfT, final JsonDeserializationContext context)
          throws JsonParseException {

        String string = json.getAsString();
        Calendar time = DatatypeConverter.parseDateTime(string);
        return time;
      }

    });
    Gson gson = builder.create();
    return gson;
  }

  public List<TrainGroupInfo> parseTrainGroup(final String content) {
    List<TrainGroupInfo> result = new ArrayList<TrainGroupInfo>();

    Gson gson = createJsonParser();

    Response response = gson.fromJson(content, Response.class);

    for (Trafiklage lage : response.getLpvTrafiklagen().getTrafiklage()) {
      TrainGroupInfo info = new TrainGroupInfo();

      info.setDate(DateUtil.formatDate(lage.getUtgangsdatum()));
      info.setTrainNo(lage.getAnnonseratTagId());
      info.setGroupNo(lage.getTagGrupp());

      result.add(info);
    }

    return result;
  }

  private boolean isDelayed(final TimeInfo time) {
    if (time.getOriginal().before(time.getActual()) || time.getOriginal().before(time.getEstimated())
        || time.getOriginal().before(time.getGuessed())) {
      return true;
    }
    return false;
  }

  public static class TrainGroupInfo {
    private String trainNo;
    private String date;
    private String groupNo;

    public String getTrainNo() {
      return trainNo;
    }

    public void setTrainNo(final String trainNo) {
      this.trainNo = trainNo;
    }

    public String getDate() {
      return date;
    }

    public void setDate(final String date) {
      this.date = date;
    }

    public String getGroupNo() {
      return groupNo;
    }

    public void setGroupNo(final String groupNo) {
      this.groupNo = groupNo;
    }
  }

  public static class Response {
    private LpvTrafiklagen lpvTrafiklagen;

    public LpvTrafiklagen getLpvTrafiklagen() {
      return lpvTrafiklagen;
    }

    public void setLpvTrafiklagen(final LpvTrafiklagen lpvTrafiklagen) {
      this.lpvTrafiklagen = lpvTrafiklagen;
    }

    public static class LpvTrafiklagen {

      private List<Trafiklage> trafiklage = new ArrayList<Trafiklage>();

      public void setTrafiklage(final List<Trafiklage> trafiklage) {
        this.trafiklage = trafiklage;
      }

      public List<Trafiklage> getTrafiklage() {
        return trafiklage;
      }

      public static class Trafiklage {

        private String trafikplatsNamn;
        private String annonseratTagId;
        private Calendar utgangsdatum;
        private Calendar annonseradTidpunktAnkomst;
        private Calendar annonseradTidpunktAvgang;
        private Calendar verkligTidpunktAnkomst;
        private Calendar verkligTidpunktAvgang;
        private Calendar beraknadTidpunktAnkomst;
        private Calendar beraknadTidpunktAvgang;
        private boolean arAnkomstTag;
        private boolean arAvgangTag;
        private boolean installdAnkomst;
        private boolean installdAvgang;
        private String sparangivelseAnkomst;
        private String sparangivelseAvgang;
        private String senasteTidrapportAktivitet;
        private Calendar senasteTidrapportTidpunkt;
        private String senasteTidrapportTrafikplats;
        private String tagGrupp;

        public String getTagGrupp() {
          return tagGrupp;
        }

        public void setTagGrupp(final String tagGrupp) {
          this.tagGrupp = tagGrupp;
        }

        public String getSenasteTidrapportAktivitet() {
          return senasteTidrapportAktivitet;
        }

        public void setSenasteTidrapportAktivitet(final String senasteTidrapportAktivitet) {
          this.senasteTidrapportAktivitet = senasteTidrapportAktivitet;
        }

        public Calendar getSenasteTidrapportTidpunkt() {
          return senasteTidrapportTidpunkt;
        }

        public void setSenasteTidrapportTidpunkt(final Calendar senasteTidrapportTidpunkt) {
          this.senasteTidrapportTidpunkt = senasteTidrapportTidpunkt;
        }

        public String getSenasteTidrapportTrafikplats() {
          return senasteTidrapportTrafikplats;
        }

        public void setSenasteTidrapportTrafikplats(final String senasteTidrapportTrafikplats) {
          this.senasteTidrapportTrafikplats = senasteTidrapportTrafikplats;
        }

        public String getSparangivelseAnkomst() {
          return sparangivelseAnkomst;
        }

        public void setSparangivelseAnkomst(final String sparangivelseAnkomst) {
          this.sparangivelseAnkomst = sparangivelseAnkomst;
        }

        public String getSparangivelseAvgang() {
          return sparangivelseAvgang;
        }

        public void setSparangivelseAvgang(final String sparangivelseAvgang) {
          this.sparangivelseAvgang = sparangivelseAvgang;
        }

        public boolean isInstalldAnkomst() {
          return installdAnkomst;
        }

        public void setInstalldAnkomst(final boolean installdAnkomst) {
          this.installdAnkomst = installdAnkomst;
        }

        public boolean isInstalldAvgang() {
          return installdAvgang;
        }

        public void setInstalldAvgang(final boolean installdAvgang) {
          this.installdAvgang = installdAvgang;
        }

        public String getTrafikplatsNamn() {
          return trafikplatsNamn;
        }

        public void setTrafikplatsNamn(final String trafikplatsNamn) {
          this.trafikplatsNamn = trafikplatsNamn;
        }

        public String getAnnonseratTagId() {
          return annonseratTagId;
        }

        public void setAnnonseratTagId(final String annonseratTagId) {
          this.annonseratTagId = annonseratTagId;
        }

        public Calendar getUtgangsdatum() {
          return utgangsdatum;
        }

        public void setUtgangsdatum(final Calendar utgangsdatum) {
          this.utgangsdatum = utgangsdatum;
        }

        public Calendar getAnnonseradTidpunktAnkomst() {
          return annonseradTidpunktAnkomst;
        }

        public void setAnnonseradTidpunktAnkomst(final Calendar annonseradTidpunktAnkomst) {
          this.annonseradTidpunktAnkomst = annonseradTidpunktAnkomst;
        }

        public Calendar getAnnonseradTidpunktAvgang() {
          return annonseradTidpunktAvgang;
        }

        public void setAnnonseradTidpunktAvgang(final Calendar annonseradTidpunktAvgang) {
          this.annonseradTidpunktAvgang = annonseradTidpunktAvgang;
        }

        public Calendar getVerkligTidpunktAnkomst() {
          return verkligTidpunktAnkomst;
        }

        public void setVerkligTidpunktAnkomst(final Calendar verkligTidpunktAnkomst) {
          this.verkligTidpunktAnkomst = verkligTidpunktAnkomst;
        }

        public Calendar getVerkligTidpunktAvgang() {
          return verkligTidpunktAvgang;
        }

        public void setVerkligTidpunktAvgang(final Calendar verkligTidpunktAvgang) {
          this.verkligTidpunktAvgang = verkligTidpunktAvgang;
        }

        public Calendar getBeraknadTidpunktAnkomst() {
          return beraknadTidpunktAnkomst;
        }

        public void setBeraknadTidpunktAnkomst(final Calendar beraknadTidpunktAnkomst) {
          this.beraknadTidpunktAnkomst = beraknadTidpunktAnkomst;
        }

        public Calendar getBeraknadTidpunktAvgang() {
          return beraknadTidpunktAvgang;
        }

        public void setBeraknadTidpunktAvgang(final Calendar beraknadTidpunktAvgang) {
          this.beraknadTidpunktAvgang = beraknadTidpunktAvgang;
        }

        public boolean isArAnkomstTag() {
          return arAnkomstTag;
        }

        public void setArAnkomstTag(final boolean arAnkomstTag) {
          this.arAnkomstTag = arAnkomstTag;
        }

        public boolean isArAvgangTag() {
          return arAvgangTag;
        }

        public void setArAvgangTag(final boolean arAvgangTag) {
          this.arAvgangTag = arAvgangTag;
        }
      }
    }
  }
}
