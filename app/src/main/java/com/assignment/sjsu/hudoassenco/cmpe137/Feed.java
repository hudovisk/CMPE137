package com.assignment.sjsu.hudoassenco.cmpe137;

import com.parse.ParseClassName;
import com.parse.ParseObject;
import com.parse.ParseUser;

/**
 * Created by fernandoabolafio on 12/3/15.
 */
@ParseClassName("Feed")
public class Feed extends ParseObject {

    public void setAuthor (ParseUser user){
        put("author",user );

    }
    public  void setAlbum (Album album){
        put("album", album);
    }

    public  ParseUser getAuthor (){
        return getParseUser("author");
    }

    public Album getAlbum () {
        return (Album) getParseObject("album");
    }
}
