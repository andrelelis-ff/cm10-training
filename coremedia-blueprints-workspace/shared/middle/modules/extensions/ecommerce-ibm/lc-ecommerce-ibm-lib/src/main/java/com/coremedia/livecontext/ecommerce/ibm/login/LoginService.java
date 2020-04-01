package com.coremedia.livecontext.ecommerce.ibm.login;

import com.coremedia.livecontext.ecommerce.common.StoreContext;

import com.coremedia.livecontext.ecommerce.ibm.link.WcPreviewToken;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

/**
 * Service interface to logon to the catalog.
 */
public interface LoginService {

  /**
   * Login an arbitraty user for a current store context.
   * <p>This operation depends on the current {@link com.coremedia.livecontext.ecommerce.common.StoreContext}.</p>
   * @param context current store contexttext
   * @return The users credentials if the login was valid
   * @throws com.coremedia.livecontext.ecommerce.common.InvalidLoginException if logon was not successful.
   */
  WcCredentials loginIdentity(String username, String password, @NonNull StoreContext context);

  /**
   * Login a service user for a current store context.
   * This user session will be reused for all user contexts.
   * <p>This operation depends on the current {@link com.coremedia.livecontext.ecommerce.common.StoreContext}.</p>
   * @param context current store contexttext
   * @return The service credentials if the login was valid
   * @throws com.coremedia.livecontext.ecommerce.common.InvalidLoginException if logon was not successful.
   */
  WcCredentials loginServiceIdentity(@NonNull StoreContext context);

  /**
   * Logout a service user for a current store context.
   * <p>This operation depends on the current {@link com.coremedia.livecontext.ecommerce.common.StoreContext}.</p>
   * @param context the current store context
   * @return true if the the logout was successful
   * @throws com.coremedia.livecontext.ecommerce.common.CommerceException
   */
  boolean logoutServiceIdentity(@NonNull StoreContext context);

  /**
   * Renew a service user login for a current store context.
   * This user session will be reused for all user contexts.
   * <p>This operation depends on the current {@link com.coremedia.livecontext.ecommerce.common.StoreContext}.</p>
   * @param context the current store context
   * @return The service credentials if the login was valid
   * @throws com.coremedia.livecontext.ecommerce.common.InvalidLoginException if logon was not successful.
   */
  WcCredentials renewServiceIdentityLogin(@NonNull StoreContext context);

  /**
   * Internal routine to clear all service credentials from cache.
   */
  void clearIdentityCache();

}
