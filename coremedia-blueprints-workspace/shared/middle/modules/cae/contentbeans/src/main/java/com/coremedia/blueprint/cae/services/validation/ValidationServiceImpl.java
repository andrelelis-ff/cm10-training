package com.coremedia.blueprint.cae.services.validation;


import com.coremedia.blueprint.common.services.validation.ValidationService;
import com.coremedia.blueprint.common.services.validation.Validator;
import org.springframework.beans.factory.annotation.Required;

import java.util.ArrayList;
import java.util.List;

public class ValidationServiceImpl<T> implements ValidationService<T> {
  private List<Validator<T>> validators = new ArrayList<>();

  /**
   * Setter for {@link #validators}
   *
   * @param validators the list of validators to use
   */
  @Required
  public void setValidators(List<Validator<T>> validators) {
    this.validators = validators;
  }

  @Override
  public List<? extends T> filterList(List<? extends T> source) {
    List<? extends T> filteredList = source;
    for (Validator<T> validator : validators) {
      filteredList = validator.filterList(filteredList);
    }
    return filteredList != null ? new ArrayList<>(filteredList) : null;
  }

  @Override
  public boolean validate(T source) {
    for (Validator<T> validator : validators) {
      if (!validator.validate(source)) {
        return false;
      }
    }
    return true;
  }

}
