package com.coremedia.ecommerce.studio.rest.exception;

import com.coremedia.ecommerce.studio.rest.CatalogRestErrorCodes;
import com.coremedia.ecommerce.studio.rest.CommerceAugmentationException;
import com.coremedia.livecontext.ecommerce.common.CommerceException;
import com.coremedia.livecontext.ecommerce.common.CommerceRemoteException;
import com.coremedia.livecontext.ecommerce.common.InvalidCatalogException;
import com.coremedia.livecontext.ecommerce.common.InvalidIdException;
import com.coremedia.livecontext.ecommerce.common.NotFoundException;
import com.coremedia.livecontext.ecommerce.common.UnauthorizedException;
import com.coremedia.rest.cap.util.ResponseUtil;
import com.google.common.collect.ImmutableMap;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * Maps {@link com.coremedia.livecontext.ecommerce.common.CommerceException} to REST responses.
 */
@RestControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
public class CommerceExceptionMapper {

  private static final HttpStatus GONE = HttpStatus.GONE;

  // Attention: use only status codes that causes no other error handling on the client side
  // the 4xx status codes have proven to be working, so we use consistent 410 (GONE).
  // only the error codes (LC-xxxx) will be evaluated in the corresponding catalog error handler
  private static final Map<Class, ResultCodes> EXCEPTION_CLASSES_TO_RESULT_CODES = ImmutableMap
          .<Class, ResultCodes>builder()
          // alphabetic order of exception classes
          .put(CommerceAugmentationException.class, new ResultCodes(CatalogRestErrorCodes.ROOT_CATEGORY_NOT_AUGMENTED, GONE))
          .put(CommerceRemoteException.class, new ResultCodes(CatalogRestErrorCodes.CATALOG_INTERNAL_ERROR, GONE))
          .put(InvalidCatalogException.class, new ResultCodes(CatalogRestErrorCodes.COULD_NOT_FIND_CATALOG, GONE))
          .put(InvalidIdException.class, new ResultCodes(CatalogRestErrorCodes.COULD_NOT_FIND_CATALOG_BEAN, GONE))
          .put(NotFoundException.class, new ResultCodes(CatalogRestErrorCodes.COULD_NOT_FIND_CATALOG_BEAN, GONE))
          .put(UnauthorizedException.class, new ResultCodes(CatalogRestErrorCodes.UNAUTHORIZED, GONE))
          .build();

  private static final ResultCodes RESULT_CODES_FALLBACK = new ResultCodes(CatalogRestErrorCodes.CATALOG_UNAVAILABLE, GONE);

  @ExceptionHandler(CommerceException.class)
  public ResponseEntity toResponse(CommerceException ex, HttpServletRequest request) {
    String name = ex.getClass().getSimpleName() + "(" + ex.getResultCode() + ")";
    String msg = ex.getMessage();
    ResultCodes resultCodes = getResultCodesForException(ex);

    return ResponseUtil.buildResponse(request, resultCodes.statusCode, resultCodes.errorCode, name, msg);
  }

  private static ResultCodes getResultCodesForException(CommerceException ex) {
    return EXCEPTION_CLASSES_TO_RESULT_CODES.getOrDefault(ex.getClass(), RESULT_CODES_FALLBACK);
  }

  private static class ResultCodes {

    private final String errorCode;
    private final HttpStatus statusCode;

    private ResultCodes(String errorCode, HttpStatus statusCode) {
      this.errorCode = errorCode;
      this.statusCode = statusCode;
    }
  }
}
