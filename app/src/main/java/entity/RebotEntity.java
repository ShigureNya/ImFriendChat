package entity;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by jimhao on 16/9/4.
 */
public class RebotEntity {
    /**
     * 消息类型
     */
    private Type type ;
    /**
     * 消息内容RebotEntity
     */
    private String msg;
    /**
     * 日期
     */
    private Date date;
    /**
     * 日期的字符串格式
     */
    private String dateStr;
    /**
     * 发送人
     */
    private String name;

    public enum Type
    {
        INPUT, OUTPUT
    }

    public RebotEntity()
    {
    }

    public RebotEntity(Type type, String msg)
    {
        super();
        this.type = type;
        this.msg = msg;
        setDate(new Date());
    }

    public String getDateStr()
    {
        return dateStr;
    }
    public void setDate(Date date){
        this.date = date ;
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        this.dateStr = df.format(date);
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
