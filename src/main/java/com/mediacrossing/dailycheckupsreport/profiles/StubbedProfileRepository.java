package com.mediacrossing.dailycheckupsreport.profiles;

import com.mediacrossing.dailycheckupsreport.Profile;
import scala.Tuple2;

import java.util.Collections;
import java.util.List;

public class StubbedProfileRepository implements ProfileRepository {

    @Override
    public List<Profile> findBy(List<Tuple2<String, String>> advertiserIdAndProfileIds) {

        // TODO Read JSON from file to return canned response

        return Collections.emptyList();
    }
}
