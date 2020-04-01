package com.coremedia.blueprint.personalization.elastic;

import org.mockito.Mockito;
import org.springframework.beans.factory.FactoryBean;

public class JavaMailSenderMockFactory implements FactoryBean {

  @Override
  public Object getObject() throws Exception {
    return Mockito.mock(org.springframework.mail.javamail.JavaMailSender.class);
  }

  @Override
  public Class<?> getObjectType() {
    return org.springframework.mail.javamail.JavaMailSender.class;
  }

  @Override
  public boolean isSingleton() {
    return true;
  }
}
