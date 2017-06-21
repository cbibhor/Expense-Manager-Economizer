package com.cbibhor.eme.expensemanagereconomizer;

/**
 * Created by Bibhor Chauhan on 18-04-2017.
 */

public class classTransaction {
    private int id;
    private String address;
    private Integer body;
    private String smsDay, smsDD, smsMM, smsYY;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getSmsDay() {
        return smsDay;
    }

    public void setSmsDay(String smsDay) {
        this.smsDay = smsDay;
    }

    public String getSmsDD() {
        return smsDD;
    }

    public void setSmsDD(String smsDD) {
        this.smsDD = smsDD;
    }

    public String getSmsMM() {
        return smsMM;
    }

    public void setSmsMM(String smsMM) {
        this.smsMM = smsMM;
    }

    public String getSmsYY() {
        return smsYY;
    }

    public void setSmsYY(String smsYY) {
        this.smsYY = smsYY;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public Integer getBody() {
        return body;
    }

    public void setBody(Integer body) {
        this.body = body;
    }
}
