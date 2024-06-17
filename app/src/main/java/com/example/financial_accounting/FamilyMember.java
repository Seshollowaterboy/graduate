package com.example.financial_accounting;

public class FamilyMember {
    private String _name;
    private String _status;

    public FamilyMember(String name, String status){
        this._name = name;
        this._status = status;
    }


    public String getName() {
        return _name;
    }

    public void setName(String _name) {
        this._name = _name;
    }

    public String getStatus() {
        return _status;
    }

    public void setStatus(String _status) {
        this._status = _status;
    }
}
