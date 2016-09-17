package entity;

/**
 * Created by Ran on 2016/9/15.
 */
public class DateEntity {
    public String year ;
    public int month ;
    public int day ;
    public String hour;
    public String minite ;
    public boolean isAM ;   //是否为上午

    public String getYear() {
        return year;
    }

    public void setYear(String year) {
        this.year = year;
    }

    public int getMonth() {
        return month;
    }

    public void setMonth(int month) {
        this.month = month;
    }

    public int getDay() {
        return day;
    }

    public void setDay(int day) {
        this.day = day;
    }

    public String getHour() {
        return hour;
    }

    public void setHour(String hour) {
        this.hour = hour;
    }

    public String getMinite() {
        return minite;
    }

    public void setMinite(String minite) {
        this.minite = minite;
    }

    public boolean isAM() {
        return isAM;
    }

    public void setAM(boolean AM) {
        isAM = AM;
    }
}
