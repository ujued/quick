package net.apisp.quick.log.def;

import java.text.SimpleDateFormat;
import java.util.Date;

import net.apisp.quick.log.Log;
import net.apisp.quick.util.Strings;

public class ConsoleLog implements Log {
    private String level;
    private String name;

    public ConsoleLog() {
    }

    public ConsoleLog(String level, String name) {
        this.level = level;
        this.name = name;
    }

    public static void main(String[] args){
        System.out.println(new ConsoleLog().limitPackageName("com.apisp.qq.ds", 13));
    }

    /**
     * 限制包名长度简单实现
     *
     * @param name
     * @param len
     * @return
     */
    private String limitPackageName(String name, int len){
        if (name.length() > len){
            StringBuilder limitName = new StringBuilder();
            boolean append = false;
            int ignoreCount = name.length() - len;
            for (int i = 0; i < name.length(); i++){
                char c = name.charAt(i);
                if (i == 0 || append){
                    limitName.append(c);
                    append = false;
                    continue;
                }
                if (c == '.'){
                    append = true;
                    limitName.append(c);
                    continue;
                }
                if (ignoreCount <= 0){
                    limitName.append(c);
                    continue;
                }
                ignoreCount--;
            }
            return limitName.toString();
        }
        return name;
    }

    /**
     * 日志前置
     *
     * @param level
     * @return
     */
    private String before(String level) {

        return String.format(new SimpleDateFormat("MM-dd HH:mm:ss").format(new Date()) + " %6s [%11s] %-36s <| ", level,
                Thread.currentThread().getName(), limitPackageName(name, 36));
    }

    @Override
    public void show(String log, Object... args) {
        System.out.println(before("SHOW") + Strings.template(log, args));
    }

    @Override
    public void error(String log, Object... args) {
        if (isErrorEnabled()) {
            System.err.println(before(Levels.ERROR) + Strings.template(log, args));
        }
    }

    @Override
    public void warn(String log, Object... args) {
        if (isWarnEnabled()) {
            System.err.println(before(Levels.WARN) + Strings.template(log, args));
        }
    }

    @Override
    public void info(String log, Object... args) {
        if (isInfoEnabled()) {
            System.out.println(before(Levels.INFO) + Strings.template(log, args));
        }
    }

    @Override
    public void debug(String log, Object... args) {
        if (isDebugEnable()) {
            System.out.println(before(Levels.DEBUG) + Strings.template(log, args));
        }
    }

    @Override
    public void show(Throwable e) {
        e.printStackTrace();
    }

    @Override
    public void error(Throwable e) {
        if (isErrorEnabled()) {
            e.printStackTrace();
        }
    }

    @Override
    public void warn(Throwable e) {
        if (isWarnEnabled()) {
            e.printStackTrace();
        }
    }

    @Override
    public void info(Throwable e) {
        if (isInfoEnabled()) {
            e.printStackTrace();
        }
    }

    @Override
    public void debug(Throwable e) {
        if (isDebugEnable()) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean isErrorEnabled() {
        return (level.equalsIgnoreCase(Levels.ERROR) || level.equalsIgnoreCase(Levels.WARN)
                || level.equalsIgnoreCase(Levels.INFO) || level.equalsIgnoreCase(Levels.DEBUG));
    }

    @Override
    public boolean isWarnEnabled() {
        return (level.equalsIgnoreCase(Levels.WARN) || level.equalsIgnoreCase(Levels.INFO)
                || level.equalsIgnoreCase(Levels.DEBUG));
    }

    @Override
    public boolean isInfoEnabled() {
        return (level.equalsIgnoreCase(Levels.INFO) || level.equalsIgnoreCase(Levels.DEBUG));
    }

    @Override
    public boolean isDebugEnable() {
        return (level.equalsIgnoreCase(Levels.DEBUG));
    }

    @Override
    public void log(String level, String log, Object... args) {
        if (this.level.equals(level)) {
            System.out.println(before(level) + Strings.template(log, args));
        }
    }

    @Override
    public void log(String level, Throwable e) {
        if (this.level.equals(level)) {
            e.printStackTrace();
        }
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public void setLevel(String level) {
        this.level = level;
    }

    @Override
    public Log normalize() {
        return this;
    }

}
