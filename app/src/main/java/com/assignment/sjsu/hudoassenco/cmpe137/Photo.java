package com.assignment.sjsu.hudoassenco.cmpe137;

import com.parse.ParseClassName;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseUser;

import java.util.List;

/**
 * Created by hudoassenco on 12/2/15.
 */
@ParseClassName("Photo")
public class Photo extends ParseObject {

    public void setAuthor(ParseUser user) {
        put("author", user);
    }

    public ParseUser getAuthor() {
        return getParseUser("author");
    }

    public void setAlbum(Album album) {
        put("originAlbum", album);
    }

    public Album getAlbum() {
        return (Album) get("originAlbum");
    }

    public ParseFile getImage() {
        return getParseFile("image");
    }

    public void setImage(ParseFile image) {
        put("image", image);
    }

}
