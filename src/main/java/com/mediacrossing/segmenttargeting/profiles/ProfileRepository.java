package com.mediacrossing.segmenttargeting.profiles;

import com.mediacrossing.segmenttargeting.Profile;
import scala.Tuple2;

import java.util.List;

public interface ProfileRepository {

    List<Profile> findBy(List<Tuple2<String, String>> advertiserIdAndProfileIds);
}
