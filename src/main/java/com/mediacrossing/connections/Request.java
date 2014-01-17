package com.mediacrossing.connections;

public interface Request {

    String getRequest(String url) throws Exception;
    String postRequest(String url, String json) throws Exception;
}
