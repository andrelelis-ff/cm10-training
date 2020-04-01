package com.coremedia.livecontext.ecommerce.ibm;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;

import javax.servlet.http.Cookie;

public class CookieNameValueMatcher extends BaseMatcher<Cookie> {
  private String name;
  private String value;

  public CookieNameValueMatcher(String name, String value) {
    this.name = name;
    this.value = value;
  }

  @Override
  public boolean matches(Object object) {
    if (!(object instanceof javax.servlet.http.Cookie)) {
      return false;
    }
    javax.servlet.http.Cookie actualCookie = (javax.servlet.http.Cookie) object;
    if (actualCookie.getName().equals(name) && actualCookie.getValue().equals(value)) {
      return true;
    }
    return false;
  }

  @Override
  public void describeTo(Description description) {
    description.appendText("cookie does not match expected name (\""+name+"\") or value (\""+value+"\")");
  }
}
