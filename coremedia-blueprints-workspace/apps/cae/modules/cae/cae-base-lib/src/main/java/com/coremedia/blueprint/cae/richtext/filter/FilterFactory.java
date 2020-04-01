package com.coremedia.blueprint.cae.richtext.filter;

import com.coremedia.xml.Filter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public interface FilterFactory {
  Filter getInstance(HttpServletRequest request, HttpServletResponse response);
}
