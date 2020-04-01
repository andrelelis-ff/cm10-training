package com.coremedia.blueprint.cae.filter;

import org.springframework.web.filter.CharacterEncodingFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * This class is part of a work around for CMS-1168. Websphere only remembers explicitly
 * set character encodings if a content type has been set for the response.
 */
public class UnknownMimetypeCharacterEncodingFilter extends CharacterEncodingFilter {
  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
    response.setContentType("application/unknown");
    super.doFilterInternal(request, response, filterChain);
  }
}
