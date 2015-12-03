package com.assignment.sjsu.hudoassenco.cmpe137;

import com.parse.ParseClassName;
import com.parse.ParseObject;
import com.parse.ParseUser;

/**
 * Created by hudoassenco on 12/2/15.
 */
@ParseClassName("Comment")
public class Comment extends ParseObject {

    public ParseUser getAuthor() {
        return getParseUser("author");
    }

}
