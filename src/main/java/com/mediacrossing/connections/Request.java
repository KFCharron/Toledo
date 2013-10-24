package com.mediacrossing.connections;

import java.io.IOException;

public interface Request {

    String getRequest(String url) throws Exception;
    String postRequest(String url, String json) throws Exception;
}
