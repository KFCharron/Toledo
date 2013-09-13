package com.mediacrossing.connections;

import org.apache.commons.codec.binary.Base64;
import scala.Tuple2;

import java.io.UnsupportedEncodingException;

public class ConnectionRequestProperties {

    public static Tuple2<String, String> authorization(String username, String password) {
        try {
            final String loginPassword = username + ":" + password;

        final String encoded = new Base64().encodeToString(loginPassword.getBytes("UTF-8"));

        return new Tuple2<String, String>("Authorization", "Basic " + encoded);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    public static Tuple2<String, String> authorization(String token) {
        return new Tuple2<String, String>("Authorization", token);
    }
}
