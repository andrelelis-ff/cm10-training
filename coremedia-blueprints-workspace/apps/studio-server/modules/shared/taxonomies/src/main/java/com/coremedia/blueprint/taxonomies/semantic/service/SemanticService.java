package com.coremedia.blueprint.taxonomies.semantic.service;

import com.coremedia.blueprint.taxonomies.semantic.SemanticContext;
import com.coremedia.cap.content.Content;

public interface SemanticService {

  SemanticContext analyze(Content content);

}
