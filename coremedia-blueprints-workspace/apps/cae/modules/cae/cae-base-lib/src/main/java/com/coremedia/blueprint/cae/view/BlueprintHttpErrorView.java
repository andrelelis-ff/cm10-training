package com.coremedia.blueprint.cae.view;

import com.coremedia.objectserver.view.HttpErrorView;
import com.coremedia.objectserver.view.ViewUtils;
import com.coremedia.objectserver.web.HttpError;
import edu.umd.cs.findbugs.annotations.NonNull;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

public class BlueprintHttpErrorView extends HttpErrorView {

  private List<Integer> errorsRendered;

  @Override
  public void render(Object self, String viewName, @NonNull HttpServletRequest request,
                     @NonNull HttpServletResponse response) {
    if (!(self instanceof HttpError)) {
      throw new IllegalArgumentException(self + " is not a " + HttpError.class);
    }

    HttpError error = (HttpError) self;
    response.setStatus(error.getErrorCode());

    Integer errorCodeInt = error.getErrorCode();
    if (errorsRendered.contains(errorCodeInt)) {
      ViewUtils.render(self, errorCodeInt.toString(), request, response);
    } else {
      super.render(self, viewName, request, response);
    }
  }

  public void setErrorsRendered(List<Integer> errorsRendered) {
    this.errorsRendered = errorsRendered;
  }
}
