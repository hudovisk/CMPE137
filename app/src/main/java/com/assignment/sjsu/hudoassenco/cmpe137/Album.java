package com.assignment.sjsu.hudoassenco.cmpe137;

import com.parse.ParseClassName;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseRelation;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by fernandoabolafio on 11/26/15.
 */
@ParseClassName("Album")
public class Album  extends ParseObject {

    public String getName () {
        return  getString("name");
    }

    public String getDescription() {
       return getString("description");
    }

    public void setName (String value){
        put("name",value);
    }
    public int getNumberOfCollaborators (){
        return getInt("numberOfCollaborators");
    }

    public void incrementNumberOfCollaborators (){
        increment("numberOfCollaborators");
    }

    public void setDescription(String value){
        put("description",value);
    }

    public void setAuthor (ParseUser user){
        put("author",user);
    }

    public ParseUser getAuthor (){
      return getParseUser("author");
    }
    public void addColaborator(ParseUser user){
        ParseRelation <ParseUser> relation = getRelation("collaborators");
        relation.add(user);
    }

}
