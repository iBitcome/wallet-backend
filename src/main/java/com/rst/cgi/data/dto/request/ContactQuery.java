package com.rst.cgi.data.dto.request;

import java.util.Objects;

public class ContactQuery {
    private String token;
    private String address;

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getToken() {
        return token;

    }

    @Override
    public String toString() {
        return "ContactQuery{" +
                "token='" + token + '\'' +
                ", address='" + address + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ContactQuery that = (ContactQuery) o;
        return Objects.equals(token, that.token) &&
                Objects.equals(address, that.address);
    }

    @Override
    public int hashCode() {

        return Objects.hash(token, address);
    }

    public void setToken(String token) {
        this.token = token;
    }
}
