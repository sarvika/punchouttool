package io.sarvika.potool.common.model;

import java.util.HashMap;
import java.util.Map;

public class ItemIn {

    private int qty;
    private String partId;
    private String partAuxId;
    private double price;
    private String desc;
    private String uom;
    private Map<String, String> classifications = new HashMap<>();

    public int getQty() {
        return qty;
    }

    public void setQty(int qty) {
        this.qty = qty;
    }

    public String getPartId() {
        return partId;
    }

    public void setPartId(String partId) {
        this.partId = partId;
    }

    public String getPartAuxId() {
        return partAuxId;
    }

    public void setPartAuxId(String partAuxId) {
        this.partAuxId = partAuxId;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getUom() {
        return uom;
    }

    public void setUom(String uom) {
        this.uom = uom;
    }

    public Map<String, String> getClassifications() {
        return classifications;
    }

    public void setClassifications(Map<String, String> classifications) {
        this.classifications = classifications;
    }
}
