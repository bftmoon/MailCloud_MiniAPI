package com.nodo.mailcloud_miniapi;

import com.franmontiel.persistentcookiejar.persistence.CookiePersistor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import okhttp3.Cookie;

class CookieContainer implements CookiePersistor {

    private final List<Cookie> cookies = new ArrayList<>();

    @Override
    public List<Cookie> loadAll() {
        List<Cookie> cookies = new ArrayList<>(this.cookies.size());
        cookies.addAll(this.cookies);
        return cookies;
    }

    @Override
    public void saveAll(Collection<Cookie> cookies) {
        this.cookies.clear();
        this.cookies.addAll(cookies);
    }

    @Override
    public void removeAll(Collection<Cookie> cookies) {
        this.cookies.removeAll(cookies);
    }

    @Override
    public void clear() {
        cookies.clear();
    }
}
