package org.gospelcoding.dailydose;

import com.orm.SugarRecord;

/**
 * Created by rick on 6/11/17.
 */

public class Category extends SugarRecord<Category>{
    String name;

    public Category(){
    }

    public Category(String newName){
        name = newName;
    }

}
