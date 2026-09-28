package com.dtv.ct.commercetool.model;

public class typeId {
    String id;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    String typeId;

    public String getTypeId() {
        return typeId;
    }

    public void setTypeId(String typeId) {
        this.typeId = typeId;
    }

    //construtor

    public typeId(String typeId, String id) {
        this.typeId = typeId;
        this.id = id;
    }
}


