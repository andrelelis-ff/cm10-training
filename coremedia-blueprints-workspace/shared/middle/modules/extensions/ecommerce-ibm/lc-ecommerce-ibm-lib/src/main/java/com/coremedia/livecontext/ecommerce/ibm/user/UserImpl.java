package com.coremedia.livecontext.ecommerce.ibm.user;

import com.coremedia.livecontext.ecommerce.ibm.common.AbstractIbmCommerceBean;
import com.coremedia.livecontext.ecommerce.ibm.common.DataMapHelper;
import com.coremedia.livecontext.ecommerce.user.User;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import java.util.Map;

public class UserImpl extends AbstractIbmCommerceBean implements User {

  private Map<String, Object> delegate;

  public Map<String, Object> getDelegate() {
    return delegate;
  }

  @Override
  @SuppressWarnings("unchecked")
  public void setDelegate(Object delegate) {
    this.delegate = (Map<String, Object>) delegate;
  }

  @Override
  public String getFirstName() {
    return getStringValue(getDelegate(), "firstName");
  }

  @Override
  public String getLastName() {
    return getStringValue(getDelegate(), "lastName");
  }

  @Override
  public String getLogonId() {
    return getStringValue(getDelegate(), "logonId");
  }

  @Override
  public String getUserId() {
    return getStringValue(getDelegate(), "userId");
  }

  @Override
  public String getExternalId() {
    return getStringValue(getDelegate(), "logonId");
  }

  @Override
  public String getExternalTechId() {
    return getExternalId();
  }

  @Override
  public String getEmail1() {
    return getStringValue(getDelegate(), "email1");
  }

  @Override
  public String getEmail2() {
    return getStringValue(getDelegate(), "email2");
  }

  @Override
  public String getEmail3() {
    return getStringValue(getDelegate(), "email3");
  }

  @Override
  public String getCity() {
    return getStringValue(getDelegate(), "city");
  }

  @Override
  public String getCountry() {
    return getStringValue(getDelegate(), "country");
  }

  @Override
  public String getLogonPassword() {
    return getStringValue(getDelegate(), "logonPassword");
  }

  @Override
  public String getLogonPasswordVerify() {
    return getStringValue(getDelegate(), "logonPasswordVerify");
  }

  @Override
  public String getChallengeAnswer() {
    return getStringValue(getDelegate(), "challengeAnswer");
  }

  @Override
  public String getChallengeQuestion() {
    return getStringValue(getDelegate(), "challengeQuestion");
  }

  @Nullable
  private static String getStringValue(@NonNull Map<String, Object> map, @NonNull String key) {
    return DataMapHelper.findString(map, key).orElse(null);
  }
}
