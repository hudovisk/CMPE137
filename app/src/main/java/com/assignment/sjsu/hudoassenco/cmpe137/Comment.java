package com.assignment.sjsu.hudoassenco.cmpe137;

import com.parse.ParseClassName;
import com.parse.ParseObject;
import com.parse.ParseUser;

/**
 * Created by hudoassenco on 12/2/15.
 */
@ParseClassName("Comment")
public class Comment extends ParseObject {

    public void setAuthor(ParseUser user) {
        put("author", user);
    }
    public ParseUser getAuthor() {
        return getParseUser("author");
    }

    public void setText(String text) {
        put("text", text);
    }

    public String getText() {
        return getString("text");
    }

    public void setPhoto(Photo photo) {
        put("originPhoto", photo);
    }
    public Photo getPhoto() {
        return (Photo) get("originPhoto");
    }

}
