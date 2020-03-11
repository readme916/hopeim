package com.tianyoukeji.parent.common;

import org.springframework.util.DigestUtils;

import java.util.Random;

public  class AvatarUtils {
//    private static final String[] TYPE=new String[]{"wavatar","monsterid","robohash"};
    public static String generatorUserAvatar(String username){

        String mdtStr = DigestUtils.md5DigestAsHex(username.getBytes());
        return "https://www.gravatar.com/avatar/"+mdtStr+"?d=robohash&s=256";
    }
    
    public static String generatorGroupAvatar(String name){
        String mdtStr = DigestUtils.md5DigestAsHex(name.getBytes());
        return "https://www.gravatar.com/avatar/"+mdtStr+"?d=wavatar&s=256";
    }

}
