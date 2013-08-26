package com.mediacrossing.segmenttargeting.profiles;

import com.mediacrossing.segmenttargeting.Profile;
import scala.Tuple2;

import java.util.List;

public class PartitionedProfileRepository implements ProfileRepository {

    @Override
    public List<Profile> findBy(List<Tuple2<String, String>> advertiserIdAndProfileIds) {
        throw new UnsupportedOperationException("Not implemented yet");
    }
}
