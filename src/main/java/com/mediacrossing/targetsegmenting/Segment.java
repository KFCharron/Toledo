package com.mediacrossing.targetsegmenting;

/**
 * Created with IntelliJ IDEA.
 * User: charronkyle
 * Date: 8/21/13
 * Time: 3:27 PM
 * To change this template use File | Settings | File Templates.
 */
public class Segment {
    private String id;
    private String name;
    private String action;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }
}
