package net.vpc.toolbox.mysql.util;

import java.text.SimpleDateFormat;
import java.util.Date;

public class MysqlUtils {

    public static String newDateString() {
        return new SimpleDateFormat("yyyy-MM-dd-HHmmss-SSS").format(new Date());
    }
}
