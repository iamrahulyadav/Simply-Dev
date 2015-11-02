package com.builder.ibalance.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Shabaz on 02-Nov-15.
 */
public class PlansModel
{
    @SerializedName("price")
    @Expose
    private Integer price;
    @SerializedName("carrier")
    @Expose
    private String carrier;
    @SerializedName("circle")
    @Expose
    private String circle;
    @SerializedName("validity")
    @Expose
    private String validity;
    @SerializedName("type")
    @Expose
    private String type;
    @SerializedName("talktime")
    @Expose
    private Float talktime;
    @SerializedName("tags")
    @Expose
    private List<String> tags = new ArrayList<String>();
    @SerializedName("benefits")
    @Expose
    private String benefits;

    /**
     *
     * @return
     *     The price
     */
    public Integer getPrice() {
        return price;
    }

    /**
     *
     * @param price
     *     The price
     */
    public void setPrice(Integer price) {
        this.price = price;
    }

    /**
     *
     * @return
     *     The carrier
     */
    public String getCarrier() {
        return carrier;
    }

    /**
     *
     * @param carrier
     *     The carrier
     */
    public void setCarrier(String carrier) {
        this.carrier = carrier;
    }

    /**
     *
     * @return
     *     The circle
     */
    public String getCircle() {
        return circle;
    }

    /**
     *
     * @param circle
     *     The circle
     */
    public void setCircle(String circle) {
        this.circle = circle;
    }

    /**
     *
     * @return
     *     The validity
     */
    public String getValidity() {
        return validity;
    }

    /**
     *
     * @param validity
     *     The validity
     */
    public void setValidity(String validity) {
        this.validity = validity;
    }

    /**
     *
     * @return
     *     The type
     */
    public String getType() {
        return type;
    }

    /**
     *
     * @param type
     *     The type
     */
    public void setType(String type) {
        this.type = type;
    }

    /**
     *
     * @return
     *     The talktime
     */
    public Float getTalktime() {
        return talktime;
    }

    /**
     *
     * @param talktime
     *     The talktime
     */
    public void setTalktime(Float talktime) {
        this.talktime = talktime;
    }

    /**
     *
     * @return
     *     The tags
     */
    public List<String> getTags() {
        return tags;
    }

    /**
     *
     * @param tags
     *     The tags
     */
    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    /**
     *
     * @return
     *     The benefits
     */
    public String getBenefits() {
        return benefits;
    }

    /**
     *
     * @param benefits
     *     The benefits
     */
    public void setBenefits(String benefits) {
        this.benefits = benefits;
    }
}
