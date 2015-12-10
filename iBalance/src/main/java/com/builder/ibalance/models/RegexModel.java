package com.builder.ibalance.models;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;


public class RegexModel {

    @SerializedName("id")
    @Expose
    private int id;
    @SerializedName("version")
    @Expose
    private int version;
    @SerializedName("REGEX")
    @Expose
    private String REGEX;

    /**
     *
     * @return
     * The id
     */
    public int getId() {
        return id;
    }

    /**
     *
     * @param id
     * The id
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     *
     * @return
     * The version
     */
    public int getVersion() {
        return version;
    }

    /**
     *
     * @param version
     * The version
     */
    public void setVersion(int version) {
        this.version = version;
    }

    /**
     *
     * @return
     * The REGEX
     */
    public String getREGEX() {
        return REGEX;
    }

    /**
     *
     * @param REGEX
     * The REGEX
     */
    public void setREGEX(String REGEX) {
        this.REGEX = REGEX;
    }

}